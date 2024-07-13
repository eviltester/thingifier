package uk.co.compendiumdev.challenger.restassured._05_post_creation_challenges;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Todo;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C012PostTodosDescriptionTooLong400Test extends RestAssuredBaseTest {

    @Test
    void can400NotCreateATodoWithDescriptionTooLing(){

        Todo createMe = new Todo();
        createMe.title = "just right";
        // max length on title is 200
        createMe.description = "*3*5*7*10*13*16*19*22*25*28*31*34*37*40*43*46*49*" +
                "52*55*58*61*64*67*70*73*76*79*82*85*88*91*94*97*101*105*109*113*" +
                "117*121*125*129*133*137*141*145*149*153*157*" +
                "161*165*169*173*177*181*185*189*193*197*201*";

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
        Assertions.assertTrue(statuses.getChallengeNamed("POST /todos (400) description too long").status);
    }


}