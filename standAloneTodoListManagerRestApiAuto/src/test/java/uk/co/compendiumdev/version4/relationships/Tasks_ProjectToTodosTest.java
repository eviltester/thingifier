package uk.co.compendiumdev.version4.relationships;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.sparkstart.Environment;
import uk.co.compendiumdev.version4.api.Api;
import uk.co.compendiumdev.version4.api.Payloads;

public class Tasks_ProjectToTodosTest {

    @Test
    void createTaskRelationshipUsingUrl(){

        Payloads.ProjectPayload project = new Payloads.ProjectPayload();
        final Response projectCreation = Api.createProject(project);

        Payloads.TodoPayload todo = new Payloads.TodoPayload();
        todo.title="do this";
        final Response todoCreation = Api.createTodo(todo);

        Assertions.assertEquals(201, projectCreation.getStatusCode());
        Assertions.assertEquals(201, todoCreation.getStatusCode());

        Payloads.ProjectPayload createdProject =
                projectCreation.body().as(Payloads.ProjectPayload.class);

        Payloads.TodoPayload createdTodo =
                todoCreation.body().as(Payloads.TodoPayload.class);

        //{"id":"%s"}
        final Response crossRefResponse = RestAssured.
                given().
                contentType(ContentType.JSON).
                body(String.format("{\"id\":\"%s\"}",
                        createdTodo.id)).when().
                post(Environment.getEnv(
                        String.format("/projects/%s/tasks", createdProject.id))).
                andReturn();

        Assertions.assertEquals(201, crossRefResponse.getStatusCode());

        // check project is cross referenced to todoinstance
        final Response checkProject =
                RestAssured.get(Environment.getEnv("/projects/" + createdProject.id));

        Payloads.ProjectsPayload projects = checkProject.body().as(Payloads.ProjectsPayload.class);
        Assertions.assertEquals(createdTodo.id, projects.projects.get(0).tasks.get(0).id);

        // check project is cross referenced to todoinstance
        final Response checkTodo =
                RestAssured.get(Environment.getEnv("/todos/" + createdTodo.id));

        Payloads.TodosPayload todos = checkTodo.body().as(Payloads.TodosPayload.class);
        Assertions.assertEquals(createdProject.id, todos.todos.get(0).tasksof.get(0).id);
    }


    @Test
    void createTaskRelationshipUsingComplexField(){

        Payloads.ProjectPayload project = new Payloads.ProjectPayload();
        final Response projectCreation = Api.createProject(project);

        Payloads.TodoPayload todo = new Payloads.TodoPayload();
        todo.title="do this";
        final Response todoCreation = Api.createTodo(todo);

        Assertions.assertEquals(201, projectCreation.getStatusCode());
        Assertions.assertEquals(201, todoCreation.getStatusCode());

        Payloads.ProjectPayload createdProject =
                projectCreation.body().as(Payloads.ProjectPayload.class);

        Payloads.TodoPayload createdTodo =
                todoCreation.body().as(Payloads.TodoPayload.class);

        //{"tasks": [{"id":"%s"}]}
        final Response crossRefResponse = RestAssured.
                given().
                contentType(ContentType.JSON).
                body(String.format("{\"tasks\": [{\"id\":\"%s\"}]}",
                        createdTodo.id)).when().
                post(Environment.getEnv(
                        String.format("/projects/%s", createdProject.id))).
                andReturn();

        Assertions.assertEquals(200, crossRefResponse.getStatusCode());

        // check project is cross referenced to todoinstance
        final Response checkProject =
                RestAssured.get(Environment.getEnv("/projects/" + createdProject.id));

        Payloads.ProjectsPayload projects = checkProject.body().as(Payloads.ProjectsPayload.class);
        Assertions.assertEquals(createdTodo.id, projects.projects.get(0).tasks.get(0).id);

        // check project is cross referenced to todoinstance
        final Response checkTodo =
                RestAssured.get(Environment.getEnv("/todos/" + createdTodo.id));

        Payloads.TodosPayload todos = checkTodo.body().as(Payloads.TodosPayload.class);
        Assertions.assertEquals(createdProject.id, todos.todos.get(0).tasksof.get(0).id);
    }
}
