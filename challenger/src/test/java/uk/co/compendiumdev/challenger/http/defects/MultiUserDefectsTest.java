package uk.co.compendiumdev.challenger.http.defects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;
import uk.co.compendiumdev.challenger.http.httpclient.HttpMessageSender;
import uk.co.compendiumdev.sparkstart.Environment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MultiUserDefectsTest {

    @BeforeAll
    public static void controlEnv(){
        Environment.stop();
    }

    @Test
    public void challengerNotLoadedWhenSkipCallToGetChallengers() throws IOException {

        Environment.stop();

        //have challenger data predefined but not in memory
        // 2ce954c6-caa1-4299-85af-205a9d9f7867.data
        String notLoadedChallengerGUID = "2ce954c6-caa1-4299-85af-205a9d9f7867";
        final File resourceFile = new File(System.getProperty("user.dir") , "/src/test/resources/" + notLoadedChallengerGUID + ".data.txt");
        final File folder = new File(System.getProperty("user.dir"), "challengersessions");
        folder.mkdirs();
        final File dataFile = new File(folder , notLoadedChallengerGUID + ".data.txt");

        if(!resourceFile.exists()){
            Assertions.fail("Could not find data file " + notLoadedChallengerGUID);
        }

        if(dataFile.exists()){
            dataFile.delete();
        }

        Files.copy(resourceFile.toPath(), dataFile.toPath());

        final HttpMessageSender http = new HttpMessageSender(Environment.getBaseUri(false, true));
        http.setHeader("X-CHALLENGER", notLoadedChallengerGUID);
        http.setHeader("Authorization", "basic YWRtaW46cGFzc3dvcmQ="); // admin:password

        final HttpResponseDetails response = http.send("/secret/token", "POST");
        Assertions.assertEquals(201, response.statusCode);

        Environment.stop();
    }

    @Test
    public void inMultiPlayerModeNoXChallengerHeaderPostTokenCausedNullPointer(){

        // in single player mode this test will 201 because we don't need challenger header
        int expectedResponse=401;
//        if(Environment.SINGLE_PLAYER_MODE){
//            expectedResponse=201;
//        }

        final HttpMessageSender http = new HttpMessageSender(Environment.getBaseUri(false));
        http.setHeader("Authorization", "basic YWRtaW46cGFzc3dvcmQ="); // admin:password

        final HttpResponseDetails response = http.send("/secret/token", "POST");
        Assertions.assertEquals(expectedResponse, response.statusCode);
    }

    @Test
    public void inMultiPlayerModeNoXChallengerHeaderGetNoteCausedNullPointer(){

        final HttpMessageSender http = new HttpMessageSender(Environment.getBaseUri(false));
        http.setHeader("Authorization", "basic YWRtaW46cGFzc3dvcmQ="); // admin:password

        final HttpResponseDetails response = http.send("/secret/note", "GET");
        Assertions.assertEquals(401, response.statusCode);
    }

    @Test
    public void inMultiPlayerModeNoXChallengerHeaderPostNoteCausedNullPointer(){

        final HttpMessageSender http = new HttpMessageSender(Environment.getBaseUri(false));
        http.setHeader("Authorization", "basic YWRtaW46cGFzc3dvcmQ="); // admin:password
        http.setHeader("Content-Type","application/json");

        final HttpResponseDetails response = http.send("/secret/note", "POST");
        Assertions.assertEquals(401, response.statusCode);
    }

    @Test
    public void getChallengesShouldReturnChallengesNotAnError() {

        final HttpMessageSender http = new HttpMessageSender(Environment.getBaseUri(false));
        http.setHeader("x-challenger", "idonotexist");

        final HttpResponseDetails response = http.send("/challenges", "GET");

        Assertions.assertEquals(200, response.statusCode);
        Assertions.assertTrue(
                response.body.contains("POST /challenger (201)"),
                "Expected challenge information in response");

    }

    @AfterAll
    public static void stopEnv(){
        Environment.stop();
    }

}



