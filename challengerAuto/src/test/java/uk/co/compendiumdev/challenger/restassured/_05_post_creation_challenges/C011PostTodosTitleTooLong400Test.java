package uk.co.compendiumdev.challenger.restassured._05_post_creation_challenges;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C011PostTodosTitleTooLong400Test extends RestAssuredBaseTest {

    @Test
    void can400NotCreateATodoWithTitleTooLong(){

        Todo createMe = new Todo();
        // max length on title is 50
        createMe.title = "*3*5*7*9*12*15*18*21*24*27*30*33*36*39*42*45*48*51*";
        createMe.description = "my description " + System.currentTimeMillis();

        RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                body(createMe).
                post(apiPath( "/todos")).
                then().
                statusCode(400).
                contentType(ContentType.JSON);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos (400) title too long").status);

    }


}