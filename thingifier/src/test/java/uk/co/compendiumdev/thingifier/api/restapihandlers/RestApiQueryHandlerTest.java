package uk.co.compendiumdev.thingifier.api.restapihandlers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public class RestApiQueryHandlerTest {

    @Test
    public void queryEntityCollectionUsesFormBodyFilters() {
        Thingifier thingifier = taskProjectThingifier();
        EntityInstance matching = createTask(thingifier, "Keep", "Open");
        createTask(thingifier, "Skip", "Open");

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier).queryRequest(query("tasks", "title=Keep"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.apiResponse().isCollection());
        Assertions.assertEquals(1, response.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertEquals(
                matching, response.apiResponse().getReturnedInstanceCollection().get(0));
        Assertions.assertEquals(
                ThingifierHttpApi.QUERY_CONTENT_TYPE,
                response.getHeaders().get(ThingifierHttpApi.ACCEPT_QUERY_HEADER));
    }

    @Test
    public void queryEntityCollectionCombinesUriParamsAndBodyParams() {
        Thingifier thingifier = taskProjectThingifier();
        EntityInstance matching = createTask(thingifier, "Keep", "Open");
        createTask(thingifier, "Keep", "Closed");
        createTask(thingifier, "Skip", "Open");

        HttpApiRequest request =
                query("tasks", "title=Keep").setFilterableQueryParams("status=Open");
        HttpApiResponse response = new ThingifierHttpApi(thingifier).queryRequest(request);

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(1, response.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertEquals(
                matching, response.apiResponse().getReturnedInstanceCollection().get(0));
    }

    @Test
    public void queryRelationshipCollectionFiltersRelatedInstancesOnly() {
        Thingifier thingifier = taskProjectThingifier();
        ThingStore store = storeFor(thingifier);
        EntityInstance project = createProject(thingifier, "Project");
        EntityInstance matching = createTask(thingifier, "Keep", "Open");
        EntityInstance relatedButFilteredOut = createTask(thingifier, "Skip", "Open");
        createTask(thingifier, "Keep", "Open");
        store.relationships().connect(project, "tasks", matching);
        store.relationships().connect(project, "tasks", relatedButFilteredOut);

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(
                                query(
                                        "projects/" + project.getPrimaryKeyValue() + "/tasks",
                                        "title=Keep"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(1, response.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertEquals(
                matching, response.apiResponse().getReturnedInstanceCollection().get(0));
    }

    @Test
    public void querySingleTargetRelationshipStillReturnsACollection() {
        Thingifier thingifier = taskProjectThingifier();
        ThingStore store = storeFor(thingifier);
        EntityInstance project = createProject(thingifier, "Project");
        EntityInstance task = createTask(thingifier, "Keep", "Open");
        store.relationships().connect(project, "tasks", task);

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(
                                query(
                                        "tasks/" + task.getPrimaryKeyValue() + "/task-of",
                                        "title=Project"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.apiResponse().isCollection());
        Assertions.assertEquals(1, response.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertEquals(
                project, response.apiResponse().getReturnedInstanceCollection().get(0));
    }

    @Test
    public void querySingleInstanceIsMethodNotAllowed() {
        Thingifier thingifier = taskProjectThingifier();
        EntityInstance task = createTask(thingifier, "Task", "Open");

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(query("tasks/" + task.getPrimaryKeyValue(), "title=Task"));

        Assertions.assertEquals(405, response.getStatusCode());
        Assertions.assertEquals(
                "OPTIONS, GET, HEAD, POST, PUT, DELETE", response.getHeaders().get("Allow"));
    }

    @Test
    public void queryRelationshipLinkIsMethodNotAllowed() {
        Thingifier thingifier = taskProjectThingifier();
        EntityInstance project = createProject(thingifier, "Project");
        EntityInstance task = createTask(thingifier, "Task", "Open");

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(
                                query(
                                        "projects/"
                                                + project.getPrimaryKeyValue()
                                                + "/tasks/"
                                                + task.getPrimaryKeyValue(),
                                        "title=Task"));

        Assertions.assertEquals(405, response.getStatusCode());
        Assertions.assertEquals("OPTIONS, DELETE", response.getHeaders().get("Allow"));
    }

    @Test
    public void missingRelationshipParentKeepsExistingNotFoundBehaviour() {
        Thingifier thingifier = taskProjectThingifier();

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(query("projects/missing/tasks", "title=Task"));

        Assertions.assertEquals(404, response.getStatusCode());
    }

    @Test
    public void missingContentTypeIsBadRequest() {
        Thingifier thingifier = taskProjectThingifier();
        HttpApiRequest request = new HttpApiRequest("tasks").setBody("title=Task");

        HttpApiResponse response = new ThingifierHttpApi(thingifier).queryRequest(request);

        Assertions.assertEquals(400, response.getStatusCode());
        Assertions.assertEquals(
                ThingifierHttpApi.QUERY_CONTENT_TYPE,
                response.getHeaders().get(ThingifierHttpApi.ACCEPT_QUERY_HEADER));
    }

    @Test
    public void unsupportedQueryContentTypeReturnsUnsupportedMediaType() {
        Thingifier thingifier = taskProjectThingifier();
        HttpApiRequest request =
                new HttpApiRequest("tasks")
                        .addHeader("Content-Type", "application/json")
                        .setBody("{\"title\":\"Task\"}");

        HttpApiResponse response = new ThingifierHttpApi(thingifier).queryRequest(request);

        Assertions.assertEquals(415, response.getStatusCode());
        Assertions.assertEquals(
                ThingifierHttpApi.QUERY_CONTENT_TYPE, response.getHeaders().get("Accept"));
        Assertions.assertEquals(
                ThingifierHttpApi.QUERY_CONTENT_TYPE,
                response.getHeaders().get(ThingifierHttpApi.ACCEPT_QUERY_HEADER));
    }

    @Test
    public void malformedQueryContentIsBadRequest() {
        Thingifier thingifier = taskProjectThingifier();

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier).queryRequest(query("tasks", "title=%"));

        Assertions.assertEquals(400, response.getStatusCode());
    }

    @Test
    public void queryBodyHonoursMaxRequestBodyLength() {
        Thingifier thingifier = taskProjectThingifier();
        thingifier.apiConfig().statusCodes().setMaxRequestBodyLengthBytes(3);

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier).queryRequest(query("tasks", "title=Task"));

        Assertions.assertEquals(413, response.getStatusCode());
    }

    @Test
    public void emptyQueryBodyIsValidAndReturnsUnfilteredCollection() {
        Thingifier thingifier = taskProjectThingifier();
        createTask(thingifier, "One", "Open");
        createTask(thingifier, "Two", "Closed");

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier).queryRequest(query("tasks", ""));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(2, response.apiResponse().getReturnedInstanceCollection().size());
    }

    @Test
    public void queryEntityCollectionAppliesSortingThenPaging() {
        Thingifier thingifier = taskProjectThingifier();
        EntityInstance expected = createTask(thingifier, "Bravo", "Open");
        createTask(thingifier, "Charlie", "Open");
        createTask(thingifier, "Alpha", "Open");
        createTask(thingifier, "Before but closed", "Closed");

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(
                                query("tasks", "status=Open&_sortBy=+title&_limit=1&_offset=1"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(1, response.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertEquals(
                expected, response.apiResponse().getReturnedInstanceCollection().get(0));
    }

    @Test
    public void getCollectionAdvertisesQuerySupport() {
        Thingifier thingifier = taskProjectThingifier();

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(new HttpApiRequest("tasks"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                ThingifierHttpApi.QUERY_CONTENT_TYPE,
                response.getHeaders().get(ThingifierHttpApi.ACCEPT_QUERY_HEADER));
    }

    private Thingifier taskProjectThingifier() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition task = thingifier.defineThing("task", "tasks");
        task.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        task.addField(Field.is("title", FieldType.STRING));
        task.addField(Field.is("status", FieldType.STRING));

        EntityDefinition project = thingifier.defineThing("project", "projects");
        project.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        project.addField(Field.is("title", FieldType.STRING));

        thingifier
                .defineRelationship(project, task, "tasks", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_ONE(), "task-of");
        return thingifier;
    }

    private EntityInstance createTask(
            final Thingifier thingifier, final String title, final String status) {
        EntityDefinition task = thingifier.getDefinitionNamed("task");
        return storeFor(thingifier)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(task)
                                .withField("title", title)
                                .withField("status", status));
    }

    private EntityInstance createProject(final Thingifier thingifier, final String title) {
        EntityDefinition project = thingifier.getDefinitionNamed("project");
        return storeFor(thingifier)
                .entities()
                .create(EntityInstanceDraft.forEntity(project).withField("title", title));
    }

    private ThingStore storeFor(final Thingifier thingifier) {
        return thingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
    }

    private HttpApiRequest query(final String path, final String body) {
        return new HttpApiRequest(path)
                .addHeader("Content-Type", ThingifierHttpApi.QUERY_CONTENT_TYPE)
                .setBody(body);
    }
}
