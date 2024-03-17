package uk.co.compendiumdev.challenge.challenges.definitions;

import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;

public class GetChallenges {

    public static ChallengeDefinitionData getChallenges200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "GET /challenges (200)",
                "Issue a GET request on the `/challenges` end point");

        aChallenge.addSolutionLink("Read Solution", "HREF", "/apichallenges/solutions/first-challenge/get-challenges-200");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "DrAjk2NaPRo");

        return aChallenge;
    }


    public static ChallengeDefinitionData getTodos200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "GET /todos (200)",
                "Issue a GET request on the `/todos` end point");

        aChallenge.addSolutionLink("Read Solution", "HREF", "/apichallenges/solutions/get/get-todos-200");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "OpisB0UZq0c");

        return aChallenge;
    }

    public static ChallengeDefinitionData getTodos404(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "GET /todo (404) not plural",
                "Issue a GET request on the `/todo` end point should 404 because nouns should be plural");

        aChallenge.addSolutionLink("Read Solution", "HREF", "/apichallenges/solutions/get/get-todo-404");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "gAJzqgcN9dc");

        return aChallenge;
    }

    public static ChallengeDefinitionData getTodo200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "GET /todos/{id} (200)",
                "Issue a GET request on the `/todos/{id}` end point to return a specific todo");

        aChallenge.addHint("Make sure you don't use {id} in the url, replace that with the id of a todo e.g. /todos/1");
        aChallenge.addSolutionLink("Read Solution", "HREF", "/apichallenges/solutions/get/get-todos-id-200");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "JDbbSY3U_rY");

        return aChallenge;
    }

    public static ChallengeDefinitionData getTodo404(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "GET /todos/{id} (404)",
                "Issue a GET request on the `/todos/{id}` end point for a todo that does not exist");

        aChallenge.addHint("Make sure you don't use {id} in the url, replace that with the id of a todo e.g. /todos/1");
        aChallenge.addHint("Make sure the id is an integer e.g. /todos/1");
        aChallenge.addHint("Make sure you are using the /todos end point e.g. /todos/1");
        aChallenge.addSolutionLink("Read Solution", "HREF", "/apichallenges/solutions/get/get-todos-id-404");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "1S5kpd8-xfM");

        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosFiltered200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "GET /todos (200) ?filter",
                "Issue a GET request on the `/todos` end point with a query filter to get only todos which are 'done'. There must exist both 'done' and 'not done' todos, to pass this challenge.");
        aChallenge.addHint("A query filter is a URL parameter using the field name and a value");
        aChallenge.addHint("A URL parameter is added to the end of a url with a ? e.g. /todos?id=1");
        aChallenge.addHint("To filter on 'done' we use the 'doneStatus' field  ? e.g. ?doneStatus=true");
        aChallenge.addHint("Make sure there are todos which are done, and not yet done");
        aChallenge.addSolutionLink("Read Solution", "HREF", "/apichallenges/solutions/get/get-todos-200-filter");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "G-sLuhyPMuw");
        return aChallenge;
    }


    /*
        ACCEPT HEADERS
     */
    // GET accept type
    //      specify accept type - XML
    //      specify accept type - JSON
    //      specify accept type - */* (ANY) to get default
    //      specify multiple accept type with a preference for XML - should receive XML
    //      none specified - get default
    //      cannot supply accepted type 406


    public static ChallengeDefinitionData getTodosAcceptXML200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "GET /todos (200) XML",
                "Issue a GET request on the `/todos` end point with an `Accept` header of `application/xml` to receive results in XML format");

        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/accept-header/get-todos-200-xml");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "cLeEuZm2VG8");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosAcceptJson200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "GET /todos (200) JSON",
                "Issue a GET request on the `/todos` end point with an `Accept` header of `application/json` to receive results in JSON format");

        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/accept-header/get-todos-200-json");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "79JTHiby2Qw");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosAcceptAny200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "GET /todos (200) ANY",
                "Issue a GET request on the `/todos` end point with an `Accept` header of `*/*` to receive results in default JSON format");

        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/accept-header/get-todos-200-any");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "O4DhJ8Ohkk8");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosPreferAcceptXML200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "GET /todos (200) XML pref",
                "Issue a GET request on the `/todos` end point with an `Accept` header of `application/xml, application/json` to receive results in the preferred XML format");

        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/accept-header/get-todos-200-xml-pref");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "sLChuy9pc9U");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosNoAccept200(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "GET /todos (200) no accept",
                "Issue a GET request on the `/todos` end point with no `Accept` header present in the message to receive results in default JSON format");

        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/accept-header/get-todos-200-no-accept");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "CSVP2PcvOdg");
        return aChallenge;
    }

    public static ChallengeDefinitionData getTodosUnavailableAccept406(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "GET /todos (406)",
                "Issue a GET request on the `/todos` end point with an `Accept` header `application/gzip` to receive 406 'NOT ACCEPTABLE' status code");

        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/accept-header/get-todos-406");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "QzfbegkY1ok");
        return aChallenge;
    }


}
