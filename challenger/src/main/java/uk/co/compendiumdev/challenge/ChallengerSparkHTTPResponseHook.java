package uk.co.compendiumdev.challenge;

import spark.Request;
import spark.Response;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks.SparkRequestResponseHook;

import static uk.co.compendiumdev.challenge.Challenges.CHALLENGE.*;

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

        if(request.requestMethod()== "POST" &&
                request.pathInfo().contentEquals("/secret/token") &&
                request.headers("Authorization")!=null &&
                request.headers("Authorization").length()>10 &&
                response.status() ==401){
            challenges.pass(CREATE_SECRET_TOKEN_401);
        }

        if(request.requestMethod()== "POST" &&
                request.pathInfo().contentEquals("/secret/token") &&
                request.headers("Authorization")!=null &&
                request.headers("Authorization").length()>10 &&
                response.status() ==201){
            challenges.pass(CREATE_SECRET_TOKEN_201);
        }

        if(request.requestMethod()== "GET" &&
                request.pathInfo().contentEquals("/secret/note") &&
                request.headers("X-AUTH-TOKEN")!=null &&
                request.headers("X-AUTH-TOKEN").length()>1 &&
                response.status() ==403){
            challenges.pass(GET_SECRET_NOTE_403);
        }

        if(request.requestMethod()== "GET" &&
                request.pathInfo().contentEquals("/secret/note") &&
                request.headers("X-AUTH-TOKEN")==null &&
                response.status() ==401){
            challenges.pass(GET_SECRET_NOTE_401);
        }

        if(request.requestMethod()== "POST" &&
                request.pathInfo().contentEquals("/secret/note") &&
                request.headers("X-AUTH-TOKEN")!=null &&
                request.headers("X-AUTH-TOKEN").length()>1 &&
                request.body().contains("\"note\"") &&
                response.status() ==403){
            challenges.pass(POST_SECRET_NOTE_403);
        }

        if(request.requestMethod()== "POST" &&
                request.pathInfo().contentEquals("/secret/note") &&
                request.headers("X-AUTH-TOKEN")==null &&
                request.body().contains("\"note\"") &&
                response.status() ==401){
            challenges.pass(POST_SECRET_NOTE_401);
        }

        if(request.requestMethod()== "POST" &&
                request.pathInfo().contentEquals("/secret/note") &&
                request.headers("X-AUTH-TOKEN")!=null &&
                request.body().contains("\"note\"") &&
                response.status() ==200){
            challenges.pass(POST_SECRET_NOTE_200);
        }

        if(request.requestMethod()== "GET" &&
                request.pathInfo().contentEquals("/secret/note") &&
                request.headers("X-AUTH-TOKEN")!=null &&
                response.status() ==200){
            challenges.pass(GET_SECRET_NOTE_200);
        }

    }
}
