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

    /*
        UPDATE TODO CHALLENGES
     */

    public static ChallengeDefinitionData putTodosIdFull200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "PUT /todos/{id} full (200)",
                "Issue a PUT request to update an existing todo with a complete payload i.e. title, description and donestatus.");

        // todo: create solution for PUT todos full 200 challenge
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-201");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "T0LFHwavsNA");
        return aChallenge;
    }

    public static ChallengeDefinitionData putTodosIdPartial200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "PUT /todos/{id} partial (200)",
                "Issue a PUT request to update an existing todo with just mandatory items in payload i.e. title.");

        // todo: create solution for PUT todos partial 200 challenge
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-201");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "T0LFHwavsNA");
        return aChallenge;
    }

    public static ChallengeDefinitionData putTodosIdNoTitle400(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "PUT /todos/{id} no title (400)",
                "Issue a PUT request to fail to update an existing todo because title is missing in payload.");

        // todo: create solution for PUT todos partial 200 challenge
        aChallenge.addHint("Title is required for Put requests because they are idempotent. You can amend using POST without a title, but not using a PUT.");
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-201");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "T0LFHwavsNA");
        // TODO: add solution text to summarise solution
        return aChallenge;
    }

    public static ChallengeDefinitionData putTodosIdNonMatchedIdsAmend400(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "PUT /todos/{id} no amend id (400)",
                "Issue a PUT request to fail to update an existing todo because id different in payload.");

        // todo: create solution for PUT todos partial 200 challenge
        aChallenge.addHint("ID is auto generated you can not amend it in the payload.");
        aChallenge.addHint("If you have a different id in the payload from the url then this is viewed as an amendment and you can not amend an auto generated field.");
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-201");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "T0LFHwavsNA");
        // TODO: add solution text to summarise solution
        return aChallenge;
    }

}
