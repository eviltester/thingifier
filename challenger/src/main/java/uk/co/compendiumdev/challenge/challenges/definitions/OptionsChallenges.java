package uk.co.compendiumdev.challenge.challenges.definitions;

import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;

public class OptionsChallenges {

    public static ChallengeDefinitionData optionsTodos200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "OPTIONS /todos (200)",
                "Issue an OPTIONS request on the `/todos` end point. You might want to manually check the 'Allow' header in the response is as expected.");

        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/options/options-todos-200");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "Ld5h1TSnXWA");

        return aChallenge;
    }
}
