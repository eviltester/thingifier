package uk.co.compendiumdev.challenge.challenges;

import uk.co.compendiumdev.challenge.CHALLENGE;

import java.util.*;

public class ChallengeDefinitions {

    private final List<ChallengeSection> sections;
    Map<CHALLENGE, ChallengeDefinitionData> challengeData;
    List<ChallengeDefinitionData> orderedChallenges;

    public Collection<ChallengeDefinitionData> getChallenges() {
        return orderedChallenges;
    }

    public Collection<ChallengeSection> getChallengeSections() {
        return sections;
    }

    public ChallengeDefinitions(){

        challengeData = new HashMap<>();
        orderedChallenges = new ArrayList<>();
        sections = new ArrayList<>();

        // todo: challenge to GET /challenger/{guid} and restore a challenger session
        // todo: challenge to POST /challenger with X-CHALLENGER header of existing challenger to restore a challenger
        // todo: challenge to POST /challenger with X-CHALLENGER header of existing challenger and body of a stored challenge set to update the challenger in the system

        ChallengeSection getStarted = new ChallengeSection("Getting Started",
                "If you want to track your challenge progress, in multi-user mode then you need to solve the challenges in this section to generate a unique ID that we can associate your progress with.");

        sections.add(getStarted);

        // create a challenger to persist challenge sessions
        // CREATE_NEW_CHALLENGER
        ChallengeDefinitionData aChallenge = createChallenge(CHALLENGE.CREATE_NEW_CHALLENGER,
                "01", "POST /challenger (201)",
                "Issue a POST request on the `/challenger` end point, with no body, to create a new challenger session. Use the generated X-CHALLENGER header in future requests to track challenge completion."
        );
        aChallenge.addHint("In multi-user mode, you need to create an X-CHALLENGER Session first", "/gui/multiuser.html");
        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-challenger-201");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "tNGuZMQgHxw");
        getStarted.addChallenge(aChallenge);



        ChallengeSection firstChallenge = new ChallengeSection("First Real Challenge",
                "For your first challenge, get the list of challenges. You'll be able to use this to see your progress in your API Client, as well as using the GUI.");

        sections.add(firstChallenge);

        // READ
        aChallenge = createChallenge(CHALLENGE.GET_CHALLENGES, "02", "GET /challenges (200)",
                "Issue a GET request on the `/challenges` end point");
        firstChallenge.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/get-challenges-200/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "DrAjk2NaPRo");

        ChallengeSection getChallenges = new ChallengeSection("GET Challenges",
                "To retrieve, or read information from an API we issue GET requests. This section has a bunch of GET request challenges to try out.");
        sections.add(getChallenges);

        aChallenge = createChallenge(CHALLENGE.GET_TODOS, "03", "GET /todos (200)",
                "Issue a GET request on the `/todos` end point");
        getChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/get-todos-200");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "OpisB0UZq0c");

        aChallenge = createChallenge(CHALLENGE.GET_TODOS_NOT_PLURAL_404, "04", "GET /todo (404) not plural",
                "Issue a GET request on the `/todo` end point should 404 because nouns should be plural");
        getChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/get-todo-404");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "gAJzqgcN9dc");

        aChallenge = createChallenge(CHALLENGE.GET_TODO, "05", "GET /todos/{id} (200)",
                "Issue a GET request on the `/todos/{id}` end point to return a specific todo");
        getChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Make sure you don't use {id} in the url, replace that with the id of a todo e.g. /todos/1");
        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/get-todos-id-200");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "JDbbSY3U_rY");

        aChallenge = createChallenge(CHALLENGE.GET_TODO_404, "06", "GET /todos/{id} (404)",
                "Issue a GET request on the `/todos/{id}` end point for a todo that does not exist");
        getChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Make sure you don't use {id} in the url, replace that with the id of a todo e.g. /todos/1");
        aChallenge.addHint("Make sure the id is an integer e.g. /todos/1");
        aChallenge.addHint("Make sure you are using the /todos end point e.g. /todos/1");
        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/get-todos-id-404");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "1S5kpd8-xfM");

        // HEAD
        ChallengeSection headChallenges = new ChallengeSection("HEAD Challenges",
                "A HEAD request, is like a GET request, but only returns the headers and status code.");
        sections.add(headChallenges);

        aChallenge = createChallenge(CHALLENGE.GET_HEAD_TODOS, "07", "HEAD /todos (200)",
                "Issue a HEAD request on the `/todos` end point");
        headChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/head-todos-200");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "zKbytTelP84");

        // CREATE
        ChallengeSection postCreateChallenges = new ChallengeSection("Creation Challenges with POST",
                "A POST request can be used to create and update data, these challenges are to 'create' data. As a Hint, if you are not sure what the message body should be, try copying in the response from the assocated GET request, and amending it.");
        sections.add(postCreateChallenges);

        aChallenge = createChallenge(CHALLENGE.POST_TODOS, "08", "POST /todos (201)",
                "Issue a POST request to successfully create a todo");
        postCreateChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-201");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "T0LFHwavsNA");

        aChallenge = createChallenge(CHALLENGE.GET_TODOS_FILTERED, "09", "GET /todos (200) ?filter",
                        "Issue a GET request on the `/todos` end point with a query filter to get only todos which are 'done'. There must exist both 'done' and 'not done' todos, to pass this challenge.");
        postCreateChallenges.addChallenge(aChallenge);
        aChallenge.addHint("A query filter is a URL parameter using the field name and a value");
        aChallenge.addHint("A URL parameter is added to the end of a url with a ? e.g. /todos?id=1");
        aChallenge.addHint("To filter on 'done' we use the 'doneStatus' field  ? e.g. ?doneStatus=true");
        aChallenge.addHint("Make sure there are todos which are done, and not yet done");
        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/get-todos-200-filter");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "G-sLuhyPMuw");

        aChallenge = createChallenge(CHALLENGE.POST_TODOS_BAD_DONE_STATUS, "10", "POST /todos (400) doneStatus",
                "Issue a POST request to create a todo but fail validation on the `doneStatus` field");
        postCreateChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-400");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "tlye5bQ72g0");

        // UPDATE
        ChallengeSection postUpdateChallenges = new ChallengeSection("Update Challenges with POST",
                "Use a POST request to amend something that already exists. These are 'partial' content updates so you usually don't need to have all details of the entity in the request, e.g. you could just update a title, or a description, or a status");
        sections.add(postUpdateChallenges);

        aChallenge = createChallenge(CHALLENGE.POST_UPDATE_TODO, "11", "POST /todos/{id} (200)",
                "Issue a POST request to successfully update a todo");
        postUpdateChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Make sure you don't use {id} in the url, replace that with the id of a todo e.g. /todos/1");
        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/post-todos-id-200");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "feXdRpZ_tgs");

        // DELETE
        ChallengeSection deleteChallenges = new ChallengeSection("DELETE Challenges",
                "Use a DELETE request to delete an entity. Since this is an extreme request, normall you have to be logged in or authenticated, but we wanted to make life easier for you so we cover authentication later. Anyone can delete To Do items without authentication in this system.");
        sections.add(deleteChallenges);

        aChallenge =
            createChallenge(CHALLENGE.DELETE_A_TODO, "12", "DELETE /todos/{id} (200)",
                "Issue a DELETE request to successfully delete a todo");
        deleteChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Make sure you don't use {id} in the url, replace that with the id of a todo e.g. /todos/1");
        aChallenge.addHint("Make sure a todo with the id exists prior to issuing the request");
        aChallenge.addHint("Check it was deleted by issuing a GET or HEAD on the /todos/{id}");
        aChallenge.addSolutionLink("Read Solution", "HREF", "https://www.eviltester.com/apichallenges/howto/delete-todos-id-200");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "6MXTkaXn9qU");

        // OPTIONS
        ChallengeSection optionsChallenges = new ChallengeSection("OPTIONS Challenges",
                "Use an OPTIONS verb and check the `Allow` header, this will show you what verbs are allowed to be used on an endpoint. When you test APIs it is worth checking to see if all the verbs listed are allowed or not.");
        sections.add(optionsChallenges);

        aChallenge =
            createChallenge(CHALLENGE.OPTIONS_TODOS, "13", "OPTIONS /todos (200)",
                "Issue an OPTIONS request on the `/todos` end point. You might want to manually check the 'Allow' header in the response is as expected.");
        optionsChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/options-todos-200/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "Ld5h1TSnXWA");

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
            createChallenge(CHALLENGE.GET_ACCEPT_XML, "14", "GET /todos (200) XML",
                "Issue a GET request on the `/todos` end point with an `Accept` header of `application/xml` to receive results in XML format");
        acceptChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/get-todos-xml-200/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "cLeEuZm2VG8");

        aChallenge =
            createChallenge(CHALLENGE.GET_ACCEPT_JSON, "15", "GET /todos (200) JSON",
                "Issue a GET request on the `/todos` end point with an `Accept` header of `application/json` to receive results in JSON format");
        acceptChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/get-todos-json-200/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "79JTHiby2Qw");

        aChallenge =
            createChallenge(CHALLENGE.GET_ACCEPT_ANY_DEFAULT_JSON, "16", "GET /todos (200) ANY",
                "Issue a GET request on the `/todos` end point with an `Accept` header of `*/*` to receive results in default JSON format");
        acceptChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/get-todos-any-200/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "O4DhJ8Ohkk8");

        aChallenge =
            createChallenge(CHALLENGE.GET_ACCEPT_XML_PREFERRED, "17", "GET /todos (200) XML pref",
                "Issue a GET request on the `/todos` end point with an `Accept` header of `application/xml, application/json` to receive results in the preferred XML format");
        acceptChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/get-todos-xml-preference-200/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "sLChuy9pc9U");

        aChallenge =
            createChallenge(CHALLENGE.GET_JSON_BY_DEFAULT_NO_ACCEPT, "18", "GET /todos (200) no accept",
                "Issue a GET request on the `/todos` end point with no `Accept` header present in the message to receive results in default JSON format");
        acceptChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/get-todos-no-accept-200/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "CSVP2PcvOdg");

        aChallenge =
            createChallenge(CHALLENGE.GET_UNSUPPORTED_ACCEPT_406, "19", "GET /todos (406)",
                "Issue a GET request on the `/todos` end point with an `Accept` header `application/gzip` to receive 406 'NOT ACCEPTABLE' status code");
        acceptChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/19-get-todos-invalid-accept-406/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "QzfbegkY1ok");



        ChallengeSection contentTypeChallenges = new ChallengeSection("Content-Type Challenges",
                "The `Content-Type` header, tells the server what format type your 'body' content is, e.g. are you sending XML or JSON.");
        sections.add(contentTypeChallenges);

        // POST control content type, and Accepting only XML ie. Accept header of `application/xml`
        //      control content type to create with - XML
        aChallenge =
            createChallenge(CHALLENGE.POST_CREATE_XML, "20", "POST /todos XML",
                "Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/xml`, and Accepting only XML ie. Accept header of `application/xml`");
        contentTypeChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/20-post-todos-xml/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "2-KBYHwb7MM");

        //      control content type to create with - JSON
        aChallenge =
            createChallenge(CHALLENGE.POST_CREATE_JSON, "21", "POST /todos JSON",
                "Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/json`, and Accepting only JSON ie. Accept header of `application/json`");
        contentTypeChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/21-post-todos-json/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "VS9qIhgp51Q");

        //      content type not supported 415 e.g. form encoded
        aChallenge =
            createChallenge(CHALLENGE.POST_TODOS_415, "22", "POST /todos (415)",
                "Issue a POST request on the `/todos` end point with an unsupported content type to generate a 415 status code");
        contentTypeChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/22-post-todos-unsupported-415/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "L8H-vkbXyr0");



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
            createChallenge(CHALLENGE.POST_CREATE_XML_ACCEPT_JSON, "23", "POST /todos XML to JSON",
                "Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/xml` but Accept `application/json`");
        mixAcceptContentTypeChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/23-post-xml-accept-json/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "kfe7VtaV7u0");

        //      content type JSON - accept type XML
        aChallenge =
            createChallenge(CHALLENGE.POST_CREATE_JSON_ACCEPT_XML, "24", "POST /todos JSON to XML",
                "Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/json` but Accept `application/xml`");
        mixAcceptContentTypeChallenges.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/24-post-json-accept-xml/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "uw1Jq8t1em4");


        ChallengeSection miscStatusCodes = new ChallengeSection("Status Code Challenges",
                "Status-codes are essential to understand, so we created some challenges that help you trigger more status codes. Remember to review httpstatuses.com to learn what the status codes mean.");
        sections.add(miscStatusCodes);

        // extra status code challenges
        //      method not allowed - 405
        aChallenge =
            createChallenge(CHALLENGE.DELETE_HEARTBEAT_405, "25", "DELETE /heartbeat (405)",
                "Issue a DELETE request on the `/heartbeat` end point and receive 405 (Method Not Allowed)");
        miscStatusCodes.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/25-26-27-28-status-codes-405-500-501-204/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");

        // cannot process request server error 500
        aChallenge =
            createChallenge(CHALLENGE.PATCH_HEARTBEAT_500, "26", "PATCH /heartbeat (500)",
                "Issue a PATCH request on the `/heartbeat` end point and receive 500 (internal server error)");
        miscStatusCodes.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/25-26-27-28-status-codes-405-500-501-204/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");

        // 501
        aChallenge =
            createChallenge(CHALLENGE.TRACE_HEARTBEAT_501, "27", "TRACE /heartbeat (501)",
                "Issue a TRACE request on the `/heartbeat` end point and receive 501 (Not Implemented)");
        miscStatusCodes.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/25-26-27-28-status-codes-405-500-501-204/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");

        // No Content 204 - ping
        aChallenge =
            createChallenge(CHALLENGE.GET_HEARTBEAT_204, "28", "GET /heartbeat (204)",
                "Issue a GET request on the `/heartbeat` end point and receive 204 when server is running");
        miscStatusCodes.addChallenge(aChallenge);
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/25-26-27-28-status-codes-405-500-501-204/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "SGfKVFdylVI");


        ChallengeSection authenticationChallenges = new ChallengeSection("Authentication Challenges",
                "Authentication is telling the system who you are. In multi-user mode you are already doing that with the X-CHALLENGER header, but we have added an extra level of security on the /secret section. So first Authenticate with Basic Authentication to find out the token to use for authorisation for later challenges.");
        sections.add(authenticationChallenges);

        // authorization and authentication
        //    POST /secret/token with incorrect username and password credentials get 401
        aChallenge =
            createChallenge(CHALLENGE.CREATE_SECRET_TOKEN_401, "29", "POST /secret/token (401)",
                "Issue a POST request on the `/secret/token` end point and receive 401 when Basic auth username/password is not admin/password");
        authenticationChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/29-authentication-post-secret-token/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "RSQGADU3SLA");

        //    POST /secret/token with correct username and password credentials get secret token 201
        aChallenge =
            createChallenge(CHALLENGE.CREATE_SECRET_TOKEN_201, "30", "POST /secret/token (201)",
                "Issue a POST request on the `/secret/token` end point and receive 201 when Basic auth username/password is admin/password");
        authenticationChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/30-authentication-post-secret-token-201/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "J2GQiuEfHkI");

        ChallengeSection authorizationChallenges = new ChallengeSection("Authorization Challenges",
                "Once the system knows who you are, authorization is if you have the correct level of access. In these challenges the authorization is granted using a custom API header X-AUTH-TOKEN or using a Bearer Authorization header.");
        sections.add(authorizationChallenges);

        //    GET /secret/note with no token and 403
        aChallenge =
            createChallenge(CHALLENGE.GET_SECRET_NOTE_403, "31", "GET /secret/note (403)",
                "Issue a GET request on the `/secret/note` end point and receive 403 when X-AUTH-TOKEN does not match a valid token");
        authorizationChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/31-secret-note-forbidden-403/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "77mnUQezdas");

        //    GET /secret/note with invalid token and 401
        aChallenge =
            createChallenge(CHALLENGE.GET_SECRET_NOTE_401, "32", "GET /secret/note (401)",
                "Issue a GET request on the `/secret/note` end point and receive 401 when no X-AUTH-TOKEN header present");
        authorizationChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/32-secret-note-401-unauthorized/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "__uZlQZ48io");

        //    GET /secret/note with token and see token
        aChallenge =
                createChallenge(CHALLENGE.GET_SECRET_NOTE_200, "33", "GET /secret/note (200)",
                        "Issue a GET request on the `/secret/note` end point receive 200 when valid X-AUTH-TOKEN used - response body should contain the note");
        authorizationChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/33-authorized-get-secret-note-200/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "2uRpzr2OmEY");

        //    POST /secret/note with token and update secret note
        aChallenge =
                createChallenge(CHALLENGE.POST_SECRET_NOTE_200, "34","POST /secret/note (200)",
                        "Issue a POST request on the `/secret/note` end point with a note payload e.g. {\"note\":\"my note\"} and receive 200 when valid X-AUTH-TOKEN used. Note is maximum length 100 chars and will be truncated when stored.");
        authorizationChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/34-post-amend-secret-note-200/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "A9T9yjzEOEE");

        //    POST /secret/note with invalid token and 401
        aChallenge =
                createChallenge(CHALLENGE.POST_SECRET_NOTE_401, "35","POST /secret/note (401)",
                        "Issue a POST request on the `/secret/note` end point with a note payload {\"note\":\"my note\"} and receive 401 when no X-AUTH-TOKEN present");
        authorizationChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/35-36-post-unauthorised-401-403/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "76U5TEvLzLI");

        //    POST /secret/note with no token and 403
        aChallenge =
            createChallenge(CHALLENGE.POST_SECRET_NOTE_403, "36","POST /secret/note (403)",
                "Issue a POST request on the `/secret/note` end point with a note payload {\"note\":\"my note\"} and receive 403 when X-AUTH-TOKEN does not match a valid token");
        authorizationChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/35-36-post-unauthorised-401-403/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "76U5TEvLzLI");



        //    GET /secret/note with bearer token authorization
        aChallenge =
                createChallenge(CHALLENGE.GET_SECRET_NOTE_BEARER_200, "37","GET /secret/note (Bearer)",
                        "Issue a GET request on the `/secret/note` end point receive 200 when using the X-AUTH-TOKEN value as an Authorization Bearer token - response body should contain the note");
        authorizationChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/37-38-bearer-token-access/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "8GsMTZxEItw");

        //    POST /secret/note with token and update secret note
        aChallenge =
                createChallenge(CHALLENGE.POST_SECRET_NOTE_BEARER_200, "38","POST /secret/note (Bearer)",
                        "Issue a POST request on the `/secret/note` end point with a note payload e.g. {\"note\":\"my note\"} and receive 200 when valid X-AUTH-TOKEN value used as an Authorization Bearer token. Status code 200 received. Note is maximum length 100 chars and will be truncated when stored.");
        authorizationChallenges.addChallenge(aChallenge);
        aChallenge.addHint("Remember to add your X-CHALLENGER guid header");
        aChallenge.addSolutionLink("Read Solution", "HREF","https://www.eviltester.com/apichallenges/howto/37-38-bearer-token-access/");
        aChallenge.addSolutionLink("Watch Insomnia Solution", "YOUTUBE", "8GsMTZxEItw");




        // misc

        ChallengeSection miscChallenges = new ChallengeSection("Miscellaneous Challenges",
                "We left these challenges to the end because they seemed fun, but... different.");
        sections.add(miscChallenges);

        // todo: consider only enabling this challenge in single user mode
        aChallenge =
            createChallenge(CHALLENGE.DELETE_ALL_TODOS, "39","DELETE /todos/{id} (200) all",
                "Issue a DELETE request to successfully delete the last todo in system so that there are no more todos in the system");
        miscChallenges.addChallenge(aChallenge);
        aChallenge.addHint("After deleting the last todo, there will be no todos left in the application");
        aChallenge.addHint("Make sure you don't use {id} in the url, replace that with the id of a todo e.g. /todos/1");
        aChallenge.addHint("You have to delete all the todo items in the system to complete this challenge");


        Set challengeNames = new HashSet();
        for(ChallengeDefinitionData challenge : orderedChallenges){
            System.out.println("Challenge: " + challenge.name);
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
