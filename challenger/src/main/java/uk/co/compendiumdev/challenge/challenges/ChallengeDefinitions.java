package uk.co.compendiumdev.challenge.challenges;

import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.challenges.ChallengeData;

import java.util.*;

public class ChallengeDefinitions {

    Map<CHALLENGE, ChallengeData> challengeData;
    List<ChallengeData> orderedChallenges;

    public Collection<ChallengeData> getChallenges() {
        return orderedChallenges;
    }

    public ChallengeDefinitions(){

        challengeData = new HashMap<>();
        orderedChallenges = new ArrayList<>();

        // todo: challenge to GET /challenger/{guid} and restore a challenger session
        // todo: challenge to POST /challenger with X-CHALLENGER header of existing challenger to restore a challenger
        // todo: challenge to POST /challenger with X-CHALLENGER header of existing challenger and body of a stored challenge set to update the challenger in the system

        // create a challenger to persist challenge sessions
        // CREATE_NEW_CHALLENGER
        addChallenge(CHALLENGE.CREATE_NEW_CHALLENGER, "POST /challenger (201)",
                "Issue a POST request on the `/challenger` end point, with no body, to create a new challenger session. Use the generated X-CHALLENGER header in future requests to track challenge completion.");

        // READ
        addChallenge(CHALLENGE.GET_CHALLENGES, "GET /challenges (200)",
                "Issue a GET request on the `/challenges` end point");

        addChallenge(CHALLENGE.GET_TODOS, "GET /todos (200)",
                "Issue a GET request on the `/todos` end point");

        addChallenge(CHALLENGE.GET_TODOS_NOT_PLURAL_404, "GET /todo (404) not plural",
                "Issue a GET request on the `/todo` end point should 404 because nouns should be plural");

        addChallenge(CHALLENGE.GET_TODO, "GET /todos/{id} (200)",
                "Issue a GET request on the `/todos/{id}` end point to return a specific todo");

        addChallenge(CHALLENGE.GET_TODO_404, "GET /todos/{id} (404)",
                "Issue a GET request on the `/todos/{id}` end point for a todo that does not exist");

        addChallenge(CHALLENGE.GET_TODOS_FILTERED, "GET /todos (200) ?filter",
                "Issue a GET request on the `/todos` end point with a query filter to get only todos which are 'done'. There must exist both 'done' and 'not done' todos, to pass this challenge.");

        // HEAD
        addChallenge(CHALLENGE.GET_HEAD_TODOS, "HEAD /todos (200)",
                "Issue a HEAD request on the `/todos` end point");


        // CREATE
        addChallenge(CHALLENGE.POST_TODOS, "POST /todos (201)",
                "Issue a POST request to successfully create a todo");

        addChallenge(CHALLENGE.POST_TODOS_BAD_DONE_STATUS, "POST /todos (400) doneStatus",
                "Issue a POST request to create a todo but fail validation on the `doneStatus` field");

        // UPDATE
        addChallenge(CHALLENGE.POST_UPDATE_TODO, "POST /todos/{id} (200)",
                "Issue a POST request to successfully update a todo");

        // DELETE
        addChallenge(CHALLENGE.DELETE_A_TODO, "DELETE /todos/{id} (200)",
                "Issue a DELETE request to successfully delete a todo");

        addChallenge(CHALLENGE.DELETE_ALL_TODOS, "DELETE /todos/{id} (200) all",
                "Issue a DELETE request to successfully delete the last todo in system so that there are no more todos in the system");

        // OPTIONS
        addChallenge(CHALLENGE.OPTIONS_TODOS, "OPTIONS /todos (200)",
                "Issue an OPTIONS request on the `/todos` end point. You might want to manually check the 'Allow' header in the response is as expected.");

        // GET accept type
        //      specify accept type - XML
        //      specify accept type - JSON
        //      specify accept type - */* (ANY) to get default
        //      specify multiple accept type with a preference for XML - should receive XML
        //      none specified - get default
        //      cannot supply accepted type 406

        addChallenge(CHALLENGE.GET_ACCEPT_XML, "GET /todos (200) XML",
                "Issue a GET request on the `/todos` end point with an `Accept` header of `application/xml` to receive results in XML format");

        addChallenge(CHALLENGE.GET_ACCEPT_JSON, "GET /todos (200) JSON",
                "Issue a GET request on the `/todos` end point with an `Accept` header of `application/json` to receive results in JSON format");

        addChallenge(CHALLENGE.GET_ACCEPT_ANY_DEFAULT_JSON, "GET /todos (200) ANY",
                "Issue a GET request on the `/todos` end point with an `Accept` header of `*/*` to receive results in default JSON format");

        addChallenge(CHALLENGE.GET_ACCEPT_XML_PREFERRED, "GET /todos (200) XML pref",
                "Issue a GET request on the `/todos` end point with an `Accept` header of `application/xml, application/json` to receive results in the preferred XML format");

        addChallenge(CHALLENGE.GET_JSON_BY_DEFAULT_NO_ACCEPT, "GET /todos (200) no accept",
                "Issue a GET request on the `/todos` end point with no `Accept` header present in the message to receive results in default JSON format");

        addChallenge(CHALLENGE.GET_UNSUPPORTED_ACCEPT_406, "GET /todos (406)",
                "Issue a GET request on the `/todos` end point with an `Accept` header `application/gzip` to receive 406 'NOT ACCEPTABLE' status code");



        // POST control content type
        //      control content type to create with - XML
        addChallenge(CHALLENGE.POST_CREATE_XML, "POST /todos XML",
                "Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/xml`");

        //      control content type to create with - JSON
        addChallenge(CHALLENGE.POST_CREATE_JSON, "POST /todos JSON",
                "Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/json`");

        //      content type not supported 415 e.g. form encoded
        addChallenge(CHALLENGE.POST_TODOS_415, "POST /todos (415)",
                "Issue a POST request on the `/todos` end point with an unsupported content type to generate a 415 status code");



        // POST
        // todo:    POST is not idempotent - same values, different results i.e. id different

        // PUT
        // todo:     PUT is idempotent - same result each time

        // POST mixed content and accept
        //      content type XML - accept type JSON
        addChallenge(CHALLENGE.POST_CREATE_XML_ACCEPT_JSON, "POST /todos XML to JSON",
                "Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/xml` but Accept `application/json`");

        //      content type JSON - accept type XML
        addChallenge(CHALLENGE.POST_CREATE_JSON_ACCEPT_XML, "POST /todos JSON to XML",
                "Issue a POST request on the `/todos` end point to create a todo using Content-Type `application/json` but Accept `application/xml`");


        // extra status code challenges
        //      method not allowed - 405
        addChallenge(CHALLENGE.DELETE_HEARTBEAT_405, "DELETE /heartbeat (405)",
                "Issue a DELETE request on the `/heartbeat` end point and receive 405 (Method Not Allowed)");

        // cannot process request server error 500
        addChallenge(CHALLENGE.PATCH_HEARTBEAT_500, "PATCH /heartbeat (500)",
                "Issue a PATCH request on the `/heartbeat` end point and receive 500 (internal server error)");

        // 501
        addChallenge(CHALLENGE.TRACE_HEARTBEAT_501, "TRACE /heartbeat (501)",
                "Issue a TRACE request on the `/heartbeat` end point and receive 501 (Not Implemented)");

        // No Content 204 - ping
        addChallenge(CHALLENGE.GET_HEARTBEAT_204, "GET /heartbeat (204)",
                "Issue a GET request on the `/heartbeat` end point and receive 204 when server is running");

        // authorization and authentication
        //    POST /secret/token with incorrect username and password credentials get 401
        addChallenge(CHALLENGE.CREATE_SECRET_TOKEN_401, "POST /secret/token (401)",
                "Issue a POST request on the `/secret/token` end point and receive 401 when Basic auth username/password is not admin/password");

        //    POST /secret/token with correct username and password credentials get secret token 201
        addChallenge(CHALLENGE.CREATE_SECRET_TOKEN_201, "POST /secret/token (201)",
                "Issue a POST request on the `/secret/token` end point and receive 201 when Basic auth username/password is admin/password");

        //    GET /secret/note with no token and 403
        addChallenge(CHALLENGE.GET_SECRET_NOTE_403, "GET /secret/note (403)",
                "Issue a GET request on the `/secret/note` end point and receive 403 when X-AUTH-TOKEN does not match a valid token");

        //    GET /secret/note with invalid token and 401
        addChallenge(CHALLENGE.GET_SECRET_NOTE_401, "GET /secret/note (401)",
                "Issue a GET request on the `/secret/note` end point and receive 401 when no X-AUTH-TOKEN header present");

        //    POST /secret/note with no token and 403
        addChallenge(CHALLENGE.POST_SECRET_NOTE_403, "POST /secret/note (403)",
                "Issue a POST request on the `/secret/note` end point with a note payload {\"note\":\"my note\"} and receive 403 when X-AUTH-TOKEN does not match a valid token");

        //    POST /secret/note with invalid token and 401
        addChallenge(CHALLENGE.POST_SECRET_NOTE_401, "POST /secret/note (401)",
                "Issue a POST request on the `/secret/note` end point with a note payload {\"note\":\"my note\"} and receive 401 when no X-AUTH-TOKEN present");

        //    GET /secret/note with token and see token
        addChallenge(CHALLENGE.GET_SECRET_NOTE_200, "GET /secret/note (200)",
                "Issue a GET request on the `/secret/note` end point receive 200 when valid X-AUTH-TOKEN used - response body should contain the note");

        //    POST /secret/note with token and update secret note
        addChallenge(CHALLENGE.POST_SECRET_NOTE_200, "POST /secret/note (200)",
                "Issue a POST request on the `/secret/note` end point with a note payload e.g. {\"note\":\"my note\"} and receive 200 when valid X-AUTH-TOKEN used. Note is maximum length 100 chars and will be truncated when stored.");

        Set challengeNames = new HashSet();
        for(ChallengeData challenge : orderedChallenges){
            System.out.println("Challenge: " + challenge.name);
            challengeNames.add(challenge.name);
        }
        if(challengeNames.size()!= orderedChallenges.size()) {
            throw new RuntimeException(
                    "Number of names, does not match number of challenges" +
                            ", possible duplicate name");
        }
    }

    private void addChallenge(final CHALLENGE id, final String name, final String description) {
        ChallengeData challenge = new ChallengeData( name, description);
        challengeData.put(id, challenge);
        orderedChallenges.add(challenge);
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
