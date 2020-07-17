package uk.co.compendiumdev.challenge;

import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiResponseHook;

import static uk.co.compendiumdev.challenge.Challenges.CHALLENGE.*;

public class ChallengerApiResponseHook implements HttpApiResponseHook {

    private final Challenges challenges;

    public ChallengerApiResponseHook(final Challenges challenges) {
        this.challenges = challenges;
    }

    @Override
    public HttpApiResponse run(final HttpApiRequest request, final HttpApiResponse response, final ThingifierApiConfig config) {

        if(request.getVerb() == HttpApiRequest.VERB.GET &&
                request.getPath().matches("todos/.*") &&
                response.getStatusCode()==200){
            challenges.pass(GET_TODO);
        }

        if(request.getVerb() == HttpApiRequest.VERB.GET &&
                request.getPath().matches("todos/.*") &&
                response.getStatusCode()==404){
            challenges.pass(GET_TODO_404);
        }


        // do not interfere with api and return null
        return null;
    }
}
