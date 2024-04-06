package uk.co.compendiumdev.thingifier.application.httprouting;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinitionDocGenerator;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.RestApiDocumentationGenerator;
import uk.co.compendiumdev.thingifier.swaggerizer.Swaggerizer;

import static spark.Spark.get;

public class ThingifierAutoDocGenRouting {

    private String urlPath;

    public ThingifierAutoDocGenRouting(final Thingifier thingifier,
                                       ThingifierApiDocumentationDefn apiDefn,
                                       DefaultGUIHTML guiManagement) {


        // TODO: this should come from the API config
        this.urlPath = "";
        if(apiDefn!=null && !apiDefn.getPathPrefix().isEmpty()) {
            this.urlPath = apiDefn.getPathPrefix();
        }

        // configure it based on a thingifier
        ApiRoutingDefinition routingDefinitions = new ApiRoutingDefinitionDocGenerator(thingifier).generate(apiDefn.getPathPrefix());

        // TODO: config to enable docs and configure the URL and add a meta tag for description and additional headers
        // / - default for documentation
        get("%s/docs".formatted(apiDefn.getPathPrefix()), (request, response) -> {
            response.type("text/html");
            response.status(200);
            return new RestApiDocumentationGenerator(thingifier, guiManagement).
                    getApiDocumentation(
                            routingDefinitions,
                            apiDefn.getAdditionalRoutes(),
                            this.urlPath,
                            "%s/docs".formatted(apiDefn.getPathPrefix()));
        });

        //guiManagement.appendMenuItem("API documentation","/docs");

        // TODO: api config to enable swagger and configure the URL
        // TODO: move into swagger package
        // now that we have an api definition we should be able to generate swagger
        get("%s/docs/swagger".formatted(apiDefn.getPathPrefix()), (request, response) -> {
            response.type("text/html");
            response.status(200);
            String nameprefix = "";
            try {
                nameprefix = apiDefn.getThingifier().getTitle().replace(" ", "-") + "-";
            }catch (Exception e){
                // invalid apidefn setup
                System.out.println("Possibly incomplete swagger generation, api not defined from model");
            }
            response.header("Content-Type", "application/octet-stream");
            response.header("Content-Disposition",
                    String.format("attachment; filename=\"%sswagger.json\"",nameprefix));

            // TODO: the swaggerizer could be stored at a class level and allow caching to be used for the output
            return new Swaggerizer(apiDefn).asJson();
        });

    }
}
