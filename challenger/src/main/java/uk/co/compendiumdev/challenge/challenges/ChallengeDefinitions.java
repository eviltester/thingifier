package uk.co.compendiumdev.challenge.challenges;

import spark.Spark;
import uk.co.compendiumdev.challenge.CHALLENGE;

import java.util.*;

public class ChallengeDefinitions {

    private final List<ChallengeSection> sections;
    Map<CHALLENGE, ChallengeData> challengeData;
    List<ChallengeData> orderedChallenges;

    public Collection<ChallengeData> getChallenges() {
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
        getStarted.addChallenge(
                createChallenge(CHALLENGE.CREATE_NEW_CHALLENGER, "POST /challenger (201)",
                "Issue a POST request on the `/challenger` end point, with no body, to create a new challenger session. Use the generated X-CHALLENGER header in future requests to track challenge completion."));



        ChallengeSection firstChallenge = new ChallengeSection("First Real Challenge",
                "For your first challenge, get the list of challenges. You'll be able to use this to see your progress in your API Client, as well as using the GUI.");

        sections.add(firstChallenge);

        // READ
        firstChallenge.addChallenge(
                createChallenge(CHALLENGE.GET_CHALLENGES, "GET /challenges (200)",
                "Issue a GET request on the `/challenges` end point"));

        ChallengeSection getChallenges = new ChallengeSection("GET Challenges",
                "To retrieve, or read information from an API we issue GET requests. This section has a bunch of GET request challenges to try out.");
        sections.add(getChallenges);

        getChallenges.addChallenge(
                createChallenge(CHALLENGE.GET_TODOS, "GET /todos (200)",
                "Issue a GET request on the `/todos` end point"));

        getChallenges.addChallenge(
            createChallenge(CHALLENGE.GET_TODOS_NOT_PLURAL_404, "GET /todo (404) not plural",
                "Issue a GET request on the `/todo` end point should 404 because nouns should be plural"));

        getChallenges.addChallenge(
            createChallenge(CHALLENGE.GET_TODO, "GET /todos/{id} (200)",
                "Issue a GET request on the `/todos/{id}` end point to return a specific todo"));

        getChallenges.addChallenge(
            createChallenge(CHALLENGE.GET_TODO_404, "GET /todos/{id} (404)",
                "Issue a GET request on the `/todos/{id}` end point for a todo that does not exist"));


        // HEAD
        ChallengeSection headChallenges = new ChallengeSection("HEAD Challenges",
                "A HEAD request, is like a GET request, but only returns the headers and status code.");
        sections.add(headChallenges);

        headChallenges.addChallenge(
                createChallenge(CHALLENGE.GET_HEAD_TODOS, "HEAD /todos (200)",
                "Issue a HEAD request on the `/todos` end point"));


        // CREATE
        ChallengeSection postCreateChallenges = new ChallengeSection("Creation Challenges with POST",
                "A POST request can be used to create and update data, these challenges are to 'create' data. As a Hint, if you are not sure what the message body should be, try copying in the response from the assocated GET request, and amending it.");
        sections.add(postCreateChallenges);

        postCreateChallenges.addChallenge(
            createChallenge(CHALLENGE.POST_TODOS, "POST /todos (201)",
                "Issue a POST request to successfully create a todo"));

        postCreateChallenges.addChallenge(
                createChallenge(CHALLENGE.GET_TODOS_FILTERED, "GET /todos (200) ?filter",
                        "Issue a GET request on the `/todos` end point with a query filter to get only todos which are 'done'. There must exist both 'done' and 'not done' todos, to pass this challenge."));

        postCreateChallenges.addChallenge(
            createChallenge(CHALLENGE.POST_TODOS_BAD_DONE_STATUS, "POST /todos (400) doneStatus",
                "Issue a POST request to create a todo but fail validation on the `doneStatus` field"));

        // UPDATE
        ChallengeSection postUpdateChallenges = new ChallengeSection("Update Challenges with POST",
                "Use a POST request to amend something that already exists. These are 'partial' content updates so you useually don't need to have all details of the entity in the request, e.g. you could just update a title, or a description, or a status");
        sections.add(postUpdateChallenges);

        postUpdateChallenges.addChallenge(
            createChallenge(CHALLENGE.POST_UPDATE_TODO, "POST /todos/{id} (200)",
                "Issue a POST request to successfully update a todo"));

        // DELETE
        ChallengeSection deleteChallenges = new ChallengeSection("DELETE Challenges",
                "Use a DELETE request to delete an entity. Since this is an extreme request, normall you have to be logged in or authenticated, but we wanted to make life easier for you so we cover authentication later. Anyone can delete To Do items without authentication in this system.");
        sections.add(deleteChallenges);

        deleteChallenges.addChallenge(
            createChallenge(CHALLENGE.DELETE_A_TODO, "DELETE /todos/{id} (200)",
                "Issue a DELETE request to successfully delete a todo"));


        // OPTIONS
        ChallengeSection optionsChallenges = new ChallengeSection("OPTIONS Challenges",
                "Use an OPTIONS verb and check the `Allow` header, this will show you what verbs are allowed to be used on an endpoint. When you test APIs it is worth checking to see if all the verbs listed are allowed or not.");
        sections.add(optionsChallenges);

        optionsChallenges.addChallenge(
            createChallenge(CHALLENGE.OPTIONS_TODOS, "OPTIONS /todos (200)",
                "Issue an OPTIONS request on the `/todos` end point. You might want to manually check the 'Allow' header in the response is as expected."));

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

        acceptChallenges.addChallenge(
            createChallenge(CHALLENGE.GET_ACCEPT_XML, "GET /todos (200) XML",
                "Issue a GET request on the `/todos` end point with an `Accept` header of `application/xml` to receive results in XML format"));

        acceptChallenges.addChallenge(
            createChallenge(CHALLENGE.GET_ACCEPT_JSON, "GET /todos (200) JSON",
                "Issue a GET request on the `/todos` end point with an `Accept` header of `application/json` to receive results in JSON format"));

        acceptChallenges.addChallenge(
            createChallenge(CHALLENGE.GET_ACCEPT_ANY_DEFAULT_JSON, "GET /todos (200) ANY",
                "Issue a GET request on the `/todos` end point with an `Accept` header of `*/*` to receive results in default JSON format"));

        acceptChallenges.addChallenge(
            createChallenge(CHALLENGE.GET_ACCEPT_XML_PREFERRED, "GET /todos (200) XML pref",
                "Issue a GET request on the `/todos` end point with an `Accept` header of `application/xml, application/json` to receive results in the preferred XML format"));

        acceptChallenges.addChallenge(
            createChallenge(CHALLENGE.GET_JSON_BY_DEFAULT_NO_ACCEPT, "GET /todos (200) no accept",
                "Issue a GET request on the `/todos` end point with no `Accept` header present in the message to receive results in default JSON format"));

        acceptChallenges.addChallenge(
            createChallenge(CHALLENGE.GET_UNSUPPORTED_ACCEPT_406, "GET /todos (406)",
                "Issue a GET request on the `/todos` end point with an `Accept` header `application/gzip` to receive 406 'NOT ACCEPTABLE' status code"));



        ChallengeSection contentTypeChallenges = new ChallengeSection("Content-Type Challenges",
                "The `Content-Type` header, tells the server what format type your 'body' content is, e.g. are you sending XML or JSON.");
        sections.add(contentTypeChallenges);

        // POST control content type, and Accepting only XML ie. Accept header of `application/xml`
        //      control content type to create with - XML
        contentTypeChallenges.addChallenge(
            createChallenge(CHALLENGE.POST_CREATE_XML, "POST /todos XML",
                "Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/xml`, and Accepting only XML ie. Accept header of `application/xml`"));

        //      control content type to create with - JSON
        contentTypeChallenges.addChallenge(
            createChallenge(CHALLENGE.POST_CREATE_JSON, "POST /todos JSON",
                "Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/json`, and Accepting only JSON ie. Accept header of `application/json`"));

        //      content type not supported 415 e.g. form encoded
        contentTypeChallenges.addChallenge(
            createChallenge(CHALLENGE.POST_TODOS_415, "POST /todos (415)",
                "Issue a POST request on the `/todos` end point with an unsupported content type to generate a 415 status code"));



        // POST
        // todo:    POST is not idempotent - same values, different results i.e. id different

        // PUT
        // todo:     PUT is idempotent - same result each time

        // POST mixed content and accept
        //      content type XML - accept type JSON
        ChallengeSection mixAcceptContentTypeChallenges = new ChallengeSection("Mix Accept and Content-Type Challenges",
                "We can mix the `Accept` and `Content-Type` headers so that we can send JSON but receive XML. These challenges encourage you to explore some combinations.");
        sections.add(mixAcceptContentTypeChallenges);

        mixAcceptContentTypeChallenges.addChallenge(
            createChallenge(CHALLENGE.POST_CREATE_XML_ACCEPT_JSON, "POST /todos XML to JSON",
                "Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/xml` but Accept `application/json`"));

        //      content type JSON - accept type XML
        mixAcceptContentTypeChallenges.addChallenge(
            createChallenge(CHALLENGE.POST_CREATE_JSON_ACCEPT_XML, "POST /todos JSON to XML",
                "Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/json` but Accept `application/xml`"));


        ChallengeSection miscStatusCodes = new ChallengeSection("Status Code Challenges",
                "Status-codes are essential to understand, so we created some challenges that help you trigger more status codes. Remember to review httpstatuses.com to learn what the status codes mean.");
        sections.add(miscStatusCodes);

        // extra status code challenges
        //      method not allowed - 405
        miscStatusCodes.addChallenge(
            createChallenge(CHALLENGE.DELETE_HEARTBEAT_405, "DELETE /heartbeat (405)",
                "Issue a DELETE request on the `/heartbeat` end point and receive 405 (Method Not Allowed)"));

        // cannot process request server error 500
        miscStatusCodes.addChallenge(
            createChallenge(CHALLENGE.PATCH_HEARTBEAT_500, "PATCH /heartbeat (500)",
                "Issue a PATCH request on the `/heartbeat` end point and receive 500 (internal server error)"));

        // 501
        miscStatusCodes.addChallenge(
            createChallenge(CHALLENGE.TRACE_HEARTBEAT_501, "TRACE /heartbeat (501)",
                "Issue a TRACE request on the `/heartbeat` end point and receive 501 (Not Implemented)"));

        // No Content 204 - ping
        miscStatusCodes.addChallenge(
            createChallenge(CHALLENGE.GET_HEARTBEAT_204, "GET /heartbeat (204)",
                "Issue a GET request on the `/heartbeat` end point and receive 204 when server is running"));


        ChallengeSection authenticationChallenges = new ChallengeSection("Authentication Challenges",
                "Authentication is telling the system who you are. In multi-user mode you are already doing that with the X-CHALLENGER header, but we have added an extra level of security on the /secret section. So first Authenticate with Basic Authentication to find out the token to use for authorisation for later challenges.");
        sections.add(authenticationChallenges);

        // authorization and authentication
        //    POST /secret/token with incorrect username and password credentials get 401
        authenticationChallenges.addChallenge(
            createChallenge(CHALLENGE.CREATE_SECRET_TOKEN_401, "POST /secret/token (401)",
                "Issue a POST request on the `/secret/token` end point and receive 401 when Basic auth username/password is not admin/password"));

        //    POST /secret/token with correct username and password credentials get secret token 201
        authenticationChallenges.addChallenge(
            createChallenge(CHALLENGE.CREATE_SECRET_TOKEN_201, "POST /secret/token (201)",
                "Issue a POST request on the `/secret/token` end point and receive 201 when Basic auth username/password is admin/password"));

        ChallengeSection authorizationChallenges = new ChallengeSection("Authorization Challenges",
                "Once the system knows who you are, authorization is if you have the correct level of access. In these challenges the authorization is granted using a custom API header X-AUTH-TOKEN or using a Bearer Authorization header.");
        sections.add(authorizationChallenges);

        //    GET /secret/note with no token and 403
        authorizationChallenges.addChallenge(
            createChallenge(CHALLENGE.GET_SECRET_NOTE_403, "GET /secret/note (403)",
                "Issue a GET request on the `/secret/note` end point and receive 403 when X-AUTH-TOKEN does not match a valid token"));

        //    GET /secret/note with invalid token and 401
        authorizationChallenges.addChallenge(
            createChallenge(CHALLENGE.GET_SECRET_NOTE_401, "GET /secret/note (401)",
                "Issue a GET request on the `/secret/note` end point and receive 401 when no X-AUTH-TOKEN header present"));

        //    GET /secret/note with token and see token
        authorizationChallenges.addChallenge(
                createChallenge(CHALLENGE.GET_SECRET_NOTE_200, "GET /secret/note (200)",
                        "Issue a GET request on the `/secret/note` end point receive 200 when valid X-AUTH-TOKEN used - response body should contain the note"));

        //    POST /secret/note with token and update secret note
        authorizationChallenges.addChallenge(
                createChallenge(CHALLENGE.POST_SECRET_NOTE_200, "POST /secret/note (200)",
                        "Issue a POST request on the `/secret/note` end point with a note payload e.g. {\"note\":\"my note\"} and receive 200 when valid X-AUTH-TOKEN used. Note is maximum length 100 chars and will be truncated when stored."));

        //    POST /secret/note with invalid token and 401
        authorizationChallenges.addChallenge(
                createChallenge(CHALLENGE.POST_SECRET_NOTE_401, "POST /secret/note (401)",
                        "Issue a POST request on the `/secret/note` end point with a note payload {\"note\":\"my note\"} and receive 401 when no X-AUTH-TOKEN present"));

        //    POST /secret/note with no token and 403
        authorizationChallenges.addChallenge(
            createChallenge(CHALLENGE.POST_SECRET_NOTE_403, "POST /secret/note (403)",
                "Issue a POST request on the `/secret/note` end point with a note payload {\"note\":\"my note\"} and receive 403 when X-AUTH-TOKEN does not match a valid token"));



        //    GET /secret/note with bearer token authorization
        authorizationChallenges.addChallenge(
                createChallenge(CHALLENGE.GET_SECRET_NOTE_BEARER_200, "GET /secret/note (Bearer)",
                        "Issue a GET request on the `/secret/note` end point receive 200 when using the X-AUTH-TOKEN value as an Authorization Bearer token - response body should contain the note"));

        //    POST /secret/note with token and update secret note
        authorizationChallenges.addChallenge(
                createChallenge(CHALLENGE.POST_SECRET_NOTE_BEARER_200, "POST /secret/note (Bearer)",
                        "Issue a POST request on the `/secret/note` end point with a note payload e.g. {\"note\":\"my note\"} and receive 200 when valid X-AUTH-TOKEN value used as an Authorization Bearer token. Status code 200 received. Note is maximum length 100 chars and will be truncated when stored."));




        // misc

        ChallengeSection miscChallenges = new ChallengeSection("Miscellaneous Challenges",
                "We left these challenges to the end because they seemed fun, but... different.");
        sections.add(miscChallenges);

        // todo: consider only enabling this challenge in single user mode
        miscChallenges.addChallenge(
            createChallenge(CHALLENGE.DELETE_ALL_TODOS, "DELETE /todos/{id} (200) all",
                "Issue a DELETE request to successfully delete the last todo in system so that there are no more todos in the system"));


        Set challengeNames = new HashSet();
        for(ChallengeData challenge : orderedChallenges){
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

    private ChallengeData createChallenge(final CHALLENGE id, final String name, final String description) {
        ChallengeData challenge = new ChallengeData( name, description);
        challengeData.put(id, challenge);
        orderedChallenges.add(challenge);
        return challenge;
    }


    public CHALLENGE getChallenge(final String name) {
        for(Map.Entry<CHALLENGE, ChallengeData>challenge : challengeData.entrySet()){
            if(challenge.getValue().name.contentEquals(name)){
                return challenge.getKey();
            }
        }
        return null;
    }
}
