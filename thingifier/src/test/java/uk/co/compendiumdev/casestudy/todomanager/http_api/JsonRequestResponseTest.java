package uk.co.compendiumdev.casestudy.todomanager.http_api;

import com.google.gson.Gson;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

public class JsonRequestResponseTest {

    private Thingifier todoManager;

    EntityDefinition todo;
    EntityDefinition project;

    // todo: Too complicated any test that uses the TodoManagerModel in thingifier needs to be
    // simplified
    // todo: move this to a case study test
    @BeforeEach
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();

        todoManager.apiConfig().setApiToEnforceAcceptHeaderForResponses(false);
        todo = todoManager.getERmodel().getSchema().getDefinitionWithSingularOrPluralNamed("todo");
        project =
                todoManager
                        .getERmodel()
                        .getSchema()
                        .getDefinitionWithSingularOrPluralNamed("project");
    }

    @Test
    public void canGetAnEmptyJsonItemsCollection() {

        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assertions.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        Assertions.assertTrue(response.getBody().equalsIgnoreCase("{\"todos\":[]}"));
    }

    @Test
    public void canGetJsonItems() {

        todoManager
                .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                .createInstance(EntityInstanceDraft.forEntity(todo).withField("title", "my title"));

        HttpApiRequest request = new HttpApiRequest("/todos");
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assertions.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        final TodoCollectionResponse todos =
                new Gson().fromJson(response.getBody(), TodoCollectionResponse.class);

        Assertions.assertEquals(1, todos.todos.length);
        Assertions.assertEquals("my title", todos.todos[0].title);
        Assertions.assertNotNull(todos.todos[0].guid);
    }

    @Test
    public void canGetJsonItemAsACollection() {

        todoManager.apiConfig().setReturnSingleGetItemsAsCollection(true);
        final EntityInstance aTodo =
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .createInstance(
                                EntityInstanceDraft.forEntity(todo).withField("title", "my title"));

        HttpApiRequest request = new HttpApiRequest("/todos/" + aTodo.getPrimaryKeyValue());
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assertions.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        final TodoCollectionResponse todos =
                new Gson().fromJson(response.getBody(), TodoCollectionResponse.class);

        Assertions.assertEquals(1, todos.todos.length);
        Assertions.assertEquals("my title", todos.todos[0].title);
        Assertions.assertNotNull(todos.todos[0].guid);
    }

    @Test
    public void canGetMultipleJsonItems() {

        todoManager
                .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                .createInstance(EntityInstanceDraft.forEntity(todo).withField("title", "my title"));
        todoManager
                .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                .createInstance(
                        EntityInstanceDraft.forEntity(todo).withField("title", "my other title"));

        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assertions.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        final TodoCollectionResponse todos =
                new Gson().fromJson(response.getBody(), TodoCollectionResponse.class);

        Assertions.assertEquals(2, todos.todos.length);
        Assertions.assertEquals(
                todos.todos[0].title,
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .findInstanceByPrimaryKey(todo, todos.todos[0].guid)
                        .getFieldValue("title")
                        .asString());
        Assertions.assertEquals(
                todos.todos[1].title,
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .findInstanceByPrimaryKey(todo, todos.todos[1].guid)
                        .getFieldValue("title")
                        .asString());
    }

    @Test
    public void cannotGetFromMissingEndpoint() {

        todoManager
                .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                .createInstance(EntityInstanceDraft.forEntity(todo).withField("title", "my title"));
        todoManager
                .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                .createInstance(
                        EntityInstanceDraft.forEntity(todo).withField("title", "my other title"));

        HttpApiRequest request = new HttpApiRequest("todos" + System.nanoTime());
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assertions.assertEquals(404, response.getStatusCode());
        System.out.println(response.getBody());

        final ErrorMessages errors = new Gson().fromJson(response.getBody(), ErrorMessages.class);

        Assertions.assertEquals(1, errors.errorMessages.length);
        Assertions.assertTrue(
                errors.errorMessages[0].startsWith("Could not find an instance with todos"),
                errors.errorMessages[0]);
    }

    /*


       POST to create


    */

    @Test
    public void canPostAndCreateAnItemWithJson() {

        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());

        // {"title":"title from json"}
        request.setBody("{\"title\":\"title from json\"}");

        Assertions.assertEquals(
                0,
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .countInstances(todo));

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        Assertions.assertEquals(201, response.getStatusCode());
        System.out.println(response.getBody());

        Assertions.assertEquals(
                1,
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .countInstances(todo));

        // header should give me the guid
        String guid = response.getHeaders().get(ApiResponse.PRIMARY_KEY_HEADER);

        final EntityInstance aTodo =
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .findInstanceByPrimaryKey(todo, guid);

        Assertions.assertEquals("title from json", aTodo.getFieldValue("title").asString());

        Assertions.assertTrue(
                response.getBody().startsWith("{\"guid\":\""), "Should have returned json");
    }

    /*


       PUT to create


    */

    @Test
    public void canPostAndCreateAnItemWithJsonAndReceiveXml() {

        HttpApiRequest request = new HttpApiRequest("todos");
        request.setHeaders(Map.of("content-type", "application/json"));
        request.getHeaders().putAll(HeadersSupport.acceptXml());
        request.getHeaders().putAll(HeadersSupport.containsJson());

        // {"title":"title from json"}
        request.setBody("{\"title\":\"title from json\"}");

        Assertions.assertEquals(
                0,
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .countInstances(todo));

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        System.out.println(response.getBody());

        Assertions.assertEquals(201, response.getStatusCode());

        Assertions.assertEquals(
                1,
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .countInstances(todo));

        // header should give me the guid
        String guid = response.getHeaders().get(ApiResponse.PRIMARY_KEY_HEADER);

        final EntityInstance aTodo =
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .findInstanceByPrimaryKey(todo, guid);

        Assertions.assertEquals("false", aTodo.getFieldValue("doneStatus").asString());
        Assertions.assertEquals("title from json", aTodo.getFieldValue("title").asString());

        Assertions.assertTrue(
                response.getBody().startsWith("<todo><doneStatus>false</doneStatus>"),
                "Should have returned xml");
    }

    /*


       POST to amend


    */

    @Test
    public void canPostToAmendAnItemWithJson() {

        final EntityInstance atodo =
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .createInstance(
                                EntityInstanceDraft.forEntity(todo).withField("title", "my title"));

        Assertions.assertEquals(
                1,
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .countInstances(todo));

        HttpApiRequest request = new HttpApiRequest("todos/" + atodo.getPrimaryKeyValue());
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());

        // {"title":"title from json"}
        request.setBody("{\"title\":\"title from json\"}");

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        Assertions.assertEquals(200, response.getStatusCode());

        System.out.println(response.getBody());

        Assertions.assertEquals(
                1,
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .countInstances(todo));

        final EntityInstance updatedTodo =
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .findInstanceByPrimaryKey(todo, atodo.getPrimaryKeyValue());

        Assertions.assertEquals("title from json", updatedTodo.getFieldValue("title").asString());
    }

    /*


       PUT to amend


    */

    @Test
    public void canPutToAmendAnItemWithJson() {

        final EntityInstance atodo =
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .createInstance(
                                EntityInstanceDraft.forEntity(todo).withField("title", "my title"));

        Assertions.assertEquals(
                1,
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .countInstances(todo));

        HttpApiRequest request = new HttpApiRequest("todos/" + atodo.getPrimaryKeyValue());
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());

        // guid is optional here but have added it anyway
        // {"title":"title from json", "guid":"%s"}
        request.setBody(
                String.format(
                        "{\"title\":\"title from json\", \"guid\":\"%s\"}",
                        atodo.getPrimaryKeyValue()));

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).put(request);

        System.out.println(response.getBody());

        Assertions.assertEquals(200, response.getStatusCode());

        Assertions.assertEquals(
                1,
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .countInstances(todo));

        final EntityInstance updatedTodo =
                todoManager
                        .getRepository(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .findInstanceByPrimaryKey(todo, atodo.getPrimaryKeyValue());

        Assertions.assertEquals("title from json", updatedTodo.getFieldValue("title").asString());
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
}
