package uk.co.compendiumdev.challenger.restassured._12_contenttype_challenges;

import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C033PostTodosUnsupportedContentType415Test extends RestAssuredBaseTest {

    @Test
    void can415CreateATodoWithInvalidContentType(){

        Todo todo = new Todo();
        todo.doneStatus = true;
        todo.title = "invalid content type";
        todo.description = "invalid content";

        String payload = new Gson().toJson(todo);

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType(ContentType.BINARY).
                body(payload.getBytes()).
                post(apiPath( "/todos")).
                then().
                statusCode(415).
                contentType(ContentType.JSON);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos (415)").status);

    }
}
