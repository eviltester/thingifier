package uk.co.compendiumdev.challenge.challengehooks;

import static uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi.HTTP_SESSION_HEADER_NAME;

import java.util.List;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpMethod;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;
import uk.co.compendiumdev.thingifier.adapter.spark.messagehooks.InternalHttpRequestHook;

/*
   This is an Internal HTTP Request because it covers functionality for endpoints that do not
   go through the normal API process i.e. heartbeat, challenges, challenger
*/
public class ChallengerInternalHTTPRequestHook implements InternalHttpRequestHook {
    private final Challengers challengers;

    public ChallengerInternalHTTPRequestHook(final Challengers challengers) {
        this.challengers = challengers;
    }

    @Override
    public InternalHttpResponse run(final InternalHttpRequest request) {

        // TODO: fix hooks so that they only run on a specific thingifier basis.
        // Until fixed so hooks only run on specific thingifiers, restrict this to Challenges API
        // end points
        List<String> validEndpointPrefixesToRunAgainst =
                List.of("challenger", "todo", "todos", "challenges", "heartbeat", "secret");
        String[] pathSegments = request.getPath().split("/");
        if (!validEndpointPrefixesToRunAgainst.contains(pathSegments[0])) {
            return null;
        }

        ChallengerAuthData challenger =
                challengers.getChallenger(request.getHeader("X-CHALLENGER"));
        if (challenger == null) {
            // cannot track challenges
            return null;
        }

        challenger.touch();
        challengers.purgeOldAuthData();

        // add challenger guid as session id to request
        request.addHeader(HTTP_SESSION_HEADER_NAME, challenger.getXChallenger());

        InternalHttpMethod method = request.getVerb();
        String path = request.getPath();

        if (method == InternalHttpMethod.GET && path.equals("challenges")) {
            challengers.pass(challenger, CHALLENGE.GET_CHALLENGES);
        }

        if (method == InternalHttpMethod.GET && path.equals("heartbeat")) {
            challengers.pass(challenger, CHALLENGE.GET_HEARTBEAT_204);
        }

        if (method == InternalHttpMethod.DELETE && path.equals("heartbeat")) {
            challengers.pass(challenger, CHALLENGE.DELETE_HEARTBEAT_405);
        }

        if (method == InternalHttpMethod.PATCH && path.equals("heartbeat")) {
            challengers.pass(challenger, CHALLENGE.PATCH_HEARTBEAT_500);
        }

        if (method == InternalHttpMethod.TRACE && path.equals("heartbeat")) {
            challengers.pass(challenger, CHALLENGE.TRACE_HEARTBEAT_501);
        }

        if (method == InternalHttpMethod.POST
                && path.equals("heartbeat")
                && request.getHeader("x-http-method-override").equalsIgnoreCase("patch")) {
            challengers.pass(challenger, CHALLENGE.OVERRIDE_PATCH_HEARTBEAT_500);
        }

        if (method == InternalHttpMethod.POST
                && path.equals("heartbeat")
                && request.getHeader("x-http-method-override").equalsIgnoreCase("delete")) {
            challengers.pass(challenger, CHALLENGE.OVERRIDE_DELETE_HEARTBEAT_405);
        }

        if (method == InternalHttpMethod.POST
                && path.equals("heartbeat")
                && request.getHeader("x-http-method-override").equalsIgnoreCase("trace")) {
            challengers.pass(challenger, CHALLENGE.OVERRIDE_TRACE_HEARTBEAT_501);
        }

        if (method == InternalHttpMethod.GET
                && path.equals("challenger/" + challenger.getXChallenger())) {
            challengers.pass(challenger, CHALLENGE.GET_RESTORABLE_CHALLENGER_PROGRESS_STATUS);
        }

        return null;
    }
}
