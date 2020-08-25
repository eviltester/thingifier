package uk.co.compendiumdev.challenge.challengesrouting;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.Spark;
import uk.co.compendiumdev.challenge.ChallengeMain;
import uk.co.compendiumdev.challenger.http.http.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.http.HttpResponseDetails;
import uk.co.compendiumdev.sparkstart.Environment;

public class SinglePlayerTest {


    static String singlePlayerGuid;
    private static HttpMessageSender http;

    @BeforeAll
    static void startInSinglePlayer(){

        if(!Environment.SINGLE_PLAYER_MODE){
            Spark.stop();
            Spark.awaitStop();
            ChallengeMain.stop();

            Environment.SINGLE_PLAYER_MODE=true;
            Environment.getBaseUri();
        }
        singlePlayerGuid = ChallengeMain.getChallenger().
                getChallengers().SINGLE_PLAYER_GUID;

        http = new HttpMessageSender(Environment.getBaseUri());
    }

    @AfterAll
    static void stopSinglePlayerServer(){
        Environment.SINGLE_PLAYER_MODE=false;
        Spark.stop();
        Spark.awaitStop();
        ChallengeMain.stop();
    }

    @Test
    void canSimulateCreateChallenger() throws InterruptedException {

        final HttpResponseDetails response = http.post("/challenger", "");

        Assertions.assertEquals(singlePlayerGuid,
                response.getHeader("X-CHALLENGER"));
        Assertions.assertEquals(201, response.statusCode);
    }

    @Test
    void getChallengesHasDifferentLocationRouteThanMultiPlayer() throws InterruptedException {

        final HttpResponseDetails response = http.get("/challenges");

        Assertions.assertEquals("/gui/challenges",
                response.getHeader("Location"));
    }
}


