package uk.co.compendiumdev.challenge.practicemodes.mirror;

import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.StringPair;
import uk.co.compendiumdev.thingifier.application.routehandlers.HttpApiRequestHandler;
import uk.co.compendiumdev.thingifier.core.query.FilterBy;

public class MirrorHttpApiTextRequestHandler implements HttpApiRequestHandler {

    public ApiResponse handle(final HttpApiRequest myRequest) {
        // convert request into a string for message body- getRequestDetails
        String requestDetails = getRequestDetails(myRequest);

        ApiResponse response;

        response = ApiResponse.success().setHeader("Content-Type", "text/plain");
        response.setBody(requestDetails);

        return response;
    }

    public String getRequestDetails(final HttpApiRequest myRequest) {
        StringBuilder output = new StringBuilder();

        output.append(String.format("%s %s",myRequest.getVerb(), myRequest.getUrl()));
        output.append("\n");

        output.append("\n");
        output.append("Parsed Query Params");
        output.append("\n");
        output.append("============");
        output.append("\n");
        for(String queryParam : myRequest.getQueryParamNames()){
            output.append(String.format("%s: %s",queryParam, myRequest.rawQueryParamsValue(queryParam)));
            output.append("\n");
        }

        output.append("\n");
        output.append("Raw Query Params");
        output.append("\n");
        output.append("============");
        output.append("\n");
        for(FilterBy queryParam : myRequest.getFilterableQueryParams().toList()){
            output.append(String.format("%s %s %s",queryParam.fieldName, queryParam.filterOperation, queryParam.fieldValue));
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
