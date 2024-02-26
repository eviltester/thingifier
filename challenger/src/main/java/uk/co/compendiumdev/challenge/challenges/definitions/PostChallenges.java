package uk.co.compendiumdev.challenge.challenges.definitions;

import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;

public class PostChallenges {


    // TODO: create solution and video for Post all
    public static ChallengeDefinitionData postAllTodos201(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /todos (201) all",
                "Issue as many POST requests as it takes to add the maximum number of TODOS allowed for a user. The maximum number should be listed in the documentation."
        );
        return aChallenge;
    }
}
