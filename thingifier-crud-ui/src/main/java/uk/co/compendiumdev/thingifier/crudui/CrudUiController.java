package uk.co.compendiumdev.thingifier.crudui;

import java.util.LinkedHashMap;
import java.util.Map;
import uk.co.compendiumdev.thingifier.yaml.ThingifierYamlException;

public final class CrudUiController {

    private final ActiveThingifierWorkspace workspace;
    private final WorkspaceMetadataJson metadataJson;
    private final WorkspaceDataExporter exporter;
    private final WorkspaceDataImporter importer;
    private final WorkspaceProjectService projectService;
    private final ProjectPathChooser projectPathChooser;
    private final SchemaPreviewService schemaPreviewService;
    private final WorkspaceSchemaUpgradeService schemaUpgradeService;

    public CrudUiController(final ActiveThingifierWorkspace workspace) {
        this(workspace, new SwingProjectPathChooser());
    }

    CrudUiController(
            final ActiveThingifierWorkspace workspace,
            final ProjectPathChooser projectPathChooser) {
        this.workspace = workspace;
        this.projectPathChooser = projectPathChooser;
        DynamicThingifierApiProxy apiProxy = new DynamicThingifierApiProxy(workspace);
        metadataJson = new WorkspaceMetadataJson();
        exporter = new WorkspaceDataExporter(workspace, apiProxy);
        importer = new WorkspaceDataImporter(workspace, apiProxy, metadataJson);
        projectService = new WorkspaceProjectService(workspace, metadataJson);
        schemaPreviewService = new SchemaPreviewService();
        schemaUpgradeService = new WorkspaceSchemaUpgradeService(workspace, metadataJson);
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

    public UiHttpResponse previewSchemaUpgrade(final String upgradeRequestJson) {
        try {
            return schemaUpgradeService.preview(upgradeRequestJson);
        } catch (CrudUiException e) {
            return JsonSupport.error(e.statusCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            return JsonSupport.error(400, e.getMessage());
        }
    }

    public UiHttpResponse applySchemaUpgrade(final String upgradeRequestJson) {
        try {
            return schemaUpgradeService.apply(upgradeRequestJson);
        } catch (CrudUiException e) {
            return JsonSupport.error(e.statusCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
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

    public UiHttpResponse saveProject(final String requestJson) {
        try {
            return projectService.save(requestJson);
        } catch (ThingifierYamlException | CrudUiException | IllegalArgumentException e) {
            return JsonSupport.error(400, e.getMessage());
        }
    }

    public UiHttpResponse loadProject(final String requestJson) {
        try {
            return projectService.load(requestJson);
        } catch (ThingifierYamlException | CrudUiException | IllegalArgumentException e) {
            return JsonSupport.error(400, e.getMessage());
        }
    }

    public UiHttpResponse browseProject(final String requestJson) {
        try {
            ProjectActionRequest request = ProjectActionRequest.fromJson(requestJson, false);
            ProjectPathSelection selection = projectPathChooser.choose(request);
            if (!selection.isAvailable()) {
                return JsonSupport.error(400, selection.message());
            }
            return UiHttpResponse.json(200, JsonSupport.toJson(selection.toMap()));
        } catch (CrudUiException | IllegalArgumentException e) {
            return JsonSupport.error(400, e.getMessage());
        }
    }

    public UiHttpResponse checkProject(final String requestJson) {
        try {
            return projectService.check(requestJson);
        } catch (ThingifierYamlException | CrudUiException | IllegalArgumentException e) {
            return JsonSupport.error(400, e.getMessage());
        }
    }

    public UiHttpResponse exportProjectFiles() {
        return exportProjectFiles("{}");
    }

    public UiHttpResponse exportProjectFiles(final String requestJson) {
        try {
            return projectService.exportFiles(requestJson);
        } catch (ThingifierYamlException | CrudUiException | IllegalArgumentException e) {
            return JsonSupport.error(400, e.getMessage());
        }
    }

    public UiHttpResponse loadProjectFiles(final String requestJson) {
        try {
            return projectService.loadFiles(requestJson);
        } catch (ThingifierYamlException | CrudUiException | IllegalArgumentException e) {
            return JsonSupport.error(400, e.getMessage());
        }
    }

    public UiHttpResponse switchStorage(final String requestJson) {
        try {
            Map<?, ?> request =
                    JsonSupport.fromJsonMap(
                            requestJson,
                            "Storage request must contain a JSON object",
                            "Could not parse storage request JSON");
            WorkspaceStorage storage =
                    WorkspaceStorage.fromModeAndPath(
                            stringValue(request.get("mode")),
                            stringValue(request.get("sqliteFile")));
            WorkspaceSnapshot snapshot = workspace.switchStorage(storage);
            Map<String, Object> body = metadataJson.toMap(snapshot);
            body.put("storageStatus", "switched");
            return UiHttpResponse.json(200, JsonSupport.toJson(body));
        } catch (CrudUiException | IllegalArgumentException | IllegalStateException e) {
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

    private String stringValue(final Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
