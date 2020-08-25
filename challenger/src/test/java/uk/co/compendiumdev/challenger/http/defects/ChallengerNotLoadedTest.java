package uk.co.compendiumdev.challenger.http.defects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenger.http.http.HttpResponseDetails;
import uk.co.compendiumdev.challenger.http.http.HttpMessageSender;
import uk.co.compendiumdev.sparkstart.Environment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ChallengerNotLoadedTest{

    @Test
    public void challengerNotLoadedWhenSkipCallToGetChallengers() throws IOException {

        //have challenger data predefined but not in memory
        // 2ce954c6-caa1-4299-85af-205a9d9f7867.data
        String notLoadedChallengerGUID = "2ce954c6-caa1-4299-85af-205a9d9f7867";
        final File resourceFile = new File(System.getProperty("user.dir") , "/src/test/resources/" + notLoadedChallengerGUID + ".data.txt");
        final File dataFile = new File(System.getProperty("user.dir") , notLoadedChallengerGUID + ".data.txt");

        if(!resourceFile.exists()){
            Assertions.fail("Could not find data file " + notLoadedChallengerGUID);
        }

        if(dataFile.exists()){
            dataFile.delete();
        }

        Files.copy(resourceFile.toPath(), dataFile.toPath());

        final HttpMessageSender http = new HttpMessageSender(Environment.getBaseUri());
        http.setHeader("X-CHALLENGER", notLoadedChallengerGUID);
        http.setHeader("Authorization", "basic YWRtaW46cGFzc3dvcmQ="); // admin:password

        final HttpResponseDetails response = http.send("/secret/token", "POST");
        Assertions.assertEquals(201, response.statusCode);
    }


}



