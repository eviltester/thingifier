package uk.co.compendiumdev.challenger.restassured._05_post_creation_challenges;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C013PostTodosMaxContent201Test extends RestAssuredBaseTest {

    @Test
    void canCreateATodoWithMaxTitleAndDescriptionLengths(){

        Todo todo = new Todo();
        todo.doneStatus = true;
        todo.title = "2*4*6*8*11*14*17*20*23*26*29*32*35*38*41*44*47*50*";
        todo.description =
                "*3*5*7*9*12*15*18*21*24*27*30*33*36*39*42*45*48*51*" +
                        "54*57*60*63*66*69*72*75*78*81*84*87*90*93*96*100*" +
                        "104*108*112*116*120*124*128*132*136*140*144*148*" +
                        "152*156*160*164*168*172*176*180*184*188*192*196*200*";

        Response response = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                contentType("application/json").
                body(todo).
                post(apiPath("/todos")).
                then().
                statusCode(201).
                contentType(ContentType.JSON).
                and().extract().response();

        Todo createdTodo = response.body().as(Todo.class);

        Assertions.assertEquals(todo.title, createdTodo.title);
        Assertions.assertEquals(todo.description, createdTodo.description);

        Assertions.assertEquals(50, createdTodo.title.length());
        Assertions.assertEquals(200, createdTodo.description.length());

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos (201) max out content").status);

    }


}