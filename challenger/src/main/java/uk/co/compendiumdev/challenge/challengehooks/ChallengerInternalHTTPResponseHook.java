package uk.co.compendiumdev.challenge.challengehooks;

import uk.co.compendiumdev.challenge.BearerAuthHeaderParser;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.InternalHttpResponse;
import uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks.InternalHttpResponseHook;

import static uk.co.compendiumdev.thingifier.api.http.HttpApiRequest.VERB.*;


public class ChallengerInternalHTTPResponseHook implements InternalHttpResponseHook {

    private final Challengers challengers;

    public ChallengerInternalHTTPResponseHook(final Challengers challengers) {
        this.challengers = challengers;
    }


    @Override
    public void run(final HttpApiRequest request, final InternalHttpResponse response) {

        ChallengerAuthData challenger = challengers.getChallenger(request.getHeader("X-CHALLENGER"));

        // we can complete a challenge while the user is null - creating the user
        if(request.getVerb()== POST &&
                request.getPath().contentEquals("challenger") &&
                response.getStatusCode() ==201){
            // challenger did not exist so we need to find it to pass the challenge

            String challengerId = response.getHeader("X-Challenger");
            challenger = challengers.getChallenger(challengerId);
            if(challenger!=null) {
                challengers.pass(challenger, CHALLENGE.CREATE_NEW_CHALLENGER);
            }
        }

        if(challenger==null){
            if(!request.getPath().contentEquals("challenger")) {
                if(response.getHeader("X-CHALLENGER") == null) {
                    response.setHeader("X-CHALLENGER", "UNKNOWN CHALLENGER - NO CHALLENGES TRACKED");
                }
            }
            // cannot track challenges
            return;
        }

        if(challenger!=null){
            if(response.getHeader("X-CHALLENGER") == null) {
                response.setHeader("X-CHALLENGER", challenger.getXChallenger());
            }
        }

        // No endpoint defined so this 404 created by Spark routing
        if(request.getVerb() == GET &&
                request.getPath().contentEquals("todo") &&
                response.getStatusCode()==404){
            challengers.pass(challenger, CHALLENGE.GET_TODOS_NOT_PLURAL_404);
        }

        if(request.getVerb()== OPTIONS &&
                request.getPath().contentEquals("todos") &&
                response.getStatusCode() ==200){
            challengers.pass(challenger,CHALLENGE.OPTIONS_TODOS);
        }

        if(request.getVerb()== POST &&
                request.getPath().contentEquals("secret/token") &&
                request.getHeader("Authorization")!=null &&
                request.getHeader("Authorization").length()>10 &&
                response.getStatusCode() ==401){
            challengers.pass(challenger,CHALLENGE.CREATE_SECRET_TOKEN_401);
        }

        if(request.getVerb()== POST &&
                request.getPath().contentEquals("secret/token") &&
                request.getHeader("Authorization")!=null &&
                request.getHeader("Authorization").length()>10 &&
                response.getStatusCode() ==201){
            challengers.pass(challenger,CHALLENGE.CREATE_SECRET_TOKEN_201);
        }

        if(request.getVerb()== GET &&
                request.getPath().contentEquals("secret/note") &&
                request.getHeader("X-AUTH-TOKEN")!=null &&
                request.getHeader("X-AUTH-TOKEN").length()>1 &&
                response.getStatusCode() ==403){
            challengers.pass(challenger,CHALLENGE.GET_SECRET_NOTE_403);
        }

        if(request.getVerb()== GET &&
                request.getPath().contentEquals("secret/note") &&
                request.getHeader("X-AUTH-TOKEN")==null &&
                response.getStatusCode() ==401){
            challengers.pass(challenger,CHALLENGE.GET_SECRET_NOTE_401);
        }

        if(request.getVerb()== POST &&
                request.getPath().contentEquals("secret/note") &&
                request.getHeader("X-AUTH-TOKEN")!=null &&
                request.getHeader("X-AUTH-TOKEN").length()>1 &&
                request.getBody().contains("\"note\"") &&
                response.getStatusCode() ==403){
            challengers.pass(challenger,CHALLENGE.POST_SECRET_NOTE_403);
        }

        if(request.getVerb()== POST &&
                request.getPath().contentEquals("secret/note") &&
                request.getHeader("X-AUTH-TOKEN")==null &&
                request.getBody().contains("\"note\"") &&
                response.getStatusCode() ==401){
            challengers.pass(challenger,CHALLENGE.POST_SECRET_NOTE_401);
        }

        if(request.getVerb()== POST &&
                request.getPath().contentEquals("secret/note") &&
                request.getHeader("X-AUTH-TOKEN")!=null &&
                request.getBody().contains("\"note\"") &&
                response.getStatusCode() ==200){
            challengers.pass(challenger,CHALLENGE.POST_SECRET_NOTE_200);
        }

        if(request.getVerb()== GET &&
                request.getPath().contentEquals("secret/note") &&
                request.getHeader("X-AUTH-TOKEN")!=null &&
                response.getStatusCode() ==200){
            challengers.pass(challenger,CHALLENGE.GET_SECRET_NOTE_200);
        }

        if(request.getVerb()== GET &&
                request.getPath().contentEquals("secret/note") &&
                request.getHeader("Authorization")!=null &&
                new BearerAuthHeaderParser(request.getHeader("Authorization")).isValid() &&
                response.getStatusCode() ==200){
            challengers.pass(challenger,CHALLENGE.GET_SECRET_NOTE_BEARER_200);
        }

        if(request.getVerb()== POST &&
                request.getPath().contentEquals("secret/note") &&
                request.getHeader("Authorization")!=null &&
                new BearerAuthHeaderParser(request.getHeader("Authorization")).isValid() &&
                request.getBody().contains("\"note\"") &&
                response.getStatusCode() ==200){
            challengers.pass(challenger,CHALLENGE.POST_SECRET_NOTE_BEARER_200);
        }

    }


}
