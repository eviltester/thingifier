package uk.co.compendiumdev.thingifier.core.repository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.query.RepositoryQuery;
import uk.co.compendiumdev.thingifier.core.query.RepositoryQuerySpec;
import uk.co.compendiumdev.thingifier.core.reporting.ERModelReport;
import uk.co.compendiumdev.thingifier.core.reporting.RepositoryJsonExporter;
import uk.co.compendiumdev.thingifier.core.repository.inmemory.InMemoryThingStore;
import uk.co.compendiumdev.thingifier.core.repository.inmemory.InMemoryThingStoreProvider;
import uk.co.compendiumdev.thingifier.core.repository.sqlite.SqliteThingStore;
import uk.co.compendiumdev.thingifier.core.repository.sqlite.SqliteThingStoreProvider;

public class ThingStoreContractTest {

    @TempDir Path tempDir;

    @Test
    public void inMemoryRepositorySupportsTheContract() {
        ThingStore repository = new InMemoryThingStore(EntityRelModel.DEFAULT_DATABASE_NAME);

        exerciseRepositoryContract(repository);
    }

    @Test
    public void sqliteInMemoryRepositorySupportsTheContract() {
        try (ThingStore repository =
                SqliteThingStore.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            exerciseRepositoryContract(repository);
        }
    }

    @Test
    public void inMemoryProviderKeepsLogicalDatabasesSeparate() {
        ThingStoreProvider provider = new InMemoryThingStoreProvider();

        exerciseProviderIsolation(provider);
    }

    @Test
    public void sqliteProviderKeepsLogicalDatabasesSeparate() {
        try (ThingStoreProvider provider = SqliteThingStoreProvider.inMemory()) {
            exerciseProviderIsolation(provider);
        }
    }

    @Test
    public void directSqliteFileProviderCreatesAndReopensDatabaseFile() {
        ERSchema schema = todoSchema();
        Path databasePath = tempDir.resolve("direct-provider.sqlite");

        try (ThingStoreProvider provider = SqliteThingStoreProvider.fileBackedFile(databasePath)) {
            ThingStore store = provider.getDefaultStore();
            store.administration().initializeFrom(schema);
            create(store, schema.getEntityDefinitionNamed("project"), "File provider");
        }

        Assertions.assertTrue(Files.exists(databasePath));

        try (ThingStoreProvider reopened = SqliteThingStoreProvider.fileBackedFile(databasePath)) {
            ThingStore store = reopened.getDefaultStore();
            store.administration().initializeFrom(schema);

            Assertions.assertEquals(
                    1, store.entityQueries().count(schema.getEntityDefinitionNamed("project")));
            Assertions.assertEquals(
                    "File provider",
                    store.entityQueries()
                            .findByPrimaryKey(schema.getEntityDefinitionNamed("project"), "1")
                            .getFieldValue("title")
                            .asString());
        }
    }

    @Test
    public void inMemoryTransactionCommitPersistsChanges() {
        ThingStore repository = new InMemoryThingStore(EntityRelModel.DEFAULT_DATABASE_NAME);

        exerciseTransactionCommit(repository);
    }

    @Test
    public void sqliteTransactionCommitPersistsChanges() {
        try (ThingStore repository =
                SqliteThingStore.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            exerciseTransactionCommit(repository);
        }
    }

    @Test
    public void inMemoryTransactionRollbackRestoresEntitiesRelationshipsAndCounters() {
        ThingStore repository = new InMemoryThingStore(EntityRelModel.DEFAULT_DATABASE_NAME);

        exerciseTransactionRollback(repository);
    }

    @Test
    public void sqliteTransactionRollbackRestoresEntitiesRelationshipsAndCounters() {
        try (ThingStore repository =
                SqliteThingStore.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            exerciseTransactionRollback(repository);
        }
    }

    @Test
    public void sqliteRepositoryCannotBeUsedAfterClose() {
        ERSchema schema = todoSchema();
        ThingStore repository = SqliteThingStore.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME);
        repository.administration().initializeFrom(schema);

        repository.close();

        Assertions.assertThrows(
                IllegalStateException.class,
                () -> repository.entityQueries().list(schema.getEntityDefinitionNamed("project")));
    }

    @Test
    public void sqliteFileBackedRepositorySurvivesCloseAndReopen() {
        ERSchema schema = todoSchema();
        Path databasePath = tempDir.resolve("thingifier.sqlite");

        try (ThingStore repository =
                SqliteThingStore.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            repository.administration().initializeFrom(schema);

            EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
            EntityDefinition taskDefinition = schema.getEntityDefinitionNamed("task");
            EntityInstance project = create(repository, projectDefinition, "Persisted");
            EntityInstance task = create(repository, taskDefinition, "Loaded relationship");
            repository.relationships().connect(task, "task-of", project);
        }

        try (ThingStore reopened =
                SqliteThingStore.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            reopened.administration().initializeFrom(schema);

            EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
            EntityDefinition taskDefinition = schema.getEntityDefinitionNamed("task");

            EntityInstance project =
                    reopened.entityQueries().findByPrimaryKey(projectDefinition, "1");
            EntityInstance task =
                    reopened.entityQueries()
                            .findByField(taskDefinition, "title", "Loaded relationship");

            Assertions.assertNotNull(project);
            Assertions.assertNotNull(task);
            Assertions.assertEquals("Persisted", project.getFieldValue("title").asString());
            Assertions.assertTrue(
                    reopened.relationships().listRelated(task, "task-of").contains(project));
            Assertions.assertTrue(
                    reopened.relationships().listRelated(project, "tasks").contains(task));
        }
    }

    @Test
    public void sqliteRepositoryQuotesGeneratedSqlIdentifiers() {
        ERSchema schema = new ERSchema();
        EntityDefinition select = schema.defineEntity("select", "selects", -1);
        select.addAsPrimaryKeyField(Field.is("key-id", FieldType.AUTO_GUID));
        select.addField(Field.is("field with space", FieldType.STRING));

        try (ThingStore repository = SqliteThingStore.inMemory("quoted")) {
            repository.administration().initializeFrom(schema);

            repository
                    .entities()
                    .create(
                            EntityInstanceDraft.forEntity(select)
                                    .withField("field with space", "works"));

            EntityInstance found =
                    repository.entityQueries().findByField(select, "field with space", "works");

            Assertions.assertNotNull(found);
            Assertions.assertEquals("works", found.getFieldValue("field with space").asString());
        }
    }

    @Test
    public void inMemoryRepositoryGeneratesAutoIdsThroughTheContract() {
        ThingStore repository = new InMemoryThingStore(EntityRelModel.DEFAULT_DATABASE_NAME);

        exerciseAutoIdGeneration(repository);
    }

    @Test
    public void sqliteRepositoryGeneratesAutoIdsThroughTheContract() {
        try (ThingStore repository =
                SqliteThingStore.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            exerciseAutoIdGeneration(repository);
        }
    }

    @Test
    public void sqliteRepositoryThrowsTypedMaxInstanceLimitFailure() {
        ERSchema schema = ticketSchema(1);
        EntityDefinition ticket = schema.getEntityDefinitionNamed("ticket");

        try (ThingStore repository =
                SqliteThingStore.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            repository.administration().initializeFrom(schema);
            createWithId(repository, ticket, "one");

            ThingStoreWriteException exception =
                    Assertions.assertThrows(
                            ThingStoreWriteException.class,
                            () -> createWithId(repository, ticket, "two"));

            Assertions.assertEquals(
                    ThingStoreWriteException.Reason.MAX_INSTANCE_LIMIT_REACHED, exception.reason());
            Assertions.assertEquals(
                    "ERROR: Cannot add instance, maximum limit of 1 reached",
                    exception.getMessage());
        }
    }

    @Test
    public void sqliteRepositoryThrowsTypedDuplicatePrimaryKeyFailure() {
        ERSchema schema = ticketSchema(-1);
        EntityDefinition ticket = schema.getEntityDefinitionNamed("ticket");

        try (ThingStore repository =
                SqliteThingStore.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            repository.administration().initializeFrom(schema);
            createWithId(repository, ticket, "same");

            ThingStoreWriteException exception =
                    Assertions.assertThrows(
                            ThingStoreWriteException.class,
                            () -> createWithId(repository, ticket, "same"));

            Assertions.assertEquals(
                    ThingStoreWriteException.Reason.DUPLICATE_PRIMARY_KEY, exception.reason());
            Assertions.assertEquals(
                    "ERROR: Cannot add instance, another instance with primary key value exists: "
                            + "same",
                    exception.getMessage());
        }
    }

    @Test
    public void sqliteRepositoryThrowsTypedMissingPrimaryKeyFailure() {
        ERSchema schema = ticketSchema(-1);
        EntityDefinition ticket = schema.getEntityDefinitionNamed("ticket");

        try (ThingStore repository =
                SqliteThingStore.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            repository.administration().initializeFrom(schema);

            ThingStoreWriteException exception =
                    Assertions.assertThrows(
                            ThingStoreWriteException.class,
                            () ->
                                    repository
                                            .entities()
                                            .create(
                                                    EntityInstanceDraft.forEntity(ticket)
                                                            .withField("title", "Missing id")));

            Assertions.assertEquals(
                    ThingStoreWriteException.Reason.MISSING_PRIMARY_KEY, exception.reason());
            Assertions.assertEquals(
                    "ERROR: Cannot add instance, primary key field id not set",
                    exception.getMessage());
        }
    }

    @Test
    public void inMemoryRepositoryOwnsRelationshipValidationAndCascade() {
        ThingStore repository = new InMemoryThingStore(EntityRelModel.DEFAULT_DATABASE_NAME);

        exerciseMandatoryRelationshipValidationAndCascade(repository);
    }

    @Test
    public void sqliteRepositoryOwnsRelationshipValidationAndCascade() {
        try (ThingStore repository =
                SqliteThingStore.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            exerciseMandatoryRelationshipValidationAndCascade(repository);
        }
    }

    @Test
    public void sqliteFileBackedRepositoryFiltersAfterReopen() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        Path databasePath = tempDir.resolve("lazy.sqlite");

        try (ThingStore repository =
                SqliteThingStore.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            repository.administration().initializeFrom(schema);

            create(repository, projectDefinition, "First");
            create(repository, projectDefinition, "Second");
        }

        try (ThingStore reopened =
                SqliteThingStore.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            reopened.administration().initializeFrom(schema);

            QueryFilterParams params = new QueryFilterParams();
            params.put("title", "First");

            List<EntityInstance> filtered =
                    reopened.entityQueries().list(projectDefinition, params);

            Assertions.assertEquals(1, filtered.size());
            Assertions.assertEquals("First", filtered.get(0).getFieldValue("title").asString());
            Assertions.assertEquals(2, reopened.entityQueries().count(projectDefinition));
        }
    }

    @Test
    public void sqliteCountInstancesWorksAfterReopen() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        Path databasePath = tempDir.resolve("count-lazy.sqlite");

        try (ThingStore repository =
                SqliteThingStore.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            repository.administration().initializeFrom(schema);

            create(repository, projectDefinition, "First");
            create(repository, projectDefinition, "Second");
        }

        try (ThingStore reopened =
                SqliteThingStore.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            reopened.administration().initializeFrom(schema);

            Assertions.assertEquals(2, reopened.entityQueries().count(projectDefinition));
        }
    }

    @Test
    public void repositoriesExportDataAsJsonThroughTheRepositoryContract() {
        ERSchema schema = todoSchema();

        ThingStore memoryRepository = new InMemoryThingStore(EntityRelModel.DEFAULT_DATABASE_NAME);
        memoryRepository.administration().initializeFrom(schema);
        addProjectAndTask(memoryRepository, schema);

        try (ThingStore sqliteRepository =
                SqliteThingStore.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            sqliteRepository.administration().initializeFrom(schema);
            addProjectAndTask(sqliteRepository, schema);

            assertExportedJsonContainsProjectAndTask(exportDataAsJson(memoryRepository, schema));
            assertExportedJsonContainsProjectAndTask(exportDataAsJson(sqliteRepository, schema));
        }
    }

    @Test
    public void sqliteJsonExportWorksAfterReopen() {
        ERSchema schema = todoSchema();
        Path databasePath = tempDir.resolve("export-lazy.sqlite");

        try (ThingStore repository =
                SqliteThingStore.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            repository.administration().initializeFrom(schema);
            addProjectAndTask(repository, schema);
        }

        try (ThingStore reopened =
                SqliteThingStore.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            reopened.administration().initializeFrom(schema);

            String json = exportDataAsJson(reopened, schema);

            assertExportedJsonContainsProjectAndTask(json);
        }
    }

    @Test
    public void repositoriesReportMarkdownThroughTheRepositoryContract() {
        ERSchema schema = todoSchema();

        try (ThingStore repository =
                SqliteThingStore.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            repository.administration().initializeFrom(schema);
            addProjectAndTask(repository, schema);

            String markdown = new ERModelReport(schema, repository.entityQueries()).asMarkdown();

            Assertions.assertTrue(markdown.contains("# Entity Definitions:"));
            Assertions.assertTrue(markdown.contains("# Instances"));
            Assertions.assertTrue(markdown.contains("## Of project"));
            Assertions.assertTrue(markdown.contains("Repository project"));
            Assertions.assertTrue(markdown.contains("Wire repository"));
        }
    }

    @Test
    public void sqliteRelationshipReadsWorkAfterReopen() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        EntityDefinition taskDefinition = schema.getEntityDefinitionNamed("task");
        Path databasePath = tempDir.resolve("relationship-lazy.sqlite");

        try (ThingStore repository =
                SqliteThingStore.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            repository.administration().initializeFrom(schema);

            EntityInstance project = create(repository, projectDefinition, "SQLite project");
            EntityInstance firstTask = create(repository, taskDefinition, "Wire repository");
            EntityInstance secondTask = create(repository, taskDefinition, "Write tests");

            repository.relationships().connect(project, "tasks", firstTask);
            repository.relationships().connect(project, "tasks", secondTask);
        }

        try (ThingStore reopened =
                SqliteThingStore.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            reopened.administration().initializeFrom(schema);

            EntityInstance project =
                    reopened.entityQueries().findByPrimaryKey(projectDefinition, "1");

            QueryFilterParams params = new QueryFilterParams();
            params.put("title", "*=Wire*");

            List<EntityInstance> filteredTasks =
                    reopened.relationships().listRelated(project, "tasks", params);

            Assertions.assertEquals(1, filteredTasks.size());
            Assertions.assertEquals(
                    "Wire repository", filteredTasks.get(0).getFieldValue("title").asString());

            QueryFilterParams regexParams = new QueryFilterParams();
            regexParams.put("title", "~=Wire [Rr]epository");

            List<EntityInstance> regexFilteredTasks =
                    reopened.relationships().listRelated(project, "tasks", regexParams);

            Assertions.assertEquals(1, regexFilteredTasks.size());
            Assertions.assertEquals(
                    "Wire repository", regexFilteredTasks.get(0).getFieldValue("title").asString());

            QueryFilterParams sortedParams = new QueryFilterParams();
            sortedParams.put("sortBy", "-id");

            List<EntityInstance> sortedTasks =
                    reopened.relationships().listRelated(project, "tasks", sortedParams);

            Assertions.assertEquals(2, sortedTasks.size());
            Assertions.assertEquals("2", sortedTasks.get(0).getPrimaryKeyValue());
            Assertions.assertEquals("1", sortedTasks.get(1).getPrimaryKeyValue());
            Assertions.assertEquals(
                    2, reopened.relationships().listRelated(project, "tasks").size());

            RepositoryQuery query =
                    new RepositoryQuery(
                                    reopened,
                                    RepositoryQuerySpec.relationship(
                                            projectDefinition, "1", "tasks"))
                            .performQuery(params);

            Assertions.assertTrue(query.isResultACollection());
            Assertions.assertEquals(1, query.getListEntityInstances().size());
            Assertions.assertEquals(
                    "Wire repository",
                    query.getListEntityInstances().get(0).getFieldValue("title").asString());
        }
    }

    @Test
    public void sqliteConvertibleRegexFilterWorksAfterReopen() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        Path databasePath = tempDir.resolve("regex-like-lazy.sqlite");

        try (ThingStore repository =
                SqliteThingStore.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            repository.administration().initializeFrom(schema);

            create(repository, projectDefinition, "Repository project");
            create(repository, projectDefinition, "repository project");
            create(repository, projectDefinition, "Another project");
        }

        try (ThingStore reopened =
                SqliteThingStore.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            reopened.administration().initializeFrom(schema);

            QueryFilterParams regexParams = new QueryFilterParams();
            regexParams.put("title", "~=Repository.*");

            List<EntityInstance> filtered =
                    reopened.entityQueries().list(projectDefinition, regexParams);

            Assertions.assertEquals(1, filtered.size());
            Assertions.assertEquals(
                    "Repository project", filtered.get(0).getFieldValue("title").asString());
            Assertions.assertEquals(3, reopened.entityQueries().count(projectDefinition));
        }
    }

    @Test
    public void sqliteRegexpFilterWorksAfterReopen() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        Path databasePath = tempDir.resolve("regex-regexp-lazy.sqlite");

        try (ThingStore repository =
                SqliteThingStore.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            repository.administration().initializeFrom(schema);

            create(repository, projectDefinition, "Repository project");
            create(repository, projectDefinition, "Another project");
        }

        try (ThingStore reopened =
                SqliteThingStore.fileBacked(EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            reopened.administration().initializeFrom(schema);

            QueryFilterParams regexParams = new QueryFilterParams();
            regexParams.put("title", "~=Repository [Pp]roject");

            List<EntityInstance> filtered =
                    reopened.entityQueries().list(projectDefinition, regexParams);

            Assertions.assertEquals(1, filtered.size());
            Assertions.assertEquals(
                    "Repository project", filtered.get(0).getFieldValue("title").asString());
            Assertions.assertEquals(2, reopened.entityQueries().count(projectDefinition));
        }
    }

    @Test
    public void sqliteInMemoryRegexpFilterWorks() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");

        try (ThingStore repository =
                SqliteThingStore.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            repository.administration().initializeFrom(schema);

            create(repository, projectDefinition, "Repository project");
            create(repository, projectDefinition, "Another project");

            QueryFilterParams regexParams = new QueryFilterParams();
            regexParams.put("title", "~=Repository [Pp]roject");

            List<EntityInstance> filtered =
                    repository.entityQueries().list(projectDefinition, regexParams);

            Assertions.assertEquals(1, filtered.size());
            Assertions.assertEquals(
                    "Repository project", filtered.get(0).getFieldValue("title").asString());
        }
    }

    @Test
    public void sqliteInvalidRegexFilterFails() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");

        try (ThingStore repository =
                SqliteThingStore.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            repository.administration().initializeFrom(schema);
            create(repository, projectDefinition, "Repository project");

            QueryFilterParams regexParams = new QueryFilterParams();
            regexParams.put("title", "~=[");

            Assertions.assertThrows(
                    RuntimeException.class,
                    () -> repository.entityQueries().list(projectDefinition, regexParams));
        }
    }

    @Test
    public void sqliteTooComplexRegexFilterFails() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");

        try (ThingStore repository =
                SqliteThingStore.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            repository.administration().initializeFrom(schema);
            create(repository, projectDefinition, "Repository project");

            QueryFilterParams regexParams = new QueryFilterParams();
            regexParams.put("title", "~=(.+)+");

            Assertions.assertThrows(
                    RuntimeException.class,
                    () -> repository.entityQueries().list(projectDefinition, regexParams));
        }
    }

    private void exerciseRepositoryContract(final ThingStore repository) {
        ERSchema schema = todoSchema();
        repository.administration().initializeFrom(schema);

        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        EntityDefinition taskDefinition = schema.getEntityDefinitionNamed("task");

        EntityInstance project = create(repository, projectDefinition, "Repository project");

        Assertions.assertEquals("1", project.getPrimaryKeyValue());
        Assertions.assertThrows(
                NoSuchMethodException.class,
                () -> EntityInstance.class.getMethod("setValue", String.class, String.class));
        Assertions.assertEquals(
                "Repository project",
                repository
                        .entityQueries()
                        .findByPrimaryKey(projectDefinition, "1")
                        .getFieldValue("title")
                        .asString());
        Assertions.assertEquals(
                project, repository.entityQueries().findByPrimaryKey(projectDefinition, "1"));
        Assertions.assertEquals(
                project, repository.entityQueries().findByQueryIdentifier(projectDefinition, "1"));
        Assertions.assertEquals(
                project,
                repository
                        .entityQueries()
                        .findByField(projectDefinition, "title", "Repository project"));
        Assertions.assertEquals(1, repository.entityQueries().list(projectDefinition).size());
        Assertions.assertEquals(1, repository.entityQueries().count(projectDefinition));
        Assertions.assertEquals(0, repository.entityQueries().count(taskDefinition));

        EntityInstance secondProject = create(repository, projectDefinition, "Another project");
        Assertions.assertEquals(2, repository.entityQueries().count(projectDefinition));
        secondProject =
                repository
                        .entities()
                        .replace(
                                secondProject,
                                EntityInstanceDraft.forEntity(projectDefinition)
                                        .withField("title", "Replacement project"));
        Assertions.assertEquals("2", secondProject.getPrimaryKeyValue());
        Assertions.assertEquals(
                "Replacement project",
                repository
                        .entityQueries()
                        .findByPrimaryKey(projectDefinition, "2")
                        .getFieldValue("title")
                        .asString());

        QueryFilterParams filteredParams = new QueryFilterParams();
        filteredParams.put("id", ">=2");
        List<EntityInstance> filteredProjects =
                repository.entityQueries().list(projectDefinition, filteredParams);
        Assertions.assertEquals(1, filteredProjects.size());
        Assertions.assertEquals(secondProject, filteredProjects.get(0));

        QueryFilterParams sortedParams = new QueryFilterParams();
        sortedParams.put("sortBy", "-id");
        List<EntityInstance> sortedProjects =
                repository.entityQueries().list(projectDefinition, sortedParams);
        Assertions.assertEquals("2", sortedProjects.get(0).getPrimaryKeyValue());
        Assertions.assertEquals("1", sortedProjects.get(1).getPrimaryKeyValue());

        QueryFilterParams regexParams = new QueryFilterParams();
        regexParams.put("title", "~=Repository.*");
        List<EntityInstance> regexProjects =
                repository.entityQueries().list(projectDefinition, regexParams);
        Assertions.assertEquals(1, regexProjects.size());
        Assertions.assertEquals(project, regexProjects.get(0));

        Assertions.assertThrows(
                RuntimeException.class,
                () ->
                        repository
                                .entities()
                                .create(
                                        EntityInstanceDraft.forEntity(projectDefinition)
                                                .withField("title", "Repository project")));

        EntityInstance task = create(repository, taskDefinition, "Wire repository");
        Assertions.assertFalse(repository.relationships().hasRelationships(project));
        Assertions.assertFalse(repository.relationships().hasRelationships(task));
        repository.relationships().connect(project, "tasks", task);
        Assertions.assertTrue(repository.relationships().hasRelationships(project));
        Assertions.assertTrue(repository.relationships().hasRelationships(task));
        Assertions.assertTrue(repository.relationships().validate(project).isValid());
        Assertions.assertTrue(repository.relationships().validate(task).isValid());

        EntityInstance secondTask = create(repository, taskDefinition, "Write tests");
        repository.relationships().connect(project, "tasks", secondTask);
        EntityInstance overAssignedProject =
                create(repository, projectDefinition, "Second assignment target");
        Assertions.assertThrows(
                RuntimeException.class,
                () -> repository.relationships().connect(task, "task-of", overAssignedProject));

        Assertions.assertThrows(
                NoSuchMethodException.class,
                () -> EntityInstance.class.getMethod("getRelationships"));
        Assertions.assertThrows(
                NoSuchMethodException.class,
                () -> EntityInstance.class.getMethod("clearAllFields"));
        Assertions.assertTrue(
                repository.relationships().listRelated(task, "task-of").contains(project));
        Assertions.assertTrue(
                repository.relationships().listRelated(project, "tasks").contains(task));

        Assertions.assertTrue(
                repository.relationships().listRelated(task, "task-of").contains(project));
        Assertions.assertTrue(
                repository.relationships().listRelated(project, "tasks").contains(task));
        Assertions.assertTrue(
                repository.relationships().listRelated(project, "tasks").contains(secondTask));

        QueryFilterParams relationshipFilterParams = new QueryFilterParams();
        relationshipFilterParams.put("title", "*=Wire*");
        List<EntityInstance> filteredTasks =
                repository.relationships().listRelated(project, "tasks", relationshipFilterParams);
        Assertions.assertEquals(1, filteredTasks.size());
        Assertions.assertEquals(task, filteredTasks.get(0));

        QueryFilterParams relationshipSortParams = new QueryFilterParams();
        relationshipSortParams.put("sortBy", "-id");
        List<EntityInstance> sortedTasks =
                repository.relationships().listRelated(project, "tasks", relationshipSortParams);
        Assertions.assertEquals("2", sortedTasks.get(0).getPrimaryKeyValue());
        Assertions.assertEquals("1", sortedTasks.get(1).getPrimaryKeyValue());

        repository.relationships().removeBetween(project, task, "tasks");

        Assertions.assertTrue(repository.relationships().listRelated(task, "task-of").isEmpty());
        Assertions.assertFalse(repository.relationships().hasRelationships(task));
        Assertions.assertFalse(
                repository.relationships().listRelated(project, "tasks").contains(task));
        Assertions.assertTrue(
                repository.relationships().listRelated(project, "tasks").contains(secondTask));

        repository.relationships().removeBetween(project, secondTask, "tasks");

        Assertions.assertTrue(repository.relationships().listRelated(project, "tasks").isEmpty());

        repository.entities().delete(task);
        repository.entities().delete(secondTask);

        Assertions.assertEquals(0, repository.entityQueries().list(taskDefinition).size());
        Assertions.assertEquals(0, repository.entityQueries().count(taskDefinition));

        repository.administration().clearAllData();

        Assertions.assertEquals(0, repository.entityQueries().list(projectDefinition).size());
        Assertions.assertEquals(0, repository.entityQueries().list(taskDefinition).size());
        Assertions.assertEquals(0, repository.entityQueries().count(projectDefinition));
        Assertions.assertEquals(0, repository.entityQueries().count(taskDefinition));

        repository.administration().resetAutoIncrementCounter(projectDefinition, "id");

        EntityInstance resetProject = create(repository, projectDefinition, "Reset project");

        Assertions.assertEquals("1", resetProject.getPrimaryKeyValue());
    }

    private void exerciseProviderIsolation(final ThingStoreProvider provider) {
        ERSchema schema = todoSchema();
        ThingStore defaultRepository = provider.getDefaultStore();
        defaultRepository.administration().initializeFrom(schema);
        provider.createStore("session-one", schema);

        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        create(defaultRepository, projectDefinition, "Only default");

        Assertions.assertEquals(
                1, defaultRepository.entityQueries().list(projectDefinition).size());
        Assertions.assertEquals(
                0, provider.getStore("session-one").entityQueries().list(projectDefinition).size());
    }

    private void exerciseAutoIdGeneration(final ThingStore repository) {
        ERSchema schema = autoIdSchema();
        repository.administration().initializeFrom(schema);

        EntityDefinition session = schema.getEntityDefinitionNamed("session");
        EntityInstance firstSession =
                repository.entities().create(EntityInstanceDraft.forEntity(session));
        EntityInstance secondSession =
                repository.entities().create(EntityInstanceDraft.forEntity(session));

        Assertions.assertNotNull(firstSession.getPrimaryKeyValue());
        Assertions.assertNotNull(secondSession.getPrimaryKeyValue());
        Assertions.assertDoesNotThrow(() -> UUID.fromString(firstSession.getPrimaryKeyValue()));
        Assertions.assertDoesNotThrow(() -> UUID.fromString(secondSession.getPrimaryKeyValue()));
        Assertions.assertNotEquals(
                firstSession.getPrimaryKeyValue(), secondSession.getPrimaryKeyValue());

        String explicitGuid = "12345678-1234-1234-1234-123456789abc";
        EntityInstance explicitSession =
                repository
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(session)
                                        .withProtectedField("guid", explicitGuid));
        Assertions.assertEquals(explicitGuid, explicitSession.getPrimaryKeyValue());

        EntityDefinition ticket = schema.getEntityDefinitionNamed("ticket");
        EntityInstance firstTicket =
                repository.entities().create(EntityInstanceDraft.forEntity(ticket));
        EntityInstance secondTicket =
                repository.entities().create(EntityInstanceDraft.forEntity(ticket));

        Assertions.assertEquals("1", firstTicket.getPrimaryKeyValue());
        Assertions.assertEquals("2", secondTicket.getPrimaryKeyValue());

        EntityInstance explicitTicket =
                repository
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(ticket)
                                        .withProtectedField("id", "25"));
        EntityInstance ticketAfterExplicit =
                repository.entities().create(EntityInstanceDraft.forEntity(ticket));

        Assertions.assertEquals("25", explicitTicket.getPrimaryKeyValue());
        Assertions.assertEquals("26", ticketAfterExplicit.getPrimaryKeyValue());
    }

    private void exerciseMandatoryRelationshipValidationAndCascade(final ThingStore repository) {
        ERSchema schema = mandatoryRelationshipSchema();
        repository.administration().initializeFrom(schema);

        EntityDefinition parentDefinition = schema.getEntityDefinitionNamed("parent");
        EntityDefinition childDefinition = schema.getEntityDefinitionNamed("child");

        EntityInstance parent = create(repository, parentDefinition, "Parent");
        EntityInstance child = create(repository, childDefinition, "Child");

        Assertions.assertFalse(repository.relationships().validate(child).isValid());

        repository.relationships().connect(child, "parent", parent);

        Assertions.assertTrue(repository.relationships().validate(child).isValid());
        Assertions.assertTrue(
                repository.relationships().listRelated(parent, "children").contains(child));
        Assertions.assertTrue(
                repository.relationships().listRelated(child, "parent").contains(parent));

        repository.entities().delete(parent);

        Assertions.assertEquals(0, repository.entityQueries().count(parentDefinition));
        Assertions.assertEquals(0, repository.entityQueries().count(childDefinition));

        EntityInstance relationshipParent =
                create(repository, parentDefinition, "Relationship parent");
        EntityInstance relationshipChild =
                create(repository, childDefinition, "Relationship child");
        repository.relationships().connect(relationshipChild, "parent", relationshipParent);

        repository.relationships().removeBetween(relationshipParent, relationshipChild, "children");

        Assertions.assertEquals(1, repository.entityQueries().count(parentDefinition));
        Assertions.assertEquals(0, repository.entityQueries().count(childDefinition));
        Assertions.assertFalse(repository.relationships().hasRelationships(relationshipParent));

        EntityInstance resetParent = create(repository, parentDefinition, "Reset parent");
        EntityInstance resetChild = create(repository, childDefinition, "Reset child");
        repository.relationships().connect(resetChild, "parent", resetParent);

        repository.relationships().removeAll(resetParent);

        Assertions.assertEquals(2, repository.entityQueries().count(parentDefinition));
        Assertions.assertEquals(0, repository.entityQueries().count(childDefinition));
        Assertions.assertFalse(repository.relationships().hasRelationships(resetParent));
    }

    private void addProjectAndTask(final ThingStore repository, final ERSchema schema) {
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        EntityDefinition taskDefinition = schema.getEntityDefinitionNamed("task");

        EntityInstance project = create(repository, projectDefinition, "Repository project");
        EntityInstance task = create(repository, taskDefinition, "Wire repository");
        repository.relationships().connect(project, "tasks", task);
    }

    private void exerciseTransactionCommit(final ThingStore repository) {
        ERSchema schema = todoSchema();
        repository.administration().initializeFrom(schema);
        EntityDefinition project = schema.getEntityDefinitionNamed("project");

        try (ThingStoreTransaction transaction = repository.beginTransaction()) {
            create(repository, project, "Committed project");
            transaction.commit();
        }

        Assertions.assertEquals(1, repository.entityQueries().count(project));
    }

    private void exerciseTransactionRollback(final ThingStore repository) {
        ERSchema schema = todoSchema();
        repository.administration().initializeFrom(schema);
        EntityDefinition project = schema.getEntityDefinitionNamed("project");
        EntityDefinition task = schema.getEntityDefinitionNamed("task");

        try (ThingStoreTransaction transaction = repository.beginTransaction()) {
            EntityInstance projectInstance = create(repository, project, "Rolled back project");
            EntityInstance taskInstance = create(repository, task, "Rolled back task");
            repository.relationships().connect(projectInstance, "tasks", taskInstance);
            transaction.rollback();
        }

        Assertions.assertEquals(0, repository.entityQueries().count(project));
        Assertions.assertEquals(0, repository.entityQueries().count(task));

        EntityInstance taskAfterRollback = create(repository, task, "Counter starts again");
        Assertions.assertEquals("1", taskAfterRollback.getPrimaryKeyValue());
    }

    private EntityInstance create(
            final ThingStore repository, final EntityDefinition entity, final String title) {
        return repository
                .entities()
                .create(EntityInstanceDraft.forEntity(entity).withField("title", title));
    }

    private EntityInstance createWithId(
            final ThingStore repository, final EntityDefinition entity, final String id) {
        return repository
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(entity)
                                .withField("id", id)
                                .withField("title", "Title " + id));
    }

    private String exportDataAsJson(final ThingStore repository, final ERSchema schema) {
        return new RepositoryJsonExporter(schema, repository.entityQueries()).asJson();
    }

    private ERSchema autoIdSchema() {
        ERSchema schema = new ERSchema();

        EntityDefinition session = schema.defineEntity("session", "sessions", -1);
        session.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));

        EntityDefinition ticket = schema.defineEntity("ticket", "tickets", -1);
        ticket.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));

        return schema;
    }

    private ERSchema ticketSchema(final int maxInstances) {
        ERSchema schema = new ERSchema();

        EntityDefinition ticket = schema.defineEntity("ticket", "tickets", maxInstances);
        ticket.addAsPrimaryKeyField(Field.is("id", FieldType.STRING));
        ticket.addField(Field.is("title", FieldType.STRING));

        return schema;
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

        schema.defineRelationship(task, project, "task-of", Cardinality.ONE_TO_ONE())
                .whenReversed(Cardinality.ONE_TO_MANY(), "tasks");

        return schema;
    }

    private ERSchema mandatoryRelationshipSchema() {
        ERSchema schema = new ERSchema();

        EntityDefinition parent = schema.defineEntity("parent", "parents", -1);
        parent.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        parent.addField(Field.is("title", FieldType.STRING));

        EntityDefinition child = schema.defineEntity("child", "children", -1);
        child.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        child.addField(Field.is("title", FieldType.STRING));

        schema.defineRelationship(child, parent, "parent", Cardinality.ONE_TO_ONE())
                .whenReversed(Cardinality.ONE_TO_MANY(), "children")
                .getFromRelationship()
                .setOptionality(Optionality.MANDATORY_RELATIONSHIP);

        return schema;
    }
}
