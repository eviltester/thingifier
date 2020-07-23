package uk.co.compendiumdev.challenger.restassured.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.payloads.Todos;
import uk.co.compendiumdev.sparkstart.Environment;

import java.util.List;

import static uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest.xChallenger;

public class TodosApi {
    public Todo createTodo(final String title,
                           final String description,
                           final boolean doneStatus) {

        Todo payload = new Todo();
        payload.id =null;
        payload.title = title;
        payload.description = description;
        payload.doneStatus = doneStatus;

        String postTo = Environment.getEnv("/todos");

        final Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                body(payload).
                post(postTo).
                then().
                statusCode(201).
                contentType(ContentType.JSON).
                extract().response();

        return response.body().as(Todo.class);
    }

    public List<Todo> getTodos() {
        String todosEndPoint = Environment.getEnv("/todos");

        Todos todosList = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                get(todosEndPoint).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                extract().response().body().as(Todos.class);

        return todosList.todos;
    }

    public Todo getTodo(final String id) {
        String todosEndPoint = Environment.getEnv("/todos");

        Todos todos = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                get(todosEndPoint + "/"  + id).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().body().
                as(Todos.class);

        return todos.todos.get(0);
    }
}
