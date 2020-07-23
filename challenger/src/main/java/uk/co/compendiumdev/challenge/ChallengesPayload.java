package uk.co.compendiumdev.challenge;

import com.google.gson.Gson;
import uk.co.compendiumdev.challenge.challenges.ChallengeData;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitions;

import java.util.ArrayList;
import java.util.List;

public class ChallengesPayload {
    private final ChallengeDefinitions challenges;
    private final ChallengerAuthData challenger;

    public ChallengesPayload(final ChallengeDefinitions challenges, final ChallengerAuthData challenger) {
        this.challenges = challenges;
        this.challenger=challenger;
    }

    private class ChallengesPayloadWrapper{
        List<ChallengeData> challenges;
    }

    public String getAsJson(){
        final ChallengesPayloadWrapper payload = new ChallengesPayloadWrapper();
        payload.challenges = getAsChallenges();
        return new Gson().toJson(payload);
    }

    public List<ChallengeData> getAsChallenges(){
        List<ChallengeData> challengesList = new ArrayList<>();
        for(ChallengeData challenge : challenges.getChallenges()){
            ChallengeData challengeStatus = new ChallengeData(challenge.name, challenge.description);
            if(this.challenger!=null){
                challengeStatus.status = challenger.statusOfChallenge(challenges.getChallenge(challenge.name));
            }
            challengesList.add(challengeStatus);
        }
        return challengesList;
    }
}
