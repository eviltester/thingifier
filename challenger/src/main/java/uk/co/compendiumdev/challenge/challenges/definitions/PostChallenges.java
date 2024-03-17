package uk.co.compendiumdev.challenge.challenges.definitions;

import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitionData;

public class PostChallenges {



    /*
        CREATE TODOS
     */
    public static ChallengeDefinitionData postTodos201(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /todos (201)",
                "Issue a POST request to successfully create a todo");

        aChallenge.addSolutionLink("Read Solution", "HREF", "/apichallenges/solutions/post-create/post-todos-201");
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

        aChallenge.addSolutionLink("Read Solution", "HREF", "/apichallenges/solutions/post-create/post-todos-400");
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
        aChallenge.addSolutionLink("Read Solution", "HREF", "/apichallenges/solutions/post-update/post-todos-id-200");
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

    /*
        CONTENT TYPE
     */

    // POST control content type, and Accepting only XML ie. Accept header of `application/xml`
    //      control content type to create with - XML
    public static ChallengeDefinitionData postCreateTodoWithXMLAcceptXML(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /todos XML",
                "Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/xml`, and Accepting only XML ie. Accept header of `application/xml`");

        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/content-type-header/post-todos-xml");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "2-KBYHwb7MM");
        return aChallenge;
    }

    //      control content type to create with - JSON
    public static ChallengeDefinitionData postCreateTodoWithJsonAcceptJson(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /todos JSON",
                "Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/json`, and Accepting only JSON ie. Accept header of `application/json`");

        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/content-type-header/post-todos-json");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "VS9qIhgp51Q");
        return aChallenge;
    }

    //      content type not supported 415 e.g. form encoded
    public static ChallengeDefinitionData postCreateUnsupportedContentType415(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /todos (415)",
                "Issue a POST request on the `/todos` end point with an unsupported content type to generate a 415 status code");

        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/content-type-header/post-todos-415");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "L8H-vkbXyr0");
        return aChallenge;
    }


    /*
        MIXED CONTENT AND ACCEPT TYPES
     */

    //      content type XML - accept type JSON
    public static ChallengeDefinitionData postTodosXmlToJson201(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /todos XML to JSON",
                "Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/xml` but Accept `application/json`");

        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/mix-accept-content/post-xml-accept-json");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "kfe7VtaV7u0");
        return aChallenge;
    }

    //      content type JSON - accept type XML
    public static ChallengeDefinitionData postTodosJsonToXml201(int challengeOrder) {
        ChallengeDefinitionData aChallenge = new ChallengeDefinitionData(
                ChallengeRenderer.renderChallengeNumber(challengeOrder),
                "POST /todos JSON to XML",
                "Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/json` but Accept `application/xml`");

        aChallenge.addSolutionLink("Read Solution", "HREF","/apichallenges/solutions/mix-accept-content/post-json-accept-xml");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "uw1Jq8t1em4");
        return aChallenge;
    }

}
