package uk.co.compendiumdev.challenge.challenges.definitions;

import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;

public class MiscChallenges {

    // TODO: create solution and video for Post all
    public static ChallengeDefinitionData postAllTodos201(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /todos (201) all",
                "Issue as many POST requests as it takes to add the maximum number of TODOS allowed for a user. The maximum number should be listed in the documentation."
        );
        return aChallenge;
    }

    public static ChallengeDefinitionData deleteAllTodos200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "DELETE /todos/{id} (200) all",
                "Issue a DELETE request to successfully delete the last todo in system so that there are no more todos in the system");

        aChallenge.addHint("After deleting the last todo, there will be no todos left in the application");
        aChallenge.addHint("Make sure you don't use {id} in the url, replace that with the id of a todo e.g. /todos/1");
        aChallenge.addHint("You have to delete all the todo items in the system to complete this challenge");

        return aChallenge;
    }
}
