package uk.co.compendiumdev.challenge.challengehooks;

import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiRequestHook;

import java.util.List;

import static uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi.HTTP_SESSION_HEADER_NAME;

public class ChallengerApiRequestHook implements HttpApiRequestHook {

    private final Challengers challengers;

    public ChallengerApiRequestHook(Challengers challengers){
        this.challengers = challengers;
    }

    @Override
    public HttpApiResponse run(final HttpApiRequest request, final ThingifierApiConfig config) {

        ChallengerAuthData challenger = challengers.getChallenger(request.getHeader("X-CHALLENGER"));
        if(challenger==null){

            // if there is no x-challenger and we are in multi-player mode then do not allow any
            // POST, DELETE, PUT, PATCH through to the API as this would amend the default database
            if(challengers.isMultiPlayerMode()){
                if(
                        request.getVerb().equals(HttpApiRequest.VERB.POST) ||
                        request.getVerb().equals(HttpApiRequest.VERB.PUT) ||
                        request.getVerb().equals(HttpApiRequest.VERB.PATCH) ||
                        request.getVerb().equals(HttpApiRequest.VERB.DELETE)
                ){
                    return new HttpApiResponse(
                            request.getHeaders(),
                            new ApiResponse(401, true, List.of("Cannot amend details. Missing a valid X-CHALLENGER header.")),
                            new JsonThing(challengers.getApiConfig().jsonOutput()),
                            challengers.getApiConfig());
                }
            }

            // cannot track challenges
            return null;
        }

        // extend the life of the challenger
        challenger.touch();

        // trim the list of challengers
        challengers.purgeOldAuthData();

        // add challenger guid as session id to request
        request.addHeader(HTTP_SESSION_HEADER_NAME, challenger.getXChallenger());

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
