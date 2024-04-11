package uk.co.compendiumdev.challenge.practicemodes.mirror;

import spark.Route;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.application.AdhocDocumentedSparkRouteConfigurer;
import uk.co.compendiumdev.thingifier.application.httprouting.ThingifierAutoDocGenRouting;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;
import uk.co.compendiumdev.thingifier.spark.SimpleSparkRouteCreator;
import uk.co.compendiumdev.thingifier.swaggerizer.Swaggerizer;


import java.util.ArrayList;
import java.util.List;

import static spark.Spark.*;

/*
    Create a set of custom end points that do not use a thingifier, but
    take advantage of the documentation generation functionality provided
    by the thingifier
 */
public class MirrorRoutes {

    public void configure(final ThingifierApiDocumentationDefn apiDefn, DefaultGUIHTML guiTemplates) {

        // /mirror should be the GUI with api below it
        String endpoint ="/mirror/request";
        String rawEndPoint ="/mirror/raw";

        RequestMirror requestMirror = new RequestMirror();

        // redirect a GET to "/fromPath" to "/toPath"
        redirect.get("/mirror", "/practice-modes/mirror");

        List<String>verbEndpoints = new ArrayList<>();
        verbEndpoints.add(endpoint);
        verbEndpoints.add(endpoint+"/*");
        verbEndpoints.add(rawEndPoint);
        verbEndpoints.add(rawEndPoint+"/*");

        AdhocDocumentedSparkRouteConfigurer routeCreatorAndDocumentor = new AdhocDocumentedSparkRouteConfigurer(apiDefn);

        for (String anEndpoint : verbEndpoints) {
            Route routeHandler = (request, result) -> {
                result.status(204);
                result.header("Allow", "GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, TRACE");
                return "";
            };
            if(anEndpoint.endsWith("/*")){
                // add to routing but not to the api documentation
                SimpleSparkRouteCreator.addHandler(anEndpoint, "options", routeHandler);
            }else {
                routeCreatorAndDocumentor.
                        add(anEndpoint, RoutingVerb.OPTIONS, 204,
                                "Options for mirror endpoint",
                                routeHandler
                        );
            }
        }

        Route mirroredRoute = (request, result) -> {
            return requestMirror.mirrorRequest(request, result);
        };

        Route rawTextMirroredRoute = (request, result) -> {
            return requestMirror.mirrorRequestAsText(request, result);
        };

        RoutingVerb[] verbs200status = {RoutingVerb.GET, RoutingVerb.POST, RoutingVerb.PUT,
                                        RoutingVerb.DELETE, RoutingVerb.PATCH, RoutingVerb.TRACE};

        for (String anEndpoint : verbEndpoints) {
            for(RoutingVerb routing : verbs200status) {
                if(anEndpoint.endsWith("/*")){
                    // add to routing but not to the api documentation
                    SimpleSparkRouteCreator.addHandler(anEndpoint, routing.name(), mirroredRoute);
                }else {
                    if (anEndpoint.startsWith(endpoint)) {
                        routeCreatorAndDocumentor.add(anEndpoint, routing, 200,
                                "Mirror a " + routing.name().toUpperCase() + " Request", mirroredRoute);
                    }
                    if (anEndpoint.startsWith(rawEndPoint)) {
                        routeCreatorAndDocumentor.add(anEndpoint, routing, 200,
                                "Raw Text Mirror of a " + routing.name().toUpperCase() + " Request", rawTextMirroredRoute);
                    }
                }
            }
        }

        /*
            Handle HEAD verb - special handling to only return headers
         */
        for (String anEndpoint : verbEndpoints) {
            Route routeHAndler = (request, result) -> {
                String body = requestMirror.mirrorRequest(request, result);
                return "";
            };
            if(anEndpoint.endsWith("/*")){
                // add to routing but not to the api documentation
                SimpleSparkRouteCreator.addHandler(anEndpoint, "head", routeHAndler);
            }else {
                routeCreatorAndDocumentor.
                        add(anEndpoint, RoutingVerb.HEAD, 204,
                                "Headers for mirror endpoint",
                                routeHAndler
                        );
            }
        }

        Thingifier dummy = new Thingifier();
        dummy.setDocumentation("HTTP Mirror Mode", "The HTTP Mirror mode shows you the request that you sent in. The raw mode shows the exact request, the request mode shows it 'interpreted' by a basic API.");
        //dummy.apiConfig().setValidatesContentType(false);
        dummy.apiConfig().setDefaultContentTypeAsJson(false);
        dummy.apidocsconfig().setHeaderSectionOverride("""
                <p>
                    Raw mode will always return the response as plain text.
                    HEAD and OPTIONS will respond like normal HEAD and OPTIONS.
                    You can not use the accept header to control the response format.
                </p>
                <p>
                    Request mode will try to honour the accept headers and return the response as JSON or XML.
                </p>
                <p>
                    Nothing is stored on the server. The request is only used to generate a response which is passed back to you.
                </p>
                <p>
                    Validation is performed on the length of the request and if the request is too large then it will be rejectec.
                </p>
                """);
        dummy.apidocsconfig().setApiIntroductionParaOverride("""
                <p>
                    For any of the endpoints listed, you can also add any number of parameters to the URL e.g. /mirror/raw/this/and/that?key=value
                </p>
                """);

        ThingifierApiDocumentationDefn apiDocDefn = routeCreatorAndDocumentor.getApiDocDefn();
        apiDocDefn.setThingifier(dummy);
        apiDocDefn.setPathPrefix("/mirror"); // where can the API endpoints be found

        ThingifierAutoDocGenRouting mirrorDocsRouting = new ThingifierAutoDocGenRouting(
                apiDocDefn.getThingifier(),
                apiDocDefn,
                guiTemplates
        );
   }
}
