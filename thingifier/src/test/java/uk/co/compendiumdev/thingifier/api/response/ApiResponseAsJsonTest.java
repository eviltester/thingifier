package uk.co.compendiumdev.thingifier.api.response;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType.STRING;

public class ApiResponseAsJsonTest {

    public JsonThing defaultJsonThing = new JsonThing(new ThingifierApiConfig().jsonOutput());

    @Test
    public void response404HasSingleErrorMessage(){

        ApiResponse response = ApiResponse.error404("oops");

        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(true, response.isErrorResponse());
        Assertions.assertEquals(false, response.isCollection());
        String json = new ApiResponseAsJson(response, defaultJsonThing).getJson();

        ErrorMessagesResponse messages = new Gson().fromJson(json, ErrorMessagesResponse.class);
        Assertions.assertEquals(1, messages.errorMessages.length);
        Assertions.assertEquals("oops", messages.errorMessages[0]);

    }

    @Test
    public void responseError(){

        ApiResponse response = ApiResponse.error(500, "oopsy");

        Assertions.assertEquals(500, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(true, response.isErrorResponse());
        Assertions.assertEquals(false, response.isCollection());

        String json = new ApiResponseAsJson(response, defaultJsonThing).getJson();

        ErrorMessagesResponse messages = new Gson().fromJson(json, ErrorMessagesResponse.class);
        Assertions.assertEquals(1, messages.errorMessages.length);
        Assertions.assertEquals("oopsy", messages.errorMessages[0]);

    }

    @Test
    public void responseErrors(){

        List<String> errors = new ArrayList();
        errors.add("oopsy");
        errors.add("doopsy");
        errors.add("do");

        ApiResponse response = ApiResponse.error(501, errors);

        Assertions.assertEquals(501, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(true, response.isErrorResponse());
        Assertions.assertEquals(false, response.isCollection());

        String json = new ApiResponseAsJson(response, defaultJsonThing).getJson();

        ErrorMessagesResponse messages = new Gson().fromJson(json, ErrorMessagesResponse.class);
        Assertions.assertEquals(3, messages.errorMessages.length);

        List<String> checkErrors = Arrays.asList(messages.errorMessages);

        Assertions.assertTrue(checkErrors.contains("oopsy"));
        Assertions.assertTrue(checkErrors.contains("doopsy"));
        Assertions.assertTrue(checkErrors.contains("do"));

    }

    @Test
    public void response200(){

        ApiResponse response = ApiResponse.success();

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(false, response.hasABody());
        Assertions.assertEquals(false, response.isErrorResponse());
        Assertions.assertEquals(false, response.isCollection());

        String json = new ApiResponseAsJson(response, defaultJsonThing).getJson();
        Assertions.assertEquals("", json);

    }

    @Test
    public void response200WithInstance(){

        Thingifier thingifier = new Thingifier();
        Thing todo = thingifier.createThing("todo", "todos");
        todo.definition().addFields( Field.is("title", STRING));
        Thing todos = thingifier.getThingNamed("todo");

        ThingInstance aTodo = todos.createManagedInstance().setValue("title", "a todo");

        ApiResponse response = ApiResponse.success().returnSingleInstance(aTodo);

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(false, response.isErrorResponse());
        Assertions.assertEquals(false, response.isCollection());

        String json = new ApiResponseAsJson(response, defaultJsonThing).getJson();
        System.out.println(json);
        Todo myTodo = new Gson().fromJson(json, Todo.class);

        Assertions.assertEquals(aTodo.getGUID(), myTodo.guid);
        Assertions.assertEquals("a todo", myTodo.title);

    }

    @Test
    public void response200WithInstances(){

        Thingifier thingifier = new Thingifier();
        Thing todo = thingifier.createThing("todo", "todos");
        todo.definition().addFields( Field.is("title", STRING));
        Thing todos = thingifier.getThingNamed("todo");

        ThingInstance aTodo = todos.createManagedInstance().setValue("title", "a todo");
        ThingInstance anotherTodo = todos.createManagedInstance().setValue("title", "another todo");

        ApiResponse response = ApiResponse.success().returnInstanceCollection(new ArrayList(todos.getInstances()));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(false, response.isErrorResponse());
        Assertions.assertEquals(true, response.isCollection());

        String json = new ApiResponseAsJson(response, defaultJsonThing).getJson();
        System.out.println(json);
        TodoCollectionResponse myTodo = new Gson().fromJson(json, TodoCollectionResponse.class);

        int foundCount=0;
        for(int todoid = 0; todoid < 2; todoid++){

            if (myTodo.todos[todoid].guid.equals(aTodo.getGUID())) {
                Assertions.assertEquals(aTodo.getGUID(), myTodo.todos[todoid].guid);
                Assertions.assertEquals("a todo", myTodo.todos[todoid].title);
                foundCount++;
            }else {
                Assertions.assertEquals(anotherTodo.getGUID(), myTodo.todos[todoid].guid);
                Assertions.assertEquals("another todo", myTodo.todos[todoid].title);
                foundCount++;
            }


        }
        Assertions.assertEquals(2, foundCount);

    }

    @Test
    public void response200WithEmptyInstances(){

        ApiResponse response = ApiResponse.success().returnInstanceCollection(new ArrayList());

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(true, response.hasABody());
        Assertions.assertEquals(false, response.isErrorResponse());
        Assertions.assertEquals(true, response.isCollection());

        String json = new ApiResponseAsJson(response, defaultJsonThing).getJson();
        System.out.println(json);
        Assertions.assertEquals("{}", json);

        TodoCollectionResponse myTodo = new Gson().fromJson(json, TodoCollectionResponse.class);

        Assertions.assertNull(myTodo.todos);
    }

    private class ErrorMessagesResponse{

        String []errorMessages;

    }


    private class TodoCollectionResponse{

        Todo[] todos;

    }

    private class Todo{

        String guid;
        String title;
    }

}
