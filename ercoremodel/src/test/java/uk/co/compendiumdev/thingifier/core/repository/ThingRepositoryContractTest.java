package uk.co.compendiumdev.thingifier.core.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

import java.nio.file.Path;
import java.util.List;

public class ThingRepositoryContractTest {

    @TempDir
    Path tempDir;

    @Test
    public void inMemoryRepositorySupportsTheContract() {
        ThingRepository repository = new InMemoryThingRepository(EntityRelModel.DEFAULT_DATABASE_NAME);

        exerciseRepositoryContract(repository);
    }

    @Test
    public void sqliteInMemoryRepositorySupportsTheContract() {
        try (ThingRepository repository = SqliteThingRepository.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            exerciseRepositoryContract(repository);
        }
    }

    @Test
    public void inMemoryProviderKeepsLogicalDatabasesSeparate() {
        ThingRepositoryProvider provider = new InMemoryThingRepositoryProvider();

        exerciseProviderIsolation(provider);
    }

    @Test
    public void sqliteProviderKeepsLogicalDatabasesSeparate() {
        try (ThingRepositoryProvider provider = SqliteThingRepositoryProvider.inMemory()) {
            exerciseProviderIsolation(provider);
        }
    }

    @Test
    public void sqliteRepositoryCannotBeUsedAfterClose() {
        ERSchema schema = todoSchema();
        ThingRepository repository = SqliteThingRepository.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME);
        repository.initializeFrom(schema);

        repository.close();

        Assertions.assertThrows(
                IllegalStateException.class,
                () -> repository.listInstances(schema.getEntityDefinitionNamed("project")));
    }

    @Test
    public void sqliteFileBackedRepositorySurvivesCloseAndReopen() {
        ERSchema schema = todoSchema();
        Path databasePath = tempDir.resolve("thingifier.sqlite");

        try (ThingRepository repository =
                     SqliteThingRepository.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            repository.initializeFrom(schema);

            EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
            EntityDefinition taskDefinition = schema.getEntityDefinitionNamed("task");
            EntityInstance project = new EntityInstance(projectDefinition).setValue("title", "Persisted");
            EntityInstance task = new EntityInstance(taskDefinition).setValue("title", "Loaded relationship");

            repository.addInstance(project);
            repository.addInstance(task);
            repository.connectRelationship(task, "task-of", project);
        }

        try (ThingRepository reopened =
                     SqliteThingRepository.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            reopened.initializeFrom(schema);

            EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
            EntityDefinition taskDefinition = schema.getEntityDefinitionNamed("task");

            EntityInstance project = reopened.findInstanceByPrimaryKey(projectDefinition, "1");
            EntityInstance task = reopened.findInstanceByFieldNameAndValue(
                    taskDefinition, "title", "Loaded relationship");

            Assertions.assertNotNull(project);
            Assertions.assertNotNull(task);
            Assertions.assertEquals("Persisted", project.getFieldValue("title").asString());
            Assertions.assertTrue(reopened.getConnectedItems(task, "task-of").contains(project));
            Assertions.assertTrue(reopened.getConnectedItems(project, "tasks").contains(task));
        }
    }

    @Test
    public void sqliteRepositoryQuotesGeneratedSqlIdentifiers() {
        ERSchema schema = new ERSchema();
        EntityDefinition select = schema.defineEntity("select", "selects", -1);
        select.addAsPrimaryKeyField(Field.is("key-id", FieldType.AUTO_GUID));
        select.addField(Field.is("field with space", FieldType.STRING));

        try (ThingRepository repository = SqliteThingRepository.inMemory("quoted")) {
            repository.initializeFrom(schema);

            EntityInstance instance = new EntityInstance(select);
            instance.setValue("field with space", "works");
            repository.addInstance(instance);

            EntityInstance found = repository.findInstanceByFieldNameAndValue(
                    select, "field with space", "works");

            Assertions.assertNotNull(found);
            Assertions.assertEquals("works", found.getFieldValue("field with space").asString());
        }
    }

    @Test
    public void sqliteFileBackedRepositoryOnlyHydratesCompatibilitySnapshotWhenRequested() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        Path databasePath = tempDir.resolve("lazy.sqlite");

        try (ThingRepository repository =
                     SqliteThingRepository.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            repository.initializeFrom(schema);

            repository.addInstance(new EntityInstance(projectDefinition).setValue("title", "First"));
            repository.addInstance(new EntityInstance(projectDefinition).setValue("title", "Second"));
        }

        try (ThingRepository reopened =
                     SqliteThingRepository.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            reopened.initializeFrom(schema);

            EntityInstanceCollection cachedProjects =
                    reopened.getInstanceCollectionForEntityNamed("project");
            Assertions.assertEquals(0, cachedProjects.countInstances());

            QueryFilterParams params = new QueryFilterParams();
            params.put("title", "First");

            List<EntityInstance> filtered = reopened.listInstances(projectDefinition, params);

            Assertions.assertEquals(1, filtered.size());
            Assertions.assertEquals(1, cachedProjects.countInstances());

            reopened.getInstanceData();

            Assertions.assertEquals(2, cachedProjects.countInstances());
        }
    }

    private void exerciseRepositoryContract(final ThingRepository repository) {
        ERSchema schema = todoSchema();
        repository.initializeFrom(schema);

        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        EntityDefinition taskDefinition = schema.getEntityDefinitionNamed("task");

        EntityInstance project = new EntityInstance(projectDefinition);
        project.setValue("title", "Repository project");

        repository.addInstance(project);

        Assertions.assertEquals("1", project.getPrimaryKeyValue());
        Assertions.assertEquals(project, repository.findInstanceByPrimaryKey(projectDefinition, "1"));
        Assertions.assertEquals(project, repository.findInstanceByQueryIdentifier(projectDefinition, "1"));
        Assertions.assertEquals(project, repository.findInstanceByFieldNameAndValue(
                projectDefinition, "title", "Repository project"));
        Assertions.assertEquals(1, repository.listInstances(projectDefinition).size());

        EntityInstance duplicate = new EntityInstance(projectDefinition);
        duplicate.setValue("title", "Repository project");
        Assertions.assertFalse(repository.checkFieldsForUniqueNess(duplicate, false).isValid());

        EntityInstance secondProject = new EntityInstance(projectDefinition);
        secondProject.setValue("title", "Another project");
        repository.addInstance(secondProject);

        QueryFilterParams filteredParams = new QueryFilterParams();
        filteredParams.put("id", ">=2");
        List<EntityInstance> filteredProjects =
                repository.listInstances(projectDefinition, filteredParams);
        Assertions.assertEquals(1, filteredProjects.size());
        Assertions.assertEquals(secondProject, filteredProjects.get(0));

        QueryFilterParams sortedParams = new QueryFilterParams();
        sortedParams.put("sortBy", "-id");
        List<EntityInstance> sortedProjects =
                repository.listInstances(projectDefinition, sortedParams);
        Assertions.assertEquals("2", sortedProjects.get(0).getPrimaryKeyValue());
        Assertions.assertEquals("1", sortedProjects.get(1).getPrimaryKeyValue());

        QueryFilterParams regexParams = new QueryFilterParams();
        regexParams.put("title", "~=Repository.*");
        List<EntityInstance> regexProjects =
                repository.listInstances(projectDefinition, regexParams);
        Assertions.assertEquals(1, regexProjects.size());
        Assertions.assertEquals(project, regexProjects.get(0));

        EntityInstance task = new EntityInstance(taskDefinition);
        task.setValue("title", "Wire repository");
        repository.addInstance(task);
        repository.connectRelationship(task, "task-of", project);

        Assertions.assertTrue(repository.getConnectedItems(task, "task-of").contains(project));
        Assertions.assertTrue(repository.getConnectedItems(project, "tasks").contains(task));

        repository.removeRelationshipsInvolving(task, project, "task-of");

        Assertions.assertTrue(repository.getConnectedItems(task, "task-of").isEmpty());
        Assertions.assertTrue(repository.getConnectedItems(project, "tasks").isEmpty());

        repository.deleteEntityInstance(task);

        Assertions.assertEquals(0, repository.listInstances(taskDefinition).size());

        repository.clearAllData();

        Assertions.assertEquals(0, repository.listInstances(projectDefinition).size());
        Assertions.assertEquals(0, repository.listInstances(taskDefinition).size());
    }

    private void exerciseProviderIsolation(final ThingRepositoryProvider provider) {
        ERSchema schema = todoSchema();
        ThingRepository defaultRepository = provider.getDefaultRepository();
        defaultRepository.initializeFrom(schema);
        provider.createRepository("session-one", schema);

        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        EntityInstance project = new EntityInstance(projectDefinition);
        project.setValue("title", "Only default");

        defaultRepository.addInstance(project);

        Assertions.assertEquals(1, defaultRepository.listInstances(projectDefinition).size());
        Assertions.assertEquals(
                0,
                provider.getRepository("session-one").listInstances(projectDefinition).size());
    }

    private ERSchema todoSchema() {
        ERSchema schema = new ERSchema();

        EntityDefinition project = schema.defineEntity("project", "projects", -1);
        project.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        project.addField(Field.is("title", FieldType.STRING).setMustBeUnique(true));

        EntityDefinition task = schema.defineEntity("task", "tasks", -1);
        task.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        task.addField(Field.is("title", FieldType.STRING));

        schema.defineRelationship(task, project, "task-of", Cardinality.ONE_TO_ONE()).
                whenReversed(Cardinality.ONE_TO_MANY(), "tasks");

        return schema;
    }
}
