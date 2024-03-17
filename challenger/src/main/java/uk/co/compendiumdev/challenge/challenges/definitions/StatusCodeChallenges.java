package uk.co.compendiumdev.challenge.challenges.definitions;

import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;

public class StatusCodeChallenges {

    // extra status code challenges
    //      method not allowed - 405
    public static ChallengeDefinitionData methodNotAllowed405UsingDelete(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "DELETE /heartbeat (405)",
                "Issue a DELETE request on the `/heartbeat` end point and receive 405 (Method Not Allowed)");

        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/status-codes/status-codes-405-500-501-204");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");
        return aChallenge;
    }

    // cannot process request server error 500
    public static ChallengeDefinitionData serverError500UsingPatch(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "PATCH /heartbeat (500)",
                "Issue a PATCH request on the `/heartbeat` end point and receive 500 (internal server error)");

        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/status-codes/status-codes-405-500-501-204");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");
        return aChallenge;
    }

    // 501
    public static ChallengeDefinitionData notImplemented501UsingTrace(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "TRACE /heartbeat (501)",
                "Issue a TRACE request on the `/heartbeat` end point and receive 501 (Not Implemented)");

        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/status-codes/status-codes-405-500-501-204");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");
        return aChallenge;
    }

    // No Content 204 - ping
    public static ChallengeDefinitionData noContent204UsingGet(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "GET /heartbeat (204)",
                "Issue a GET request on the `/heartbeat` end point and receive 204 when server is running");

        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/status-codes/status-codes-405-500-501-204");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");
        return aChallenge;
    }


    /*
        Status codes using method overrides
     */
    public static ChallengeDefinitionData overridePostToPatchFor500(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /heartbeat as PATCH (500)",
                "Issue a POST request on the `/heartbeat` end point and receive 500 when you override the Method Verb to a PATCH");

        aChallenge.addHint("Use a normal POST Request, but add an X-HTTP-Method-Override header");
        aChallenge.addSolutionLink("Add a header 'X-HTTP-Method-Override: PATCH' to a POST /heartbeat request", "","");
        //aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/25-26-27-28-status-codes-405-500-501-204/");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");
        return aChallenge;
    }

    public static ChallengeDefinitionData overridePostToDeleteFor405(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /heartbeat as DELETE (405)",
                "Issue a POST request on the `/heartbeat` end point and receive 405 when you override the Method Verb to a DELETE");

        aChallenge.addHint("Use a normal POST Request, but add an X-HTTP-Method-Override header");
        aChallenge.addSolutionLink("Add a header 'X-HTTP-Method-Override: DELETE' to a POST /heartbeat request", "","");
        //aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/25-26-27-28-status-codes-405-500-501-204/");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");
        return aChallenge;
    }

    // 501
    public static ChallengeDefinitionData overridePostToTraceFor501(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /heartbeat as Trace (501)",
                "Issue a POST request on the `/heartbeat` end point and receive 501 (Not Implemented) when you override the Method Verb to a TRACE");
        aChallenge.addHint("Use a normal POST Request, but add an X-HTTP-Method-Override header");
        aChallenge.addSolutionLink("Add a header 'X-HTTP-Method-Override: TRACE' to a POST /heartbeat request", "","");

//        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/25-26-27-28-status-codes-405-500-501-204/");
//        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");
        return aChallenge;
    }
}
