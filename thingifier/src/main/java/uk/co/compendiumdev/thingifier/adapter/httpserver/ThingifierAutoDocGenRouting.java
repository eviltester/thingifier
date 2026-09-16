package uk.co.compendiumdev.thingifier.adapter.httpserver;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.get;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinitionDocGenerator;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.RestApiDocumentationGenerator;
import uk.co.compendiumdev.thingifier.swaggerizer.OpenApiSpecificationVersion;
import uk.co.compendiumdev.thingifier.swaggerizer.ScalarUiPage;
import uk.co.compendiumdev.thingifier.swaggerizer.SwaggerGenerationConfig;
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
        final String openApi32Path = "%s/docs/openapi-3.2.json".formatted(pathPrefix);
        final String swaggerUiPath = "%s/docs/swagger-ui".formatted(pathPrefix);
        final String scalarUiPath = "%s/docs/scalar-ui".formatted(pathPrefix);

        guiManagement.prefixMenuItem("Docs", docsPath);
        if (apiDefn.willShowSwaggerUiLink()) {
            guiManagement.appendMenuItem("Swagger UI", swaggerUiPath);
        }
        if (apiDefn.willShowScalarUiLink()) {
            guiManagement.appendMenuItem("Scalar UI", scalarUiPath);
        }

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

        registerOpenApiEndpoint(apiDefn, openApiPath, OpenApiSpecificationVersion.OPENAPI_3_1);
        registerOpenApiEndpoint(apiDefn, openApi31Path, OpenApiSpecificationVersion.OPENAPI_3_1);
        registerOpenApiEndpoint(apiDefn, openApi32Path, OpenApiSpecificationVersion.OPENAPI_3_2);
        registerOpenApiEndpoint(apiDefn, openApi30Path, OpenApiSpecificationVersion.OPENAPI_3_0);

        if (apiDefn.willCreateSwaggerUi()) {
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
                                        openApi32Path,
                                        docsPath,
                                        swaggerUiPath)
                                .html();
                    });
        }

        if (apiDefn.willCreateScalarUi()) {
            get(
                    scalarUiPath,
                    (request, response) -> {
                        response.type("text/html");
                        response.status(200);
                        return new ScalarUiPage(
                                        apiDefn,
                                        guiManagement,
                                        openApiPath,
                                        openApi30Path,
                                        openApi31Path,
                                        openApi32Path,
                                        docsPath,
                                        scalarUiPath)
                                .html();
                    });
        }

        // TODO: api config to enable swagger and configure the URL
        // TODO: move into swagger package
        // now that we have an api definition we should be able to generate swagger
        get(
                swaggerDownloadPath,
                (request, response) -> {
                    final SwaggerGenerationConfig config =
                            swaggerGenerationConfig(
                                    OpenApiSpecificationVersion.OPENAPI_3_1, request);

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
                    nameprefix = nameprefix + variantFilenamePrefix(config);
                    response.header("Content-Type", "application/octet-stream");
                    response.header(
                            "Content-Disposition",
                            String.format("attachment; filename=\"%sswagger.json\"", nameprefix));

                    // TODO: the swaggerizer could be stored at a class level and allow caching to
                    // be used for the output
                    return new Swaggerizer(apiDefn)
                            .asJsonWithPreferredServer(config, HttpRequestOrigin.from(request));
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
                    final SwaggerGenerationConfig config =
                            swaggerGenerationConfig(version, request);
                    if (request.queryParam("download") != null) {
                        response.header(
                                "Content-Disposition",
                                "attachment; filename=\"%s\""
                                        .formatted(openApiDownloadFilename(path, config)));
                    }
                    return new Swaggerizer(apiDefn)
                            .asJsonWithPreferredServer(config, HttpRequestOrigin.from(request));
                });
    }

    private SwaggerGenerationConfig swaggerGenerationConfig(
            final OpenApiSpecificationVersion version, final HttpServerRequest request) {
        final boolean permissive = request.queryParam("permissive") != null;
        final boolean strongSchemas = truthyQueryParam(request.queryParam("strongschema"));

        final SwaggerGenerationConfig config = new SwaggerGenerationConfig();
        config.openApiSpecificationVersion = version;
        config.includeMethodNotAllowedEndpoints = permissive;
        config.includeFieldValidation = !permissive || strongSchemas;
        config.strongSchemas = strongSchemas;
        config.pathParameterPlacement = pathParameterPlacement(request.queryParam("pathparams"));
        return config;
    }

    private boolean truthyQueryParam(final String value) {
        if (value == null) {
            return false;
        }
        return !"false".equalsIgnoreCase(value.trim()) && !"0".equals(value.trim());
    }

    private SwaggerGenerationConfig.PathParameterPlacement pathParameterPlacement(
            final String placement) {
        if ("operation".equalsIgnoreCase(placement)) {
            return SwaggerGenerationConfig.PathParameterPlacement.OPERATION;
        }
        return SwaggerGenerationConfig.PathParameterPlacement.PATH;
    }

    private String openApiDownloadFilename(
            final String path, final SwaggerGenerationConfig config) {
        final String filename = path.substring(path.lastIndexOf("/") + 1);
        return variantFilenamePrefix(config) + filename;
    }

    private String variantFilenamePrefix(final SwaggerGenerationConfig config) {
        final StringBuilder prefix = new StringBuilder();

        if (config.includeMethodNotAllowedEndpoints) {
            prefix.append("permissive-");
        }
        if (config.strongSchemas) {
            prefix.append("strong-");
        }
        if (config.pathParameterPlacement
                == SwaggerGenerationConfig.PathParameterPlacement.OPERATION) {
            prefix.append("operational-");
        }

        return prefix.toString();
    }
}
