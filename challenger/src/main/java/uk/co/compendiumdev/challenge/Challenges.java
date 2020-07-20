package uk.co.compendiumdev.challenge;

import com.google.gson.Gson;

import java.util.*;

public class Challenges {

    Map<CHALLENGE, ChallengeData> challengeStatus;
    List<ChallengeData> orderedChallengeStatus;

    public void pass(final CHALLENGE id) {
        try{
            challengeStatus.get(id).status=true;
        }catch(Exception e){
            System.out.println("Challenge Error " + id.name() + " " + e.getMessage() );
        }
    }

    public Collection<ChallengeData> getChallenges() {
        return orderedChallengeStatus;
    }

    public enum CHALLENGE{
        GET_CHALLENGES,
        GET_TODOS,
        GET_TODO,
        GET_TODO_404,
        POST_TODOS,
        POST_UPDATE_TODO,
        POST_TODOS_BAD_DONE_STATUS,
        DELETE_A_TODO,
        DELETE_ALL_TODOS,
        GET_TODOS_FILTERED,
        GET_TODOS_NOT_PLURAL_404,
        OPTIONS_TODOS,
        GET_HEAD_TODOS,
        POST_TODOS_415,
        GET_ACCEPT_XML, GET_ACCEPT_JSON;
    }

    public Challenges(){
        challengeStatus = new HashMap<>();
        orderedChallengeStatus = new ArrayList<>();

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
                "Issue a DELETE request to successfully delete the last todo");

        // OPTIONS
        addChallenge(CHALLENGE.OPTIONS_TODOS, "OPTIONS /todos (200)",
                "Issue an OPTIONS request on the `/todos` end point to check the 'Allow' header in the resonse");

        // GET accept type
        //      specify accept type - XML
        //      specify accept type - JSON
        //      specify accept type - */* (ANY) to get default
        //      specify multiple accept type with a preference for XML - should receive XML
        //      none specified - get default
        //      cannot supply accepted type 406

        addChallenge(CHALLENGE.GET_ACCEPT_XML, "GET /todos (200) XML",
                "Issue a GET request on the `/todos` end point with a Content-Type header of `application/xml` to receive results in XML format");

        addChallenge(CHALLENGE.GET_ACCEPT_JSON, "GET /todos (200) JSON",
                "Issue a GET request on the `/todos` end point with a Content-Type header of `application/json` to receive results in JSON format");


        // todo: expand out the challenges

        // POST control content type
        //      control content type - XML
        //      control content type - JSON
        //      content type not supported 415 e.g. form encoded
        addChallenge(CHALLENGE.POST_TODOS_415, "POST /todos (415)",
                "Issue a POST request on the `/todos` end point with an unsupported content type to generate a 415 status code");



        // POST
        //      not idempotent - same values, different results i.e. id different



        // PUT
        //      idempotent - same result each time

        // POST to create with
        //      content type XML - accept type JSON
        //      content type JSON - accept type XML

        // status code challenges
        //     method not allowed - 405



        Set challengeNames = new HashSet();
        for(ChallengeData challenge : orderedChallengeStatus){
            System.out.println("Challenge: " + challenge.name);
            challengeNames.add(challenge.name);
        }
        if(challengeNames.size()!=orderedChallengeStatus.size()) {
            throw new RuntimeException(
                    "Number of names, does not match number of challenges" +
                            ", possible duplicate name");
        }
    }

    private void addChallenge(final CHALLENGE id, final String name, final String description) {
        ChallengeData challenge = new ChallengeData( name, description);
        challengeStatus.put(id, challenge);
        orderedChallengeStatus.add(challenge);
    }

    private class ChallengesPayload{
        List<ChallengeData> challenges;
    }

    public String getAsJson(){
        final ChallengesPayload payload = new ChallengesPayload();
        payload.challenges = orderedChallengeStatus;
        return new Gson().toJson(payload);
    }
}
