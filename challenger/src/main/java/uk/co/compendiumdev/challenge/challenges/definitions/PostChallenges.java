package uk.co.compendiumdev.challenge.challenges.definitions;

import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;

public class PostChallenges {


    /*
        CHALLENGER
     */
    // TODO: create solution and video for Post all
    public static ChallengeDefinitionData postAllTodos201(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /todos (201) all",
                "Issue as many POST requests as it takes to add the maximum number of TODOS allowed for a user. The maximum number should be listed in the documentation."
        );
        return aChallenge;
    }

    /*
        CREATE TODOS
     */
    public static ChallengeDefinitionData postTodos201(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /todos (201)",
                "Issue a POST request to successfully create a todo");

        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-201");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "T0LFHwavsNA");
        return aChallenge;
    }

    public static ChallengeDefinitionData postTodosTitleTooLong400(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /todos (400) title too long",
                "Issue a POST request to create a todo but fail length validation on the `title` field because your title exceeds maximum allowable characters.");

//        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-400");
//        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "tlye5bQ72g0");
        // TODO: create solution for failing title too long

        return aChallenge;
    }

    public static ChallengeDefinitionData postTodosBadDoneStatus400(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /todos (400) doneStatus",
                "Issue a POST request to create a todo but fail validation on the `doneStatus` field");

        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-400");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "tlye5bQ72g0");
        return aChallenge;
    }

    public static ChallengeDefinitionData postTodosDescriptionTooLong400(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /todos (400) description too long",
                "Issue a POST request to create a todo but fail length validation on the `description` because your description exceeds maximum allowable characters.");

//        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-400");
//        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "tlye5bQ72g0");
        // TODO: create solution for failing description too long
        return aChallenge;
    }

    public static ChallengeDefinitionData postTodosMaxTitleDescriptionTooLong400(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /todos (201) max out content",
                "Issue a POST request to create a todo with maximum length title and description fields.");

        aChallenge.addHint("Max lengths are listed in the API Documentation");
        aChallenge.addHint("CounterStrings are very useful for testing with maximum field lengths");
//        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-400");
//        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "tlye5bQ72g0");
        // TODO: create solution for max out title and description
        return aChallenge;
    }

    public static ChallengeDefinitionData postTodosPayloadTooLong400(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /todos (413) content too long",
                "Issue a POST request to create a todo but fail payload length validation on the `description` because your whole payload exceeds maximum allowable 5000 characters.");

        aChallenge.addHint("Try using a long 5000 char string as the description or title text");
//        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-400");
//        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "tlye5bQ72g0");
        // TODO: create solution for failing content too long
        return aChallenge;
    }

    public static ChallengeDefinitionData postTodosInvalidExtraField400(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /todos (400) extra",
                "Issue a POST request to create a todo but fail validation because your payload contains an unrecognised field.");

        aChallenge.addHint("Try to create a todo with a title, description and a priority");
//        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-400");
//        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "tlye5bQ72g0");
        // TODO: create solution for unrecognised field names
        return aChallenge;
    }

    /*
        UPDATE TODOs
     */


    public static ChallengeDefinitionData postTodosId200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /todos/{id} (200)",
                "Issue a POST request to successfully update a todo");

        aChallenge.addHint("Make sure you don't use {id} in the url, replace that with the id of a todo e.g. /todos/1");
        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-id-200");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "feXdRpZ_tgs");
        return aChallenge;
    }

    public static ChallengeDefinitionData postTodosId404(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /todos/{id} (404)",
                "Issue a POST request for a todo which does not exist. Expect to receive a 404 response.");

        aChallenge.addHint("Make sure you don't use {id} in the url, replace that with the id of a todo that does not exist e.g. /todos/100");
        // todo add solution and hints for POST 404
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-id-200");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "feXdRpZ_tgs");       return aChallenge;
        return aChallenge;
    }

}
