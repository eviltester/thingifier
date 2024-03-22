package uk.co.compendiumdev.challenge.challenges.definitions;

import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;

public class ChallengerChallenges {

    public static ChallengeDefinitionData createChallenger201(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /challenger (201)",
                "Issue a POST request on the `/challenger` end point, with no body, to create a new challenger session. Use the generated X-CHALLENGER header in future requests to track challenge completion."
        );
        aChallenge.addHint("In multi-user mode, you need to create an X-CHALLENGER Session first", "/gui/multiuser");
        aChallenge.addSolutionLink("Send request using POST to /challenger endpoint. The response has an X-CHALLENGER header, add this header X-CHALLENGER and the GUID value to all future requests.","","");
        aChallenge.addSolutionLink("Read Solution", "HREF", "/apichallenges/solutions/create-session/post-challenger-201");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "tNGuZMQgHxw");
        return aChallenge;
    }

    // challenge to GET /challenger/{guid} and restore a challenger session
    public static ChallengeDefinitionData getRestoreExistingChallenger200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "GET /challenger/guid (200)",
                "Issue a GET request on the `/challenger` end point with an existing challenger GUID to restore that challenger's progress into memory."
        );
        aChallenge.addHint("In multi-user mode, you need to create an X-CHALLENGER Session first and let it go idle so it is removed in the 10 minute purge", "/gui/multiuser");
        aChallenge.addHint("Remember to add the X-CHALLENGER header to track your progress", "");
        aChallenge.addHint("Add the guid in the URL as the last part of the path", "");
        aChallenge.addSolutionLink("GET /challenger/{guid} for a challenger previously saved in the persistence store", "", "");
        //aChallenge.addSolutionLink("Read Solution", "HREF", "/apichallenges/solutions/post-create/post-challenger-201");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "tNGuZMQgHxw");
        return aChallenge;
    }

    // challenge to POST /challenger with X-CHALLENGER header of existing challenger to restore a challenger

    public static ChallengeDefinitionData postRestoreExistingChallenger200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /challenger (existing X-CHALLENGER)",
                "Issue a POST request on the `/challenger` end point, with an existing challenger GUID as the X-CHALLENGER header to restore that challenger's progress into memory."
        );
        aChallenge.addHint("In multi-user mode, you need to create an X-CHALLENGER Session first and let it go idle so it is removed in the 10 minute purge", "/gui/multiuser");
        aChallenge.addSolutionLink("POST /challenger with the challenger GUID in the X-CHALLENGER header for a challenger previously saved in the persistence store", "", "");
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-challenger-201");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "tNGuZMQgHxw");

        return aChallenge;
    }

    public static ChallengeDefinitionData getRestorableExistingChallengerProgress200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "GET /challenger/guid (existing X-CHALLENGER)",
                "Issue a GET request on the `/challenger/{guid}` end point, with an existing challenger GUID. This will return the progress data payload that can be used to later restore your progress to this status."
        );
        aChallenge.addHint("A challenger must have been created already for this to work", "");
        aChallenge.addHint("Remember to add the X-CHALLENGER header to track your progress", "");
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-challenger-201");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "tNGuZMQgHxw");

        return aChallenge;
    }

    public static ChallengeDefinitionData putRestoreChallengerProgress200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "PUT /challenger/guid RESTORE",
                "Issue a PUT request on the `/challenger/{guid}` end point, with an existing challenger GUID to restore that challenger's progress into memory."
        );
        aChallenge.addHint("Use the challenger payload returned from the earlier GET request", "");
        aChallenge.addHint("Remember to add the X-CHALLENGER header to track your progress", "");
        aChallenge.addHint("The challenger should already exist in memory and this will restore status to an earlier point", "");
        aChallenge.addSolutionLink("Using the payload from the earlier 'GET /challenger/guid' request, use PUT to reset the challenger progress", "", "");
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-challenger-201");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "tNGuZMQgHxw");

        return aChallenge;
    }

    public static ChallengeDefinitionData putRestoreChallengerProgress201(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "PUT /challenger/guid CREATE",
                "Issue a PUT request on the `/challenger/{guid}` end point, with a challenger GUID not currently in memory to restore that challenger's progress into memory."
        );
        aChallenge.addHint("Use the challenger payload returned from the earlier GET request", "");
        aChallenge.addHint("Remember to add the X-CHALLENGER header to track your progress", "");
        aChallenge.addHint("This will create the Challenger in memory because it should not already exist", "");
        aChallenge.addSolutionLink("Using the payload from the earlier 'GET /challenger/guid' request, use PUT to reset the challenger progress", "", "");
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-challenger-201");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "tNGuZMQgHxw");

        return aChallenge;
    }

    public static ChallengeDefinitionData getRestorableTodos200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "GET /challenger/database/guid (200)",
                "Issue a GET request on the `/challenger/database/{guid}` end point, to retrieve the current todos database for the user. You can use this to restore state later."
        );
        aChallenge.addHint("Remember to add the X-CHALLENGER header to track your progress", "");
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-challenger-201");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "tNGuZMQgHxw");

        return aChallenge;
    }

    public static ChallengeDefinitionData putRestorableTodos204(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "PUT /challenger/database/guid (Update)",
                "Issue a PUT request on the `/challenger/database/{guid}` end point, with a payload to restore the Todos database in memory."
        );
        aChallenge.addHint("Use the Todos database payload returned from the earlier GET request", "");
        aChallenge.addHint("Remember to add the X-CHALLENGER header to track your progress", "");
        aChallenge.addSolutionLink("Using the payload from the earlier 'GET /challenger/database/guid' request, use PUT to reset the challenger todos data", "", "");
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-challenger-201");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "tNGuZMQgHxw");

        return aChallenge;
    }
}
