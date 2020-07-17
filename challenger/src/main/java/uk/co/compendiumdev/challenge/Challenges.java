package uk.co.compendiumdev.challenge;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class Challenges {

    Map<CHALLENGE, ChallengeData> challengeStatus;

    public void pass(final CHALLENGE id) {
        try{
            challengeStatus.get(id).status=true;
        }catch(Exception e){
            System.out.println("Challenge Error " + id.name() + " " + e.getMessage() );
        }
    }

    public enum CHALLENGE{
        GET_TODOS, GET_TODO, GET_TODO_404;
    }

    public Challenges(){
        challengeStatus = new HashMap<>();

        addChallenge(CHALLENGE.GET_TODOS, "GET /todos",
                "Issue a GET request on the `/todos` end point");
        addChallenge(CHALLENGE.GET_TODO, "GET /todos/{guid}",
                "Issue a GET request on the `/todos/{guid}` end point to return a specific todo");
        addChallenge(CHALLENGE.GET_TODO_404, "GET /todos/{guid}",
                "Issue a GET request on the `/todos/{guid}` end point for a todo that does not exist");

    }

    private void addChallenge(final CHALLENGE id, final String name, final String description) {
        challengeStatus.put(id, new ChallengeData( name, description));
    }

    public String getAsJson(){
        return new Gson().toJson(challengeStatus);
    }
}
