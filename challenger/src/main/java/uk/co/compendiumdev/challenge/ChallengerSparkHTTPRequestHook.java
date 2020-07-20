package uk.co.compendiumdev.challenge;

import spark.Request;
import spark.Response;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks.SparkRequestResponseHook;

import static uk.co.compendiumdev.challenge.Challenges.CHALLENGE.*;

public class ChallengerSparkHTTPRequestHook implements SparkRequestResponseHook {
    private final Challenges challenges;

    public ChallengerSparkHTTPRequestHook(final Challenges challenges) {
        this.challenges = challenges;
    }

    @Override
    public void run(final Request request, final Response response) {

        if(request.requestMethod().toUpperCase().contentEquals("GET") &&
                request.pathInfo().contentEquals("/challenges")){
            challenges.pass(GET_CHALLENGES);
        }

        if(request.requestMethod().toUpperCase().contentEquals("GET") &&
                request.pathInfo().contentEquals("/heartbeat")){
            challenges.pass(GET_HEARTBEAT_204);
        }

        if(request.requestMethod().toUpperCase().contentEquals("DELETE") &&
                request.pathInfo().contentEquals("/heartbeat")){
            challenges.pass(DELETE_HEARTBEAT_405);
        }

        if(request.requestMethod().toUpperCase().contentEquals("PATCH") &&
                request.pathInfo().contentEquals("/heartbeat")){
            challenges.pass(PATCH_HEARTBEAT_500);
        }

        if(request.requestMethod().toUpperCase().contentEquals("TRACE") &&
                request.pathInfo().contentEquals("/heartbeat")){
            challenges.pass(TRACE_HEARTBEAT_501);
        }
    }
}
