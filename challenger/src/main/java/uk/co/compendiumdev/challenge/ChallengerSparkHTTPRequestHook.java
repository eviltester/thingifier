package uk.co.compendiumdev.challenge;

import spark.Request;
import spark.Response;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks.SparkRequestResponseHook;

import static uk.co.compendiumdev.challenge.Challenges.CHALLENGE.GET_CHALLENGES;

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

    }
}
