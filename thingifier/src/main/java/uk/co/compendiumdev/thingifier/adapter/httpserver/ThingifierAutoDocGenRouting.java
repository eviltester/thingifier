package uk.co.compendiumdev.thingifier.adapter.httpserver;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.get;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinitionDocGenerator;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.RestApiDocumentationGenerator;
import uk.co.compendiumdev.thingifier.swaggerizer.OpenApiSpecificationVersion;
import uk.co.compendiumdev.thingifier.swaggerizer.SwaggerUiPage;
import uk.co.compendiumdev.thingifier.swaggerizer.Swaggerizer;

public class ThingifierAutoDocGenRouting {

    public ThingifierAutoDocGenRouting(
            final Thingifier thingifier,
            ThingifierApiDocumentationDefn apiDefn,
            DefaultGUIHTML guiManagement) {

        // configure it based on a thingifier
        ApiRoutingDefinition routingDefinitions =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate(apiDefn.getPathPrefix());
        final String pathPrefix = apiDefn.getPathPrefix();
        final String docsPath = "%s/docs".formatted(pathPrefix);
        final String swaggerDownloadPath = "%s/docs/swagger".formatted(pathPrefix);
        final String openApiPath = "%s/docs/openapi.json".formatted(pathPrefix);
        final String openApi30Path = "%s/docs/openapi-3.0.json".formatted(pathPrefix);
        final String openApi31Path = "%s/docs/openapi-3.1.json".formatted(pathPrefix);
        final String swaggerUiPath = "%s/docs/swagger-ui".formatted(pathPrefix);

        // TODO: config to enable docs and configure the URL and add a meta tag for description and
        // additional headers
        // / - default for documentation
        get(
                docsPath,
                (request, response) -> {
                    response.type("text/html");
                    response.status(200);
                    return new RestApiDocumentationGenerator(thingifier, guiManagement)
                            .getApiDocumentation(
                                    routingDefinitions,
                                    apiDefn.getAdditionalRoutes(),
                                    apiDefn,
                                    apiDefn.getPathPrefix(),
                                    docsPath);
                });

        // guiManagement.appendMenuItem("API documentation","/docs");

        registerOpenApiEndpoint(apiDefn, openApiPath, OpenApiSpecificationVersion.OPENAPI_3_1);
        registerOpenApiEndpoint(apiDefn, openApi31Path, OpenApiSpecificationVersion.OPENAPI_3_1);
        registerOpenApiEndpoint(apiDefn, openApi30Path, OpenApiSpecificationVersion.OPENAPI_3_0);

        get(
                swaggerUiPath,
                (request, response) -> {
                    response.type("text/html");
                    response.status(200);
                    return new SwaggerUiPage(
                                    apiDefn,
                                    guiManagement,
                                    openApiPath,
                                    openApi30Path,
                                    openApi31Path,
                                    docsPath,
                                    swaggerDownloadPath,
                                    swaggerUiPath)
                            .html();
                });

        // TODO: api config to enable swagger and configure the URL
        // TODO: move into swagger package
        // now that we have an api definition we should be able to generate swagger
        get(
                swaggerDownloadPath,
                (request, response) -> {
                    String permissive = request.queryParam("permissive");

                    response.type("text/html");
                    response.status(200);
                    String nameprefix = "";
                    try {
                        nameprefix = apiDefn.getThingifier().getTitle().replace(" ", "-") + "-";
                    } catch (Exception e) {
                        // invalid apidefn setup
                        System.out.println(
                                "Possibly incomplete swagger generation, api not defined from model");
                    }
                    if (permissive != null) {
                        nameprefix = nameprefix + "permissive-";
                    }
                    response.header("Content-Type", "application/octet-stream");
                    response.header(
                            "Content-Disposition",
                            String.format("attachment; filename=\"%sswagger.json\"", nameprefix));

                    // TODO: the swaggerizer could be stored at a class level and allow caching to
                    // be used for the output
                    return new Swaggerizer(apiDefn)
                            .asJsonWithPreferredServer(
                                    permissive != null, HttpRequestOrigin.from(request));
                });
    }

    private void registerOpenApiEndpoint(
            final ThingifierApiDocumentationDefn apiDefn,
            final String path,
            final OpenApiSpecificationVersion version) {
        get(
                path,
                (request, response) -> {
                    response.type("application/json");
                    response.status(200);
                    return new Swaggerizer(apiDefn)
                            .asJsonWithPreferredServer(
                                    version, false, HttpRequestOrigin.from(request));
                });
    }
}
