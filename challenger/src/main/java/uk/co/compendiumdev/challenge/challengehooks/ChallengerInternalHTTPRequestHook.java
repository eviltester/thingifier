package uk.co.compendiumdev.challenge.challengehooks;

import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks.InternalHttpRequestHook;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;

import static uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi.HTTP_SESSION_HEADER_NAME;

public class ChallengerInternalHTTPRequestHook  implements InternalHttpRequestHook {
    private final Challengers challengers;

    public ChallengerInternalHTTPRequestHook(final Challengers challengers) {
        this.challengers = challengers;
    }

    @Override
    public HttpApiResponse run(final HttpApiRequest request) {
        updateAuthTokenFrom(request.getHeader("X-CHALLENGER"));
        challengers.purgeOldAuthData();

        ChallengerAuthData challenger = challengers.getChallenger(request.getHeader("X-CHALLENGER"));
        if(challenger==null){
            // cannot track challenges
            return null;
        }

        // add challenger guid as session id to request
        request.addHeader(HTTP_SESSION_HEADER_NAME, challenger.getXChallenger());

        HttpApiRequest.VERB method = request.getVerb();
        String path = request.getPath();

        if(method == HttpApiRequest.VERB.GET &&
                path.equals("challenges")){
            challengers.pass(challenger, CHALLENGE.GET_CHALLENGES);
        }

        if(method == HttpApiRequest.VERB.GET &&
                path.equals("heartbeat")){
            challengers.pass(challenger, CHALLENGE.GET_HEARTBEAT_204);
        }

        if(method == HttpApiRequest.VERB.DELETE &&
                path.equals("heartbeat")){
            challengers.pass(challenger,CHALLENGE.DELETE_HEARTBEAT_405);
        }

        if(method == HttpApiRequest.VERB.PATCH &&
                path.equals("heartbeat")){
            challengers.pass(challenger,CHALLENGE.PATCH_HEARTBEAT_500);
        }

        if(method == HttpApiRequest.VERB.TRACE &&
                path.equals("heartbeat")){
            challengers.pass(challenger,CHALLENGE.TRACE_HEARTBEAT_501);
        }

        if(method == HttpApiRequest.VERB.POST &&
                path.equals("heartbeat") && request.getHeader("x-http-method-override").equals("patch")){
            challengers.pass(challenger,CHALLENGE.OVERRIDE_PATCH_HEARTBEAT_500);
        }

        if(method == HttpApiRequest.VERB.POST &&
                path.equals("heartbeat") && request.getHeader("x-http-method-override").equals("delete")){
            challengers.pass(challenger,CHALLENGE.OVERRIDE_DELETE_HEARTBEAT_405);
        }

        if(method == HttpApiRequest.VERB.POST &&
                path.equals("heartbeat") && request.getHeader("x-http-method-override").equals("trace")){
            challengers.pass(challenger,CHALLENGE.OVERRIDE_TRACE_HEARTBEAT_501);
        }

        if(method == HttpApiRequest.VERB.GET &&
                path.equals("challenger/" + challenger.getXChallenger())){
            challengers.pass(challenger,CHALLENGE.GET_RESTORABLE_CHALLENGER_PROGRESS_STATUS);
        }

        return null;
    }

    private void updateAuthTokenFrom(final String header) {
        if(header==null || header.isEmpty())
            return;

        ChallengerAuthData data = challengers.getChallenger(header);
        if(data!=null){
            data.touch();
        }
    }
}
