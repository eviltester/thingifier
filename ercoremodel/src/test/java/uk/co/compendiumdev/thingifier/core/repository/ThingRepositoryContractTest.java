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
import uk.co.compendiumdev.thingifier.core.query.RepositoryUrlQuery;

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

    @Test
    public void sqliteCountInstancesDoesNotHydrateCompatibilitySnapshot() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        Path databasePath = tempDir.resolve("count-lazy.sqlite");

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
            Assertions.assertEquals(2, reopened.countInstances(projectDefinition));
            Assertions.assertEquals(0, cachedProjects.countInstances());
            Assertions.assertFalse(reopened.hasLoadedCompatibilitySnapshot());
        }
    }

    @Test
    public void repositoriesExportDataAsJsonThroughTheRepositoryContract() {
        ERSchema schema = todoSchema();

        ThingRepository memoryRepository =
                new InMemoryThingRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
        memoryRepository.initializeFrom(schema);
        addProjectAndTask(memoryRepository, schema);

        try (ThingRepository sqliteRepository =
                     SqliteThingRepository.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            sqliteRepository.initializeFrom(schema);
            addProjectAndTask(sqliteRepository, schema);

            assertExportedJsonContainsProjectAndTask(memoryRepository.exportDataAsJson(schema));
            assertExportedJsonContainsProjectAndTask(sqliteRepository.exportDataAsJson(schema));
            Assertions.assertFalse(sqliteRepository.hasLoadedCompatibilitySnapshot());
        }
    }

    @Test
    public void sqliteJsonExportDoesNotHydrateCompatibilitySnapshot() {
        ERSchema schema = todoSchema();
        Path databasePath = tempDir.resolve("export-lazy.sqlite");

        try (ThingRepository repository =
                     SqliteThingRepository.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            repository.initializeFrom(schema);
            addProjectAndTask(repository, schema);
        }

        try (ThingRepository reopened =
                     SqliteThingRepository.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            reopened.initializeFrom(schema);

            EntityInstanceCollection cachedProjects =
                    reopened.getInstanceCollectionForEntityNamed("project");
            EntityInstanceCollection cachedTasks =
                    reopened.getInstanceCollectionForEntityNamed("task");

            Assertions.assertEquals(0, cachedProjects.countInstances());
            Assertions.assertEquals(0, cachedTasks.countInstances());

            String json = reopened.exportDataAsJson(schema);

            assertExportedJsonContainsProjectAndTask(json);
            Assertions.assertFalse(reopened.hasLoadedCompatibilitySnapshot());
            Assertions.assertEquals(0, cachedProjects.countInstances());
            Assertions.assertEquals(0, cachedTasks.countInstances());
        }
    }

    @Test
    public void repositoriesReportMarkdownThroughTheRepositoryContract() {
        ERSchema schema = todoSchema();

        try (ThingRepository repository =
                     SqliteThingRepository.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            repository.initializeFrom(schema);
            addProjectAndTask(repository, schema);

            String markdown = repository.reportAsMarkdown(schema);

            Assertions.assertTrue(markdown.contains("# Entity Definitions:"));
            Assertions.assertTrue(markdown.contains("# Instances"));
            Assertions.assertTrue(markdown.contains("## Of project"));
            Assertions.assertTrue(markdown.contains("Repository project"));
            Assertions.assertTrue(markdown.contains("Wire repository"));
            Assertions.assertFalse(repository.hasLoadedCompatibilitySnapshot());
        }
    }

    @Test
    public void sqliteRelationshipReadsDoNotHydrateCompatibilitySnapshot() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        EntityDefinition taskDefinition = schema.getEntityDefinitionNamed("task");
        Path databasePath = tempDir.resolve("relationship-lazy.sqlite");

        try (ThingRepository repository =
                     SqliteThingRepository.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            repository.initializeFrom(schema);

            EntityInstance project = repository.addInstance(
                    new EntityInstance(projectDefinition).setValue("title", "SQLite project"));
            EntityInstance firstTask = repository.addInstance(
                    new EntityInstance(taskDefinition).setValue("title", "Wire repository"));
            EntityInstance secondTask = repository.addInstance(
                    new EntityInstance(taskDefinition).setValue("title", "Write tests"));

            repository.connectRelationship(project, "tasks", firstTask);
            repository.connectRelationship(project, "tasks", secondTask);
        }

        try (ThingRepository reopened =
                     SqliteThingRepository.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            reopened.initializeFrom(schema);

            Assertions.assertFalse(reopened.hasLoadedCompatibilitySnapshot());

            EntityInstance project = reopened.findInstanceByPrimaryKey(projectDefinition, "1");

            QueryFilterParams params = new QueryFilterParams();
            params.put("title", "*=Wire*");

            List<EntityInstance> filteredTasks =
                    reopened.listRelatedInstances(project, "tasks", params);

            Assertions.assertEquals(1, filteredTasks.size());
            Assertions.assertEquals("Wire repository",
                    filteredTasks.get(0).getFieldValue("title").asString());
            Assertions.assertFalse(reopened.hasLoadedCompatibilitySnapshot());

            QueryFilterParams regexParams = new QueryFilterParams();
            regexParams.put("title", "~=Wire [Rr]epository");

            List<EntityInstance> regexFilteredTasks =
                    reopened.listRelatedInstances(project, "tasks", regexParams);

            Assertions.assertEquals(1, regexFilteredTasks.size());
            Assertions.assertEquals("Wire repository",
                    regexFilteredTasks.get(0).getFieldValue("title").asString());
            Assertions.assertFalse(reopened.hasLoadedCompatibilitySnapshot());

            RepositoryUrlQuery query =
                    new RepositoryUrlQuery(schema, reopened, "project/1/tasks").
                            performQuery(new QueryFilterParams());

            Assertions.assertTrue(query.isResultACollection());
            Assertions.assertEquals(2, query.getListEntityInstances().size());
            Assertions.assertFalse(reopened.hasLoadedCompatibilitySnapshot());
        }
    }

    @Test
    public void sqliteConvertibleRegexFilterDoesNotHydrateCompatibilitySnapshot() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        Path databasePath = tempDir.resolve("regex-like-lazy.sqlite");

        try (ThingRepository repository =
                     SqliteThingRepository.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            repository.initializeFrom(schema);

            repository.addInstance(new EntityInstance(projectDefinition).
                    setValue("title", "Repository project"));
            repository.addInstance(new EntityInstance(projectDefinition).
                    setValue("title", "repository project"));
            repository.addInstance(new EntityInstance(projectDefinition).
                    setValue("title", "Another project"));
        }

        try (ThingRepository reopened =
                     SqliteThingRepository.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            reopened.initializeFrom(schema);

            QueryFilterParams regexParams = new QueryFilterParams();
            regexParams.put("title", "~=Repository.*");

            List<EntityInstance> filtered =
                    reopened.listInstances(projectDefinition, regexParams);

            Assertions.assertEquals(1, filtered.size());
            Assertions.assertEquals("Repository project",
                    filtered.get(0).getFieldValue("title").asString());
            Assertions.assertFalse(reopened.hasLoadedCompatibilitySnapshot());
            Assertions.assertEquals(1,
                    reopened.getInstanceCollectionForEntityNamed("project").countInstances());
        }
    }

    @Test
    public void sqliteRegexpFilterDoesNotHydrateCompatibilitySnapshot() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        Path databasePath = tempDir.resolve("regex-regexp-lazy.sqlite");

        try (ThingRepository repository =
                     SqliteThingRepository.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            repository.initializeFrom(schema);

            repository.addInstance(new EntityInstance(projectDefinition).
                    setValue("title", "Repository project"));
            repository.addInstance(new EntityInstance(projectDefinition).
                    setValue("title", "Another project"));
        }

        try (ThingRepository reopened =
                     SqliteThingRepository.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            reopened.initializeFrom(schema);

            QueryFilterParams regexParams = new QueryFilterParams();
            regexParams.put("title", "~=Repository [Pp]roject");

            List<EntityInstance> filtered =
                    reopened.listInstances(projectDefinition, regexParams);

            Assertions.assertEquals(1, filtered.size());
            Assertions.assertEquals("Repository project",
                    filtered.get(0).getFieldValue("title").asString());
            Assertions.assertFalse(reopened.hasLoadedCompatibilitySnapshot());
            Assertions.assertEquals(1,
                    reopened.getInstanceCollectionForEntityNamed("project").countInstances());
        }
    }

    @Test
    public void sqliteInMemoryRegexpFilterDoesNotHydrateCompatibilitySnapshot() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");

        try (ThingRepository repository =
                     SqliteThingRepository.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            repository.initializeFrom(schema);

            repository.addInstance(new EntityInstance(projectDefinition).
                    setValue("title", "Repository project"));
            repository.addInstance(new EntityInstance(projectDefinition).
                    setValue("title", "Another project"));

            QueryFilterParams regexParams = new QueryFilterParams();
            regexParams.put("title", "~=Repository [Pp]roject");

            List<EntityInstance> filtered =
                    repository.listInstances(projectDefinition, regexParams);

            Assertions.assertEquals(1, filtered.size());
            Assertions.assertEquals("Repository project",
                    filtered.get(0).getFieldValue("title").asString());
            Assertions.assertFalse(repository.hasLoadedCompatibilitySnapshot());
        }
    }

    @Test
    public void sqliteInvalidRegexFilterFailsWithoutHydratingCompatibilitySnapshot() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");

        try (ThingRepository repository =
                     SqliteThingRepository.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            repository.initializeFrom(schema);
            repository.addInstance(new EntityInstance(projectDefinition).
                    setValue("title", "Repository project"));

            QueryFilterParams regexParams = new QueryFilterParams();
            regexParams.put("title", "~=[");

            Assertions.assertThrows(
                    SqliteRegexFilterPolicy.UnsupportedRegexFilterException.class,
                    () -> repository.listInstances(projectDefinition, regexParams));
            Assertions.assertFalse(repository.hasLoadedCompatibilitySnapshot());
        }
    }

    @Test
    public void sqliteTooComplexRegexFilterFailsWithoutHydratingCompatibilitySnapshot() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");

        try (ThingRepository repository =
                     SqliteThingRepository.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            repository.initializeFrom(schema);
            repository.addInstance(new EntityInstance(projectDefinition).
                    setValue("title", "Repository project"));

            QueryFilterParams regexParams = new QueryFilterParams();
            regexParams.put("title", "~=(.+)+");

            Assertions.assertThrows(
                    SqliteRegexFilterPolicy.UnsupportedRegexFilterException.class,
                    () -> repository.listInstances(projectDefinition, regexParams));
            Assertions.assertFalse(repository.hasLoadedCompatibilitySnapshot());
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
        Assertions.assertEquals(1, repository.countInstances(projectDefinition));
        Assertions.assertEquals(0, repository.countInstances(taskDefinition));

        EntityInstance duplicate = new EntityInstance(projectDefinition);
        duplicate.setValue("title", "Repository project");
        Assertions.assertFalse(repository.checkFieldsForUniqueNess(duplicate, false).isValid());

        EntityInstance secondProject = new EntityInstance(projectDefinition);
        secondProject.setValue("title", "Another project");
        repository.addInstance(secondProject);
        Assertions.assertEquals(2, repository.countInstances(projectDefinition));

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
        repository.connectRelationship(project, "tasks", task);

        EntityInstance secondTask = new EntityInstance(taskDefinition);
        secondTask.setValue("title", "Write tests");
        repository.addInstance(secondTask);
        repository.connectRelationship(project, "tasks", secondTask);

        Assertions.assertTrue(repository.getConnectedItems(task, "task-of").contains(project));
        Assertions.assertTrue(repository.getConnectedItems(project, "tasks").contains(task));

        Assertions.assertTrue(
                repository.listRelatedInstances(task, "task-of").contains(project));
        Assertions.assertTrue(
                repository.listRelatedInstances(project, "tasks").contains(task));
        Assertions.assertTrue(
                repository.listRelatedInstances(project, "tasks").contains(secondTask));

        QueryFilterParams relationshipFilterParams = new QueryFilterParams();
        relationshipFilterParams.put("title", "*=Wire*");
        List<EntityInstance> filteredTasks =
                repository.listRelatedInstances(project, "tasks", relationshipFilterParams);
        Assertions.assertEquals(1, filteredTasks.size());
        Assertions.assertEquals(task, filteredTasks.get(0));

        QueryFilterParams relationshipSortParams = new QueryFilterParams();
        relationshipSortParams.put("sortBy", "-id");
        List<EntityInstance> sortedTasks =
                repository.listRelatedInstances(project, "tasks", relationshipSortParams);
        Assertions.assertEquals("2", sortedTasks.get(0).getPrimaryKeyValue());
        Assertions.assertEquals("1", sortedTasks.get(1).getPrimaryKeyValue());

        repository.removeRelationshipsInvolving(project, task, "tasks");

        Assertions.assertTrue(repository.getConnectedItems(task, "task-of").isEmpty());
        Assertions.assertFalse(repository.getConnectedItems(project, "tasks").contains(task));
        Assertions.assertTrue(repository.getConnectedItems(project, "tasks").contains(secondTask));

        repository.removeRelationshipsInvolving(project, secondTask, "tasks");

        Assertions.assertTrue(repository.getConnectedItems(project, "tasks").isEmpty());

        repository.deleteEntityInstance(task);
        repository.deleteEntityInstance(secondTask);

        Assertions.assertEquals(0, repository.listInstances(taskDefinition).size());
        Assertions.assertEquals(0, repository.countInstances(taskDefinition));

        repository.clearAllData();

        Assertions.assertEquals(0, repository.listInstances(projectDefinition).size());
        Assertions.assertEquals(0, repository.listInstances(taskDefinition).size());
        Assertions.assertEquals(0, repository.countInstances(projectDefinition));
        Assertions.assertEquals(0, repository.countInstances(taskDefinition));
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

    private void addProjectAndTask(
            final ThingRepository repository,
            final ERSchema schema) {
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        EntityDefinition taskDefinition = schema.getEntityDefinitionNamed("task");

        EntityInstance project = new EntityInstance(projectDefinition).
                setValue("title", "Repository project");
        EntityInstance task = new EntityInstance(taskDefinition).
                setValue("title", "Wire repository");

        repository.addInstance(project);
        repository.addInstance(task);
        repository.connectRelationship(project, "tasks", task);
    }

    private void assertExportedJsonContainsProjectAndTask(final String json) {
        Assertions.assertTrue(json.contains("\"projects\""));
        Assertions.assertTrue(json.contains("\"tasks\""));
        Assertions.assertTrue(json.contains("\"id\": 1"));
        Assertions.assertTrue(json.contains("\"title\": \"Repository project\""));
        Assertions.assertTrue(json.contains("\"title\": \"Wire repository\""));
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
