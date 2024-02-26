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

}
