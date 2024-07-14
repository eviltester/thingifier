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

public class C034GetChallengerSessionForRestoringTest extends RestAssuredBaseTest {

    @Test
    public void canGetChallengerSession(){

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

        Assertions.assertTrue(challengerResponse.challengeStatus.GET_RESTORABLE_CHALLENGER_PROGRESS_STATUS);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("GET /challenger/guid (existing X-CHALLENGER)").status,
                "challenge not passed");

    }
}
