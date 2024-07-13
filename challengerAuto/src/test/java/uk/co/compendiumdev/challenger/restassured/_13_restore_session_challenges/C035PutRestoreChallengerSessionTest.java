package uk.co.compendiumdev.challenger.restassured._13_restore_session_challenges;

import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.payloads.Challenger;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

public class C035PutRestoreChallengerSessionTest extends RestAssuredBaseTest {

    @Test
    public void canPutChallengerSessionToReset(){

        Assertions.assertNotNull(xChallenger);
        Assertions.assertTrue(xChallenger.length()>5);

        Response cResponse = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                get(apiPath("/challenger/" + xChallenger)).
                then().
                statusCode(200).
                contentType(ContentType.JSON).and().extract().response();

        Challenger challengerResponse = new Gson().fromJson(cResponse.body().asString(), Challenger.class);

        Assertions.assertTrue(challengerResponse.challengeStatus.CREATE_NEW_CHALLENGER);

        challengerResponse.challengeStatus.CREATE_NEW_CHALLENGER=false;

        Response nResponse = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                body(challengerResponse).
                put(apiPath("/challenger/" + xChallenger)).
                then().
                statusCode(200). // updated existing
                        contentType(ContentType.JSON).and().extract().response();

        Challenger newChallengerResponse = new Gson().fromJson(nResponse.body().asString(), Challenger.class);

        Assertions.assertFalse(newChallengerResponse.challengeStatus.CREATE_NEW_CHALLENGER);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("PUT /challenger/guid RESTORE").status,
                "challenge not passed");


        // Reset the create new challenger status back to true to allow other tests to pass

        cResponse = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                get(apiPath("/challenger/" + xChallenger)).
                then().
                statusCode(200).
                contentType(ContentType.JSON).and().extract().response();

        challengerResponse = new Gson().fromJson(cResponse.body().asString(), Challenger.class);

        challengerResponse.challengeStatus.CREATE_NEW_CHALLENGER=true;

        nResponse = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                body(challengerResponse).
                put(apiPath("/challenger/" + xChallenger)).
                then().
                statusCode(200). // updated existing
                        contentType(ContentType.JSON).and().extract().response();


    }
}
