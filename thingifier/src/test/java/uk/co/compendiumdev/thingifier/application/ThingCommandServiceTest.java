package uk.co.compendiumdev.thingifier.application;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.command.AmendThingCommand;
import uk.co.compendiumdev.thingifier.application.command.ConnectExistingRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateAndConnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DeleteThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DisconnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.RelationshipReference;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
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
                new ThingCommandService(store)
                        .execute(
                                new CreateThingCommand(
                                        EntityInstanceDraft.forEntity(task)
                                                .withField("title", "Task"),
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
                new ThingCommandService(store)
                        .execute(
                                new CreateThingCommand(
                                        EntityInstanceDraft.forEntity(task)
                                                .withField("title", "Task"),
                                        List.of(
                                                RelationshipReference.explicit(
                                                        "task-of",
                                                        project,
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
                new ThingCommandService(store)
                        .execute(
                                new CreateThingCommand(
                                        EntityInstanceDraft.forEntity(task)
                                                .withField("title", "Task"),
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
                new ThingCommandService(store)
                        .execute(
                                new CreateThingCommand(
                                        EntityInstanceDraft.forEntity(task)
                                                .withField("title", "Task"),
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
                new ThingCommandService(store)
                        .execute(
                                new AmendThingCommand(
                                        taskInstance,
                                        EntityInstanceDraft.forEntity(task)
                                                .withField("title", "Rejected title"),
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
                new ThingCommandService(store)
                        .execute(
                                new AmendThingCommand(
                                        taskInstance,
                                        EntityInstanceDraft.forEntity(task)
                                                .withField("title", "Rejected title"),
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
                new ThingCommandService(store).execute(new DeleteThingCommand(taskInstance));

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
                new ThingCommandService(store)
                        .execute(
                                new DisconnectRelationshipCommand(
                                        projectInstance, taskInstance, "tasks"));

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
                new ThingCommandService(store)
                        .execute(
                                new ConnectExistingRelationshipCommand(
                                        projectInstance, "tasks", taskInstance));

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
                new ThingCommandService(store)
                        .execute(
                                new CreateAndConnectRelationshipCommand(
                                        projectInstance,
                                        "tasks",
                                        EntityInstanceDraft.forEntity(task)
                                                .withField("title", "Rolled back"),
                                        List.of()));

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
                new ThingCommandService(store)
                        .execute(
                                new CreateAndConnectRelationshipCommand(
                                        projectInstance,
                                        "tasks",
                                        EntityInstanceDraft.forEntity(task)
                                                .withField("title", "Rolled back"),
                                        List.of(
                                                RelationshipReference.compressed(
                                                        "category", "guid", "missing"))));

        Assertions.assertTrue(result.isError());
        Assertions.assertEquals(taskCount, store.entityQueries().count(task));
        Assertions.assertTrue(
                store.relationships().listRelated(projectInstance, "tasks").isEmpty());
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

    private ThingStore storeFor(final Thingifier thingifier) {
        return thingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
    }
}
