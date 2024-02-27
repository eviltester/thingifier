package uk.co.compendiumdev.challenge.challenges.definitions;

import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;

public class ChallengerChallenges {

    public static ChallengeDefinitionData createChallenger201(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /challenger (201)",
                "Issue a POST request on the `/challenger` end point, with no body, to create a new challenger session. Use the generated X-CHALLENGER header in future requests to track challenge completion."
        );
        aChallenge.addHint("In multi-user mode, you need to create an X-CHALLENGER Session first", "/gui/multiuser.html");
        aChallenge.addSolutionLink("Send request using POST to /challenger endpoint. The response has an X-CHALLENGER header, add this header X-CHALLENGER and the GUID value to all future requests.","","");
        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-challenger-201");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "tNGuZMQgHxw");
        return aChallenge;
    }

    // challenge to GET /challenger/{guid} and restore a challenger session
    public static ChallengeDefinitionData getRestoreExistingChallenger200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "GET /challenger/guid (200)",
                "Issue a GET request on the `/challenger` end point, with an existing challenger GUID to restore that challenger's progress into memory."
        );
        aChallenge.addHint("In multi-user mode, you need to create an X-CHALLENGER Session first and let it go idle so it is removed in the 10 minute purge", "/gui/multiuser.html");
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-challenger-201");
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
        aChallenge.addHint("In multi-user mode, you need to create an X-CHALLENGER Session first and let it go idle so it is removed in the 10 minute purge", "/gui/multiuser.html");
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-challenger-201");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "tNGuZMQgHxw");

        return aChallenge;
    }

}
