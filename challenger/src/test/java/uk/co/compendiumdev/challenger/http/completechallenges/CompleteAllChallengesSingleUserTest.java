package uk.co.compendiumdev.challenger.http.completechallenges;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;

public class CompleteAllChallengesSingleUserTest extends ChallengeCompleteTest{

    @Override
    public boolean getIsSinglePlayerMode(){
        return true;
    }

    @Override
    public int getNumberOfChallengesToFail(){
        // POST to retrieve session only works in multi-user - but is excluded in challenges
        // GET to retrieve session only works in multi-user - but is excluded in challenges
        // PUT new restored challenger progress only works in multi-user - but is excluded in challenges
        return 0;
    }

    @Test
    void canSimulateCreateChallenger() throws InterruptedException {

        final HttpResponseDetails response = http.post("/challenger", "");

        Assertions.assertEquals(Challengers.SINGLE_PLAYER_GUID,
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
