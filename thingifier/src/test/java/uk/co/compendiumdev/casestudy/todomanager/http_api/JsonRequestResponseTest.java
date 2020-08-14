package uk.co.compendiumdev.casestudy.todomanager.http_api;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

import java.util.UUID;

public class JsonRequestResponseTest {

    private Thingifier todoManager;

    Thing todo;
    Thing project;


    @BeforeEach
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();

        todoManager.apiConfig().setApiToEnforceAcceptHeaderForResponses(false);
        todo = todoManager.getThingNamed("todo");
        project = todoManager.getThingNamed("project");


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


        todo.createManagedInstance().setValue("title", "my title");

        HttpApiRequest request = new HttpApiRequest("/todos");
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assertions.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        final TodoCollectionResponse todos = new Gson().fromJson(response.getBody(), TodoCollectionResponse.class);

        Assertions.assertEquals(1, todos.todos.length);
        Assertions.assertEquals("my title", todos.todos[0].title);
        Assertions.assertNotNull(todos.todos[0].guid);

    }

    @Test
    public void canGetJsonItemAsACollection() {


        final ThingInstance aTodo = todo.createManagedInstance().setValue("title", "my title");

        HttpApiRequest request = new HttpApiRequest("/todos/" + aTodo.getGUID());
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assertions.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        final TodoCollectionResponse todos = new Gson().fromJson(response.getBody(), TodoCollectionResponse.class);

        Assertions.assertEquals(1, todos.todos.length);
        Assertions.assertEquals("my title", todos.todos[0].title);
        Assertions.assertNotNull(todos.todos[0].guid);

    }

    // this will only happen if routings allow it, normally we will route through plurals so it won't happen via http
    // except on an admin query interface routing
    @Test
    public void canGetJsonItemAsAnInstance() {


        final ThingInstance aTodo = todo.createManagedInstance().setValue("title", "my title");

        HttpApiRequest request = new HttpApiRequest("/todo/" + aTodo.getGUID());
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assertions.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        final Todo todo = new Gson().fromJson(response.getBody(), Todo.class);

        Assertions.assertEquals("my title", todo.title);
        Assertions.assertNotNull(todo.guid);

    }


    @Test
    public void canGetMultipleJsonItems() {


        todo.createManagedInstance().setValue("title", "my title");
        todo.createManagedInstance().setValue("title", "my other title");

        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assertions.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        final TodoCollectionResponse todos = new Gson().fromJson(response.getBody(), TodoCollectionResponse.class);

        Assertions.assertEquals(2, todos.todos.length);
        Assertions.assertEquals(todos.todos[0].title, todo.findInstanceByGUID(todos.todos[0].guid).getFieldValue("title").asString());
        Assertions.assertEquals(todos.todos[1].title, todo.findInstanceByGUID(todos.todos[1].guid).getFieldValue("title").asString());

    }

    @Test
    public void cannotGetFromMissingEndpoint() {


        todo.createManagedInstance().setValue("title", "my title");
        todo.createManagedInstance().setValue("title", "my other title");

        HttpApiRequest request = new HttpApiRequest("todos" + System.nanoTime());
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assertions.assertEquals(404, response.getStatusCode());
        System.out.println(response.getBody());

        final ErrorMessages errors = new Gson().fromJson(response.getBody(), ErrorMessages.class);

        Assertions.assertEquals(1, errors.errorMessages.length);
        Assertions.assertTrue(errors.errorMessages[0].startsWith("Could not find an instance with todos"),
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


        //{"title":"title from json"}
        request.setBody("{\"title\":\"title from json\"}");

        Assertions.assertEquals(0, todo.countInstances());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        Assertions.assertEquals(201, response.getStatusCode());
        System.out.println(response.getBody());

        Assertions.assertEquals(1, todo.countInstances());

        // header should give me the guid
        String guid = response.getHeaders().get(ApiResponse.GUID_HEADER);

        final ThingInstance aTodo = todo.findInstanceByGUID(guid);

        Assertions.assertEquals("title from json", aTodo.getFieldValue("title").asString());

        Assertions.assertTrue(response.getBody().startsWith("{\"guid\":\""),
                "Should have returned json");

    }

         /*


        PUT to create


     */

    @Test
    public void canPutAndCreateAnItemWithJsonAndReceiveXml() {

        HttpApiRequest request = new HttpApiRequest("todos/" + UUID.randomUUID().toString());
        request.getHeaders().putAll(HeadersSupport.acceptXml());
        request.getHeaders().putAll(HeadersSupport.containsJson());

        //{"title":"title from json"}
        request.setBody("{\"title\":\"title from json\"}");

        Assertions.assertEquals(0, todo.countInstances());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).put(request);

        System.out.println(response.getBody());

        Assertions.assertEquals(201, response.getStatusCode());


        Assertions.assertEquals(1, todo.countInstances());

        // header should give me the guid
        String guid = response.getHeaders().get(ApiResponse.GUID_HEADER);

        final ThingInstance aTodo = todo.findInstanceByGUID(guid);

        Assertions.assertEquals("false", aTodo.getFieldValue("doneStatus").asString());
        Assertions.assertEquals("title from json", aTodo.getFieldValue("title").asString());

        Assertions.assertTrue(response.getBody().startsWith("<todo><doneStatus>false</doneStatus>"),
                "Should have returned xml");

    }



       /*


        POST to amend


     */

    @Test
    public void canPostToAmendAnItemWithJson() {

        final ThingInstance atodo = todo.createManagedInstance().setValue("title", "my title");

        Assertions.assertEquals(1, todo.countInstances());

        HttpApiRequest request = new HttpApiRequest("todos/" + atodo.getGUID());
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());


        //{"title":"title from json"}
        request.setBody("{\"title\":\"title from json\"}");


        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        Assertions.assertEquals(200, response.getStatusCode());

        System.out.println(response.getBody());

        Assertions.assertEquals(1, todo.countInstances());

        Assertions.assertEquals("title from json", atodo.getFieldValue("title").asString());

    }

           /*


        PUT to amend


     */

    @Test
    public void canPutToAmendAnItemWithJson() {

        final ThingInstance atodo = todo.createManagedInstance().setValue("title", "my title");

        Assertions.assertEquals(1, todo.countInstances());

        HttpApiRequest request = new HttpApiRequest("todos/" + atodo.getGUID());
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());


        // guid is optional here but have added it anyway
        //{"title":"title from json", "guid":"%s"}
        request.setBody(String.format("{\"title\":\"title from json\", \"guid\":\"%s\"}", atodo.getGUID()));


        final HttpApiResponse response = new ThingifierHttpApi(todoManager).put(request);

        System.out.println(response.getBody());

        Assertions.assertEquals(200, response.getStatusCode());

        Assertions.assertEquals(1, todo.countInstances());

        Assertions.assertEquals("title from json", atodo.getFieldValue("title").asString());

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
