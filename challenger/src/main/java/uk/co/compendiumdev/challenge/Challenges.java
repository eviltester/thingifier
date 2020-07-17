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
        GET_TODOS, GET_TODO, GET_TODO_404, POST_TODOS, GET_CHALLENGES, POST_TODOS_BAD_DONE_STATUS, DELETE_A_TODO, DELETE_ALL_TODOS, POST_UPDATE_TODO;
    }

    public Challenges(){
        challengeStatus = new HashMap<>();
        orderedChallengeStatus = new ArrayList<>();

        /* todo: Basic set of CRUD challenges */

        // READ
        addChallenge(CHALLENGE.GET_CHALLENGES, "GET /challenges (200)",
                "Issue a GET request on the `/challenges` end point");

        addChallenge(CHALLENGE.GET_TODOS, "GET /todos (200)",
                "Issue a GET request on the `/todos` end point");
        addChallenge(CHALLENGE.GET_TODO, "GET /todos/{id} (200)",
                "Issue a GET request on the `/todos/{id}` end point to return a specific todo");
        addChallenge(CHALLENGE.GET_TODO_404, "GET /todos/{id} (404)",
                "Issue a GET request on the `/todos/{id}` end point for a todo that does not exist");

        // CREATE
        addChallenge(CHALLENGE.POST_TODOS, "POST /todos (201)",
                "Issue a POST request to successfully create a todo");
        addChallenge(CHALLENGE.POST_TODOS_BAD_DONE_STATUS, "POST /todos (400)",
                "Issue a POST request to create a todo but fail validation on the `doneStatus` field");

        // UPDATE
        addChallenge(CHALLENGE.POST_UPDATE_TODO, "POST /todos.{id} (200)",
                "Issue a POST request to successfully update a todo");

        // DELETE
        addChallenge(CHALLENGE.DELETE_A_TODO, "DELETE /todos/{id} (200)",
                "Issue a DELETE request to successfully delete a todo");
        addChallenge(CHALLENGE.DELETE_ALL_TODOS, "DELETE /todos/{id} (200)",
                "Issue a DELETE request to successfully delete the last todo");

        // todo: expand out the challenges
        // PUT
        // OPTIONS
        // HEAD
        // status code challenges
        // method not allowed
    }

    private void addChallenge(final CHALLENGE id, final String name, final String description) {
        ChallengeData challenge = new ChallengeData( name, description);
        challengeStatus.put(id, challenge);
        orderedChallengeStatus.add(challenge);
    }

    public String getAsJson(){
        return new Gson().toJson(orderedChallengeStatus);
    }
}
