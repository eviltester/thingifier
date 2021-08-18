package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.thingifier.api.http.AcceptHeaderParser;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.routehandlers.HttpApiRequestHandler;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public class MirrorHttpApiRequestHandler implements HttpApiRequestHandler {
    private final EntityDefinition entityDefn;

    public MirrorHttpApiRequestHandler(final EntityDefinition entityDefn) {
        this.entityDefn=entityDefn;
    }

    public ApiResponse handle(final HttpApiRequest myRequest) {
        // convert request into a string for message body- getRequestDetails
        String requestDetails = getRequestDetails(myRequest);

        final AcceptHeaderParser parser = new AcceptHeaderParser(
                myRequest.getHeader("accept"));

        ApiResponse response=null;

        // handle text separately as the main api does not 'do' text
        if(parser.hasAskedForTEXT()){
            response = ApiResponse.success().setHeader("Content-Type", "text/plain");
            response.setBody(requestDetails);
        }

        if(response==null) {
            // let main code handle formatting etc.
            EntityInstance fake = new EntityInstance(entityDefn).
                    setValue("details", requestDetails);
            response = ApiResponse.success().returnSingleInstance(fake);
        }

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
        output.append("Headers");
        output.append("\n");
        output.append("=======");
        output.append("\n");
        for(String header : myRequest.getHeaders().keySet()){
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
