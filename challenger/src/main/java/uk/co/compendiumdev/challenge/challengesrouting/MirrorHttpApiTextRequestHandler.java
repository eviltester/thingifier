package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.StringPair;
import uk.co.compendiumdev.thingifier.application.routehandlers.HttpApiRequestHandler;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public class MirrorHttpApiTextRequestHandler implements HttpApiRequestHandler {

    public ApiResponse handle(final HttpApiRequest myRequest) {
        // convert request into a string for message body- getRequestDetails
        String requestDetails = getRequestDetails(myRequest);

        ApiResponse response=null;

        response = ApiResponse.success().setHeader("Content-Type", "text/plain");
        response.setBody(requestDetails);

        return response;
    }

    private String getRequestDetails(final HttpApiRequest myRequest) {
        StringBuilder output = new StringBuilder();

        output.append(String.format("%s %s",myRequest.getVerb(), myRequest.getUrl()));
        output.append("\n");

        output.append("\n");
        output.append("Query Params");
        output.append("\n");
        output.append("============");
        output.append("\n");
        for(String queryParam : myRequest.getQueryParamNames()){
            output.append(String.format("%s: %s",queryParam, myRequest.rawQueryParamsValue(queryParam)));
            output.append("\n");
        }

        output.append("\n");
        output.append("IP");
        output.append("\n");
        output.append("=======");
        output.append("\n");
        output.append(myRequest.getIP());
        output.append("\n");

        output.append("\n");
        output.append("Raw Headers");
        output.append("\n");
        output.append("=======");
        output.append("\n");
        for(StringPair header : myRequest.getHeadersList()){
            output.append(String.format("%s: %s",header.key, header.value));
            output.append("\n");
        }

        output.append("\n");
        output.append("Processed Headers");
        output.append("\n");
        output.append("=======");
        output.append("\n");
        for(String header : myRequest.getHeaders().asMap().keySet()){
            output.append(String.format("%s: %s",header, myRequest.getHeader(header)));
            output.append("\n");
        }
        output.append("\n");

        output.append("Body");
        output.append("\n");
        output.append("====");
        output.append("\n");
        output.append(myRequest.getBody());
        output.append("\n");
        return output.toString();
    }

}
