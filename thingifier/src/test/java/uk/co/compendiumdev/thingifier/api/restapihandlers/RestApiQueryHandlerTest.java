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
                ThingifierHttpApi.SUPPORTED_QUERY_CONTENT_TYPES,
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
                ThingifierHttpApi.SUPPORTED_QUERY_CONTENT_TYPES,
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
                ThingifierHttpApi.SUPPORTED_QUERY_CONTENT_TYPES,
                response.getHeaders().get("Accept"));
        Assertions.assertEquals(
                ThingifierHttpApi.SUPPORTED_QUERY_CONTENT_TYPES,
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
    public void structuredJsonQueryEntityCollectionFiltersByDoneStatus() {
        Thingifier thingifier = todoThingifier();
        EntityInstance matching = createTodo(thingifier, "Keep", "false", "Open item");
        createTodo(thingifier, "Skip", "true", "Closed item");

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(
                                structuredJsonQuery(
                                        "todos", "{\"filter\":{\"doneStatus\":false}}"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.apiResponse().isCollection());
        Assertions.assertEquals(1, response.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertEquals(
                matching, response.apiResponse().getReturnedInstanceCollection().get(0));
        Assertions.assertEquals(
                ThingifierHttpApi.SUPPORTED_QUERY_CONTENT_TYPES,
                response.getHeaders().get(ThingifierHttpApi.ACCEPT_QUERY_HEADER));
    }

    @Test
    public void structuredJsonQueryContainsUsesLiteralText() {
        Thingifier thingifier = todoThingifier();
        EntityInstance matching = createTodo(thingifier, "Scan * literally", "false", "Open item");
        createTodo(thingifier, "Scan documents", "false", "Open item");

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(
                                structuredJsonQuery(
                                        "todos", "{\"filter\":{\"title\":{\"contains\":\"*\"}}}"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(1, response.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertEquals(
                matching, response.apiResponse().getReturnedInstanceCollection().get(0));
    }

    @Test
    public void structuredJsonQueryExactStringKeepsOperatorPrefixedTextLiteral() {
        Thingifier thingifier = todoThingifier();
        EntityInstance matching = createTodo(thingifier, ">blocked", "false", "Open item");
        createTodo(thingifier, "zebra", "false", "Open item");

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(
                                structuredJsonQuery(
                                        "todos", "{\"filter\":{\"title\":\">blocked\"}}"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(1, response.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertEquals(
                matching, response.apiResponse().getReturnedInstanceCollection().get(0));
    }

    @Test
    public void structuredJsonQueryFiltersByGuidExactly() {
        Thingifier thingifier = taskProjectThingifier();
        EntityInstance matching = createProject(thingifier, "Project");
        createProject(thingifier, "Other");

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(
                                structuredJsonQuery(
                                        "projects",
                                        "{\"filter\":{\"guid\":\""
                                                + matching.getPrimaryKeyValue()
                                                + "\"}}"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(1, response.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertEquals(
                matching, response.apiResponse().getReturnedInstanceCollection().get(0));
    }

    @Test
    public void structuredJsonQueryFiltersByNumericComparisons() {
        Thingifier thingifier = todoThingifier();
        createTodo(thingifier, "First", "false", "One");
        EntityInstance expected = createTodo(thingifier, "Second", "false", "Two");
        createTodo(thingifier, "Third", "false", "Three");

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(
                                structuredJsonQuery(
                                        "todos",
                                        "{\"filter\":{\"id\":{\"greaterThan\":1,"
                                                + "\"lessThan\":3}}}"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(1, response.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertEquals(
                expected, response.apiResponse().getReturnedInstanceCollection().get(0));
    }

    @Test
    public void structuredJsonQueryCanSortAndPaginateResults() {
        Thingifier thingifier = todoThingifier();
        createTodo(thingifier, "First", "false", "One");
        EntityInstance expected = createTodo(thingifier, "Second", "false", "Two");
        createTodo(thingifier, "Third", "false", "Three");

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(
                                structuredJsonQuery(
                                        "todos",
                                        "{\"sort\":[{\"field\":\"id\",\"direction\":\"desc\"}],"
                                                + "\"limit\":1,\"offset\":1}"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(1, response.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertEquals(
                expected, response.apiResponse().getReturnedInstanceCollection().get(0));
    }

    @Test
    public void structuredJsonQueryFiltersRelationshipCollections() {
        Thingifier thingifier = taskProjectThingifier();
        ThingStore store = storeFor(thingifier);
        EntityInstance project = createProject(thingifier, "Project");
        EntityInstance matching = createTask(thingifier, "Keep this", "Open");
        EntityInstance relatedButFilteredOut = createTask(thingifier, "Skip this", "Open");
        createTask(thingifier, "Keep unrelated", "Open");
        store.relationships().connect(project, "tasks", matching);
        store.relationships().connect(project, "tasks", relatedButFilteredOut);

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(
                                structuredJsonQuery(
                                        "projects/" + project.getPrimaryKeyValue() + "/tasks",
                                        "{\"filter\":{\"title\":{\"contains\":\"Keep\"}}}"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(1, response.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertEquals(
                matching, response.apiResponse().getReturnedInstanceCollection().get(0));
    }

    @Test
    public void structuredJsonQueryWorksForNonTodoCollections() {
        Thingifier thingifier = taskProjectThingifier();
        EntityInstance matching = createProject(thingifier, "Migration");
        createProject(thingifier, "Support");

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(
                                structuredJsonQuery(
                                        "projects", "{\"filter\":{\"title\":\"Migration\"}}"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(1, response.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertEquals(
                matching, response.apiResponse().getReturnedInstanceCollection().get(0));
    }

    @Test
    public void structuredJsonQueryAcceptsVendorJsonMediaTypeWithParameters() {
        Thingifier thingifier = todoThingifier();
        EntityInstance matching = createTodo(thingifier, "Keep", "false", "Open item");

        HttpApiRequest request =
                new HttpApiRequest("todos")
                        .addHeader(
                                "Content-Type",
                                ThingifierHttpApi.STRUCTURED_QUERY_CONTENT_TYPE + "; charset=utf-8")
                        .setBody("{\"filter\":{\"title\":\"Keep\"}}");
        HttpApiResponse response = new ThingifierHttpApi(thingifier).queryRequest(request);

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(1, response.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertEquals(
                matching, response.apiResponse().getReturnedInstanceCollection().get(0));
    }

    @Test
    public void structuredJsonQueryRejectsMalformedJson() {
        Thingifier thingifier = todoThingifier();

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(structuredJsonQuery("todos", "{\"filter\":"));

        Assertions.assertEquals(400, response.getStatusCode());
    }

    @Test
    public void structuredJsonQueryRejectsSingleQuotedJson() {
        Thingifier thingifier = todoThingifier();

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(structuredJsonQuery("todos", "{'filter':{'title':'Task'}}"));

        Assertions.assertEquals(400, response.getStatusCode());
    }

    @Test
    public void structuredJsonQueryRejectsUnknownFields() {
        Thingifier thingifier = todoThingifier();

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(
                                structuredJsonQuery(
                                        "todos", "{\"filter\":{\"unknown\":\"value\"}}"));

        Assertions.assertEquals(422, response.getStatusCode());
    }

    @Test
    public void structuredJsonQueryRejectsUnsupportedOperators() {
        Thingifier thingifier = todoThingifier();

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(
                                structuredJsonQuery(
                                        "todos",
                                        "{\"filter\":{\"title\":{\"startsWith\":\"Task\"}}}"));

        Assertions.assertEquals(422, response.getStatusCode());
    }

    @Test
    public void structuredJsonQueryRejectsInvalidPaginationValues() {
        Thingifier thingifier = todoThingifier();

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(structuredJsonQuery("todos", "{\"limit\":-1}"));

        Assertions.assertEquals(422, response.getStatusCode());
    }

    @Test
    public void structuredJsonQueryRejectsInvalidSortFields() {
        Thingifier thingifier = todoThingifier();

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(
                                structuredJsonQuery(
                                        "todos",
                                        "{\"sort\":[{\"field\":\"missing\","
                                                + "\"direction\":\"asc\"}]}"));

        Assertions.assertEquals(422, response.getStatusCode());
    }

    @Test
    public void structuredJsonQueryStillRejectsApplicationJsonContentType() {
        Thingifier thingifier = todoThingifier();
        HttpApiRequest request =
                new HttpApiRequest("todos")
                        .addHeader("Content-Type", "application/json")
                        .setBody("{\"filter\":{\"title\":\"Task\"}}");

        HttpApiResponse response = new ThingifierHttpApi(thingifier).queryRequest(request);

        Assertions.assertEquals(415, response.getStatusCode());
    }

    @Test
    public void structuredJsonQueryKeepsUnsupportedAcceptAsNotAcceptable() {
        Thingifier thingifier = todoThingifier();
        HttpApiRequest request =
                structuredJsonQuery("todos", "{\"filter\":{\"doneStatus\":false}}")
                        .addHeader("Accept", "text/*");

        HttpApiResponse response = new ThingifierHttpApi(thingifier).queryRequest(request);

        Assertions.assertEquals(406, response.getStatusCode());
    }

    @Test
    public void getCollectionAdvertisesQuerySupport() {
        Thingifier thingifier = taskProjectThingifier();

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(new HttpApiRequest("tasks"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(
                ThingifierHttpApi.SUPPORTED_QUERY_CONTENT_TYPES,
                response.getHeaders().get(ThingifierHttpApi.ACCEPT_QUERY_HEADER));
    }

    @Test
    public void jsonPathQueryEntityCollectionFiltersByDoneStatus() {
        Thingifier thingifier = todoThingifier();
        EntityInstance matching = createTodo(thingifier, "Keep", "false", "Open item");
        createTodo(thingifier, "Skip", "true", "Closed item");

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(jsonPathQuery("todos", "$.todos[?(@.doneStatus == false)]"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.apiResponse().isCollection());
        Assertions.assertEquals(1, response.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertEquals(
                matching, response.apiResponse().getReturnedInstanceCollection().get(0));
        Assertions.assertEquals(
                ThingifierHttpApi.SUPPORTED_QUERY_CONTENT_TYPES,
                response.getHeaders().get(ThingifierHttpApi.ACCEPT_QUERY_HEADER));
    }

    @Test
    public void jsonPathQueryEntityCollectionFiltersById() {
        Thingifier thingifier = todoThingifier();
        createTodo(thingifier, "First", "false", "One");
        EntityInstance matching = createTodo(thingifier, "Second", "true", "Two");

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(jsonPathQuery("todos", "$.todos[?(@.id == 2)]"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(1, response.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertEquals(
                matching, response.apiResponse().getReturnedInstanceCollection().get(0));
    }

    @Test
    public void jsonPathQueryEntityCollectionFiltersByTitleAndDescription() {
        Thingifier thingifier = todoThingifier();
        EntityInstance matching = createTodo(thingifier, "Keep", "false", "Detailed");
        createTodo(thingifier, "Keep", "false", "Other");
        createTodo(thingifier, "Skip", "false", "Detailed");

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(
                                jsonPathQuery(
                                        "todos",
                                        "$.todos[?(@.title == 'Keep' && @.description == 'Detailed')]"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(1, response.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertEquals(
                matching, response.apiResponse().getReturnedInstanceCollection().get(0));
    }

    @Test
    public void jsonPathQueryUsesResponseEntityViewBeforeFiltering() {
        Thingifier thingifier = todoThingifier();
        EntityDefinition todo = thingifier.getDefinitionNamed("todo");
        todo.addField(Field.is("secret", FieldType.STRING));
        todo.defineView("PublicTodo").hideResponseFields("id", "secret");
        thingifier.apiSpec().route("QUERY", "/todos").entityView("PublicTodo");
        EntityInstance matching =
                storeFor(thingifier)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(todo)
                                        .withField("title", "Visible")
                                        .withField("doneStatus", "false")
                                        .withField("description", "Open item")
                                        .withField("secret", "alpha"));
        storeFor(thingifier)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(todo)
                                .withField("title", "Hidden")
                                .withField("doneStatus", "false")
                                .withField("description", "Other item")
                                .withField("secret", "beta"));

        HttpApiResponse hiddenFieldResponse =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(jsonPathQuery("todos", "$.todos[?(@.secret == 'alpha')]"));

        Assertions.assertEquals(200, hiddenFieldResponse.getStatusCode());
        Assertions.assertEquals(
                0, hiddenFieldResponse.apiResponse().getReturnedInstanceCollection().size());

        HttpApiResponse visibleFieldResponse =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(jsonPathQuery("todos", "$.todos[?(@.title == 'Visible')]"));

        Assertions.assertEquals(200, visibleFieldResponse.getStatusCode());
        Assertions.assertEquals(
                1, visibleFieldResponse.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertEquals(
                matching,
                visibleFieldResponse.apiResponse().getReturnedInstanceCollection().get(0));
        Assertions.assertFalse(visibleFieldResponse.getBody().contains("secret"));
        Assertions.assertFalse(visibleFieldResponse.getBody().contains("\"id\""));
    }

    @Test
    public void jsonPathQueryCanSelectEqualProjectedResourcesByIdentity() {
        Thingifier thingifier = todoThingifier();
        EntityDefinition todo = thingifier.getDefinitionNamed("todo");
        todo.defineView("PublicTodo").hideResponseFields("id");
        thingifier.apiSpec().route("QUERY", "/todos").entityView("PublicTodo");
        EntityInstance first = createTodo(thingifier, "Same", "false", "Same description");
        EntityInstance second = createTodo(thingifier, "Same", "false", "Same description");

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier).queryRequest(jsonPathQuery("todos", "$.todos"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(2, response.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertTrue(
                response.apiResponse().getReturnedInstanceCollection().contains(first));
        Assertions.assertTrue(
                response.apiResponse().getReturnedInstanceCollection().contains(second));
    }

    @Test
    public void jsonPathQueryRejectsNestedObjectEqualToAProjectedResource() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition item = thingifier.defineThing("item", "items");
        item.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        item.addField(Field.is("tag", FieldType.STRING));
        item.addField(
                Field.is("details", FieldType.OBJECT).withField(Field.is("tag", FieldType.STRING)));
        item.defineView("PublicItem").hideResponseFields("id");
        thingifier.apiSpec().route("QUERY", "/items").entityView("PublicItem");
        storeFor(thingifier)
                .entities()
                .create(EntityInstanceDraft.forEntity(item).withField("tag", "same"));
        storeFor(thingifier)
                .entities()
                .create(EntityInstanceDraft.forEntity(item).withField("details.tag", "same"));

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(jsonPathQuery("items", "$.items[1].details"));

        Assertions.assertEquals(422, response.getStatusCode());
    }

    @Test
    public void jsonPathQueryCanSelectCollectionArray() {
        Thingifier thingifier = todoThingifier();
        EntityInstance first = createTodo(thingifier, "First", "false", "One");
        EntityInstance second = createTodo(thingifier, "Second", "true", "Two");

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier).queryRequest(jsonPathQuery("todos", "$.todos"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(2, response.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertTrue(
                response.apiResponse().getReturnedInstanceCollection().contains(first));
        Assertions.assertTrue(
                response.apiResponse().getReturnedInstanceCollection().contains(second));
    }

    @Test
    public void jsonPathQueryWithNoMatchesReturnsEmptyCollection() {
        Thingifier thingifier = todoThingifier();
        createTodo(thingifier, "One", "false", "Open item");

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(jsonPathQuery("todos", "$.todos[?(@.title == 'Missing')]"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.apiResponse().isCollection());
        Assertions.assertEquals(0, response.apiResponse().getReturnedInstanceCollection().size());
    }

    @Test
    public void jsonPathQueryCanSelectEmptyKeylessEntityCollection() {
        Thingifier thingifier = keylessItemThingifier();

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier).queryRequest(jsonPathQuery("items", "$.items"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.apiResponse().isCollection());
        Assertions.assertEquals(0, response.apiResponse().getReturnedInstanceCollection().size());
    }

    @Test
    public void jsonPathQueryCanFilterKeylessEntityCollection() {
        Thingifier thingifier = keylessItemThingifier();
        EntityInstance matching = createItem(thingifier, "Keep", "visible");
        createItem(thingifier, "Skip", "visible");

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(jsonPathQuery("items", "$.items[?(@.name == 'Keep')]"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.apiResponse().isCollection());
        Assertions.assertEquals(1, response.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertEquals(
                matching, response.apiResponse().getReturnedInstanceCollection().get(0));
    }

    @Test
    public void jsonPathQueryRelationshipCollectionFiltersRelatedInstancesOnly() {
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
                                jsonPathQuery(
                                        "projects/" + project.getPrimaryKeyValue() + "/tasks",
                                        "$.tasks[?(@.title == 'Keep')]"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(1, response.apiResponse().getReturnedInstanceCollection().size());
        Assertions.assertEquals(
                matching, response.apiResponse().getReturnedInstanceCollection().get(0));
    }

    @Test
    public void malformedJsonPathQueryContentIsBadRequest() {
        Thingifier thingifier = todoThingifier();

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier).queryRequest(jsonPathQuery("todos", "$[?"));

        Assertions.assertEquals(400, response.getStatusCode());
    }

    @Test
    public void emptyJsonPathQueryContentIsBadRequest() {
        Thingifier thingifier = todoThingifier();

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier).queryRequest(jsonPathQuery("todos", ""));

        Assertions.assertEquals(400, response.getStatusCode());
    }

    @Test
    public void jsonPathQueryProjectionIsUnprocessable() {
        Thingifier thingifier = todoThingifier();
        createTodo(thingifier, "Task", "false", "Open item");

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(jsonPathQuery("todos", "$.todos[*].title"));

        Assertions.assertEquals(422, response.getStatusCode());
    }

    @Test
    public void jsonPathQueryPartialObjectProjectionIsUnprocessable() {
        Thingifier thingifier = todoThingifier();
        createTodo(thingifier, "Task", "false", "Open item");

        HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(jsonPathQuery("todos", "$.todos[*]['id','title']"));

        Assertions.assertEquals(422, response.getStatusCode());
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

    private Thingifier todoThingifier() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition todo = thingifier.defineThing("todo", "todos");
        todo.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        todo.addField(Field.is("title", FieldType.STRING));
        todo.addField(Field.is("doneStatus", FieldType.BOOLEAN));
        todo.addField(Field.is("description", FieldType.STRING));
        return thingifier;
    }

    private Thingifier keylessItemThingifier() {
        Thingifier thingifier = new Thingifier();
        EntityDefinition item = thingifier.defineThing("item", "items");
        item.addField(Field.is("name", FieldType.STRING));
        item.addField(Field.is("visibility", FieldType.STRING));
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

    private EntityInstance createTodo(
            final Thingifier thingifier,
            final String title,
            final String doneStatus,
            final String description) {
        EntityDefinition todo = thingifier.getDefinitionNamed("todo");
        return storeFor(thingifier)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(todo)
                                .withField("title", title)
                                .withField("doneStatus", doneStatus)
                                .withField("description", description));
    }

    private EntityInstance createItem(
            final Thingifier thingifier, final String name, final String visibility) {
        EntityDefinition item = thingifier.getDefinitionNamed("item");
        return storeFor(thingifier)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(item)
                                .withField("name", name)
                                .withField("visibility", visibility));
    }

    private ThingStore storeFor(final Thingifier thingifier) {
        return thingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
    }

    private HttpApiRequest query(final String path, final String body) {
        return new HttpApiRequest(path)
                .addHeader("Content-Type", ThingifierHttpApi.QUERY_CONTENT_TYPE)
                .setBody(body);
    }

    private HttpApiRequest jsonPathQuery(final String path, final String body) {
        return new HttpApiRequest(path)
                .addHeader("Content-Type", ThingifierHttpApi.JSONPATH_QUERY_CONTENT_TYPE)
                .setBody(body);
    }

    private HttpApiRequest structuredJsonQuery(final String path, final String body) {
        return new HttpApiRequest(path)
                .addHeader("Content-Type", ThingifierHttpApi.STRUCTURED_QUERY_CONTENT_TYPE)
                .setBody(body);
    }
}
