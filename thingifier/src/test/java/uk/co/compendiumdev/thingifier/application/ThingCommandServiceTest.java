package uk.co.compendiumdev.thingifier.application;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierSchemaCatalog;
import uk.co.compendiumdev.thingifier.application.command.AmendThingCommand;
import uk.co.compendiumdev.thingifier.application.command.BodyFieldValue;
import uk.co.compendiumdev.thingifier.application.command.ConnectExistingRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateAndConnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DeleteThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DisconnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.PutThingCommand;
import uk.co.compendiumdev.thingifier.application.command.RelateThingCommand;
import uk.co.compendiumdev.thingifier.application.command.RelationshipReference;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public class ThingCommandServiceTest {

    @Test
    public void createCommandCreatesThingAndConnectsRelationships() {
        Thingifier thingifier = taskProjectThingifier();
        ThingStore store = storeFor(thingifier);
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityDefinition project = thingifier.getDefinitionNamed("project");
        EntityInstance projectInstance =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "Project"));

        ThingCommandResult result =
                serviceFor(thingifier, store)
                        .execute(
                                new CreateThingCommand(
                                        task.getName(),
                                        fields("title", "Task"),
                                        List.of(
                                                RelationshipReference.compressed(
                                                        "task-of",
                                                        "guid",
                                                        projectInstance.getPrimaryKeyValue())),
                                        true));

        Assertions.assertTrue(result.isSuccessful());
        Assertions.assertEquals("Task", result.getInstance().getFieldValue("title").asString());
        Assertions.assertEquals(
                projectInstance,
                store.relationships().listRelated(result.getInstance(), "task-of").get(0));
    }

    @Test
    public void createCommandCanResolveExplicitRelationshipReferenceByTargetField() {
        Thingifier thingifier = taskProjectThingifier();
        ThingStore store = storeFor(thingifier);
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityDefinition project = thingifier.getDefinitionNamed("project");
        EntityInstance projectInstance =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "Project"));

        ThingCommandResult result =
                serviceFor(thingifier, store)
                        .execute(
                                new CreateThingCommand(
                                        task.getName(),
                                        fields("title", "Task"),
                                        List.of(
                                                RelationshipReference.explicit(
                                                        "task-of",
                                                        project.getName(),
                                                        "projects",
                                                        "title",
                                                        "Project")),
                                        true));

        Assertions.assertTrue(result.isSuccessful());
        Assertions.assertEquals(
                projectInstance,
                store.relationships().listRelated(result.getInstance(), "task-of").get(0));
    }

    @Test
    public void createCommandRollsBackWhenRelationshipReferenceCannotBeResolved() {
        Thingifier thingifier = taskProjectThingifier();
        ThingStore store = storeFor(thingifier);
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        int taskCount = store.entityQueries().count(task);

        ThingCommandResult result =
                serviceFor(thingifier, store)
                        .execute(
                                new CreateThingCommand(
                                        task.getName(),
                                        fields("title", "Task"),
                                        List.of(
                                                RelationshipReference.compressed(
                                                        "task-of", "guid", "missing")),
                                        true));

        Assertions.assertTrue(result.isError());
        Assertions.assertEquals(taskCount, store.entityQueries().count(task));
        Assertions.assertEquals(
                List.of(
                        "Invalid relationships: cannot find task-of to relate to with guid missing"),
                result.getErrorMessages());
    }

    @Test
    public void createCommandDoesNotDeleteExistingRelatedItemWhenFinalValidationFails() {
        Thingifier thingifier = taskProjectThingifierWithMandatoryCategory();
        ThingStore store = storeFor(thingifier);
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityDefinition project = thingifier.getDefinitionNamed("project");
        EntityInstance projectInstance =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "Project"));
        int taskCount = store.entityQueries().count(task);

        ThingCommandResult result =
                serviceFor(thingifier, store)
                        .execute(
                                new CreateThingCommand(
                                        task.getName(),
                                        fields("title", "Task"),
                                        List.of(
                                                RelationshipReference.compressed(
                                                        "task-of",
                                                        "guid",
                                                        projectInstance.getPrimaryKeyValue())),
                                        true));

        Assertions.assertTrue(result.isError());
        Assertions.assertEquals(taskCount, store.entityQueries().count(task));
        Assertions.assertNotNull(
                store.entityQueries()
                        .findByQueryIdentifier(project, projectInstance.getPrimaryKeyValue()));
    }

    @Test
    public void createCommandRejectsDuplicateRequestedPrimaryKey() {
        Thingifier thingifier = taskProjectThingifier();
        ThingStore store = storeFor(thingifier);
        EntityDefinition project = thingifier.getDefinitionNamed("project");
        EntityInstance existing =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "Existing"));

        ThingCommandResult result =
                serviceFor(thingifier, store)
                        .execute(
                                new CreateThingCommand(
                                        project.getName(),
                                        existing.getPrimaryKeyValue(),
                                        fields("title", "Duplicate"),
                                        List.of(),
                                        true));

        Assertions.assertTrue(result.isError());
        Assertions.assertEquals(409, result.statusCode());
        Assertions.assertEquals(1, store.entityQueries().count(project));
    }

    @Test
    public void putCreateRejectsAutoFieldDefinitions() {
        Thingifier thingifier = taskProjectThingifier();
        ThingStore store = storeFor(thingifier);

        ThingCommandResult result =
                serviceFor(thingifier, store)
                        .execute(
                                new PutThingCommand(
                                        "task", "123", fields("title", "Nope"), List.of()));

        Assertions.assertTrue(result.isError());
        Assertions.assertEquals(
                List.of("Cannot create task with PUT due to Auto fields guid"),
                result.getErrorMessages());
    }

    @Test
    public void createCommandValidatesDeclaredBodyTypesInApplication() {
        Thingifier thingifier = typedTodoThingifier();
        ThingStore store = storeFor(thingifier);

        ThingCommandResult result =
                serviceFor(thingifier, store, true)
                        .execute(
                                new CreateThingCommand(
                                        "todo",
                                        "",
                                        fields("doneStatus", "true"),
                                        List.of(
                                                bodyField(
                                                        "doneStatus",
                                                        "true",
                                                        BodyFieldValue.SourceType.STRING)),
                                        List.of(),
                                        true));

        Assertions.assertTrue(result.isError());
        Assertions.assertEquals(
                List.of("doneStatus should be BOOLEAN but was STRING"), result.getErrorMessages());
        Assertions.assertEquals(
                0, store.entityQueries().count(thingifier.getDefinitionNamed("todo")));
    }

    @Test
    public void createCommandNormalizesNumericIntegerBodyValuesInApplication() {
        Thingifier thingifier = typedTodoThingifier();
        ThingStore store = storeFor(thingifier);

        ThingCommandResult result =
                serviceFor(thingifier, store, true)
                        .execute(
                                new CreateThingCommand(
                                        "todo",
                                        "",
                                        fields("priority", "2.0"),
                                        List.of(
                                                bodyField(
                                                        "priority",
                                                        "2.0",
                                                        BodyFieldValue.SourceType.NUMERIC)),
                                        List.of(),
                                        true));

        Assertions.assertTrue(result.isSuccessful());
        Assertions.assertEquals("2", result.getInstance().getFieldValue("priority").asString());
    }

    @Test
    public void amendCommandRollsBackFieldsAndRelationshipsOnValidationFailure() {
        Thingifier thingifier = mandatoryTaskProjectThingifier();
        ThingStore store = storeFor(thingifier);
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityDefinition project = thingifier.getDefinitionNamed("project");
        EntityInstance taskInstance =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(task)
                                        .withField("title", "Original title"));
        EntityInstance projectInstance =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "Original project"));
        store.relationships().connect(taskInstance, "task-of", projectInstance);

        ThingCommandResult result =
                serviceFor(thingifier, store)
                        .execute(
                                new AmendThingCommand(
                                        task.getName(),
                                        taskInstance.getPrimaryKeyValue(),
                                        fields("title", "Rejected title"),
                                        true,
                                        List.of()));

        EntityInstance restored =
                store.entityQueries()
                        .findByQueryIdentifier(task, taskInstance.getPrimaryKeyValue());
        Assertions.assertTrue(result.isError());
        Assertions.assertEquals("Original title", restored.getFieldValue("title").asString());
        Assertions.assertEquals(
                projectInstance, store.relationships().listRelated(restored, "task-of").get(0));
    }

    @Test
    public void amendCommandRollsBackWhenRelationshipReferenceCannotBeResolved() {
        Thingifier thingifier = taskProjectThingifier();
        ThingStore store = storeFor(thingifier);
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityDefinition project = thingifier.getDefinitionNamed("project");
        EntityInstance taskInstance =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(task)
                                        .withField("title", "Original title"));
        EntityInstance projectInstance =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "Project"));
        store.relationships().connect(taskInstance, "task-of", projectInstance);

        ThingCommandResult result =
                serviceFor(thingifier, store)
                        .execute(
                                new AmendThingCommand(
                                        task.getName(),
                                        taskInstance.getPrimaryKeyValue(),
                                        fields("title", "Rejected title"),
                                        false,
                                        List.of(
                                                RelationshipReference.compressed(
                                                        "task-of", "guid", "missing"))));

        EntityInstance restored =
                store.entityQueries()
                        .findByQueryIdentifier(task, taskInstance.getPrimaryKeyValue());
        Assertions.assertTrue(result.isError());
        Assertions.assertEquals("Original title", restored.getFieldValue("title").asString());
        Assertions.assertEquals(
                List.of("cannot find task-of to relate to with guid missing"),
                result.getErrorMessages());
        Assertions.assertEquals(
                projectInstance, store.relationships().listRelated(restored, "task-of").get(0));
    }

    @Test
    public void deleteCommandDeletesInstance() {
        Thingifier thingifier = taskProjectThingifier();
        ThingStore store = storeFor(thingifier);
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityInstance taskInstance =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(task)
                                        .withField("title", "Delete me"));

        ThingCommandResult result =
                serviceFor(thingifier, store)
                        .execute(
                                new DeleteThingCommand(
                                        task.getName(),
                                        taskInstance.getPrimaryKeyValue(),
                                        taskInstance.getPrimaryKeyValue()));

        Assertions.assertTrue(result.isSuccessful());
        Assertions.assertNull(
                store.entityQueries()
                        .findByQueryIdentifier(task, taskInstance.getPrimaryKeyValue()));
    }

    @Test
    public void disconnectRelationshipCommandRemovesOnlyRelationship() {
        Thingifier thingifier = taskProjectThingifier();
        ThingStore store = storeFor(thingifier);
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityDefinition project = thingifier.getDefinitionNamed("project");
        EntityInstance taskInstance =
                store.entities()
                        .create(EntityInstanceDraft.forEntity(task).withField("title", "Task"));
        EntityInstance projectInstance =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "Project"));
        store.relationships().connect(projectInstance, "tasks", taskInstance);

        ThingCommandResult result =
                serviceFor(thingifier, store)
                        .execute(
                                new DisconnectRelationshipCommand(
                                        project.getName(),
                                        projectInstance.getPrimaryKeyValue(),
                                        "tasks",
                                        taskInstance.getPrimaryKeyValue(),
                                        ""));

        Assertions.assertTrue(result.isSuccessful());
        Assertions.assertTrue(
                store.relationships().listRelated(projectInstance, "tasks").isEmpty());
        Assertions.assertNotNull(
                store.entityQueries()
                        .findByQueryIdentifier(task, taskInstance.getPrimaryKeyValue()));
    }

    @Test
    public void failedConnectExistingRelationshipDoesNotDeleteExistingChild() {
        Thingifier thingifier = taskProjectThingifierWithMandatoryCategory();
        ThingStore store = storeFor(thingifier);
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityDefinition project = thingifier.getDefinitionNamed("project");
        EntityInstance taskInstance =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(task)
                                        .withField("title", "Existing task"));
        EntityInstance projectInstance =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "Project"));

        ThingCommandResult result =
                serviceFor(thingifier, store)
                        .execute(
                                new ConnectExistingRelationshipCommand(
                                        project.getName(),
                                        projectInstance.getPrimaryKeyValue(),
                                        "tasks",
                                        fields("guid", taskInstance.getPrimaryKeyValue()),
                                        ""));

        Assertions.assertTrue(result.isError());
        Assertions.assertNotNull(
                store.entityQueries()
                        .findByQueryIdentifier(task, taskInstance.getPrimaryKeyValue()));
        Assertions.assertTrue(
                store.relationships().listRelated(projectInstance, "tasks").isEmpty());
    }

    @Test
    public void failedCreateAndConnectRelationshipRollsBackCreatedChild() {
        Thingifier thingifier = taskProjectThingifierWithMandatoryCategory();
        ThingStore store = storeFor(thingifier);
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityDefinition project = thingifier.getDefinitionNamed("project");
        EntityInstance projectInstance =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "Project"));
        int taskCount = store.entityQueries().count(task);

        ThingCommandResult result =
                serviceFor(thingifier, store)
                        .execute(
                                new CreateAndConnectRelationshipCommand(
                                        project.getName(),
                                        projectInstance.getPrimaryKeyValue(),
                                        "tasks",
                                        task.getName(),
                                        fields("title", "Rolled back"),
                                        List.of(),
                                        ""));

        Assertions.assertTrue(result.isError());
        Assertions.assertTrue(result.rolledBackCreatedInstance());
        Assertions.assertEquals(taskCount, store.entityQueries().count(task));
        Assertions.assertTrue(
                store.relationships().listRelated(projectInstance, "tasks").isEmpty());
    }

    @Test
    public void failedCreateAndConnectRelationshipRollsBackOnUnresolvedChildRelationship() {
        Thingifier thingifier = taskProjectThingifierWithMandatoryCategory();
        ThingStore store = storeFor(thingifier);
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityDefinition project = thingifier.getDefinitionNamed("project");
        EntityInstance projectInstance =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "Project"));
        int taskCount = store.entityQueries().count(task);

        ThingCommandResult result =
                serviceFor(thingifier, store)
                        .execute(
                                new CreateAndConnectRelationshipCommand(
                                        project.getName(),
                                        projectInstance.getPrimaryKeyValue(),
                                        "tasks",
                                        task.getName(),
                                        fields("title", "Rolled back"),
                                        List.of(
                                                RelationshipReference.compressed(
                                                        "category", "guid", "missing")),
                                        ""));

        Assertions.assertTrue(result.isError());
        Assertions.assertEquals(taskCount, store.entityQueries().count(task));
        Assertions.assertTrue(
                store.relationships().listRelated(projectInstance, "tasks").isEmpty());
    }

    @Test
    public void relateCommandConnectsExistingTargetWithoutCreatingChild() {
        Thingifier thingifier = taskProjectThingifier();
        ThingStore store = storeFor(thingifier);
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityDefinition project = thingifier.getDefinitionNamed("project");
        EntityInstance taskInstance =
                store.entities()
                        .create(EntityInstanceDraft.forEntity(task).withField("title", "Task"));
        EntityInstance projectInstance =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "Project"));

        ThingCommandResult result =
                serviceFor(thingifier, store)
                        .execute(
                                new RelateThingCommand(
                                        project.getName(),
                                        projectInstance.getPrimaryKeyValue(),
                                        "tasks",
                                        fields("guid", taskInstance.getPrimaryKeyValue()),
                                        List.of(),
                                        ""));

        Assertions.assertTrue(result.isSuccessful());
        Assertions.assertFalse(result.createdInstance());
        Assertions.assertEquals(1, store.entityQueries().count(task));
        Assertions.assertEquals(
                taskInstance, store.relationships().listRelated(projectInstance, "tasks").get(0));
    }

    @Test
    public void relateCommandAllowsStringAutoIncrementReferenceWhenConnectingExistingTarget() {
        Thingifier thingifier = numericIdTaskProjectThingifier();
        ThingStore store = storeFor(thingifier);
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityDefinition project = thingifier.getDefinitionNamed("project");
        EntityInstance taskInstance =
                store.entities()
                        .create(EntityInstanceDraft.forEntity(task).withField("title", "Task"));
        EntityInstance projectInstance =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "Project"));

        ThingCommandResult result =
                serviceFor(thingifier, store, true)
                        .execute(
                                new RelateThingCommand(
                                        project.getName(),
                                        projectInstance.getPrimaryKeyValue(),
                                        "tasks",
                                        fields("id", taskInstance.getPrimaryKeyValue()),
                                        List.of(
                                                bodyField(
                                                        "id",
                                                        taskInstance.getPrimaryKeyValue(),
                                                        BodyFieldValue.SourceType.STRING)),
                                        List.of(),
                                        ""));

        Assertions.assertTrue(result.isSuccessful());
        Assertions.assertFalse(result.createdInstance());
        Assertions.assertEquals(1, store.entityQueries().count(task));
        Assertions.assertEquals(
                taskInstance, store.relationships().listRelated(projectInstance, "tasks").get(0));
    }

    @Test
    public void relateCommandCreatesAndConnectsWhenNoTargetReferenceIsSupplied() {
        Thingifier thingifier = taskProjectThingifier();
        ThingStore store = storeFor(thingifier);
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityDefinition project = thingifier.getDefinitionNamed("project");
        EntityInstance projectInstance =
                store.entities()
                        .create(
                                EntityInstanceDraft.forEntity(project)
                                        .withField("title", "Project"));

        ThingCommandResult result =
                serviceFor(thingifier, store)
                        .execute(
                                new RelateThingCommand(
                                        project.getName(),
                                        projectInstance.getPrimaryKeyValue(),
                                        "tasks",
                                        fields("title", "Created task"),
                                        List.of(),
                                        ""));

        Assertions.assertTrue(result.isSuccessful());
        Assertions.assertTrue(result.createdInstance());
        Assertions.assertEquals(1, store.entityQueries().count(task));
        Assertions.assertEquals(
                result.getInstance(),
                store.relationships().listRelated(projectInstance, "tasks").get(0));
    }

    private Thingifier taskProjectThingifier() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition task = thingifier.defineThing("task", "tasks");
        task.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        task.addField(Field.is("title", FieldType.STRING));

        EntityDefinition project = thingifier.defineThing("project", "projects");
        project.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        project.addField(Field.is("title", FieldType.STRING));

        thingifier
                .defineRelationship(task, project, "task-of", Cardinality.ONE_TO_ONE())
                .whenReversed(Cardinality.ONE_TO_MANY(), "tasks");
        return thingifier;
    }

    private Thingifier mandatoryTaskProjectThingifier() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition task = thingifier.defineThing("task", "tasks");
        task.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        task.addField(Field.is("title", FieldType.STRING));

        EntityDefinition project = thingifier.defineThing("project", "projects");
        project.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        project.addField(Field.is("title", FieldType.STRING));

        RelationshipDefinition relationship =
                thingifier.defineRelationship(task, project, "task-of", Cardinality.ONE_TO_ONE());
        relationship.getFromRelationship().setOptionality(Optionality.MANDATORY_RELATIONSHIP);
        relationship.whenReversed(Cardinality.ONE_TO_MANY(), "tasks");
        return thingifier;
    }

    private Thingifier taskProjectThingifierWithMandatoryCategory() {
        Thingifier thingifier = taskProjectThingifier();
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        EntityDefinition category = thingifier.defineThing("category", "categories");
        category.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        category.addField(Field.is("title", FieldType.STRING));
        thingifier
                .defineRelationship(task, category, "category", Cardinality.ONE_TO_ONE())
                .getFromRelationship()
                .setOptionality(Optionality.MANDATORY_RELATIONSHIP);
        return thingifier;
    }

    private Thingifier typedTodoThingifier() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition todo = thingifier.defineThing("todo", "todos");
        todo.addField(Field.is("doneStatus", FieldType.BOOLEAN));
        todo.addField(Field.is("priority", FieldType.INTEGER));
        return thingifier;
    }

    private Thingifier numericIdTaskProjectThingifier() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition task = thingifier.defineThing("task", "tasks");
        task.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        task.addField(Field.is("title", FieldType.STRING));

        EntityDefinition project = thingifier.defineThing("project", "projects");
        project.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        project.addField(Field.is("title", FieldType.STRING));

        thingifier
                .defineRelationship(project, task, "tasks", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_MANY(), "tasksof");
        return thingifier;
    }

    private ThingStore storeFor(final Thingifier thingifier) {
        return thingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
    }

    private ThingCommandService serviceFor(final Thingifier thingifier, final ThingStore store) {
        return new ThingCommandService(store, new ThingifierSchemaCatalog(thingifier));
    }

    private ThingCommandService serviceFor(
            final Thingifier thingifier, final ThingStore store, final boolean enforceTypes) {
        return new ThingCommandService(
                store, new ThingifierSchemaCatalog(thingifier), enforceTypes);
    }

    private List<NamedValue> fields(final String name, final String value) {
        return List.of(new NamedValue(name, value));
    }

    private BodyFieldValue bodyField(
            final String name, final String value, final BodyFieldValue.SourceType sourceType) {
        return new BodyFieldValue(name, value, sourceType);
    }
}
