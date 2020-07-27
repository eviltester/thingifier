package uk.co.compendiumdev.challenge.challengehooks;

import spark.Request;
import spark.Response;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks.SparkRequestResponseHook;

public class ChallengerSparkHTTPRequestHook implements SparkRequestResponseHook {

    private final Challengers challengers;

    public ChallengerSparkHTTPRequestHook(final Challengers authData) {
        this.challengers = authData;
    }

    @Override
    public void run(final Request request, final Response response) {

        updateAuthTokenFrom(request.headers("X-CHALLENGER"));
        challengers.purgeOldAuthData();

        ChallengerAuthData challenger = challengers.getChallenger(request.headers("X-CHALLENGER"));
        if(challenger==null){
            // cannot track challenges
            return;
        }

        if(request.requestMethod().toUpperCase().contentEquals("GET") &&
                request.pathInfo().contentEquals("/challenges")){
            challengers.pass(challenger, CHALLENGE.GET_CHALLENGES);
        }

        if(request.requestMethod().toUpperCase().contentEquals("GET") &&
                request.pathInfo().contentEquals("/heartbeat")){
            challengers.pass(challenger,CHALLENGE.GET_HEARTBEAT_204);
        }

        if(request.requestMethod().toUpperCase().contentEquals("DELETE") &&
                request.pathInfo().contentEquals("/heartbeat")){
            challengers.pass(challenger,CHALLENGE.DELETE_HEARTBEAT_405);
        }

        if(request.requestMethod().toUpperCase().contentEquals("PATCH") &&
                request.pathInfo().contentEquals("/heartbeat")){
            challengers.pass(challenger,CHALLENGE.PATCH_HEARTBEAT_500);
        }

        if(request.requestMethod().toUpperCase().contentEquals("TRACE") &&
                request.pathInfo().contentEquals("/heartbeat")){
            challengers.pass(challenger,CHALLENGE.TRACE_HEARTBEAT_501);
        }
    }

    private void updateAuthTokenFrom(final String header) {
        if(header==null)
            return;

        ChallengerAuthData data = challengers.getChallenger(header);
        if(data!=null){
            data.touch();
        }
    }


}
