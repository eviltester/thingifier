package uk.co.compendiumdev.challenger.restassured;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.restassured.api.ChallengesStatus;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;

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
}
