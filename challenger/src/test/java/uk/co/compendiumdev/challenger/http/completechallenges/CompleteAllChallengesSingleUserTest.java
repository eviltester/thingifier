package uk.co.compendiumdev.challenger.http.completechallenges;

public class CompleteAllChallengesSingleUserTest extends ChallengeCompleteTest{

    @Override
    public boolean getIsSinglePlayerMode(){
        return true;
    }

    @Override
    public int getNumberOfChallengesToFail(){
        // POST to retrieve session only works in multi-user
        // GET to retrieve session only works in multi-user
        return 2;
    }

}
