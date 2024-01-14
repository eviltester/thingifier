package uk.co.compendiumdev.challenger.http.challenges;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.compendiumdev.challenger.http.http.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.http.HttpResponseDetails;
import uk.co.compendiumdev.sparkstart.Environment;

public class MultiPlayerModeHookTest {

    // todo: force multi-player mode for these tests
    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "DELETE"})
    public void inMultiPlayerModeNoXChallengerHeaderAmendmentVerbsShould401(String verb){

        int expectedResponse=401;

        final HttpMessageSender http = new HttpMessageSender(Environment.getBaseUri());

        final HttpResponseDetails response = http.send("/todos/1", verb);
        Assertions.assertEquals(expectedResponse, response.statusCode);
        Assertions.assertTrue(response.body.contains("Cannot amend details."));
    }

}
