package uk.co.compendiumdev.thingifier.crudui.adapter.spark;

import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Spark;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.spark.conversion.SparkToInternalHttpRequest;
import uk.co.compendiumdev.thingifier.crudui.ActiveThingifierWorkspace;
import uk.co.compendiumdev.thingifier.crudui.CrudUiController;
import uk.co.compendiumdev.thingifier.crudui.DynamicThingifierApiProxy;
import uk.co.compendiumdev.thingifier.crudui.ResourceReader;
import uk.co.compendiumdev.thingifier.crudui.UiHttpResponse;

public final class CrudUiApplication implements AutoCloseable {

    private final ActiveThingifierWorkspace workspace;
    private final CrudUiController controller;
    private final DynamicThingifierApiProxy apiProxy;
    private final ResourceReader resourceReader;
    private final int port;

    public CrudUiApplication(final ActiveThingifierWorkspace workspace, final int port) {
        this.workspace = workspace;
        this.controller = new CrudUiController(workspace);
        this.apiProxy = new DynamicThingifierApiProxy(workspace);
        this.resourceReader = new ResourceReader();
        this.port = port;
    }

    public void start() {
        Spark.port(port);
        Spark.staticFileLocation("/public");
        configureRoutes();
        Spark.awaitInitialization();
    }

    public void configureRoutes() {
        Spark.get("/", (request, response) -> write(response, index()));
        Spark.get("/schema", (request, response) -> write(response, schema()));
        Spark.get("/ui/workspace", (request, response) -> write(response, controller.workspace()));
        Spark.post(
                "/ui/model/yaml",
                (request, response) -> write(response, controller.loadYaml(request.body())));
        Spark.post(
                "/ui/schema/from-yaml",
                (request, response) -> write(response, controller.schemaFromYaml(request.body())));
        Spark.post(
                "/ui/schema/preview",
                (request, response) -> write(response, controller.previewSchema(request.body())));
        Spark.post(
                "/ui/schema/upgrade/preview",
                (request, response) ->
                        write(response, controller.previewSchemaUpgrade(request.body())));
        Spark.post(
                "/ui/schema/upgrade/apply",
                (request, response) ->
                        write(response, controller.applySchemaUpgrade(request.body())));
        Spark.get("/ui/export", (request, response) -> write(response, controller.exportData()));
        Spark.post(
                "/ui/import",
                (request, response) -> write(response, controller.importData(request.body())));
        Spark.post(
                "/ui/project/save",
                (request, response) -> write(response, controller.saveProject(request.body())));
        Spark.post(
                "/ui/project/load",
                (request, response) -> write(response, controller.loadProject(request.body())));
        Spark.get(
                "/docs", (request, response) -> write(response, controller.apiDocumentationPage()));
        Spark.get(
                "/docs/swagger",
                (request, response) ->
                        write(
                                response,
                                controller.downloadOpenApi(
                                        request.queryParams("permissive") != null)));
        Spark.get(
                "/openapi.json", (request, response) -> write(response, controller.openApiJson()));
        Spark.get("/swagger", (request, response) -> write(response, controller.swaggerUi()));

        Spark.get("/api", this::forwardApi);
        Spark.get("/api/*", this::forwardApi);
        Spark.head("/api", this::forwardApi);
        Spark.head("/api/*", this::forwardApi);
        Spark.post("/api", this::forwardApi);
        Spark.post("/api/*", this::forwardApi);
        Spark.put("/api", this::forwardApi);
        Spark.put("/api/*", this::forwardApi);
        Spark.delete("/api", this::forwardApi);
        Spark.delete("/api/*", this::forwardApi);
    }

    private UiHttpResponse index() {
        return UiHttpResponse.html(resourceReader.read("/public/index.html"));
    }

    private UiHttpResponse schema() {
        return UiHttpResponse.html(resourceReader.read("/public/schema.html"));
    }

    private String forwardApi(final Request request, final Response response) {
        InternalHttpRequest internalRequest = SparkToInternalHttpRequest.convert(request);
        return write(response, apiProxy.forward(internalRequest));
    }

    private String write(final Response response, final UiHttpResponse uiResponse) {
        response.status(uiResponse.statusCode());
        response.type(uiResponse.contentType());
        for (Map.Entry<String, String> header : uiResponse.headers().entrySet()) {
            response.raw().setHeader(header.getKey(), header.getValue());
        }
        response.body(uiResponse.body());
        return uiResponse.body();
    }

    @Override
    public void close() {
        workspace.close();
    }
}
