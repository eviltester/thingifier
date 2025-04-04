package uk.co.compendiumdev.casestudy.todomanager.http_api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.casestudy.todomanager.TodoManagerModel;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;


public class XmlRequestResponseTest {

    private Thingifier todoManager;

    EntityInstanceCollection todo;
    EntityInstanceCollection project;


    // todo: Too complicated any test that uses the TodoManagerModel in thingifier needs to be simplified
    // todo: move this to a case study test

    @BeforeEach
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();

        todo = todoManager.getThingInstancesNamed("todo", EntityRelModel.DEFAULT_DATABASE_NAME);
        project = todoManager.getThingInstancesNamed("project", EntityRelModel.DEFAULT_DATABASE_NAME);


    }

    @Test
    public void canGetAnEmptyXmlItemsCollection(){



        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().putAll(HeadersSupport.acceptXml());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assertions.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        Assertions.assertTrue(response.getBody().equalsIgnoreCase("<todos></todos>"));
    }


    @Test
    public void canGetXmlItemsWhenAskedForXml(){


        todo.addInstance(new EntityInstance(todo.definition())).setValue("title", "my title");

        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().putAll(HeadersSupport.acceptXml());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assertions.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        Assertions.assertTrue(response.getBody().startsWith("<todos><todo>"));
    }

    @Test
    public void canGetXmlErrorMessagesWhenAskedForXml(){


        todo.addInstance(new EntityInstance(todo.definition())).setValue("title", "my title");

        HttpApiRequest request = new HttpApiRequest("todosyoohoo");
        request.getHeaders().putAll(HeadersSupport.acceptXml());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assertions.assertEquals(404, response.getStatusCode());
        System.out.println(response.getBody());

        Assertions.assertEquals(
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

        Assertions.assertEquals(0, todo.countInstances());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        Assertions.assertEquals(201, response.getStatusCode());
        System.out.println(response.getBody());

        Assertions.assertEquals(1, todo.countInstances());

        // header should give me the guid
        String guid = response.getHeaders().get(ApiResponse.PRIMARY_KEY_HEADER);

        final EntityInstance aTodo = todo.findInstanceByPrimaryKey(guid);

        Assertions.assertEquals("test title", aTodo.getFieldValue("title").asString());

        Assertions.assertTrue(response.getBody().startsWith("<todo><doneStatus>false</doneStatus>"),
                "Should have returned xml as body: " + response.getBody());

    }

    @Test
    public void canPostAndAmendAnItemWithXml(){

        final EntityInstance atodo = todo.addInstance(new EntityInstance(todo.definition())).setValue("title", "my title");

        Assertions.assertEquals(1, todo.countInstances());

        HttpApiRequest request = new HttpApiRequest("todos/" + atodo.getPrimaryKeyValue());
        request.getHeaders().putAll(HeadersSupport.acceptXml());
        request.getHeaders().putAll(HeadersSupport.containsXml());


        request.setBody("<todo><title>test title</title></todo>");


        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        Assertions.assertEquals(200, response.getStatusCode());

        System.out.println(response.getBody());

        Assertions.assertEquals(1, todo.countInstances());

        Assertions.assertEquals("test title", atodo.getFieldValue("title").asString());

    }

        /*


        PUT to create


     */

    @Test
    public void canPutAndCreateAnItemWithXmlAndReceiveJson(){

        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().putAll(HeadersSupport.acceptJson());
        request.getHeaders().putAll(HeadersSupport.containsXml());

        request.setBody("<todo><title>test title</title></todo>");

        Assertions.assertEquals(0, todo.countInstances());

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).post(request);

        System.out.println(response.getBody());

        Assertions.assertEquals(201, response.getStatusCode());


        Assertions.assertEquals(1, todo.countInstances());

        // header should give me the guid
        String guid = response.getHeaders().get(ApiResponse.PRIMARY_KEY_HEADER);

        final EntityInstance aTodo = todo.findInstanceByPrimaryKey(guid);

        Assertions.assertEquals("test title", aTodo.getFieldValue("title").asString());

        //{"doneStatus":"FALSE","guid":
        Assertions.assertTrue(response.getBody().startsWith("{\"guid\":"),
                "Should have returned json as body " + response.getBody());

    }


     /*


        PUT to amend


     */

    @Test
    public void canPutToAmendAnItemWithJson(){

        final EntityInstance atodo = todo.addInstance(new EntityInstance(todo.definition())).setValue("title", "my title");

        Assertions.assertEquals(1, todo.countInstances());

        HttpApiRequest request = new HttpApiRequest("todos/"+atodo.getPrimaryKeyValue());
        request.getHeaders().putAll(HeadersSupport.acceptXml());
        request.getHeaders().putAll(HeadersSupport.containsXml());


        request.setBody("<todo><title>test title</title></todo>");


        final HttpApiResponse response = new ThingifierHttpApi(todoManager).put(request);

        System.out.println(response.getBody());

        Assertions.assertEquals(200, response.getStatusCode());

        Assertions.assertEquals(1, todo.countInstances());

        Assertions.assertEquals("test title", atodo.getFieldValue("title").asString());

    }

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
