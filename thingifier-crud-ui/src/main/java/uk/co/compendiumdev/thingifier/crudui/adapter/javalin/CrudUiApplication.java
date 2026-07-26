package uk.co.compendiumdev.thingifier.crudui.adapter.javalin;

import java.util.Map;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpRouteRegistry;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;
import uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes;
import uk.co.compendiumdev.thingifier.adapter.httpserver.conversion.HttpServerRequestToInternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.javalin.JavalinHttpServer;
import uk.co.compendiumdev.thingifier.crudui.ActiveThingifierWorkspace;
import uk.co.compendiumdev.thingifier.crudui.CrudUiController;
import uk.co.compendiumdev.thingifier.crudui.DynamicThingifierApiProxy;
import uk.co.compendiumdev.thingifier.crudui.ResourceReader;
import uk.co.compendiumdev.thingifier.crudui.UiHttpResponse;
import uk.co.compendiumdev.thingifier.swaggerizer.OpenApiSpecificationVersion;

public final class CrudUiApplication implements AutoCloseable {

    private final ActiveThingifierWorkspace workspace;
    private final CrudUiController controller;
    private final DynamicThingifierApiProxy apiProxy;
    private final ResourceReader resourceReader;
    private final int port;
    private JavalinHttpServer server;

    public CrudUiApplication(final ActiveThingifierWorkspace workspace, final int port) {
        this.workspace = workspace;
        this.controller = new CrudUiController(workspace);
        this.apiProxy = new DynamicThingifierApiProxy(workspace);
        this.resourceReader = new ResourceReader();
        this.port = port;
    }

    public void start() {
        HttpRouteRegistry registry = new HttpRouteRegistry();
        HttpRouteRegistry.use(registry);
        configureRoutes();
        server = new JavalinHttpServer(port, "/public", registry);
        server.start();
    }

    public void configureRoutes() {
        ServerRoutes.get("/", (request, response) -> write(response, index()));
        ServerRoutes.get("/schema", (request, response) -> write(response, schema()));
        ServerRoutes.get(
                "/ui/workspace", (request, response) -> write(response, controller.workspace()));
        ServerRoutes.post(
                "/ui/model/yaml",
                (request, response) -> write(response, controller.loadYaml(request.body())));
        ServerRoutes.post(
                "/ui/schema/from-yaml",
                (request, response) -> write(response, controller.schemaFromYaml(request.body())));
        ServerRoutes.post(
                "/ui/schema/preview",
                (request, response) -> write(response, controller.previewSchema(request.body())));
        ServerRoutes.post(
                "/ui/schema/upgrade/preview",
                (request, response) ->
                        write(response, controller.previewSchemaUpgrade(request.body())));
        ServerRoutes.post(
                "/ui/schema/upgrade/apply",
                (request, response) ->
                        write(response, controller.applySchemaUpgrade(request.body())));
        ServerRoutes.get(
                "/ui/export", (request, response) -> write(response, controller.exportData()));
        ServerRoutes.post(
                "/ui/import",
                (request, response) -> write(response, controller.importData(request.body())));
        ServerRoutes.post(
                "/ui/project/save",
                (request, response) -> write(response, controller.saveProject(request.body())));
        ServerRoutes.post(
                "/ui/project/load",
                (request, response) -> write(response, controller.loadProject(request.body())));
        ServerRoutes.post(
                "/ui/project/browse",
                (request, response) -> write(response, controller.browseProject(request.body())));
        ServerRoutes.post(
                "/ui/project/check",
                (request, response) -> write(response, controller.checkProject(request.body())));
        ServerRoutes.post(
                "/ui/project/export-files",
                (request, response) ->
                        write(response, controller.exportProjectFiles(request.body())));
        ServerRoutes.post(
                "/ui/project/load-files",
                (request, response) ->
                        write(response, controller.loadProjectFiles(request.body())));
        ServerRoutes.post(
                "/ui/storage/switch",
                (request, response) -> write(response, controller.switchStorage(request.body())));
        ServerRoutes.get(
                "/docs", (request, response) -> write(response, controller.apiDocumentationPage()));
        ServerRoutes.get(
                "/docs/swagger",
                (request, response) ->
                        write(
                                response,
                                controller.downloadOpenApi(
                                        request.queryParam("permissive") != null)));
        ServerRoutes.get(
                "/openapi.json", (request, response) -> write(response, controller.openApiJson()));
        ServerRoutes.get(
                "/openapi-3.1.json",
                (request, response) ->
                        write(
                                response,
                                controller.openApiJson(OpenApiSpecificationVersion.OPENAPI_3_1)));
        ServerRoutes.get(
                "/openapi-3.2.json",
                (request, response) ->
                        write(
                                response,
                                controller.openApiJson(OpenApiSpecificationVersion.OPENAPI_3_2)));
        ServerRoutes.get(
                "/openapi-3.0.json",
                (request, response) ->
                        write(
                                response,
                                controller.openApiJson(OpenApiSpecificationVersion.OPENAPI_3_0)));
        ServerRoutes.get(
                "/swagger", (request, response) -> write(response, controller.swaggerUi()));

        ServerRoutes.get("/api", this::forwardApi);
        ServerRoutes.get("/api/*", this::forwardApi);
        ServerRoutes.head("/api", this::forwardApi);
        ServerRoutes.head("/api/*", this::forwardApi);
        ServerRoutes.post("/api", this::forwardApi);
        ServerRoutes.post("/api/*", this::forwardApi);
        ServerRoutes.put("/api", this::forwardApi);
        ServerRoutes.put("/api/*", this::forwardApi);
        ServerRoutes.delete("/api", this::forwardApi);
        ServerRoutes.delete("/api/*", this::forwardApi);
    }

    private UiHttpResponse index() {
        return UiHttpResponse.html(resourceReader.read("/public/index.html"));
    }

    private UiHttpResponse schema() {
        return UiHttpResponse.html(resourceReader.read("/public/schema.html"));
    }

    private String forwardApi(final HttpServerRequest request, final HttpServerResponse response) {
        InternalHttpRequest internalRequest =
                HttpServerRequestToInternalHttpRequest.convert(request);
        return write(response, apiProxy.forward(internalRequest));
    }

    private String write(final HttpServerResponse response, final UiHttpResponse uiResponse) {
        response.status(uiResponse.statusCode());
        response.type(uiResponse.contentType());
        for (Map.Entry<String, String> header : uiResponse.headers().entrySet()) {
            response.header(header.getKey(), header.getValue());
        }
        response.body(uiResponse.body());
        return uiResponse.body();
    }

    @Override
    public void close() {
        if (server != null) {
            server.stop();
            server = null;
        }
        workspace.close();
        HttpRouteRegistry.clearCurrent();
    }
}
