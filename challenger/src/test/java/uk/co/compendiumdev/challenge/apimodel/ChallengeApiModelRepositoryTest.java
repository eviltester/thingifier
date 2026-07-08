package uk.co.compendiumdev.challenge.apimodel;


import uk.co.compendiumdev.challenge.testsupport.ThingifierRepositoryTestSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.repository.SqliteThingRepository;
import uk.co.compendiumdev.thingifier.core.repository.SqliteThingRepositoryProvider;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

public class ChallengeApiModelRepositoryTest {

    @Test
    public void challengeApiCanUseSqliteInMemoryRepository() {
        try (SqliteThingRepositoryProvider provider = SqliteThingRepositoryProvider.inMemory()) {
            Thingifier thingifier = new ChallengeApiModel().get(provider);

            Assertions.assertTrue(
                    thingifier.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                            instanceof SqliteThingRepository);
            Assertions.assertEquals(
                    10,
                    ThingifierRepositoryTestSupport.collection(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME, "todo").
                            countInstances());
        }
    }

    @Test
    public void challengeApiGetUsesSqliteRepositoryReadsForSimpleTodoQueries() {
        try (SqliteThingRepositoryProvider provider = SqliteThingRepositoryProvider.inMemory()) {
            Thingifier thingifier = new ChallengeApiModel().get(provider);
            ThingRepository repository = thingifier.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
            EntityDefinition todo = thingifier.getDefinitionNamed("todo");

            EntityInstance firstTodo = repository.findInstanceByQueryIdentifier(todo, "1");
            firstTodo.setValue("doneStatus", "true");
            repository.updateInstance(firstTodo);

            ApiResponse allTodos = thingifier.api().get(
                    "/todos", new QueryFilterParams(), new HttpHeadersBlock());
            Assertions.assertEquals(200, allTodos.getStatusCode());
            Assertions.assertEquals(10, allTodos.getReturnedInstanceCollection().size());

            ApiResponse todoById = thingifier.api().get(
                    "/todos/1", new QueryFilterParams(), new HttpHeadersBlock());
            Assertions.assertEquals(200, todoById.getStatusCode());
            Assertions.assertEquals(1, todoById.getReturnedInstanceCollection().size());
            Assertions.assertEquals(
                    "1",
                    todoById.getReturnedInstanceCollection().get(0).getPrimaryKeyValue());

            QueryFilterParams doneStatusFilter = new QueryFilterParams();
            doneStatusFilter.put("doneStatus", "false");
            ApiResponse notDoneTodos = thingifier.api().get(
                    "/todos", doneStatusFilter, new HttpHeadersBlock());
            Assertions.assertEquals(200, notDoneTodos.getStatusCode());
            Assertions.assertEquals(9, notDoneTodos.getReturnedInstanceCollection().size());

            QueryFilterParams idRangeFilter = new QueryFilterParams();
            idRangeFilter.put("id", ">=2");
            ApiResponse idRangeTodos = thingifier.api().get(
                    "/todos", idRangeFilter, new HttpHeadersBlock());
            Assertions.assertEquals(200, idRangeTodos.getStatusCode());
            Assertions.assertEquals(9, idRangeTodos.getReturnedInstanceCollection().size());

            QueryFilterParams sortByDescendingId = new QueryFilterParams();
            sortByDescendingId.put("sortBy", "-id");
            ApiResponse sortedTodos = thingifier.api().get(
                    "/todos", sortByDescendingId, new HttpHeadersBlock());
            Assertions.assertEquals(200, sortedTodos.getStatusCode());
            Assertions.assertEquals(
                    "10",
                    sortedTodos.getReturnedInstanceCollection().get(0).getPrimaryKeyValue());
        }
    }

    @Test
    public void relationshipGetRoutesStillFallBackToLegacyQueryTraversal() {
        try (SqliteThingRepositoryProvider provider = SqliteThingRepositoryProvider.inMemory()) {
            Thingifier thingifier = new Thingifier(new EntityRelModel(provider));

            EntityDefinition project = thingifier.defineThing("project", "projects");
            project.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
            project.addField(Field.is("title", FieldType.STRING));

            EntityDefinition task = thingifier.defineThing("task", "tasks");
            task.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
            task.addField(Field.is("title", FieldType.STRING));

            thingifier.defineRelationship(project, task, "tasks", Cardinality.ONE_TO_MANY()).
                    whenReversed(Cardinality.ONE_TO_ONE(), "task-of");

            ThingRepository repository = thingifier.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
            EntityInstance projectInstance = new EntityInstance(project).setValue("title", "Office");
            EntityInstance taskInstance = new EntityInstance(task).setValue("title", "File paperwork");
            repository.addInstance(projectInstance);
            repository.addInstance(taskInstance);
            repository.connectRelationship(projectInstance, "tasks", taskInstance);

            ApiResponse response = thingifier.api().get(
                    "/project/1/tasks", new QueryFilterParams(), new HttpHeadersBlock());

            Assertions.assertEquals(200, response.getStatusCode());
            Assertions.assertEquals(1, response.getReturnedInstanceCollection().size());
            Assertions.assertEquals(
                    "File paperwork",
                    response.getReturnedInstanceCollection().get(0).getFieldValue("title").asString());
        }
    }

    @Test
    public void simpleEntityWritesUseSqliteRepositoryWithoutLoadingCompatibilitySnapshot() {
        try (SqliteThingRepositoryProvider provider = SqliteThingRepositoryProvider.inMemory()) {
            Thingifier thingifier = new Thingifier(new EntityRelModel(provider));

            EntityDefinition note = thingifier.defineThing("note", "notes");
            note.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
            note.addField(Field.is("title", FieldType.STRING));

            ThingRepository repository = thingifier.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);

            ApiResponse create = thingifier.api().post(
                    "/notes",
                    bodyParser(thingifier, "{\"title\":\"created\"}"),
                    new HttpHeadersBlock());

            Assertions.assertEquals(201, create.getStatusCode());
            Assertions.assertEquals(1, repository.listInstances(note).size());

            ApiResponse postAmend = thingifier.api().post(
                    "/notes/1",
                    bodyParser(thingifier, "{\"title\":\"posted\"}"),
                    new HttpHeadersBlock());

            Assertions.assertEquals(200, postAmend.getStatusCode());
            Assertions.assertEquals(
                    "posted",
                    repository.findInstanceByQueryIdentifier(note, "1").
                            getFieldValue("title").asString());

            ApiResponse putAmend = thingifier.api().put(
                    "/notes/1",
                    bodyParser(thingifier, "{\"title\":\"put\"}"),
                    new HttpHeadersBlock());

            Assertions.assertEquals(200, putAmend.getStatusCode());
            Assertions.assertEquals(
                    "put",
                    repository.findInstanceByQueryIdentifier(note, "1").
                            getFieldValue("title").asString());

            ApiResponse delete = thingifier.api().delete(
                    "/notes/1", new HttpHeadersBlock());

            Assertions.assertEquals(200, delete.getStatusCode());
            Assertions.assertEquals(0, repository.listInstances(note).size());
        }
    }

    @Test
    public void sqliteMemoryProvidersHaveSeparateNamedDatabaseLifetimes() {
        EntityRelModel firstModel = null;
        EntityRelModel secondModel = null;
        try (SqliteThingRepositoryProvider firstProvider = SqliteThingRepositoryProvider.inMemory();
             SqliteThingRepositoryProvider secondProvider = SqliteThingRepositoryProvider.inMemory()) {
            firstModel = new EntityRelModel(firstProvider);
            secondModel = new EntityRelModel(secondProvider);

            EntityDefinition firstNote = firstModel.createEntityDefinition("note", "notes");
            firstNote.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
            firstNote.addField(Field.is("title", FieldType.STRING));

            EntityDefinition secondNote = secondModel.createEntityDefinition("note", "notes");
            secondNote.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
            secondNote.addField(Field.is("title", FieldType.STRING));

            firstModel.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).
                    addInstance(new EntityInstance(firstNote).setValue("title", "first"));

            Assertions.assertEquals(
                    1,
                    firstModel.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).
                            listInstances(firstNote).size());
            Assertions.assertEquals(
                    0,
                    secondModel.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).
                            listInstances(secondNote).size());
        } finally {
            if (firstModel != null) {
                firstModel.close();
            }
            if (secondModel != null) {
                secondModel.close();
            }
        }
    }

    private BodyParser bodyParser(final Thingifier thingifier, final String json) {
        return new BodyParser(new HttpApiRequest("/path").setBody(json), thingifier.getThingNames());
    }
}
