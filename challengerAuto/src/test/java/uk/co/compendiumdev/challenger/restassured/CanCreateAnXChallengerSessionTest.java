package uk.co.compendiumdev.challenger.restassured;

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

public class CanCreateAnXChallengerSessionTest extends RestAssuredBaseTest {

    @Test
    public void xChallengerSessionCreatedForTests(){

        // POST /challenger is in the @BeforeAll of the RestAssuredBaseTest
        // and tracked invisibly to the tests with the xChallenger variable

        Assertions.assertNotNull(xChallenger);
        Assertions.assertTrue(xChallenger.length()>5);

        ChallengesStatus statuses = new ChallengesStatus();
        statuses.get();
        Assertions.assertTrue(statuses.getChallengeNamed("POST /challenger (201)").status,
                            "challenge not passed");
    }

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
    }

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


        ChallengesStatus statuses = new ChallengesStatus();
        statuses.getFor(aNewGuid);
        if(!singleUserMode) {
            Assertions.assertTrue(statuses.getChallengeNamed("PUT /challenger/guid CREATE").status,
                    "challenge not passed");
        }
    }
}
