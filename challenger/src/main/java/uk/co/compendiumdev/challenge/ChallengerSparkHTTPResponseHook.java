package uk.co.compendiumdev.challenge;

import spark.Request;
import spark.Response;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks.SparkRequestResponseHook;

import static uk.co.compendiumdev.challenge.Challenges.CHALLENGE.GET_TODOS_NOT_PLURAL_404;
import static uk.co.compendiumdev.challenge.Challenges.CHALLENGE.OPTIONS_TODOS;

public class ChallengerSparkHTTPResponseHook implements SparkRequestResponseHook {

    private final Challenges challenges;

    public ChallengerSparkHTTPResponseHook(final Challenges challenges) {
        this.challenges = challenges;
    }


    @Override
    public void run(final Request request, final Response response) {

        // No endpoint defined so this 404 created by Spark routing
        if(request.requestMethod() == "GET" &&
                request.pathInfo().contentEquals("/todo") &&
                response.status()==404){
            challenges.pass(GET_TODOS_NOT_PLURAL_404);
        }

        if(request.requestMethod()== "OPTIONS" &&
                request.pathInfo().contentEquals("/todos") &&
                response.status() ==200){
            challenges.pass(OPTIONS_TODOS);
        }
    }
}
