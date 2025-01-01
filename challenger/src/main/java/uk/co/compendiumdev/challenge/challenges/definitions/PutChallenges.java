package uk.co.compendiumdev.challenge.challenges.definitions;

import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;

public class PutChallenges {

    public static ChallengeDefinitionData putTodosId400(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "PUT /todos/{id} (400)",
                "Issue a PUT request to unsuccessfully create a todo");

        aChallenge.addHint("Add a JSON payload in the request", "");
        aChallenge.addHint("If you don't know the format of the payload, use the response from a GET /todos/{id} request and amend it", "");
        aChallenge.addHint("Do not include an 'id' in the payload", "");
        aChallenge.addHint("The id in the URL should not exist", "");
        aChallenge.addHint("You must add an X-CHALLENGER header for a valid session", "");

        aChallenge.addSolutionLink("Send a PUT request to /todos/{id} with a valid creation payload", "", "");
        aChallenge.addSolutionLink("Read Solution", "HREF", "/apichallenges/solutions/put-create/put-todos-400-create");

        // todo: create video solution for PUT todos 400 challenge
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

        aChallenge.addHint("Add a JSON payload in the request", "");
        aChallenge.addHint("If you don't know the format of the payload, use the response from a GET /todos/{id} request and amend it", "");
        aChallenge.addHint("Do not include an 'id' in the payload", "");


        aChallenge.addSolutionLink("Send a PUT request to /todos/{id} with a full payload. Do not attempt to change the id.", "", "");
        aChallenge.addSolutionLink("Read Solution", "HREF", "/apichallenges/solutions/put-update/put-todos-id-200-update-full");

        // todo: create solution video for PUT todos full 200 challenge
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "T0LFHwavsNA");
        return aChallenge;
    }

    public static ChallengeDefinitionData putTodosIdPartial200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "PUT /todos/{id} partial (200)",
                "Issue a PUT request to update an existing todo with just mandatory items in payload i.e. title.");

        aChallenge.addHint("Add a JSON payload in the request", "");
        aChallenge.addHint("If you don't know the format of the payload, use the response from a GET /todos/{id} request and amend it", "");
        aChallenge.addHint("Do not include an 'id' in the payload", "");

        aChallenge.addSolutionLink("Send a PUT request to /todos/{id} with a partial payload. Mandatory field title must be included.", "", "");
        aChallenge.addSolutionLink("Read Solution", "HREF", "/apichallenges/solutions/put-update/put-todos-id-200-update-partial");

        // todo: create solution video for PUT todos partial 200 challenge
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "T0LFHwavsNA");
        return aChallenge;
    }

    public static ChallengeDefinitionData putTodosIdNoTitle400(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "PUT /todos/{id} no title (400)",
                "Issue a PUT request to fail to update an existing todo because title is missing in payload.");


        aChallenge.addHint("Title is required for Put requests because they are idempotent. You can amend using POST without a title, but not using a PUT.");

        aChallenge.addSolutionLink("Send a PUT request to /todos/{id} without a title field in the payload.", "", "");
        aChallenge.addSolutionLink("Read Solution", "HREF", "/apichallenges/solutions/put-update/put-todos-id-400-no-title");

        // todo: create solution video for PUT todos partial 200 challenge

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


        aChallenge.addSolutionLink("Send a PUT request to /todos/{id} with a different id in the url than in the payload.", "", "");
        aChallenge.addSolutionLink("Read Solution", "HREF", "/apichallenges/solutions/put-update/put-todos-id-400-no-amend-id");

        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "T0LFHwavsNA");
        // TODO: add solution video for put totos id no amend id
        return aChallenge;
    }

}
