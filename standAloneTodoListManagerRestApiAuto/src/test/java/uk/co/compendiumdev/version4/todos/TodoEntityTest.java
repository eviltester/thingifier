package uk.co.compendiumdev.version4.todos;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.sparkstart.Environment;
import uk.co.compendiumdev.version4.api.Api;
import uk.co.compendiumdev.version4.api.Payloads;


public class TodoEntityTest {





    private Response createATodo(){
        Payloads.TodoPayload todo = new Payloads.TodoPayload();
        todo.doneStatus=true;
        todo.title="Created Todo";
        todo.description="hello world";

        return Api.createTodo(todo);
    }


    @Test
    void canCreateATodo(){

        final Response response = createATodo();

        Assertions.assertEquals(201, response.getStatusCode());

        final Payloads.TodoPayload created = response.body().as(Payloads.TodoPayload.class);
        Assertions.assertEquals("Created Todo", created.title);
        Assertions.assertEquals("hello world", created.description);
    }

    @Test
    void canCreateAMinimalTodo(){

        Payloads.TodoPayload todo = new Payloads.TodoPayload();
        todo.title="Created Todo";
        final Response response = Api.createTodo(todo);

        Assertions.assertEquals(201, response.getStatusCode());

        final Payloads.TodoPayload created = response.body().as(Payloads.TodoPayload.class);
        Assertions.assertEquals("Created Todo", created.title);
        Assertions.assertEquals("", created.description);
        Assertions.assertEquals(false, created.doneStatus);
    }

    @Test
    void titleIsMandatoryOnCreateTodo(){

        Payloads.TodoPayload todo = new Payloads.TodoPayload();
        todo.doneStatus=true;
        todo.description="no title";

        final Response response = Api.createTodo(todo);

        Assertions.assertEquals(400, response.getStatusCode());

        final Payloads.ErrorMessageResponse errors =
                response.body().as(Payloads.ErrorMessageResponse.class);
        Assertions.assertEquals(1, errors.errorMessages.size());
        Assertions.assertEquals("title : field is mandatory",
                errors.errorMessages.get(0));
    }


    @Test
    void canGetASpecificTodo(){

        Response response = createATodo();
        final Payloads.TodoPayload created = response.body().as(Payloads.TodoPayload.class);

        // get the todoinstance we just created
        response = RestAssured.
                get(Environment.getEnv("/todos/" + created.id));
        Assertions.assertEquals(200, response.getStatusCode());

        final Payloads.TodosPayload retrievedTodos = response.body().as(Payloads.TodosPayload.class);
        Assertions.assertEquals(1, retrievedTodos.todos.size());

        Payloads.TodoPayload retrieved = retrievedTodos.todos.get(0);
        Assertions.assertEquals("Created Todo", retrieved.title);
        Assertions.assertEquals("hello world", retrieved.description);
        Assertions.assertEquals(created.id, retrieved.id);
        Assertions.assertEquals(true, retrieved.doneStatus);
    }

    @Test
    void canDeleteASpecificTodo(){

        Response response = createATodo();
        final Payloads.TodoPayload created = response.body().as(Payloads.TodoPayload.class);

        // delete the todoinstance we just created
        response = RestAssured.
                delete(Environment.getEnv("/todos/" + created.id));
        Assertions.assertEquals(200, response.getStatusCode());

        // check it has gone
        response = RestAssured.
                get(Environment.getEnv("/todos/" + created.id));
        Assertions.assertEquals(404, response.getStatusCode());
    }



}
