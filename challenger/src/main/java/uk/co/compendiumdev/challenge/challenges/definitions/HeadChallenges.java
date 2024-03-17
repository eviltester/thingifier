package uk.co.compendiumdev.challenge.challenges.definitions;

import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;

public class HeadChallenges {

    public static ChallengeDefinitionData headTodos200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "HEAD /todos (200)",
                "Issue a HEAD request on the `/todos` end point");

        aChallenge.addSolutionLink("Read Solution", "HREF", "/apichallenges/solutions/head/head-todos-200");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "zKbytTelP84");        return aChallenge;
    }

}
