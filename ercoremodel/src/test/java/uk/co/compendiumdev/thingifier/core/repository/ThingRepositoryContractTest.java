package uk.co.compendiumdev.thingifier.core.repository;

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
import uk.co.compendiumdev.thingifier.core.query.RepositoryUrlQuery;
import uk.co.compendiumdev.thingifier.core.repository.inmemory.InMemoryThingRepository;
import uk.co.compendiumdev.thingifier.core.repository.inmemory.InMemoryThingRepositoryProvider;
import uk.co.compendiumdev.thingifier.core.repository.sqlite.SqliteThingRepository;
import uk.co.compendiumdev.thingifier.core.repository.sqlite.SqliteThingRepositoryProvider;

public class ThingRepositoryContractTest {

    @TempDir Path tempDir;

    @Test
    public void inMemoryRepositorySupportsTheContract() {
        ThingRepository repository =
                new InMemoryThingRepository(EntityRelModel.DEFAULT_DATABASE_NAME);

        exerciseRepositoryContract(repository);
    }

    @Test
    public void sqliteInMemoryRepositorySupportsTheContract() {
        try (ThingRepository repository =
                SqliteThingRepository.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
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
        ThingRepository repository =
                SqliteThingRepository.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME);
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
                SqliteThingRepository.fileBacked(
                        EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            repository.initializeFrom(schema);

            EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
            EntityDefinition taskDefinition = schema.getEntityDefinitionNamed("task");
            EntityInstance project = create(repository, projectDefinition, "Persisted");
            EntityInstance task = create(repository, taskDefinition, "Loaded relationship");
            repository.connectRelationship(task, "task-of", project);
        }

        try (ThingRepository reopened =
                SqliteThingRepository.fileBacked(
                        EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            reopened.initializeFrom(schema);

            EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
            EntityDefinition taskDefinition = schema.getEntityDefinitionNamed("task");

            EntityInstance project = reopened.findInstanceByPrimaryKey(projectDefinition, "1");
            EntityInstance task =
                    reopened.findInstanceByFieldNameAndValue(
                            taskDefinition, "title", "Loaded relationship");

            Assertions.assertNotNull(project);
            Assertions.assertNotNull(task);
            Assertions.assertEquals("Persisted", project.getFieldValue("title").asString());
            Assertions.assertTrue(reopened.listRelatedInstances(task, "task-of").contains(project));
            Assertions.assertTrue(reopened.listRelatedInstances(project, "tasks").contains(task));
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

            repository.createInstance(
                    EntityInstanceDraft.forEntity(select).withField("field with space", "works"));

            EntityInstance found =
                    repository.findInstanceByFieldNameAndValue(select, "field with space", "works");

            Assertions.assertNotNull(found);
            Assertions.assertEquals("works", found.getFieldValue("field with space").asString());
        }
    }

    @Test
    public void inMemoryRepositoryGeneratesAutoIdsThroughTheContract() {
        ThingRepository repository =
                new InMemoryThingRepository(EntityRelModel.DEFAULT_DATABASE_NAME);

        exerciseAutoIdGeneration(repository);
    }

    @Test
    public void sqliteRepositoryGeneratesAutoIdsThroughTheContract() {
        try (ThingRepository repository =
                SqliteThingRepository.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            exerciseAutoIdGeneration(repository);
        }
    }

    @Test
    public void inMemoryRepositoryOwnsRelationshipValidationAndCascade() {
        ThingRepository repository =
                new InMemoryThingRepository(EntityRelModel.DEFAULT_DATABASE_NAME);

        exerciseMandatoryRelationshipValidationAndCascade(repository);
    }

    @Test
    public void sqliteRepositoryOwnsRelationshipValidationAndCascade() {
        try (ThingRepository repository =
                SqliteThingRepository.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            exerciseMandatoryRelationshipValidationAndCascade(repository);
        }
    }

    @Test
    public void sqliteFileBackedRepositoryFiltersAfterReopen() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        Path databasePath = tempDir.resolve("lazy.sqlite");

        try (ThingRepository repository =
                SqliteThingRepository.fileBacked(
                        EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            repository.initializeFrom(schema);

            create(repository, projectDefinition, "First");
            create(repository, projectDefinition, "Second");
        }

        try (ThingRepository reopened =
                SqliteThingRepository.fileBacked(
                        EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            reopened.initializeFrom(schema);

            QueryFilterParams params = new QueryFilterParams();
            params.put("title", "First");

            List<EntityInstance> filtered = reopened.listInstances(projectDefinition, params);

            Assertions.assertEquals(1, filtered.size());
            Assertions.assertEquals("First", filtered.get(0).getFieldValue("title").asString());
            Assertions.assertEquals(2, reopened.countInstances(projectDefinition));
        }
    }

    @Test
    public void sqliteCountInstancesWorksAfterReopen() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        Path databasePath = tempDir.resolve("count-lazy.sqlite");

        try (ThingRepository repository =
                SqliteThingRepository.fileBacked(
                        EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            repository.initializeFrom(schema);

            create(repository, projectDefinition, "First");
            create(repository, projectDefinition, "Second");
        }

        try (ThingRepository reopened =
                SqliteThingRepository.fileBacked(
                        EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            reopened.initializeFrom(schema);

            Assertions.assertEquals(2, reopened.countInstances(projectDefinition));
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
        }
    }

    @Test
    public void sqliteJsonExportWorksAfterReopen() {
        ERSchema schema = todoSchema();
        Path databasePath = tempDir.resolve("export-lazy.sqlite");

        try (ThingRepository repository =
                SqliteThingRepository.fileBacked(
                        EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            repository.initializeFrom(schema);
            addProjectAndTask(repository, schema);
        }

        try (ThingRepository reopened =
                SqliteThingRepository.fileBacked(
                        EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            reopened.initializeFrom(schema);

            String json = reopened.exportDataAsJson(schema);

            assertExportedJsonContainsProjectAndTask(json);
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
        }
    }

    @Test
    public void sqliteRelationshipReadsWorkAfterReopen() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        EntityDefinition taskDefinition = schema.getEntityDefinitionNamed("task");
        Path databasePath = tempDir.resolve("relationship-lazy.sqlite");

        try (ThingRepository repository =
                SqliteThingRepository.fileBacked(
                        EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            repository.initializeFrom(schema);

            EntityInstance project = create(repository, projectDefinition, "SQLite project");
            EntityInstance firstTask = create(repository, taskDefinition, "Wire repository");
            EntityInstance secondTask = create(repository, taskDefinition, "Write tests");

            repository.connectRelationship(project, "tasks", firstTask);
            repository.connectRelationship(project, "tasks", secondTask);
        }

        try (ThingRepository reopened =
                SqliteThingRepository.fileBacked(
                        EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            reopened.initializeFrom(schema);

            EntityInstance project = reopened.findInstanceByPrimaryKey(projectDefinition, "1");

            QueryFilterParams params = new QueryFilterParams();
            params.put("title", "*=Wire*");

            List<EntityInstance> filteredTasks =
                    reopened.listRelatedInstances(project, "tasks", params);

            Assertions.assertEquals(1, filteredTasks.size());
            Assertions.assertEquals(
                    "Wire repository", filteredTasks.get(0).getFieldValue("title").asString());

            QueryFilterParams regexParams = new QueryFilterParams();
            regexParams.put("title", "~=Wire [Rr]epository");

            List<EntityInstance> regexFilteredTasks =
                    reopened.listRelatedInstances(project, "tasks", regexParams);

            Assertions.assertEquals(1, regexFilteredTasks.size());
            Assertions.assertEquals(
                    "Wire repository", regexFilteredTasks.get(0).getFieldValue("title").asString());

            QueryFilterParams sortedParams = new QueryFilterParams();
            sortedParams.put("sortBy", "-id");

            List<EntityInstance> sortedTasks =
                    reopened.listRelatedInstances(project, "tasks", sortedParams);

            Assertions.assertEquals(2, sortedTasks.size());
            Assertions.assertEquals("2", sortedTasks.get(0).getPrimaryKeyValue());
            Assertions.assertEquals("1", sortedTasks.get(1).getPrimaryKeyValue());
            Assertions.assertEquals(2, reopened.listRelatedInstances(project, "tasks").size());

            RepositoryUrlQuery query =
                    new RepositoryUrlQuery(schema, reopened, "project/1/tasks")
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

        try (ThingRepository repository =
                SqliteThingRepository.fileBacked(
                        EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            repository.initializeFrom(schema);

            create(repository, projectDefinition, "Repository project");
            create(repository, projectDefinition, "repository project");
            create(repository, projectDefinition, "Another project");
        }

        try (ThingRepository reopened =
                SqliteThingRepository.fileBacked(
                        EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            reopened.initializeFrom(schema);

            QueryFilterParams regexParams = new QueryFilterParams();
            regexParams.put("title", "~=Repository.*");

            List<EntityInstance> filtered = reopened.listInstances(projectDefinition, regexParams);

            Assertions.assertEquals(1, filtered.size());
            Assertions.assertEquals(
                    "Repository project", filtered.get(0).getFieldValue("title").asString());
            Assertions.assertEquals(3, reopened.countInstances(projectDefinition));
        }
    }

    @Test
    public void sqliteRegexpFilterWorksAfterReopen() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        Path databasePath = tempDir.resolve("regex-regexp-lazy.sqlite");

        try (ThingRepository repository =
                SqliteThingRepository.fileBacked(
                        EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            repository.initializeFrom(schema);

            create(repository, projectDefinition, "Repository project");
            create(repository, projectDefinition, "Another project");
        }

        try (ThingRepository reopened =
                SqliteThingRepository.fileBacked(
                        EntityRelModel.DEFAULT_DATABASE_NAME, databasePath)) {
            reopened.initializeFrom(schema);

            QueryFilterParams regexParams = new QueryFilterParams();
            regexParams.put("title", "~=Repository [Pp]roject");

            List<EntityInstance> filtered = reopened.listInstances(projectDefinition, regexParams);

            Assertions.assertEquals(1, filtered.size());
            Assertions.assertEquals(
                    "Repository project", filtered.get(0).getFieldValue("title").asString());
            Assertions.assertEquals(2, reopened.countInstances(projectDefinition));
        }
    }

    @Test
    public void sqliteInMemoryRegexpFilterWorks() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");

        try (ThingRepository repository =
                SqliteThingRepository.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            repository.initializeFrom(schema);

            create(repository, projectDefinition, "Repository project");
            create(repository, projectDefinition, "Another project");

            QueryFilterParams regexParams = new QueryFilterParams();
            regexParams.put("title", "~=Repository [Pp]roject");

            List<EntityInstance> filtered =
                    repository.listInstances(projectDefinition, regexParams);

            Assertions.assertEquals(1, filtered.size());
            Assertions.assertEquals(
                    "Repository project", filtered.get(0).getFieldValue("title").asString());
        }
    }

    @Test
    public void sqliteInvalidRegexFilterFails() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");

        try (ThingRepository repository =
                SqliteThingRepository.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            repository.initializeFrom(schema);
            create(repository, projectDefinition, "Repository project");

            QueryFilterParams regexParams = new QueryFilterParams();
            regexParams.put("title", "~=[");

            Assertions.assertThrows(
                    RuntimeException.class,
                    () -> repository.listInstances(projectDefinition, regexParams));
        }
    }

    @Test
    public void sqliteTooComplexRegexFilterFails() {
        ERSchema schema = todoSchema();
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");

        try (ThingRepository repository =
                SqliteThingRepository.inMemory(EntityRelModel.DEFAULT_DATABASE_NAME)) {
            repository.initializeFrom(schema);
            create(repository, projectDefinition, "Repository project");

            QueryFilterParams regexParams = new QueryFilterParams();
            regexParams.put("title", "~=(.+)+");

            Assertions.assertThrows(
                    RuntimeException.class,
                    () -> repository.listInstances(projectDefinition, regexParams));
        }
    }

    private void exerciseRepositoryContract(final ThingRepository repository) {
        ERSchema schema = todoSchema();
        repository.initializeFrom(schema);

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
                        .findInstanceByPrimaryKey(projectDefinition, "1")
                        .getFieldValue("title")
                        .asString());
        Assertions.assertEquals(
                project, repository.findInstanceByPrimaryKey(projectDefinition, "1"));
        Assertions.assertEquals(
                project, repository.findInstanceByQueryIdentifier(projectDefinition, "1"));
        Assertions.assertEquals(
                project,
                repository.findInstanceByFieldNameAndValue(
                        projectDefinition, "title", "Repository project"));
        Assertions.assertEquals(1, repository.listInstances(projectDefinition).size());
        Assertions.assertEquals(1, repository.countInstances(projectDefinition));
        Assertions.assertEquals(0, repository.countInstances(taskDefinition));

        EntityInstance duplicate =
                MutableEntityInstance.snapshotFromDraft(
                        EntityInstanceDraft.forEntity(projectDefinition)
                                .withField("title", "Repository project"));
        Assertions.assertFalse(repository.checkFieldsForUniqueNess(duplicate, false).isValid());

        EntityInstance secondProject = create(repository, projectDefinition, "Another project");
        Assertions.assertEquals(2, repository.countInstances(projectDefinition));
        secondProject =
                repository.replaceInstance(
                        secondProject,
                        EntityInstanceDraft.forEntity(projectDefinition)
                                .withField("title", "Replacement project"));
        Assertions.assertEquals("2", secondProject.getPrimaryKeyValue());
        Assertions.assertEquals(
                "Replacement project",
                repository
                        .findInstanceByPrimaryKey(projectDefinition, "2")
                        .getFieldValue("title")
                        .asString());

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

        EntityInstance task = create(repository, taskDefinition, "Wire repository");
        Assertions.assertFalse(repository.hasRelationshipInstances(project));
        Assertions.assertFalse(repository.hasRelationshipInstances(task));
        repository.connectRelationship(project, "tasks", task);
        Assertions.assertTrue(repository.hasRelationshipInstances(project));
        Assertions.assertTrue(repository.hasRelationshipInstances(task));
        Assertions.assertTrue(repository.validateRelationships(project).isValid());
        Assertions.assertTrue(repository.validateRelationships(task).isValid());

        EntityInstance secondTask = create(repository, taskDefinition, "Write tests");
        repository.connectRelationship(project, "tasks", secondTask);
        EntityInstance overAssignedProject =
                create(repository, projectDefinition, "Second assignment target");
        Assertions.assertThrows(
                RuntimeException.class,
                () -> repository.connectRelationship(task, "task-of", overAssignedProject));

        Assertions.assertThrows(
                NoSuchMethodException.class,
                () -> EntityInstance.class.getMethod("getRelationships"));
        Assertions.assertThrows(
                NoSuchMethodException.class,
                () -> EntityInstance.class.getMethod("clearAllFields"));
        Assertions.assertTrue(repository.listRelatedInstances(task, "task-of").contains(project));
        Assertions.assertTrue(repository.listRelatedInstances(project, "tasks").contains(task));

        Assertions.assertTrue(repository.listRelatedInstances(task, "task-of").contains(project));
        Assertions.assertTrue(repository.listRelatedInstances(project, "tasks").contains(task));
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

        Assertions.assertTrue(repository.listRelatedInstances(task, "task-of").isEmpty());
        Assertions.assertFalse(repository.hasRelationshipInstances(task));
        Assertions.assertFalse(repository.listRelatedInstances(project, "tasks").contains(task));
        Assertions.assertTrue(
                repository.listRelatedInstances(project, "tasks").contains(secondTask));

        repository.removeRelationshipsInvolving(project, secondTask, "tasks");

        Assertions.assertTrue(repository.listRelatedInstances(project, "tasks").isEmpty());

        repository.deleteEntityInstance(task);
        repository.deleteEntityInstance(secondTask);

        Assertions.assertEquals(0, repository.listInstances(taskDefinition).size());
        Assertions.assertEquals(0, repository.countInstances(taskDefinition));

        repository.clearAllData();

        Assertions.assertEquals(0, repository.listInstances(projectDefinition).size());
        Assertions.assertEquals(0, repository.listInstances(taskDefinition).size());
        Assertions.assertEquals(0, repository.countInstances(projectDefinition));
        Assertions.assertEquals(0, repository.countInstances(taskDefinition));

        repository.resetAutoIncrementCounter(projectDefinition, "id");

        EntityInstance resetProject = create(repository, projectDefinition, "Reset project");

        Assertions.assertEquals("1", resetProject.getPrimaryKeyValue());
    }

    private void exerciseProviderIsolation(final ThingRepositoryProvider provider) {
        ERSchema schema = todoSchema();
        ThingRepository defaultRepository = provider.getDefaultRepository();
        defaultRepository.initializeFrom(schema);
        provider.createRepository("session-one", schema);

        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        create(defaultRepository, projectDefinition, "Only default");

        Assertions.assertEquals(1, defaultRepository.listInstances(projectDefinition).size());
        Assertions.assertEquals(
                0, provider.getRepository("session-one").listInstances(projectDefinition).size());
    }

    private void exerciseAutoIdGeneration(final ThingRepository repository) {
        ERSchema schema = autoIdSchema();
        repository.initializeFrom(schema);

        EntityDefinition session = schema.getEntityDefinitionNamed("session");
        EntityInstance firstSession =
                repository.createInstance(EntityInstanceDraft.forEntity(session));
        EntityInstance secondSession =
                repository.createInstance(EntityInstanceDraft.forEntity(session));

        Assertions.assertNotNull(firstSession.getPrimaryKeyValue());
        Assertions.assertNotNull(secondSession.getPrimaryKeyValue());
        Assertions.assertDoesNotThrow(() -> UUID.fromString(firstSession.getPrimaryKeyValue()));
        Assertions.assertDoesNotThrow(() -> UUID.fromString(secondSession.getPrimaryKeyValue()));
        Assertions.assertNotEquals(
                firstSession.getPrimaryKeyValue(), secondSession.getPrimaryKeyValue());

        String explicitGuid = "12345678-1234-1234-1234-123456789abc";
        EntityInstance explicitSession =
                repository.createInstance(
                        EntityInstanceDraft.forEntity(session)
                                .withProtectedField("guid", explicitGuid));
        Assertions.assertEquals(explicitGuid, explicitSession.getPrimaryKeyValue());

        EntityDefinition ticket = schema.getEntityDefinitionNamed("ticket");
        EntityInstance firstTicket =
                repository.createInstance(EntityInstanceDraft.forEntity(ticket));
        EntityInstance secondTicket =
                repository.createInstance(EntityInstanceDraft.forEntity(ticket));

        Assertions.assertEquals("1", firstTicket.getPrimaryKeyValue());
        Assertions.assertEquals("2", secondTicket.getPrimaryKeyValue());

        EntityInstance explicitTicket =
                repository.createInstance(
                        EntityInstanceDraft.forEntity(ticket).withProtectedField("id", "25"));
        EntityInstance ticketAfterExplicit =
                repository.createInstance(EntityInstanceDraft.forEntity(ticket));

        Assertions.assertEquals("25", explicitTicket.getPrimaryKeyValue());
        Assertions.assertEquals("26", ticketAfterExplicit.getPrimaryKeyValue());
    }

    private void exerciseMandatoryRelationshipValidationAndCascade(
            final ThingRepository repository) {
        ERSchema schema = mandatoryRelationshipSchema();
        repository.initializeFrom(schema);

        EntityDefinition parentDefinition = schema.getEntityDefinitionNamed("parent");
        EntityDefinition childDefinition = schema.getEntityDefinitionNamed("child");

        EntityInstance parent = create(repository, parentDefinition, "Parent");
        EntityInstance child = create(repository, childDefinition, "Child");

        Assertions.assertFalse(repository.validateRelationships(child).isValid());

        repository.connectRelationship(child, "parent", parent);

        Assertions.assertTrue(repository.validateRelationships(child).isValid());
        Assertions.assertTrue(repository.listRelatedInstances(parent, "children").contains(child));
        Assertions.assertTrue(repository.listRelatedInstances(child, "parent").contains(parent));

        repository.deleteEntityInstance(parent);

        Assertions.assertEquals(0, repository.countInstances(parentDefinition));
        Assertions.assertEquals(0, repository.countInstances(childDefinition));
    }

    private void addProjectAndTask(final ThingRepository repository, final ERSchema schema) {
        EntityDefinition projectDefinition = schema.getEntityDefinitionNamed("project");
        EntityDefinition taskDefinition = schema.getEntityDefinitionNamed("task");

        EntityInstance project = create(repository, projectDefinition, "Repository project");
        EntityInstance task = create(repository, taskDefinition, "Wire repository");
        repository.connectRelationship(project, "tasks", task);
    }

    private EntityInstance create(
            final ThingRepository repository, final EntityDefinition entity, final String title) {
        return repository.createInstance(
                EntityInstanceDraft.forEntity(entity).withField("title", title));
    }

    private ERSchema autoIdSchema() {
        ERSchema schema = new ERSchema();

        EntityDefinition session = schema.defineEntity("session", "sessions", -1);
        session.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));

        EntityDefinition ticket = schema.defineEntity("ticket", "tickets", -1);
        ticket.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));

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
