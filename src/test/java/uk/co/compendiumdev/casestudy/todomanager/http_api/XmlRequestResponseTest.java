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

import java.util.HashMap;
import java.util.Map;

public class XmlRequestResponseTest {

    private Thingifier todoManager;

    Thing todo;
    Thing project;

    Map<String, String> acceptXml;

    @Before
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();

        todo = todoManager.getThingNamed("todo");
        project = todoManager.getThingNamed("project");

        acceptXml = new HashMap<String, String>();
        acceptXml.put("Accept", "application/xml");

    }

    @Test
    public void canGetAnEmptyXmlItemsCollection(){



        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().putAll(acceptXml);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assert.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        Assert.assertTrue(response.getBody().equalsIgnoreCase("<todos></todos>"));
    }


    @Test
    public void canGetXmlItemsWhenAskedForXml(){


        todo.addInstance(todo.createInstance().setValue("title", "my title"));

        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().putAll(acceptXml);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assert.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        Assert.assertTrue(response.getBody().startsWith("<todos><todo>"));
    }

    @Test
    public void canGetXmlErrorMessagesWhenAskedForXml(){


        todo.addInstance(todo.createInstance().setValue("title", "my title"));

        HttpApiRequest request = new HttpApiRequest("todosyoohoo");
        request.getHeaders().putAll(acceptXml);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assert.assertEquals(404, response.getStatusCode());
        System.out.println(response.getBody());

        Assert.assertEquals(
                "<errorMessages><errorMessage>" +
                        "Could not find an instance with todosyoohoo"+
                        "</errorMessage></errorMessages>",
                        response.getBody());
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
