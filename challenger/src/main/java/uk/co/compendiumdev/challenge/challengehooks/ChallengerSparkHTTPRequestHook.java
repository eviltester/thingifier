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

        String method = request.requestMethod().toUpperCase();
        String path = request.pathInfo();
//        if(method.equals("GET") &&
//                path.equals("/challenges")){
//            challengers.pass(challenger, CHALLENGE.GET_CHALLENGES);
//        }

//        if(method.equals("GET") &&
//                path.equals("/heartbeat")){
//            challengers.pass(challenger,CHALLENGE.GET_HEARTBEAT_204);
//        }

//        if(method.equals("DELETE") &&
//                path.equals("/heartbeat")){
//            challengers.pass(challenger,CHALLENGE.DELETE_HEARTBEAT_405);
//        }
//
//        if(method.equals("PATCH") &&
//                path.equals("/heartbeat")){
//            challengers.pass(challenger,CHALLENGE.PATCH_HEARTBEAT_500);
//        }
//
//        if(method.equals("TRACE") &&
//                path.equals("/heartbeat")){
//            challengers.pass(challenger,CHALLENGE.TRACE_HEARTBEAT_501);
//        }
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
