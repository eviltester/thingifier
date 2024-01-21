package uk.co.compendiumdev.challenger.http.challenges;

import org.junit.jupiter.api.BeforeAll;

public class CompleteAllChallengesSingleUserTest extends ChallengeCompleteTest{

    @Override
    public boolean getIsSinglePlayerMode(){
        return true;
    }
}
