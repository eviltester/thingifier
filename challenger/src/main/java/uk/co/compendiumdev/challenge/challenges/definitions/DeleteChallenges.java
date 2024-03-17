package uk.co.compendiumdev.challenge.challenges.definitions;

import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;

public class DeleteChallenges {

    public static ChallengeDefinitionData deleteTodosId200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "DELETE /todos/{id} (200)",
                "Issue a DELETE request to successfully delete a todo");

        aChallenge.addHint("Make sure you don't use {id} in the url, replace that with the id of a todo e.g. /todos/1");
        aChallenge.addHint("Make sure a todo with the id exists prior to issuing the request");
        aChallenge.addHint("Check it was deleted by issuing a GET or HEAD on the /todos/{id}");
        aChallenge.addSolutionLink("Read Solution", "HREF", "/apichallenges/solutions/delete/delete-todos-id-200");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "6MXTkaXn9qU");

        return aChallenge;
    }
}
