package uk.co.compendiumdev.casestudy.todomanager.http_api;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.UUID;

public class JsonRequestResponseTest {

    private Thingifier todoManager;

    Thing todo;
    Thing project;


    @Before
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();

        todo = todoManager.getThingNamed("todo");
        project = todoManager.getThingNamed("project");


    }

    @Test
    public void canGetAnEmptyJsonItemsCollection() {

        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assert.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        Assert.assertTrue(response.getBody().equalsIgnoreCase("{\"todos\":[]}"));
    }


    @Test
    public void canGetJsonItems() {


        todo.addInstance(todo.createInstance().setValue("title", "my title"));

        HttpApiRequest request = new HttpApiRequest("/todos");
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assert.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        final TodoCollectionResponse todos = new Gson().fromJson(response.getBody(), TodoCollectionResponse.class);

        Assert.assertEquals(1, todos.todos.length);
        Assert.assertEquals("my title", todos.todos[0].title);
        Assert.assertNotNull(todos.todos[0].guid);

    }


    @Test
    public void canGetMultipleJsonItems() {


        todo.addInstance(todo.createInstance().setValue("title", "my title"));
        todo.addInstance(todo.createInstance().setValue("title", "my other title"));

        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assert.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        final TodoCollectionResponse todos = new Gson().fromJson(response.getBody(), TodoCollectionResponse.class);

        Assert.assertEquals(2, todos.todos.length);
        Assert.assertEquals(todos.todos[0].title, todo.findInstanceByGUID(todos.todos[0].guid).getValue("title"));
        Assert.assertEquals(todos.todos[1].title, todo.findInstanceByGUID(todos.todos[1].guid).getValue("title"));

    }

    @Test
    public void cannotGetFromMissingEndpoint() {


        todo.addInstance(todo.createInstance().setValue("title", "my title"));
        todo.addInstance(todo.createInstance().setValue("title", "my other title"));

        HttpApiRequest request = new HttpApiRequest("todos" + System.nanoTime());
        request.getHeaders().putAll(HeadersSupport.acceptJson());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assert.assertEquals(404, response.getStatusCode());
        System.out.println(response.getBody());

        final ErrorMessages errors = new Gson().fromJson(response.getBody(), ErrorMessages.class);

        Assert.assertEquals(1, errors.errorMessages.length);
        Assert.assertTrue(errors.errorMessages[0], errors.errorMessages[0].startsWith("Could not find an instance with todos"));

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

        Assert.assertEquals(0, todo.countInstances());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        Assert.assertEquals(201, response.getStatusCode());
        System.out.println(response.getBody());

        Assert.assertEquals(1, todo.countInstances());

        // header should give me the guid
        String guid = response.getHeaders().get(ApiResponse.GUID_HEADER);

        final ThingInstance aTodo = todo.findInstanceByGUID(guid);

        Assert.assertEquals("title from json", aTodo.getValue("title"));

        //{"todo":"doneStatus":"FALSE","guid":
        Assert.assertTrue("Should have returned json", response.getBody().startsWith("{\"todo\":{\"doneStatus\":\"FALSE\",\"guid\":"));

    }

    // We only support single items as input so this is not acceptable
    // //{"todo":{"title":"title from json"}}


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

        Assert.assertEquals(0, todo.countInstances());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).put(request);

        System.out.println(response.getBody());

        Assert.assertEquals(201, response.getStatusCode());


        Assert.assertEquals(1, todo.countInstances());

        // header should give me the guid
        String guid = response.getHeaders().get(ApiResponse.GUID_HEADER);

        final ThingInstance aTodo = todo.findInstanceByGUID(guid);

        Assert.assertEquals("title from json", aTodo.getValue("title"));

        //{"todo":"doneStatus":"FALSE","guid":
        Assert.assertTrue("Should have returned xml", response.getBody().startsWith("<todo><doneStatus>FALSE</doneStatus>"));

    }



       /*


        POST to amend


     */

    @Test
    public void canPostToAmendAnItemWithJson() {

        final ThingInstance atodo = todo.createInstance().setValue("title", "my title");
        todo.addInstance(atodo);

        Assert.assertEquals(1, todo.countInstances());

        HttpApiRequest request = new HttpApiRequest("todos/" + atodo.getGUID());
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());


        //{"title":"title from json"}
        request.setBody("{\"title\":\"title from json\"}");


        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        Assert.assertEquals(200, response.getStatusCode());

        // TODO: amendments should possibly return todos[] or a single object {" but certainly not {"todo":

        System.out.println(response.getBody());

        Assert.assertEquals(1, todo.countInstances());

        Assert.assertEquals("title from json", atodo.getValue("title"));

    }

           /*


        PUT to amend


     */

    @Test
    public void canPutToAmendAnItemWithJson() {

        final ThingInstance atodo = todo.createInstance().setValue("title", "my title");
        todo.addInstance(atodo);

        Assert.assertEquals(1, todo.countInstances());

        HttpApiRequest request = new HttpApiRequest("todos/" + atodo.getGUID());
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsJson());


        // guid is optional here but have added it anyway
        //{"title":"title from json", "guid":"%s"}
        request.setBody(String.format("{\"title\":\"title from json\", \"guid\":\"%s\"}", atodo.getGUID()));


        final HttpApiResponse response = new ThingifierHttpApi(todoManager).put(request);

        System.out.println(response.getBody());

        Assert.assertEquals(200, response.getStatusCode());

        // TODO: amendments should possibly return todos[] or a single object {" but certainly not {"todo":


        Assert.assertEquals(1, todo.countInstances());

        Assert.assertEquals("title from json", atodo.getValue("title"));

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
