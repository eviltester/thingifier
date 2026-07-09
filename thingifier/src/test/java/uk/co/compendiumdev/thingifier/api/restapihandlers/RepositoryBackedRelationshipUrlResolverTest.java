package uk.co.compendiumdev.thingifier.api.restapihandlers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

public class RepositoryBackedRelationshipUrlResolverTest {

    private Thingifier thingifier;
    private ThingRepository repository;
    private EntityDefinition todo;
    private EntityDefinition project;
    private EntityInstance task;
    private EntityInstance projectInstance;
    private RepositoryBackedRelationshipUrlResolver resolver;

    @BeforeEach
    public void createModel() {
        thingifier = new Thingifier();

        todo = thingifier.defineThing("todo", "todos");
        todo.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        todo.addField(Field.is("id", FieldType.AUTO_INCREMENT));
        todo.addField(Field.is("title", FieldType.STRING));

        project = thingifier.defineThing("project", "projects");
        project.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        project.addField(Field.is("id", FieldType.AUTO_INCREMENT));
        project.addField(Field.is("title", FieldType.STRING));

        thingifier
                .defineRelationship(project, todo, "tasks", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_MANY(), "task-of");

        repository = thingifier.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
        task =
                repository.createInstance(
                        EntityInstanceDraft.forEntity(todo)
                                .withField("title", "relationship task"));
        projectInstance =
                repository.createInstance(
                        EntityInstanceDraft.forEntity(project)
                                .withField("title", "relationship project"));
        repository.connectRelationship(projectInstance, "tasks", task);

        resolver =
                new RepositoryBackedRelationshipUrlResolver(
                        thingifier, EntityRelModel.DEFAULT_DATABASE_NAME);
    }

    @Test
    public void resolvesRelationshipCollectionPathWithSingularEntityName() {
        RepositoryBackedRelationshipUrlResolver.RelationshipUrlResolution resolution =
                resolver.resolveCollection(
                        String.format("project/%s/tasks", projectInstance.getPrimaryKeyValue()));

        Assertions.assertTrue(resolution.matchedRelationshipPath());
        Assertions.assertFalse(resolution.relationshipInstancePath());
        Assertions.assertEquals(project, resolution.parentEntity());
        Assertions.assertEquals(projectInstance, resolution.parentInstance());
        Assertions.assertEquals("tasks", resolution.relationshipName());
        Assertions.assertNull(resolution.childInstance());
    }

    @Test
    public void resolvesRelationshipCollectionPathWithPluralEntityName() {
        RepositoryBackedRelationshipUrlResolver.RelationshipUrlResolution resolution =
                resolver.resolveCollection(
                        String.format("projects/%s/tasks", projectInstance.getPrimaryKeyValue()));

        Assertions.assertTrue(resolution.matchedRelationshipPath());
        Assertions.assertEquals(projectInstance, resolution.parentInstance());
        Assertions.assertEquals("tasks", resolution.relationshipName());
    }

    @Test
    public void resolvesReverseRelationshipCollectionPath() {
        RepositoryBackedRelationshipUrlResolver.RelationshipUrlResolution resolution =
                resolver.resolveCollection(
                        String.format("todo/%s/task-of", task.getPrimaryKeyValue()));

        Assertions.assertTrue(resolution.matchedRelationshipPath());
        Assertions.assertEquals(todo, resolution.parentEntity());
        Assertions.assertEquals(task, resolution.parentInstance());
        Assertions.assertEquals("task-of", resolution.relationshipName());
    }

    @Test
    public void relationshipCollectionPathCanMatchEvenWhenParentInstanceDoesNotExist() {
        RepositoryBackedRelationshipUrlResolver.RelationshipUrlResolution resolution =
                resolver.resolveCollection("project/not-a-guid/tasks");

        Assertions.assertTrue(resolution.matchedRelationshipPath());
        Assertions.assertEquals(project, resolution.parentEntity());
        Assertions.assertNull(resolution.parentInstance());
        Assertions.assertEquals("tasks", resolution.relationshipName());
    }

    @Test
    public void doesNotMatchUnknownRelationshipForEntity() {
        RepositoryBackedRelationshipUrlResolver.RelationshipUrlResolution resolution =
                resolver.resolveCollection(
                        String.format("project/%s/unknown", projectInstance.getPrimaryKeyValue()));

        Assertions.assertFalse(resolution.matchedRelationshipPath());
    }

    @Test
    public void resolvesRelationshipInstancePathByPrimaryKey() {
        RepositoryBackedRelationshipUrlResolver.RelationshipUrlResolution resolution =
                resolver.resolveRelationshipInstance(
                        String.format(
                                "project/%s/tasks/%s",
                                projectInstance.getPrimaryKeyValue(), task.getPrimaryKeyValue()));

        Assertions.assertTrue(resolution.matchedRelationshipPath());
        Assertions.assertTrue(resolution.relationshipInstancePath());
        Assertions.assertEquals(projectInstance, resolution.parentInstance());
        Assertions.assertEquals(task, resolution.childInstance());
        Assertions.assertEquals("tasks", resolution.relationshipName());
    }

    @Test
    public void resolvesRelationshipInstancePathByAutoIncrementId() {
        RepositoryBackedRelationshipUrlResolver.RelationshipUrlResolution resolution =
                resolver.resolveRelationshipInstance(
                        String.format(
                                "project/%s/tasks/%s",
                                projectInstance.getPrimaryKeyValue(),
                                task.getFieldValue("id").asString()));

        Assertions.assertTrue(resolution.matchedRelationshipPath());
        Assertions.assertTrue(resolution.relationshipInstancePath());
        Assertions.assertEquals(task, resolution.childInstance());
    }

    @Test
    public void relationshipInstancePathRequiresChildToBeConnected() {
        EntityInstance unconnectedTask =
                repository.createInstance(
                        EntityInstanceDraft.forEntity(todo).withField("title", "unconnected task"));

        RepositoryBackedRelationshipUrlResolver.RelationshipUrlResolution resolution =
                resolver.resolveRelationshipInstance(
                        String.format(
                                "project/%s/tasks/%s",
                                projectInstance.getPrimaryKeyValue(),
                                unconnectedTask.getPrimaryKeyValue()));

        Assertions.assertTrue(resolution.matchedRelationshipPath());
        Assertions.assertTrue(resolution.relationshipInstancePath());
        Assertions.assertNull(resolution.childInstance());
    }

    @Test
    public void relationshipInstancePathDoesNotMatchCollectionOnlyUrl() {
        RepositoryBackedRelationshipUrlResolver.RelationshipUrlResolution resolution =
                resolver.resolveRelationshipInstance(
                        String.format("project/%s/tasks", projectInstance.getPrimaryKeyValue()));

        Assertions.assertFalse(resolution.matchedRelationshipPath());
    }
}
