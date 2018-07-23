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
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsJsonTest;

import java.util.HashMap;
import java.util.Map;

public class JsonRequestResponseTest {

    private Thingifier todoManager;

    Thing todo;
    Thing project;

    Map<String, String> acceptJson;

    @Before
    public void createDefinitions() {

        todoManager = TodoManagerModel.definedAsThingifier();

        todo = todoManager.getThingNamed("todo");
        project = todoManager.getThingNamed("project");

        acceptJson = new HashMap<String, String>();
        acceptJson.put("Accept", "application/json");

    }

    @Test
    public void canGetJsonItems(){


        todo.addInstance(todo.createInstance().setValue("title", "my title"));

        HttpApiRequest request = new HttpApiRequest("/todos");
        request.getHeaders().putAll(acceptJson);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assert.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        final TodoCollectionResponse todos = new Gson().fromJson(response.getBody(), TodoCollectionResponse.class);

        Assert.assertEquals(1, todos.todos.length);
        Assert.assertEquals("my title", todos.todos[0].title );
        Assert.assertNotNull(todos.todos[0].guid );

    }

    @Test
    public void canGetMultipleJsonItems(){


        todo.addInstance(todo.createInstance().setValue("title", "my title"));
        todo.addInstance(todo.createInstance().setValue("title", "my other title"));

        HttpApiRequest request = new HttpApiRequest("todos");
        request.getHeaders().putAll(acceptJson);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assert.assertEquals(200, response.getStatusCode());
        System.out.println(response.getBody());

        final TodoCollectionResponse todos = new Gson().fromJson(response.getBody(), TodoCollectionResponse.class);

        Assert.assertEquals(2, todos.todos.length);
        Assert.assertEquals(todos.todos[0].title, todo.findInstanceByGUID(todos.todos[0].guid).getValue("title"));
        Assert.assertEquals(todos.todos[1].title, todo.findInstanceByGUID(todos.todos[1].guid).getValue("title"));

    }

    @Test
    public void cannotGetFromMissingEndpoint(){


        todo.addInstance(todo.createInstance().setValue("title", "my title"));
        todo.addInstance(todo.createInstance().setValue("title", "my other title"));

        HttpApiRequest request = new HttpApiRequest("todos" + System.nanoTime());
        request.getHeaders().putAll(acceptJson);

        final HttpApiResponse response = new ThingifierHttpApi(todoManager).get(request);
        Assert.assertEquals(404, response.getStatusCode());
        System.out.println(response.getBody());

        final ErrorMessages errors = new Gson().fromJson(response.getBody(), ErrorMessages.class);

        Assert.assertEquals(1, errors.errorMessages.length);
        errors.errorMessages[0].startsWith("Could not find an instance with todos");

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
