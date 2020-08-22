package uk.co.compendiumdev.challenger.restassured.challenges;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengeMain;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenger.restassured.api.AgainstSparkAppBaseTest;
import uk.co.compendiumdev.challenger.restassured.api.RestAssuredBaseTest;
import uk.co.compendiumdev.challenger.restassured.http.HttpMessageSender;
import uk.co.compendiumdev.challenger.restassured.http.HttpResponseDetails;
import uk.co.compendiumdev.sparkstart.Environment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ChallengeCompleteTest{

    private static Challengers challengers;
    private static ChallengerAuthData challenger;
    private static HttpMessageSender http;
    private static Map<String, String> x_challenger_header;

    @BeforeAll
    public static void createAChallengerToUse(){
        Environment.getBaseUri();
        challengers = ChallengeMain.getChallenger().getChallengers();
        challenger = challengers.createNewChallenger();

        http = new HttpMessageSender(Environment.getBaseUri());
        x_challenger_header = new HashMap<>();
        x_challenger_header.put("X-CHALLENGER", challenger.getXChallenger());
    }

    @Test
    public void canGetChallengesPass() {

        final HttpResponseDetails response =
                http.send("/challenges", "GET", x_challenger_header, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_CHALLENGES));
    }

    @Test
    public void canGetTodosPass() {

        final HttpResponseDetails response =
                http.send("/todos", "GET", x_challenger_header, "");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_TODOS));
    }

    @Test
    public void canGet404TodoPass() {

        final HttpResponseDetails response =
                http.send("/todo", "GET", x_challenger_header, "");

        Assertions.assertEquals(404, response.statusCode);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_TODOS_NOT_PLURAL_404));
    }
}



