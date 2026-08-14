package uk.co.compendiumdev.casestudy.todomanager.http_api;

import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

public class XmlRequestResponseTest {

    private Thingifier todoManager;

    EntityDefinition todo;
    EntityDefinition project;

    // todo: Too complicated any test that uses the TodoManagerModel in thingifier needs to be
    // simplified
    // todo: move this to a case study test

    @BeforeEach
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();

        todo = todoManager.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");
        project =
                todoManager
                        .getERmodel()
                        .getSchema()
                        .getDefinitionWithSingularOrPluralNamed("project");
    }

    @Test
    public void canGetAnEmptyXmlItemsCollection() {

        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().putAll(HeadersSupport.acceptXml());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assertions.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        Assertions.assertTrue(response.getBody().equalsIgnoreCase("<todos></todos>"));
    }

    @Test
    public void canGetXmlItemsWhenAskedForXml() {

        todoManager
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(EntityInstanceDraft.forEntity(todo).withField("title", "my title"));

        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().putAll(HeadersSupport.acceptXml());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assertions.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        Assertions.assertTrue(response.getBody().startsWith("<todos><todo>"));
    }

    @ParameterizedTest
    @MethodSource("xmlCompatibleAcceptHeaders")
    public void canGetXmlItemsWhenAskedForXmlCompatibleMediaType(
            final String accept, final String expectedContentType) {

        todoManager
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(EntityInstanceDraft.forEntity(todo).withField("title", "my title"));

        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().put("accept", accept);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);

        Assertions.assertEquals(200, response.getStatusCode(), accept);
        Assertions.assertEquals(expectedContentType, response.getType());
        Assertions.assertTrue(response.getBody().startsWith("<todos><todo>"));
    }

    @Test
    public void canGetXmlErrorMessagesWhenAskedForXml() {

        todoManager
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(EntityInstanceDraft.forEntity(todo).withField("title", "my title"));

        HttpApiRequest request = new HttpApiRequest("todosyoohoo");
        request.getHeaders().putAll(HeadersSupport.acceptXml());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assertions.assertEquals(404, response.getStatusCode());
        System.out.println(response.getBody());

        Assertions.assertEquals(
                "<errorMessages><errorMessage>"
                        + "Could not find an instance with todosyoohoo"
                        + "</errorMessage></errorMessages>",
                response.getBody());
    }

    /*


       POST to create


    */

    @Test
    public void canPostAndCreateAnItemWithXml() {

        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().putAll(HeadersSupport.acceptXml());
        request.getHeaders().putAll(HeadersSupport.containsXml());

        request.setBody("<todo><title>test title</title></todo>");

        Assertions.assertEquals(
                0,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        Assertions.assertEquals(201, response.getStatusCode());
        System.out.println(response.getBody());

        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));

        // header should give me the guid
        String guid = response.getHeaders().get(ApiResponse.PRIMARY_KEY_HEADER);

        final EntityInstance aTodo =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .findByPrimaryKey(todo, guid);

        Assertions.assertEquals("test title", aTodo.getFieldValue("title").asString());

        Assertions.assertTrue(
                response.getBody().startsWith("<todo><doneStatus>false</doneStatus>"),
                "Should have returned xml as body: " + response.getBody());
    }

    @ParameterizedTest
    @MethodSource("xmlCompatibleContentTypes")
    public void canPostAndCreateAnItemWithXmlCompatibleContentType(final String contentType) {

        todoManager.apiConfig().setApiToEnforceContentTypeForRequests(true);

        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().putAll(HeadersSupport.acceptXml());
        request.getHeaders().put("content-type", contentType);

        request.setBody("<todo><title>test title</title></todo>");

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        Assertions.assertEquals(201, response.getStatusCode(), contentType);
        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));
        Assertions.assertTrue(
                response.getBody().startsWith("<todo><doneStatus>false</doneStatus>"),
                "Should have returned xml as body: " + response.getBody());
    }

    @ParameterizedTest
    @MethodSource("unsupportedXmlBasedContentTypes")
    public void unsupportedXmlBasedContentTypeIsRejectedForPosts(final String contentType) {

        todoManager.apiConfig().setApiToEnforceContentTypeForRequests(true);

        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().put("content-type", contentType);
        request.setBody("<todo><title>test title</title></todo>");

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        Assertions.assertEquals(415, response.getStatusCode(), contentType);
        Assertions.assertTrue(response.getBody().contains("Unsupported Content Type"));
        Assertions.assertEquals(
                0,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));
    }

    @Test
    public void canPostAndAmendAnItemWithXml() {

        final EntityInstance atodo =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(EntityInstanceDraft.forEntity(todo).withField("title", "my title"));

        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));

        HttpApiRequest request = new HttpApiRequest("todos/" + atodo.getPrimaryKeyValue());
        request.getHeaders().putAll(HeadersSupport.acceptXml());
        request.getHeaders().putAll(HeadersSupport.containsXml());

        request.setBody("<todo><title>test title</title></todo>");

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        Assertions.assertEquals(200, response.getStatusCode());

        System.out.println(response.getBody());

        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));

        final EntityInstance updatedTodo =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .findByPrimaryKey(todo, atodo.getPrimaryKeyValue());

        Assertions.assertEquals("test title", updatedTodo.getFieldValue("title").asString());
    }

    /*


       PUT to create


    */

    @Test
    public void canPutAndCreateAnItemWithXmlAndReceiveJson() {

        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsXml());

        request.setBody("<todo><title>test title</title></todo>");

        Assertions.assertEquals(
                0,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        System.out.println(response.getBody());

        Assertions.assertEquals(201, response.getStatusCode());

        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));

        // header should give me the guid
        String guid = response.getHeaders().get(ApiResponse.PRIMARY_KEY_HEADER);

        final EntityInstance aTodo =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .findByPrimaryKey(todo, guid);

        Assertions.assertEquals("test title", aTodo.getFieldValue("title").asString());

        // {"doneStatus":"FALSE","guid":
        Assertions.assertTrue(
                response.getBody().startsWith("{\"guid\":"),
                "Should have returned json as body " + response.getBody());
    }

    /*


       PUT to amend


    */

    @Test
    public void canPutToAmendAnItemWithJson() {

        final EntityInstance atodo =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(EntityInstanceDraft.forEntity(todo).withField("title", "my title"));

        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));

        HttpApiRequest request = new HttpApiRequest("todos/" + atodo.getPrimaryKeyValue());
        request.getHeaders().putAll(HeadersSupport.acceptXml());
        request.getHeaders().putAll(HeadersSupport.containsXml());

        request.setBody("<todo><title>test title</title></todo>");

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).put(request);

        System.out.println(response.getBody());

        Assertions.assertEquals(200, response.getStatusCode());

        Assertions.assertEquals(
                1,
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(todo));

        final EntityInstance updatedTodo =
                todoManager
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .findByPrimaryKey(todo, atodo.getPrimaryKeyValue());

        Assertions.assertEquals("test title", updatedTodo.getFieldValue("title").asString());
    }

    private class TodoCollectionResponse {

        Todo[] todos;
    }

    private class Todo {

        String guid;
        String title;
    }

    private class ErrorMessages {

        String[] errorMessages;
    }

    private static Stream<Arguments> xmlCompatibleAcceptHeaders() {
        return Stream.of(
                Arguments.of("text/xml", "text/xml"),
                Arguments.of(
                        "application/vnd.example.todo+xml", "application/vnd.example.todo+xml"),
                Arguments.of("application/*+xml", "application/todo+xml"));
    }

    private static Stream<String> xmlCompatibleContentTypes() {
        return Stream.of("text/xml", "application/vnd.example.todo+xml");
    }

    private static Stream<String> unsupportedXmlBasedContentTypes() {
        return Stream.of(
                "application/problem+xml",
                "application/soap+xml",
                "application/xhtml+xml",
                "image/svg+xml",
                "application/atom+xml",
                "application/rss+xml",
                "application/*+xml");
    }
}
