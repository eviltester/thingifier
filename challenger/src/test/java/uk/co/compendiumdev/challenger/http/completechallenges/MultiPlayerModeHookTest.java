package uk.co.compendiumdev.challenger.http.completechallenges;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.compendiumdev.challenger.http.http.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.http.HttpResponseDetails;
import uk.co.compendiumdev.sparkstart.Environment;

public class MultiPlayerModeHookTest {


    @BeforeAll
    public static void controlEnv(){
        Environment.stop();
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "DELETE"})
    public void inMultiPlayerModeNoXChallengerHeaderAmendmentVerbsShould401(String verb){

        int expectedResponse=401;

        // force multi-player mode for these tests
        final HttpMessageSender http = new HttpMessageSender(Environment.getBaseUri(false));

        final HttpResponseDetails response = http.send("/todos/1", verb);
        Assertions.assertEquals(expectedResponse, response.statusCode);
        Assertions.assertTrue(response.body.contains("Cannot amend details."));
    }

    @AfterAll
    public static void stopEnv(){
        Environment.stop();
    }

}
