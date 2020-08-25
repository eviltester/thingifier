package uk.co.compendiumdev.challenge.challengehooks;

import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiRequestHook;

public class ChallengerApiRequestHook implements HttpApiRequestHook {

    private final Challengers challengers;

    public ChallengerApiRequestHook(Challengers challengers){
        this.challengers = challengers;
    }

    @Override
    public HttpApiResponse run(final HttpApiRequest request, final ThingifierApiConfig config) {

        ChallengerAuthData challenger = challengers.getChallenger(request.getHeader("X-CHALLENGER"));
        if(challenger==null){
            // cannot track challenges
            return null;
        }

        if(request.getVerb() == HttpApiRequest.VERB.GET &&
            request.getPath().contentEquals("todos") &&
            request.getQueryParams().size()==0){
            challengers.pass(challenger, CHALLENGE.GET_TODOS);
        }

        if(request.getVerb() == HttpApiRequest.VERB.HEAD &&
                request.getPath().contentEquals("todos") &&
                request.getQueryParams().size()==0){
            challengers.pass(challenger,CHALLENGE.GET_HEAD_TODOS);
        }

        return null;
    }
}
