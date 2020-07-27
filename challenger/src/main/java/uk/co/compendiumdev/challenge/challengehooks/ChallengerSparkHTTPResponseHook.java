package uk.co.compendiumdev.challenge.challengehooks;

import spark.Request;
import spark.Response;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks.SparkRequestResponseHook;


public class ChallengerSparkHTTPResponseHook implements SparkRequestResponseHook {

    private final Challengers challengers;

    public ChallengerSparkHTTPResponseHook(final Challengers challengers) {
        this.challengers = challengers;
    }


    @Override
    public void run(final Request request, final Response response) {

        ChallengerAuthData challenger = challengers.getChallenger(request.headers("X-CHALLENGER"));

        if(challenger==null){
            if(!request.pathInfo().contentEquals("/challenger")) {
                response.header("X-CHALLENGER", "UNKNOWN CHALLENGER - NO CHALLENGES TRACKED");
            }
            // cannot track challenges
            return;
        }

        if(challenger!=null){
            response.header("X-CHALLENGER", challenger.getXChallenger());
        }

        // No endpoint defined so this 404 created by Spark routing
        if(request.requestMethod() == "GET" &&
                request.pathInfo().contentEquals("/todo") &&
                response.status()==404){
            challengers.pass(challenger, CHALLENGE.GET_TODOS_NOT_PLURAL_404);
        }

        if(request.requestMethod()== "OPTIONS" &&
                request.pathInfo().contentEquals("/todos") &&
                response.status() ==200){
            challengers.pass(challenger,CHALLENGE.OPTIONS_TODOS);
        }

        if(request.requestMethod()== "POST" &&
                request.pathInfo().contentEquals("/challenger") &&
                response.status() ==201){
            challengers.pass(challenger,CHALLENGE.CREATE_NEW_CHALLENGER);
        }

        if(request.requestMethod()== "POST" &&
                request.pathInfo().contentEquals("/secret/token") &&
                request.headers("Authorization")!=null &&
                request.headers("Authorization").length()>10 &&
                response.status() ==401){
            challengers.pass(challenger,CHALLENGE.CREATE_SECRET_TOKEN_401);
        }

        if(request.requestMethod()== "POST" &&
                request.pathInfo().contentEquals("/secret/token") &&
                request.headers("Authorization")!=null &&
                request.headers("Authorization").length()>10 &&
                response.status() ==201){
            challengers.pass(challenger,CHALLENGE.CREATE_SECRET_TOKEN_201);
        }

        if(request.requestMethod()== "GET" &&
                request.pathInfo().contentEquals("/secret/note") &&
                request.headers("X-AUTH-TOKEN")!=null &&
                request.headers("X-AUTH-TOKEN").length()>1 &&
                response.status() ==403){
            challengers.pass(challenger,CHALLENGE.GET_SECRET_NOTE_403);
        }

        if(request.requestMethod()== "GET" &&
                request.pathInfo().contentEquals("/secret/note") &&
                request.headers("X-AUTH-TOKEN")==null &&
                response.status() ==401){
            challengers.pass(challenger,CHALLENGE.GET_SECRET_NOTE_401);
        }

        if(request.requestMethod()== "POST" &&
                request.pathInfo().contentEquals("/secret/note") &&
                request.headers("X-AUTH-TOKEN")!=null &&
                request.headers("X-AUTH-TOKEN").length()>1 &&
                request.body().contains("\"note\"") &&
                response.status() ==403){
            challengers.pass(challenger,CHALLENGE.POST_SECRET_NOTE_403);
        }

        if(request.requestMethod()== "POST" &&
                request.pathInfo().contentEquals("/secret/note") &&
                request.headers("X-AUTH-TOKEN")==null &&
                request.body().contains("\"note\"") &&
                response.status() ==401){
            challengers.pass(challenger,CHALLENGE.POST_SECRET_NOTE_401);
        }

        if(request.requestMethod()== "POST" &&
                request.pathInfo().contentEquals("/secret/note") &&
                request.headers("X-AUTH-TOKEN")!=null &&
                request.body().contains("\"note\"") &&
                response.status() ==200){
            challengers.pass(challenger,CHALLENGE.POST_SECRET_NOTE_200);
        }

        if(request.requestMethod()== "GET" &&
                request.pathInfo().contentEquals("/secret/note") &&
                request.headers("X-AUTH-TOKEN")!=null &&
                response.status() ==200){
            challengers.pass(challenger,CHALLENGE.GET_SECRET_NOTE_200);
        }

    }
}
