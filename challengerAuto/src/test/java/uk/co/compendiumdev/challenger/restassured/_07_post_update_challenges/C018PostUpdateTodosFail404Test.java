package uk.co.compendiumdev.challenger.restassured._07_post_update_challenges;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.ErrorMessages;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.payloads.Todos;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;
import uk.co.compendiumdev.challenger.restassured.api.TodosApi;

import java.util.List;

public class C018PostUpdateTodosFail404Test extends RestAssuredBaseTest {

    @Test
    void canNotUpdateANonExistantTodoWithPost(){

        TodosApi api = new TodosApi();
        List<Todo> todos = api.getTodos();

        int maxId = 0;
        for(Todo aTodo : todos){
            if(aTodo.id>maxId){
                maxId=aTodo.id;
            }
        }


        Todo updatedDetails = new Todo();
        updatedDetails.id = maxId+1;
        updatedDetails.title = "Title Updated " + System.currentTimeMillis();
        updatedDetails.description = "Description Updated " + System.currentTimeMillis();
        updatedDetails.doneStatus=true;

        final Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                body(updatedDetails).
                post(apiPath("/todos/" + updatedDetails.id)).
                then().
                statusCode(404).
                contentType(ContentType.JSON).
                extract().response();

        ErrorMessages errors = response.body().as(ErrorMessages.class);

        List<Todo> todosCheck = api.getTodos();

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos/{id} (404)").status);

        // check it reported as updated in the details of the response
        Assertions.assertEquals(String.format("No such todo entity instance with id == %d found", updatedDetails.id), errors.errorMessages.get(0));

        // and did not add any
        Assertions.assertEquals(todos.size(), todosCheck.size());
    }
}
