package uk.co.compendiumdev.thingifier.api.response;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.co.compendiumdev.thingifier.generic.FieldType.STRING;

public class ApiResponseAsJsonTest {


    @Test
    public void response404HasSingleErrorMessage(){

        ApiResponse response = ApiResponse.error404("oops");

        Assert.assertEquals(404, response.getStatusCode());
        Assert.assertEquals(true, response.hasABody());
        Assert.assertEquals(true, response.isErrorResponse());
        Assert.assertEquals(false, response.isCollection());
        String json = new ApiResponseAsJson(response).getJson();

        ErrorMessagesResponse messages = new Gson().fromJson(json, ErrorMessagesResponse.class);
        Assert.assertEquals(1, messages.errorMessages.length);
        Assert.assertEquals("oops", messages.errorMessages[0]);

    }

    @Test
    public void responseError(){

        ApiResponse response = ApiResponse.error(500, "oopsy");

        Assert.assertEquals(500, response.getStatusCode());
        Assert.assertEquals(true, response.hasABody());
        Assert.assertEquals(true, response.isErrorResponse());
        Assert.assertEquals(false, response.isCollection());

        String json = new ApiResponseAsJson(response).getJson();

        ErrorMessagesResponse messages = new Gson().fromJson(json, ErrorMessagesResponse.class);
        Assert.assertEquals(1, messages.errorMessages.length);
        Assert.assertEquals("oopsy", messages.errorMessages[0]);

    }

    @Test
    public void responseErrors(){

        List<String> errors = new ArrayList();
        errors.add("oopsy");
        errors.add("doopsy");
        errors.add("do");

        ApiResponse response = ApiResponse.error(501, errors);

        Assert.assertEquals(501, response.getStatusCode());
        Assert.assertEquals(true, response.hasABody());
        Assert.assertEquals(true, response.isErrorResponse());
        Assert.assertEquals(false, response.isCollection());

        String json = new ApiResponseAsJson(response).getJson();

        ErrorMessagesResponse messages = new Gson().fromJson(json, ErrorMessagesResponse.class);
        Assert.assertEquals(3, messages.errorMessages.length);

        List<String> checkErrors = Arrays.asList(messages.errorMessages);

        Assert.assertTrue(checkErrors.contains("oopsy"));
        Assert.assertTrue(checkErrors.contains("doopsy"));
        Assert.assertTrue(checkErrors.contains("do"));

    }

    @Test
    public void response200(){

        ApiResponse response = ApiResponse.success();

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(false, response.hasABody());
        Assert.assertEquals(false, response.isErrorResponse());
        Assert.assertEquals(false, response.isCollection());

        String json = new ApiResponseAsJson(response).getJson();
        Assert.assertEquals("", json);

    }

    @Test
    public void response200WithInstance(){

        Thingifier thingifier = new Thingifier();
        Thing todo = thingifier.createThing("todo", "todos");
        todo.definition().addFields( Field.is("title", STRING));
        Thing todos = thingifier.getThingNamed("todo");

        ThingInstance aTodo = todos.createInstance().setValue("title", "a todo");
        todos.addInstance(aTodo);

        ApiResponse response = ApiResponse.success().returnSingleInstance(aTodo);

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(true, response.hasABody());
        Assert.assertEquals(false, response.isErrorResponse());
        Assert.assertEquals(false, response.isCollection());

        String json = new ApiResponseAsJson(response).getJson();
        System.out.println(json);
        TodoResponse myTodo = new Gson().fromJson(json, TodoResponse.class);

        Assert.assertEquals(aTodo.getGUID(), myTodo.todo.guid);
        Assert.assertEquals("a todo", myTodo.todo.title);

    }

    @Test
    public void response200WithInstances(){

        Thingifier thingifier = new Thingifier();
        Thing todo = thingifier.createThing("todo", "todos");
        todo.definition().addFields( Field.is("title", STRING));
        Thing todos = thingifier.getThingNamed("todo");

        ThingInstance aTodo = todos.createInstance().setValue("title", "a todo");
        todos.addInstance(aTodo);
        ThingInstance anotherTodo = todos.createInstance().setValue("title", "another todo");
        todos.addInstance(anotherTodo);

        ApiResponse response = ApiResponse.success().returnInstanceCollection(new ArrayList(todos.getInstances()));

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(true, response.hasABody());
        Assert.assertEquals(false, response.isErrorResponse());
        Assert.assertEquals(true, response.isCollection());

        String json = new ApiResponseAsJson(response).getJson();
        System.out.println(json);
        TodoResponse myTodo = new Gson().fromJson(json, TodoResponse.class);

        int foundCount=0;
        for(int todoid = 0; todoid < 2; todoid++){

            if (myTodo.todos[todoid].guid.equals(aTodo.getGUID())) {
                Assert.assertEquals(aTodo.getGUID(), myTodo.todos[todoid].guid);
                Assert.assertEquals("a todo", myTodo.todos[todoid].title);
                foundCount++;
            }else {
                Assert.assertEquals(anotherTodo.getGUID(), myTodo.todos[todoid].guid);
                Assert.assertEquals("another todo", myTodo.todos[todoid].title);
                foundCount++;
            }


        }
        Assert.assertEquals(2, foundCount);

    }

    @Test
    public void response200WithEmptyInstances(){

        ApiResponse response = ApiResponse.success().returnInstanceCollection(new ArrayList());

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(true, response.hasABody());
        Assert.assertEquals(false, response.isErrorResponse());
        Assert.assertEquals(true, response.isCollection());

        String json = new ApiResponseAsJson(response).getJson();
        System.out.println(json);
        Assert.assertEquals("{}", json);

        TodoResponse myTodo = new Gson().fromJson(json, TodoResponse.class);

        Assert.assertNull(myTodo.todo);
        Assert.assertNull(myTodo.todos);
    }

    private class ErrorMessagesResponse{

        String []errorMessages;

    }

    private class TodoResponse{

        Todo todo;
        Todo[] todos;

    }

    private class Todo{

        String guid;
        String title;
    }

}
