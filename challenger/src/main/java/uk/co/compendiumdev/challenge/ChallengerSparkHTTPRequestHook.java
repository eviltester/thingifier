package uk.co.compendiumdev.challenge;

import spark.Request;
import spark.Response;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks.SparkRequestResponseHook;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.co.compendiumdev.challenge.Challenges.CHALLENGE.*;

public class ChallengerSparkHTTPRequestHook implements SparkRequestResponseHook {
    private final Challenges challenges;
    private final Map<String, ChallengeAuthData> authData;

    public ChallengerSparkHTTPRequestHook(final Challenges challenges, final Map<String, ChallengeAuthData> authData) {
        this.challenges = challenges;
        this.authData = authData;
    }

    @Override
    public void run(final Request request, final Response response) {

        updateAuthTokenFrom(request.headers("X-AUTH-TOKEN"));
        purgeOldAuthData(1);

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

    private void updateAuthTokenFrom(final String header) {
        if(header==null)
            return;

        ChallengeAuthData data = authData.get(header);
        if(data!=null){
            data.touch();
        }
    }

    private void purgeOldAuthData(final int minutes) {
        final int millis = 10 * 60 * 1000;

        List<String> deleteMe = new ArrayList();
        final long cutOffTime = System.currentTimeMillis() - millis;
        for(ChallengeAuthData data : authData.values()){
            if(data.getLastAccessed() < cutOffTime ){
                deleteMe.add(data.getGuid());
            }
        }

        for(String deleteKey : deleteMe){
            authData.remove(deleteKey);
        }
    }
}
