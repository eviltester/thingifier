package uk.co.compendiumdev.challenge.challenges.definitions;

import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;

public class PutChallenges {

    public static ChallengeDefinitionData putTodosId400(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "PUT /todos/{id} (400)",
                "Issue a PUT request to unsuccessfully create a todo");

        // todo: create solution for PUT todos 400 challenge
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-201");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "T0LFHwavsNA");
        return aChallenge;
    }

}
