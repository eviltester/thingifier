package uk.co.compendiumdev.challenge.challenges;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.challenges.definitions.ChallengerChallenges;
import uk.co.compendiumdev.challenge.challenges.definitions.GetChallenges;
import uk.co.compendiumdev.challenge.challenges.definitions.HeadChallenges;
import uk.co.compendiumdev.challenge.challenges.definitions.PostChallenges;

import java.util.*;

public class ChallengeDefinitions {

    Logger logger = LoggerFactory.getLogger(ChallengeDefinitions.class);

    private final List<ChallengeSection> sections;
    Map<CHALLENGE, ChallengeDefinitionData> challengeData;
    List<ChallengeDefinitionData> orderedChallenges;

    public Collection<ChallengeDefinitionData> getChallenges() {
        return orderedChallenges;
    }

    public Collection<ChallengeSection> getChallengeSections() {
        return sections;
    }



    private String renderChallengeNumber(int challengeOrder){
        return String.format("%02d", challengeOrder);
    }

    // TODO: refactor this into private methods to make it easier to re-order and manage

    public ChallengeDefinitions(){

        challengeData = new HashMap<>();
        orderedChallenges = new ArrayList<>();
        sections = new ArrayList<>();

        int challengeOrder = 1;
        ChallengeDefinitionData aChallenge;


        ChallengeSection getStarted = new ChallengeSection("Getting Started",
                "If you want to track your challenge progress, in multi-user mode then you need to solve the challenges in this section to generate a unique ID that we can associate your progress with.");
        sections.add(getStarted);

        // create a challenger to persist challenge sessions
        storeChallengeAs(CHALLENGE.CREATE_NEW_CHALLENGER, ChallengerChallenges.createChallenger201(challengeOrder++), getStarted);




        ChallengeSection firstChallenge = new ChallengeSection("First Real Challenge",
                "For your first challenge, get the list of challenges. You'll be able to use this to see your progress in your API Client, as well as using the GUI.");
        sections.add(firstChallenge);

        // GET all challenges as a list
        storeChallengeAs(CHALLENGE.GET_CHALLENGES, GetChallenges.getChallenges200(challengeOrder++), firstChallenge);



        ChallengeSection getChallenges = new ChallengeSection("GET Challenges",
                "To retrieve, or read information from an API we issue GET requests. This section has a bunch of GET request challenges to try out.");
        sections.add(getChallenges);

        storeChallengeAs(CHALLENGE.GET_TODOS, GetChallenges.getTodos200(challengeOrder++), getChallenges);
        storeChallengeAs(CHALLENGE.GET_TODOS_NOT_PLURAL_404, GetChallenges.getTodos404(challengeOrder++), getChallenges);
        storeChallengeAs(CHALLENGE.GET_TODO, GetChallenges.getTodo200(challengeOrder++), getChallenges);
        storeChallengeAs(CHALLENGE.GET_TODO_404, GetChallenges.getTodo404(challengeOrder++), getChallenges);
        storeChallengeAs(CHALLENGE.GET_TODOS_FILTERED, GetChallenges.getTodosFiltered200(challengeOrder++), getChallenges);



        // HEAD
        ChallengeSection headChallenges = new ChallengeSection("HEAD Challenges",
                "A HEAD request, is like a GET request, but only returns the headers and status code.");
        sections.add(headChallenges);

        storeChallengeAs(CHALLENGE.GET_HEAD_TODOS, HeadChallenges.headTodos200(challengeOrder++), headChallenges);




        // CREATE POST
        ChallengeSection postCreateChallenges = new ChallengeSection("Creation Challenges with POST",
                "A POST request can be used to create and update data, these challenges are to 'create' data. As a Hint, if you are not sure what the message body should be, try copying in the response from the associated GET request, and amending it.");
        sections.add(postCreateChallenges);

        storeChallengeAs(CHALLENGE.POST_TODOS, PostChallenges.postTodos201(challengeOrder++), postCreateChallenges);
        storeChallengeAs(CHALLENGE.POST_TODOS_BAD_DONE_STATUS, PostChallenges.postTodosBadDoneStatus400(challengeOrder++), postCreateChallenges);
        storeChallengeAs(CHALLENGE.POST_TODOS_TOO_LONG_TITLE_LENGTH, PostChallenges.postTodosTitleTooLong400(challengeOrder++), postCreateChallenges);
        storeChallengeAs(CHALLENGE.POST_TODOS_TOO_LONG_DESCRIPTION_LENGTH, PostChallenges.postTodosDescriptionTooLong400(challengeOrder++), postCreateChallenges);
        storeChallengeAs(CHALLENGE.POST_MAX_OUT_TITILE_DESCRIPTION_LENGTH, PostChallenges.postTodosMaxTitleDescriptionTooLong400(challengeOrder++), postCreateChallenges);
        storeChallengeAs(CHALLENGE.POST_TODOS_TOO_LONG_PAYLOAD_SIZE, PostChallenges.postTodosPayloadTooLong400(challengeOrder++), postCreateChallenges);
        storeChallengeAs(CHALLENGE.POST_TODOS_INVALID_EXTRA_FIELD, PostChallenges.postTodosInvalidExtraField400(challengeOrder++), postCreateChallenges);






        // CREATE wtih PUT
        ChallengeSection putCreateChallenges = new ChallengeSection("Creation Challenges with PUT",
                "A PUT request can often used to create and update data. The todo application we are using has automatically generated ids, so you cannot use PUT to create. As a Hint, if you are not sure what the message body should be, try copying in the response from the associated GET request, and amending it.");
        sections.add(putCreateChallenges);

        aChallenge = createChallenge(CHALLENGE.PUT_TODOS_400, renderChallengeNumber(challengeOrder), "PUT /todos/{id} (400)",
                "Issue a PUT request to unsuccessfully create a todo");
        putCreateChallenges.addChallenge(aChallenge);
        // todo: create solution for PUT todos 400 challenge
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-201");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "T0LFHwavsNA");
        challengeOrder++;


        // UPDATE
        ChallengeSection postUpdateChallenges = new ChallengeSection("Update Challenges with POST",
                "Use a POST request to amend something that already exists. These are 'partial' content updates so you usually don't need to have all details of the entity in the request, e.g. you could just update a title, or a description, or a status");
        sections.add(postUpdateChallenges);

        aChallenge = createChallenge(CHALLENGE.POST_UPDATE_TODO, renderChallengeNumber(challengeOrder), "POST /todos/{id} (200)",
                "Issue a POST request to successfully update a todo");
        postUpdateChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Make sure you don't use {id} in the url, replace that with the id of a todo e.g. /todos/1");
        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-id-200");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "feXdRpZ_tgs");
        challengeOrder++;

        aChallenge = createChallenge(CHALLENGE.POST_TODOS_404, renderChallengeNumber(challengeOrder), "POST /todos/{id} (404)",
                "Issue a POST request for a todo which does not exist. Expect to receive a 404 response.");
        postUpdateChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Make sure you don't use {id} in the url, replace that with the id of a todo that does not exist e.g. /todos/100");
        // todo add solution and hints for POST 404
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-id-200");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "feXdRpZ_tgs");
        challengeOrder++;



        // UPDATE wtih PUT
        ChallengeSection putUpdateChallenges = new ChallengeSection("Update Challenges with PUT",
                "A PUT request can be used to amend data. REST Put requests are idempotent, they provide the same result each time.");
        sections.add(putUpdateChallenges);

        aChallenge = createChallenge(CHALLENGE.PUT_TODOS_FULL_200, renderChallengeNumber(challengeOrder), "PUT /todos/{id} full (200)",
                "Issue a PUT request to update an existing todo with a complete payload i.e. title, description and donestatus.");
        putUpdateChallenges.addChallenge(aChallenge);
        // todo: create solution for PUT todos full 200 challenge
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-201");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "T0LFHwavsNA");
        challengeOrder++;

        aChallenge = createChallenge(CHALLENGE.PUT_TODOS_PARTIAL_200, renderChallengeNumber(challengeOrder), "PUT /todos/{id} partial (200)",
                "Issue a PUT request to update an existing todo with just mandatory items in payload i.e. title.");
        putUpdateChallenges.addChallenge(aChallenge);
        // todo: create solution for PUT todos partial 200 challenge
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-201");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "T0LFHwavsNA");
        challengeOrder++;

        aChallenge = createChallenge(CHALLENGE.PUT_TODOS_MISSING_TITLE_400, renderChallengeNumber(challengeOrder), "PUT /todos/{id} no title (400)",
                "Issue a PUT request to fail to update an existing todo because title is missing in payload.");
        putUpdateChallenges.addChallenge(aChallenge);
        // todo: create solution for PUT todos partial 200 challenge
        aChallenge.addHint("Title is required for Put requests because they are idempotent. You can amend using POST without a title, but not using a PUT.");
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-201");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "T0LFHwavsNA");
        // TODO: add solution text to summarise solution
        challengeOrder++;

        aChallenge = createChallenge(CHALLENGE.PUT_TODOS_400_NO_AMEND_ID, renderChallengeNumber(challengeOrder), "PUT /todos/{id} no amend id (400)",
                "Issue a PUT request to fail to update an existing todo because id different in payload.");
        putUpdateChallenges.addChallenge(aChallenge);
        // todo: create solution for PUT todos partial 200 challenge
        aChallenge.addHint("ID is auto generated you can not amend it in the payload.");
        aChallenge.addHint("If you have a different id in the payload from the url then this is viewed as an amendment and you can not amend an auto generated field.");
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-201");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "T0LFHwavsNA");
        // TODO: add solution text to summarise solution
        challengeOrder++;



        // DELETE
        ChallengeSection deleteChallenges = new ChallengeSection("DELETE Challenges",
                "Use a DELETE request to delete an entity. Since this is an extreme request, normally you have to be logged in or authenticated, but we wanted to make life easier for you so we cover authentication later. Anyone can delete To Do items without authentication in this system.");
        sections.add(deleteChallenges);

        aChallenge =
            createChallenge(CHALLENGE.DELETE_A_TODO, renderChallengeNumber(challengeOrder), "DELETE /todos/{id} (200)",
                "Issue a DELETE request to successfully delete a todo");
        deleteChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Make sure you don't use {id} in the url, replace that with the id of a todo e.g. /todos/1");
        aChallenge.addHint("Make sure a todo with the id exists prior to issuing the request");
        aChallenge.addHint("Check it was deleted by issuing a GET or HEAD on the /todos/{id}");
        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/delete-todos-id-200");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "6MXTkaXn9qU");
        challengeOrder++;

        // OPTIONS
        ChallengeSection optionsChallenges = new ChallengeSection("OPTIONS Challenges",
                "Use an OPTIONS verb and check the `Allow` header, this will show you what verbs are allowed to be used on an endpoint. When you test APIs it is worth checking to see if all the verbs listed are allowed or not.");
        sections.add(optionsChallenges);

        aChallenge =
            createChallenge(CHALLENGE.OPTIONS_TODOS, renderChallengeNumber(challengeOrder), "OPTIONS /todos (200)",
                "Issue an OPTIONS request on the `/todos` end point. You might want to manually check the 'Allow' header in the response is as expected.");
        optionsChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/options-todos-200/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "Ld5h1TSnXWA");
        challengeOrder++;

        // GET accept type
        //      specify accept type - XML
        //      specify accept type - JSON
        //      specify accept type - */* (ANY) to get default
        //      specify multiple accept type with a preference for XML - should receive XML
        //      none specified - get default
        //      cannot supply accepted type 406

        ChallengeSection acceptChallenges = new ChallengeSection("Accept Challenges",
                "The `Accept` header, tells the server what format you want the response to be in. By changing the `Accept` header you can specify JSON or XML.");
        sections.add(acceptChallenges);

        aChallenge =
            createChallenge(CHALLENGE.GET_ACCEPT_XML, renderChallengeNumber(challengeOrder), "GET /todos (200) XML",
                "Issue a GET request on the `/todos` end point with an `Accept` header of `application/xml` to receive results in XML format");
        acceptChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/get-todos-xml-200/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "cLeEuZm2VG8");
        challengeOrder++;

        aChallenge =
            createChallenge(CHALLENGE.GET_ACCEPT_JSON, renderChallengeNumber(challengeOrder), "GET /todos (200) JSON",
                "Issue a GET request on the `/todos` end point with an `Accept` header of `application/json` to receive results in JSON format");
        acceptChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/get-todos-json-200/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "79JTHiby2Qw");
        challengeOrder++;

        aChallenge =
            createChallenge(CHALLENGE.GET_ACCEPT_ANY_DEFAULT_JSON, renderChallengeNumber(challengeOrder), "GET /todos (200) ANY",
                "Issue a GET request on the `/todos` end point with an `Accept` header of `*/*` to receive results in default JSON format");
        acceptChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/get-todos-any-200/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "O4DhJ8Ohkk8");
        challengeOrder++;

        aChallenge =
            createChallenge(CHALLENGE.GET_ACCEPT_XML_PREFERRED, renderChallengeNumber(challengeOrder), "GET /todos (200) XML pref",
                "Issue a GET request on the `/todos` end point with an `Accept` header of `application/xml, application/json` to receive results in the preferred XML format");
        acceptChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/get-todos-xml-preference-200/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "sLChuy9pc9U");
        challengeOrder++;

        aChallenge =
            createChallenge(CHALLENGE.GET_JSON_BY_DEFAULT_NO_ACCEPT, renderChallengeNumber(challengeOrder), "GET /todos (200) no accept",
                "Issue a GET request on the `/todos` end point with no `Accept` header present in the message to receive results in default JSON format");
        acceptChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/get-todos-no-accept-200/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "CSVP2PcvOdg");
        challengeOrder++;

        aChallenge =
            createChallenge(CHALLENGE.GET_UNSUPPORTED_ACCEPT_406, renderChallengeNumber(challengeOrder), "GET /todos (406)",
                "Issue a GET request on the `/todos` end point with an `Accept` header `application/gzip` to receive 406 'NOT ACCEPTABLE' status code");
        acceptChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/19-get-todos-invalid-accept-406/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "QzfbegkY1ok");
        challengeOrder++;



        ChallengeSection contentTypeChallenges = new ChallengeSection("Content-Type Challenges",
                "The `Content-Type` header, tells the server what format type your 'body' content is, e.g. are you sending XML or JSON.");
        sections.add(contentTypeChallenges);

        // POST control content type, and Accepting only XML ie. Accept header of `application/xml`
        //      control content type to create with - XML
        aChallenge =
            createChallenge(CHALLENGE.POST_CREATE_XML, renderChallengeNumber(challengeOrder), "POST /todos XML",
                "Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/xml`, and Accepting only XML ie. Accept header of `application/xml`");
        contentTypeChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/20-post-todos-xml/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "2-KBYHwb7MM");
        challengeOrder++;

        //      control content type to create with - JSON
        aChallenge =
            createChallenge(CHALLENGE.POST_CREATE_JSON, renderChallengeNumber(challengeOrder), "POST /todos JSON",
                "Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/json`, and Accepting only JSON ie. Accept header of `application/json`");
        contentTypeChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/21-post-todos-json/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "VS9qIhgp51Q");
        challengeOrder++;

        //      content type not supported 415 e.g. form encoded
        aChallenge =
            createChallenge(CHALLENGE.POST_TODOS_415, renderChallengeNumber(challengeOrder), "POST /todos (415)",
                "Issue a POST request on the `/todos` end point with an unsupported content type to generate a 415 status code");
        contentTypeChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/22-post-todos-unsupported-415/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "L8H-vkbXyr0");
        challengeOrder++;


        ChallengeSection restoreChallenger = new ChallengeSection("Fancy a Break? Restore your session",
                "Your challenge progress is saved, and as long as you remember you challenger ID you can restore it. Leaving a challenger idle for 10 minutes and coming back will restore the progress in memory.");
        sections.add(restoreChallenger);

        // challenge to GET /challenger/{guid} and restore a challenger session

        aChallenge = createChallenge(CHALLENGE.GET_RESTORE_EXISTING_CHALLENGER,
                renderChallengeNumber(challengeOrder), "GET /challenger/guid (200)",
                "Issue a GET request on the `/challenger` end point, with an existing challenger GUID to restore that challenger's progress into memory."
        );
        aChallenge.addHint("In multi-user mode, you need to create an X-CHALLENGER Session first and let it go idle so it is removed in the 10 minute purge", "/gui/multiuser.html");
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-challenger-201");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "tNGuZMQgHxw");
        restoreChallenger.addChallenge(aChallenge);
        challengeOrder++;


        // challenge to POST /challenger with X-CHALLENGER header of existing challenger to restore a challenger
        aChallenge = createChallenge(CHALLENGE.POST_RESTORE_EXISTING_CHALLENGER,
                renderChallengeNumber(challengeOrder), "POST /challenger (existing X-CHALLENGER)",
                "Issue a POST request on the `/challenger` end point, with an existing challenger GUID as the X-CHALLENGER header to restore that challenger's progress into memory."
        );
        aChallenge.addHint("In multi-user mode, you need to create an X-CHALLENGER Session first and let it go idle so it is removed in the 10 minute purge", "/gui/multiuser.html");
        //aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-challenger-201");
        //aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "tNGuZMQgHxw");
        restoreChallenger.addChallenge(aChallenge);
        challengeOrder++;

        // POST
        // todo:    POST is not idempotent - same values, different results i.e. id different

        // PUT
        // todo:     PUT is idempotent - same result each time

        // POST mixed content and accept
        //      content type XML - accept type JSON
        ChallengeSection mixAcceptContentTypeChallenges = new ChallengeSection("Mix Accept and Content-Type Challenges",
                "We can mix the `Accept` and `Content-Type` headers so that we can send JSON but receive XML. These challenges encourage you to explore some combinations.");
        sections.add(mixAcceptContentTypeChallenges);

        aChallenge =
            createChallenge(CHALLENGE.POST_CREATE_XML_ACCEPT_JSON, renderChallengeNumber(challengeOrder), "POST /todos XML to JSON",
                "Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/xml` but Accept `application/json`");
        mixAcceptContentTypeChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/23-post-xml-accept-json/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "kfe7VtaV7u0");
        challengeOrder++;

        //      content type JSON - accept type XML
        aChallenge =
            createChallenge(CHALLENGE.POST_CREATE_JSON_ACCEPT_XML, renderChallengeNumber(challengeOrder), "POST /todos JSON to XML",
                "Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/json` but Accept `application/xml`");
        mixAcceptContentTypeChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/24-post-json-accept-xml/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "uw1Jq8t1em4");
        challengeOrder++;


        ChallengeSection miscStatusCodes = new ChallengeSection("Status Code Challenges",
                "Status-codes are essential to understand, so we created some challenges that help you trigger more status codes. Remember to review httpstatuses.com to learn what the status codes mean.");
        sections.add(miscStatusCodes);

        // extra status code challenges
        //      method not allowed - 405
        aChallenge =
            createChallenge(CHALLENGE.DELETE_HEARTBEAT_405, renderChallengeNumber(challengeOrder), "DELETE /heartbeat (405)",
                "Issue a DELETE request on the `/heartbeat` end point and receive 405 (Method Not Allowed)");
        miscStatusCodes.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/25-26-27-28-status-codes-405-500-501-204/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");
        challengeOrder++;

        // cannot process request server error 500
        aChallenge =
            createChallenge(CHALLENGE.PATCH_HEARTBEAT_500, renderChallengeNumber(challengeOrder), "PATCH /heartbeat (500)",
                "Issue a PATCH request on the `/heartbeat` end point and receive 500 (internal server error)");
        miscStatusCodes.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/25-26-27-28-status-codes-405-500-501-204/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");
        challengeOrder++;

        // 501
        aChallenge =
            createChallenge(CHALLENGE.TRACE_HEARTBEAT_501, renderChallengeNumber(challengeOrder), "TRACE /heartbeat (501)",
                "Issue a TRACE request on the `/heartbeat` end point and receive 501 (Not Implemented)");
        miscStatusCodes.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/25-26-27-28-status-codes-405-500-501-204/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");
        challengeOrder++;

        // No Content 204 - ping
        aChallenge =
            createChallenge(CHALLENGE.GET_HEARTBEAT_204, renderChallengeNumber(challengeOrder), "GET /heartbeat (204)",
                "Issue a GET request on the `/heartbeat` end point and receive 204 when server is running");
        miscStatusCodes.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/25-26-27-28-status-codes-405-500-501-204/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");
        challengeOrder++;


        ChallengeSection authenticationChallenges = new ChallengeSection("Authentication Challenges",
                "Authentication is telling the system who you are. In multi-user mode you are already doing that with the X-CHALLENGER header, but we have added an extra level of security on the /secret section. So first Authenticate with Basic Authentication to find out the token to use for authorisation for later challenges.");
        sections.add(authenticationChallenges);

        // authorization and authentication
        //    POST /secret/token with incorrect username and password credentials get 401
        aChallenge =
            createChallenge(CHALLENGE.CREATE_SECRET_TOKEN_401, renderChallengeNumber(challengeOrder), "POST /secret/token (401)",
                "Issue a POST request on the `/secret/token` end point and receive 401 when Basic auth username/password is not admin/password");
        authenticationChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/29-authentication-post-secret-token/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "RSQGADU3SLA");
        challengeOrder++;

        //    POST /secret/token with correct username and password credentials get secret token 201
        aChallenge =
            createChallenge(CHALLENGE.CREATE_SECRET_TOKEN_201, renderChallengeNumber(challengeOrder), "POST /secret/token (201)",
                "Issue a POST request on the `/secret/token` end point and receive 201 when Basic auth username/password is admin/password");
        authenticationChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/30-authentication-post-secret-token-201/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "J2GQiuEfHkI");
        challengeOrder++;

        ChallengeSection authorizationChallenges = new ChallengeSection("Authorization Challenges",
                "Once the system knows who you are, authorization is if you have the correct level of access. In these challenges the authorization is granted using a custom API header X-AUTH-TOKEN or using a Bearer Authorization header.");
        sections.add(authorizationChallenges);

        //    GET /secret/note with no token and 403
        aChallenge =
            createChallenge(CHALLENGE.GET_SECRET_NOTE_403, renderChallengeNumber(challengeOrder), "GET /secret/note (403)",
                "Issue a GET request on the `/secret/note` end point and receive 403 when X-AUTH-TOKEN does not match a valid token");
        authorizationChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/31-secret-note-forbidden-403/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "77mnUQezdas");
        challengeOrder++;

        //    GET /secret/note with invalid token and 401
        aChallenge =
            createChallenge(CHALLENGE.GET_SECRET_NOTE_401, renderChallengeNumber(challengeOrder), "GET /secret/note (401)",
                "Issue a GET request on the `/secret/note` end point and receive 401 when no X-AUTH-TOKEN header present");
        authorizationChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/32-secret-note-401-unauthorized/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "__uZlQZ48io");
        challengeOrder++;

        //    GET /secret/note with token and see token
        aChallenge =
                createChallenge(CHALLENGE.GET_SECRET_NOTE_200, renderChallengeNumber(challengeOrder), "GET /secret/note (200)",
                        "Issue a GET request on the `/secret/note` end point receive 200 when valid X-AUTH-TOKEN used - response body should contain the note");
        authorizationChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/33-authorized-get-secret-note-200/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "2uRpzr2OmEY");
        challengeOrder++;

        //    POST /secret/note with token and update secret note
        aChallenge =
                createChallenge(CHALLENGE.POST_SECRET_NOTE_200, renderChallengeNumber(challengeOrder),"POST /secret/note (200)",
                        "Issue a POST request on the `/secret/note` end point with a note payload e.g. {\"note\":\"my note\"} and receive 200 when valid X-AUTH-TOKEN used. Note is maximum length 100 chars and will be truncated when stored.");
        authorizationChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/34-post-amend-secret-note-200/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "A9T9yjzEOEE");
        challengeOrder++;

        //    POST /secret/note with invalid token and 401
        aChallenge =
                createChallenge(CHALLENGE.POST_SECRET_NOTE_401, renderChallengeNumber(challengeOrder),"POST /secret/note (401)",
                        "Issue a POST request on the `/secret/note` end point with a note payload {\"note\":\"my note\"} and receive 401 when no X-AUTH-TOKEN present");
        authorizationChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/35-36-post-unauthorised-401-403/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "76U5TEvLzLI");
        challengeOrder++;

        //    POST /secret/note with no token and 403
        aChallenge =
            createChallenge(CHALLENGE.POST_SECRET_NOTE_403, renderChallengeNumber(challengeOrder),"POST /secret/note (403)",
                "Issue a POST request on the `/secret/note` end point with a note payload {\"note\":\"my note\"} and receive 403 when X-AUTH-TOKEN does not match a valid token");
        authorizationChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/35-36-post-unauthorised-401-403/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "76U5TEvLzLI");
        challengeOrder++;



        //    GET /secret/note with bearer token authorization
        aChallenge =
                createChallenge(CHALLENGE.GET_SECRET_NOTE_BEARER_200, renderChallengeNumber(challengeOrder),"GET /secret/note (Bearer)",
                        "Issue a GET request on the `/secret/note` end point receive 200 when using the X-AUTH-TOKEN value as an Authorization Bearer token - response body should contain the note");
        authorizationChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/37-38-bearer-token-access/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "8GsMTZxEItw");
        challengeOrder++;

        //    POST /secret/note with token and update secret note
        aChallenge =
                createChallenge(CHALLENGE.POST_SECRET_NOTE_BEARER_200, renderChallengeNumber(challengeOrder),"POST /secret/note (Bearer)",
                        "Issue a POST request on the `/secret/note` end point with a note payload e.g. {\"note\":\"my note\"} and receive 200 when valid X-AUTH-TOKEN value used as an Authorization Bearer token. Status code 200 received. Note is maximum length 100 chars and will be truncated when stored.");
        authorizationChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/37-38-bearer-token-access/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "8GsMTZxEItw");
        challengeOrder++;




        // misc

        ChallengeSection miscChallenges = new ChallengeSection("Miscellaneous Challenges",
                "We left these challenges to the end because they seemed fun, but... different.");
        sections.add(miscChallenges);

        aChallenge =
            createChallenge(CHALLENGE.DELETE_ALL_TODOS, renderChallengeNumber(challengeOrder),"DELETE /todos/{id} (200) all",
                "Issue a DELETE request to successfully delete the last todo in system so that there are no more todos in the system");
        miscChallenges.addChallenge(aChallenge);
        aChallenge.addHint("After deleting the last todo, there will be no todos left in the application");
        aChallenge.addHint("Make sure you don't use {id} in the url, replace that with the id of a todo e.g. /todos/1");
        aChallenge.addHint("You have to delete all the todo items in the system to complete this challenge");
        challengeOrder++;

        storeChallengeAs(CHALLENGE.POST_ALL_TODOS, PostChallenges.postAllTodos201(challengeOrder), miscChallenges);
        challengeOrder++;


        Set challengeNames = new HashSet();
        for(ChallengeDefinitionData challenge : orderedChallenges){
            logger.info("Setup Challenge: " + challenge.name);
            challengeNames.add(challenge.name);
        }
        int sectionChallengesCount = 0;
        for(ChallengeSection section : sections){
            sectionChallengesCount += section.getChallenges().size();
        }
        if(sectionChallengesCount!= orderedChallenges.size()) {
            throw new RuntimeException(
                    "Number challenges in sections, does not match number of challenges" +
                            ", possibly forgot to add section or challenge to section");
        }
        if(challengeNames.size()!= orderedChallenges.size()) {
            throw new RuntimeException(
                    "Number of names, does not match number of challenges" +
                            ", possible duplicate name");
        }
    }

    private ChallengeDefinitionData storeChallengeAs(
            final CHALLENGE id,
            final ChallengeDefinitionData challenge,
            ChallengeSection section) {

        challengeData.put(id, challenge);
        orderedChallenges.add(challenge);
        section.addChallenge(challenge);

        return challenge;
    }

    private ChallengeDefinitionData createChallenge(final CHALLENGE id,
                                                    final String orderId,
                                                    final String name,
                                                    final String description) {
        ChallengeDefinitionData challenge = new ChallengeDefinitionData( orderId, name, description);
        challengeData.put(id, challenge);
        orderedChallenges.add(challenge);
        return challenge;
    }


    public CHALLENGE getChallenge(final String name) {
        for(Map.Entry<CHALLENGE, ChallengeDefinitionData>challenge : challengeData.entrySet()){
            if(challenge.getValue().name.contentEquals(name)){
                return challenge.getKey();
            }
        }
        return null;
    }
}
