package uk.co.compendiumdev.casestudy.todomanager.http_api;

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


public class XmlRequestResponseTest {

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
    public void canGetAnEmptyXmlItemsCollection(){



        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().putAll(HeadersSupport.acceptXml());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assert.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        Assert.assertTrue(response.getBody().equalsIgnoreCase("<todos></todos>"));
    }


    @Test
    public void canGetXmlItemsWhenAskedForXml(){


        todo.addInstance(todo.createInstance().setValue("title", "my title"));

        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().putAll(HeadersSupport.acceptXml());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assert.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        Assert.assertTrue(response.getBody().startsWith("<todos><todo>"));
    }

    @Test
    public void canGetXmlErrorMessagesWhenAskedForXml(){


        todo.addInstance(todo.createInstance().setValue("title", "my title"));

        HttpApiRequest request = new HttpApiRequest("todosyoohoo");
        request.getHeaders().putAll(HeadersSupport.acceptXml());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assert.assertEquals(404, response.getStatusCode());
        System.out.println(response.getBody());

        Assert.assertEquals(
                "<errorMessages><errorMessage>" +
                        "Could not find an instance with todosyoohoo"+
                        "</errorMessage></errorMessages>",
                        response.getBody());
    }



    /*


        POST to create


     */

    @Test
    public void canPostAndCreateAnItemWithXml(){

        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().putAll(HeadersSupport.acceptXml());
        request.getHeaders().putAll(HeadersSupport.containsXml());

        request.setBody("<todo><title>test title</title></todo>");

        Assert.assertEquals(0, todo.countInstances());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        Assert.assertEquals(201, response.getStatusCode());
        System.out.println(response.getBody());

        Assert.assertEquals(1, todo.countInstances());

        // header should give me the guid
        String guid = response.getHeaders().get(ApiResponse.GUID_HEADER);

        final ThingInstance aTodo = todo.findInstanceByGUID(guid);

        Assert.assertEquals("test title", aTodo.getValue("title"));

        //{"todo":"doneStatus":"FALSE","guid":
        Assert.assertTrue("Should have returned xml", response.getBody().startsWith("<todo><doneStatus>FALSE</doneStatus>"));

    }

    // We only support single items as input so this is not acceptable
    // "<todos><todo><title>test title</title></todo></todos>"

    @Test
    public void canPostAndAmendAnItemWithXml(){

        final ThingInstance atodo = todo.createInstance().setValue("title", "my title");
        todo.addInstance(atodo);

        Assert.assertEquals(1, todo.countInstances());

        HttpApiRequest request = new HttpApiRequest("todos/" + atodo.getGUID());
        request.getHeaders().putAll(HeadersSupport.acceptXml());
        request.getHeaders().putAll(HeadersSupport.containsXml());


        //<todo><title>test title</title></todo>
        request.setBody("<todo><title>test title</title></todo>");


        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        Assert.assertEquals(200, response.getStatusCode());

        // TODO: should amendments should really return <todos> not <todo>

        System.out.println(response.getBody());

        Assert.assertEquals(1, todo.countInstances());

        Assert.assertEquals("test title", atodo.getValue("title"));

    }

        /*


        PUT to create


     */

    @Test
    public void canPutAndCreateAnItemWithXmlAndReceiveJson(){

        HttpApiRequest request = new HttpApiRequest("todos/"+UUID.randomUUID().toString());
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsXml());

        request.setBody("<todo><title>test title</title></todo>");

        Assert.assertEquals(0, todo.countInstances());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).put(request);

        System.out.println(response.getBody());

        Assert.assertEquals(201, response.getStatusCode());


        Assert.assertEquals(1, todo.countInstances());

        // header should give me the guid
        String guid = response.getHeaders().get(ApiResponse.GUID_HEADER);

        final ThingInstance aTodo = todo.findInstanceByGUID(guid);

        Assert.assertEquals("test title", aTodo.getValue("title"));

        //{"todo":"doneStatus":"FALSE","guid":
        Assert.assertTrue("Should have returned json", response.getBody().startsWith("{\"todo\":{\"doneStatus\":\"FALSE\",\"guid\":"));

    }


     /*


        PUT to amend


     */

    @Test
    public void canPutToAmendAnItemWithJson(){

        final ThingInstance atodo = todo.createInstance().setValue("title", "my title");
        todo.addInstance(atodo);

        Assert.assertEquals(1, todo.countInstances());

        HttpApiRequest request = new HttpApiRequest("todos/"+atodo.getGUID());
        request.getHeaders().putAll(HeadersSupport.acceptXml());
        request.getHeaders().putAll(HeadersSupport.containsXml());


        request.setBody("<todo><title>test title</title></todo>");


        final HttpApiResponse response = new ThingifierHttpApi(todoManager).put(request);

        System.out.println(response.getBody());

        Assert.assertEquals(200, response.getStatusCode());

        // TODO: amendments should possibly return <todos>


        Assert.assertEquals(1, todo.countInstances());

        Assert.assertEquals("test title", atodo.getValue("title"));

    }

    // We only support single items as input so this is not acceptable
    // "<todos><todo><title>test title</title></todo></todos>"
    private class TodoCollectionResponse{

        Todo[] todos;

    }

    private class Todo{

        String guid;
        String title;
    }

    private class ErrorMessages{

        String[] errorMessages;
    }
}
