package uk.co.compendiumdev.challenge.practicemodes.mirror;

import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.routehandlers.HttpApiRequestHandler;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public class MirrorHttpApiRequestHandler implements HttpApiRequestHandler {
    private final EntityDefinition entityDefn;

    public MirrorHttpApiRequestHandler(final EntityDefinition entityDefn) {
        this.entityDefn=entityDefn;
    }

    public ApiResponse handle(final HttpApiRequest myRequest) {
        // convert request into a string for message body- getRequestDetails
        String requestDetails = new MirrorHttpApiTextRequestHandler().getRequestDetails(myRequest);

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

}
