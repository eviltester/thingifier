package uk.co.compendiumdev.challenger.http.defects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;
import uk.co.compendiumdev.sparkstart.Environment;

public class WhenXChallengerDoesNotExistTest {

    @Test
    public void getChallengesShouldReturnChallengesNotAnError() {

        final HttpMessageSender http = new HttpMessageSender(Environment.getBaseUri());
        http.setHeader("x-challenger", "idonotexist");

        final HttpResponseDetails response = http.send("/challenges", "GET");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(
                        response.body.contains("POST /challenger (201)"),
                "Expected challenge information in response");

    }
}
