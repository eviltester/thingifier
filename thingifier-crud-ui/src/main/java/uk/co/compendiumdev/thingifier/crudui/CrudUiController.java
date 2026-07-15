package uk.co.compendiumdev.thingifier.crudui;

import java.util.LinkedHashMap;
import java.util.Map;
import uk.co.compendiumdev.thingifier.yaml.ThingifierYamlException;

public final class CrudUiController {

    private final ActiveThingifierWorkspace workspace;
    private final WorkspaceMetadataJson metadataJson;
    private final WorkspaceDataExporter exporter;
    private final WorkspaceDataImporter importer;
    private final SchemaPreviewService schemaPreviewService;

    public CrudUiController(final ActiveThingifierWorkspace workspace) {
        this.workspace = workspace;
        DynamicThingifierApiProxy apiProxy = new DynamicThingifierApiProxy(workspace);
        metadataJson = new WorkspaceMetadataJson();
        exporter = new WorkspaceDataExporter(workspace, apiProxy);
        importer = new WorkspaceDataImporter(workspace, apiProxy, metadataJson);
        schemaPreviewService = new SchemaPreviewService();
    }

    public UiHttpResponse workspace() {
        return UiHttpResponse.json(200, metadataJson.toJson(workspace.snapshot()));
    }

    public UiHttpResponse loadYaml(final String yamlText) {
        try {
            workspace.replaceWithYaml(yamlText);
            return workspace();
        } catch (ThingifierYamlException | IllegalArgumentException e) {
            return JsonSupport.error(400, e.getMessage());
        }
    }

    public UiHttpResponse schemaFromYaml(final String yamlText) {
        try {
            return schemaPreviewService.fromYaml(yamlText);
        } catch (ThingifierYamlException | IllegalArgumentException e) {
            return JsonSupport.error(400, e.getMessage());
        }
    }

    public UiHttpResponse previewSchema(final String schemaDraftJson) {
        try {
            return schemaPreviewService.previewDraft(schemaDraftJson);
        } catch (CrudUiException | IllegalArgumentException e) {
            return JsonSupport.error(400, e.getMessage());
        }
    }

    public UiHttpResponse exportData() {
        try {
            return exporter.exportData();
        } catch (CrudUiException e) {
            return JsonSupport.error(e.statusCode(), e.getMessage());
        }
    }

    public UiHttpResponse importData(final String jsonText) {
        try {
            return importer.importData(jsonText);
        } catch (ThingifierYamlException | CrudUiException | IllegalArgumentException e) {
            return JsonSupport.error(400, e.getMessage());
        }
    }

    public UiHttpResponse apiDocumentationPage() {
        return UiHttpResponse.html(new ApiDocumentationPage(workspace.snapshot()).html());
    }

    public UiHttpResponse swaggerUi() {
        return UiHttpResponse.html(new SwaggerUiPage(workspace.snapshot()).html());
    }

    public UiHttpResponse openApiJson() {
        return UiHttpResponse.json(
                200, new OpenApiDocumentation(workspace.snapshot()).openApiJson());
    }

    public UiHttpResponse downloadOpenApi(final boolean permissive) {
        OpenApiDocumentation openApi = new OpenApiDocumentation(workspace.snapshot());
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(
                "Content-Disposition",
                "attachment; filename=\"" + openApi.downloadFilename(permissive) + "\"");
        headers.put("Cache-Control", "no-store");
        String body = permissive ? openApi.permissiveOpenApiJson() : openApi.openApiJson();
        return new UiHttpResponse(200, "application/json", body, headers);
    }
}
