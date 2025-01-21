package uk.co.compendiumdev.challenge.practicemodes.simpleapi;

import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.InternalHttpResponse;
import uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks.InternalHttpResponseHook;

import java.util.List;

import static uk.co.compendiumdev.thingifier.api.http.HttpApiRequest.VERB.OPTIONS;

public class SimpleApiCorsHeadersResponseHook implements InternalHttpResponseHook {

    @Override
    public void run(final HttpApiRequest request, final InternalHttpResponse response) {

        // TODO: hooks should only apply to a specific routing set and this should not be necessary
        List<String> validEndpointPrefixesToRunAgainst = List.of("simpleapi");
        String[] pathSegments = request.getPath().split("/");
        if(!validEndpointPrefixesToRunAgainst.contains(pathSegments[0])){
            return;
        }

        // allow cross origin requests
        // and swagger https://support.smartbear.com/swaggerhub/docs/en/edit-apis/cors-requirements-for--try-it-out-.html
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "*");
        // this is necessary for swagger UI to show headers in the UI
        response.setHeader("Access-Control-Expose-Headers", "*");
        if (request.getVerb() == OPTIONS && request.getHeaders().headerExists("Access-Control-Allow-Methods")) {
            response.setHeader("Access-Control-Allow-Methods", request.getHeader("Access-Control-Allow-Methods"));
        }
    }
}