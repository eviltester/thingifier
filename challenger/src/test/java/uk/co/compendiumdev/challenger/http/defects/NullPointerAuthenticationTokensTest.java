package uk.co.compendiumdev.challenger.http.defects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.http.http.HttpResponseDetails;
import uk.co.compendiumdev.challenger.http.http.HttpMessageSender;
import uk.co.compendiumdev.sparkstart.Environment;

public class NullPointerAuthenticationTokensTest{

    // todo: force multi-player mode for these tests
    @Test
    public void inMultiPlayerModeNoXChallengerHeaderPostTokenCausedNullPointer(){

        // in single player mode this test will 201 because we don't need challenger header
        int expectedResponse=401;
//        if(Environment.SINGLE_PLAYER_MODE){
//            expectedResponse=201;
//        }

        final HttpMessageSender http = new HttpMessageSender(Environment.getBaseUri());
        http.setHeader("Authorization", "basic YWRtaW46cGFzc3dvcmQ="); // admin:password

        final HttpResponseDetails response = http.send("/secret/token", "POST");
        Assertions.assertEquals(expectedResponse, response.statusCode);
    }

    @Test
    public void inMultiPlayerModeNoXChallengerHeaderGetNoteCausedNullPointer(){

        final HttpMessageSender http = new HttpMessageSender(Environment.getBaseUri());
        http.setHeader("Authorization", "basic YWRtaW46cGFzc3dvcmQ="); // admin:password

        final HttpResponseDetails response = http.send("/secret/note", "GET");
        Assertions.assertEquals(401, response.statusCode);
    }

    @Test
    public void inMultiPlayerModeNoXChallengerHeaderPostNoteCausedNullPointer(){

        final HttpMessageSender http = new HttpMessageSender(Environment.getBaseUri());
        http.setHeader("Authorization", "basic YWRtaW46cGFzc3dvcmQ="); // admin:password

        final HttpResponseDetails response = http.send("/secret/note", "POST");
        Assertions.assertEquals(401, response.statusCode);
    }


}



