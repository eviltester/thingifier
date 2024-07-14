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

import java.util.UUID;

public class C036PutCreateNewChallengerSessionTest extends RestAssuredBaseTest {

    @Test
    public void canPutChallengerSessionToCreate(){

        Assertions.assertNotNull(xChallenger);
        Assertions.assertTrue(xChallenger.length()>5);

        Response aresponse = RestAssured.
                given().
                header("X-CHALLENGER", xChallenger).
                accept("application/json").
                get(apiPath("/challenger/" + xChallenger)).
                then().
                statusCode(200).
                contentType(ContentType.JSON).and().extract().response();

        // use Gson to handle case of missing challenges from single user mode
        Challenger challengerResponse = new Gson().fromJson(aresponse.body().asString(), Challenger.class);

        String aNewGuid = UUID.randomUUID().toString();

        int expectedStatusCode = 201; // 201 if multi user mode
        boolean singleUserMode = challengerResponse.xChallenger.equals("rest-api-challenges-single-player");
        if(singleUserMode){
            expectedStatusCode = 200; // we can only amend in single user mode
        }

        challengerResponse.xChallenger=aNewGuid;


        // create it with PUT using mostly previous values
        RestAssured.
                given().
                header("X-CHALLENGER", aNewGuid).
                accept("application/json").
                body(challengerResponse).
                put(apiPath("/challenger/" + aNewGuid)).
                then().
                statusCode(expectedStatusCode). // updated existing
                contentType(ContentType.JSON);


        /*
            This challenge can only be tracked in multi-user mode
            if it is the first challenge to be completed
            because it would be tracked against the 'new'
            Challenger, and not the current challenger.
         */
        ChallengesStatus statuses = new ChallengesStatus();
        statuses.getFor(aNewGuid);
        if(!singleUserMode) {
            Assertions.assertTrue(statuses.getChallengeNamed("PUT /challenger/guid CREATE").status,
                    "challenge not passed");
        }
    }


}
