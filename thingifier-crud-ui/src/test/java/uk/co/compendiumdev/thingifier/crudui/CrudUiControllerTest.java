package uk.co.compendiumdev.thingifier.crudui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CrudUiControllerTest {

    @Test
    public void workspaceRouteReturnsSchemaMetadata() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            CrudUiController controller = new CrudUiController(workspace);

            UiHttpResponse response = controller.workspace();

            Assertions.assertEquals(200, response.statusCode());
            Assertions.assertTrue(response.body().contains("\"entities\""));
            Assertions.assertTrue(response.body().contains("\"relationships\""));
            Assertions.assertTrue(response.body().contains("\"schemaYaml\""));
        }
    }

    @Test
    public void invalidYamlDoesNotReplaceWorkspace() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            CrudUiController controller = new CrudUiController(workspace);

            UiHttpResponse response = controller.loadYaml("formatVersion: 1\nentities: [");

            Assertions.assertEquals(400, response.statusCode());
            Assertions.assertEquals("Todo Manager", workspace.snapshot().definition().title());
        }
    }

    @Test
    public void schemaYamlPreviewDoesNotReplaceWorkspace() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            CrudUiController controller = new CrudUiController(workspace);
            long version = workspace.snapshot().version();
            String title = workspace.snapshot().definition().title();

            UiHttpResponse response =
                    controller.schemaFromYaml(TestResources.text("/models/minimal-todo.yaml"));
            JsonObject body = JsonParser.parseString(response.body()).getAsJsonObject();

            Assertions.assertEquals(200, response.statusCode());
            Assertions.assertTrue(body.get("valid").getAsBoolean());
            Assertions.assertEquals(version, workspace.snapshot().version());
            Assertions.assertEquals(title, workspace.snapshot().definition().title());
        }
    }

    @Test
    public void schemaDraftPreviewDoesNotReplaceWorkspace() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            CrudUiController controller = new CrudUiController(workspace);
            JsonObject parsed =
                    JsonParser.parseString(
                                    controller
                                            .schemaFromYaml(
                                                    TestResources.text("/models/minimal-todo.yaml"))
                                            .body())
                            .getAsJsonObject();
            long version = workspace.snapshot().version();

            UiHttpResponse response = controller.previewSchema(parsed.get("draft").toString());

            Assertions.assertEquals(200, response.statusCode());
            Assertions.assertTrue(
                    JsonParser.parseString(response.body())
                            .getAsJsonObject()
                            .get("valid")
                            .getAsBoolean());
            Assertions.assertEquals(version, workspace.snapshot().version());
            Assertions.assertEquals("Todo Manager", workspace.snapshot().definition().title());
        }
    }

    @Test
    public void malformedSchemaYamlPreviewReturnsBadRequest() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            CrudUiController controller = new CrudUiController(workspace);

            UiHttpResponse response = controller.schemaFromYaml("formatVersion: 1\nentities: [");

            Assertions.assertEquals(400, response.statusCode());
            Assertions.assertEquals("Todo Manager", workspace.snapshot().definition().title());
        }
    }

    @Test
    public void staticIndexResourceIsAvailable() {
        String index = new ResourceReader().read("/public/index.html");

        Assertions.assertTrue(index.contains("Thingifier CRUD UI"));
        Assertions.assertTrue(index.contains("/assets/app.js"));
        Assertions.assertTrue(index.contains("href=\"/favicon.svg\""));
        Assertions.assertTrue(index.contains("API Docs"));
        Assertions.assertTrue(index.contains("Swagger UI"));
        Assertions.assertTrue(index.contains("Download OpenAPI"));
        Assertions.assertTrue(index.contains("Workspace"));
        Assertions.assertTrue(index.contains("Schema Edit"));
        Assertions.assertTrue(index.contains("href=\"/schema\""));
        Assertions.assertTrue(index.contains("main class=\"workspace\""));
        Assertions.assertFalse(index.contains("id=\"schema-editor\""));
        Assertions.assertFalse(index.contains("YAML Draft"));
        Assertions.assertFalse(index.contains("https://unpkg.com/mermaid"));
        Assertions.assertFalse(index.contains("workspace-version"));
    }

    @Test
    public void staticSchemaResourceIsAvailable() {
        String schema = new ResourceReader().read("/public/schema.html");

        Assertions.assertTrue(schema.contains("Thingifier Schema Edit"));
        Assertions.assertTrue(schema.contains("href=\"/favicon.svg\""));
        Assertions.assertTrue(schema.contains("id=\"schema-workspace-link\""));
        Assertions.assertTrue(schema.contains("href=\"/\""));
        Assertions.assertTrue(schema.contains("href=\"/schema\""));
        Assertions.assertTrue(schema.contains("id=\"schema-editor\""));
        Assertions.assertTrue(schema.contains("id=\"schema-tree\""));
        Assertions.assertTrue(schema.contains("id=\"schema-detail-host\""));
        Assertions.assertTrue(schema.contains("id=\"schema-dirty-status\""));
        Assertions.assertTrue(schema.contains("id=\"schema-toggle-diagram-button\""));
        Assertions.assertTrue(schema.contains("id=\"schema-zoom-out-button\""));
        Assertions.assertTrue(schema.contains("id=\"schema-zoom-reset-button\""));
        Assertions.assertTrue(schema.contains("id=\"schema-zoom-in-button\""));
        Assertions.assertTrue(schema.contains("id=\"schema-layout-toggle-button\""));
        Assertions.assertTrue(schema.contains("id=\"schema-diagram-resizer\""));
        Assertions.assertTrue(schema.contains("id=\"schema-toggle-exports-button\""));
        Assertions.assertTrue(schema.contains("Save as YAML"));
        Assertions.assertTrue(schema.contains("YAML Draft"));
        Assertions.assertTrue(schema.contains("Parse YAML"));
        Assertions.assertTrue(schema.contains("Save Mermaid"));
        Assertions.assertTrue(schema.contains("Save Graphviz"));
        Assertions.assertTrue(schema.contains("schema-copy-yaml"));
        Assertions.assertTrue(schema.contains("schema-copy-mermaid"));
        Assertions.assertTrue(schema.contains("schema-copy-graphviz"));
        Assertions.assertTrue(schema.contains("schema-mermaid-diagram"));
        Assertions.assertTrue(schema.contains("Relationship Key"));
        Assertions.assertTrue(schema.contains("exactly one"));
        Assertions.assertTrue(schema.contains("zero or one"));
        Assertions.assertTrue(schema.contains("one or more"));
        Assertions.assertTrue(schema.contains("zero or more"));
        Assertions.assertTrue(schema.contains("er-key-symbol"));
        Assertions.assertFalse(schema.contains("<code>}|"));
        Assertions.assertFalse(schema.contains("<code>o{"));
        Assertions.assertTrue(schema.contains("https://unpkg.com/mermaid"));
        Assertions.assertTrue(schema.contains("https://unpkg.com/tippy.js"));
        Assertions.assertFalse(schema.contains("API Docs"));
        Assertions.assertFalse(schema.contains("Swagger UI"));
        Assertions.assertFalse(schema.contains("Download OpenAPI"));
        Assertions.assertFalse(schema.contains("outline-tree"));
        Assertions.assertFalse(schema.contains("workspace-version"));
    }

    @Test
    public void openApiJsonUsesActiveWorkspaceApiServer() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
            CrudUiController controller = new CrudUiController(workspace);

            UiHttpResponse response = controller.openApiJson();

            Assertions.assertEquals(200, response.statusCode());
            Assertions.assertEquals("application/json", response.contentType());
            Assertions.assertTrue(response.body().contains("\"openapi\""));
            Assertions.assertTrue(response.body().contains("\"/api\""));
            Assertions.assertTrue(response.body().contains("\"/projects\""));
            Assertions.assertTrue(response.body().contains("\"/projects/{id}/tasks\""));
        }
    }

    @Test
    public void openApiDownloadAddsAttachmentHeader() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
            CrudUiController controller = new CrudUiController(workspace);

            UiHttpResponse response = controller.downloadOpenApi(false);

            Assertions.assertEquals(200, response.statusCode());
            Assertions.assertEquals("application/json", response.contentType());
            Assertions.assertEquals(
                    "attachment; filename=\"project-tasks-openapi.json\"",
                    response.headers().get("Content-Disposition"));
            Assertions.assertTrue(response.body().contains("\"Project Tasks\""));
        }
    }

    @Test
    public void apiDocumentationPageListsSchemaAndGeneratedRoutes() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
            CrudUiController controller = new CrudUiController(workspace);

            UiHttpResponse response = controller.apiDocumentationPage();

            Assertions.assertEquals(200, response.statusCode());
            Assertions.assertEquals("text/html", response.contentType());
            Assertions.assertTrue(response.body().contains("Project Tasks API Documentation"));
            Assertions.assertTrue(response.body().contains("href=\"/favicon.svg\""));
            Assertions.assertTrue(response.body().contains("/api/projects"));
            Assertions.assertTrue(response.body().contains("/api/projects/{id}/tasks"));
            Assertions.assertTrue(response.body().contains("Download OpenAPI"));
        }
    }

    @Test
    public void swaggerUiPageEmbedsUnpkgSwaggerUiForCurrentOpenApiJson() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            CrudUiController controller = new CrudUiController(workspace);

            UiHttpResponse response = controller.swaggerUi();

            Assertions.assertEquals(200, response.statusCode());
            Assertions.assertTrue(
                    response.body().contains("https://unpkg.com/swagger-ui-dist/swagger-ui.css"));
            Assertions.assertTrue(
                    response.body()
                            .contains("https://unpkg.com/swagger-ui-dist/swagger-ui-bundle.js"));
            Assertions.assertTrue(response.body().contains("href=\"/favicon.svg\""));
            Assertions.assertTrue(response.body().contains("SwaggerUIBundle"));
            Assertions.assertTrue(response.body().contains("/openapi.json"));
        }
    }

    @Test
    public void staticAssetsContainExpandableRelationshipOutline() {
        ResourceReader reader = new ResourceReader();
        String script = reader.read("/public/assets/app.js");
        String styles = reader.read("/public/assets/styles.css");

        Assertions.assertTrue(script.contains("relationshipNodesFor"));
        Assertions.assertTrue(script.contains("renderRelationshipNode"));
        Assertions.assertTrue(script.contains("relationshipManagementPanel"));
        Assertions.assertTrue(script.contains("Connect existing"));
        Assertions.assertTrue(script.contains("Create and connect"));
        Assertions.assertTrue(script.contains("Remove from relationship"));
        Assertions.assertTrue(script.contains("connectExistingRelationship"));
        Assertions.assertTrue(script.contains("createAndConnectRelationship"));
        Assertions.assertTrue(script.contains("removeRelationship"));
        Assertions.assertTrue(script.contains("unrelatedRowsFor"));
        Assertions.assertTrue(script.contains("targetEntity.fields.filter(field => !field.auto)"));
        Assertions.assertTrue(script.contains("refreshRelationshipView"));
        Assertions.assertTrue(script.contains("instanceLabel"));
        Assertions.assertTrue(script.contains("<auto-assigned>"));
        Assertions.assertTrue(script.contains("activeWorkspaceVersion"));
        Assertions.assertTrue(script.contains("workspaceChangedSince"));
        Assertions.assertTrue(script.contains("state.expandedNodes[key] === true"));
        Assertions.assertTrue(script.contains("initializeSchemaEditor"));
        Assertions.assertTrue(script.contains("/ui/schema/from-yaml"));
        Assertions.assertTrue(script.contains("/ui/schema/preview"));
        Assertions.assertTrue(script.contains("renderSchemaTree"));
        Assertions.assertTrue(script.contains("renderSchemaDetail"));
        Assertions.assertTrue(script.contains("renderEntityDetail"));
        Assertions.assertTrue(script.contains("renderFieldDetail"));
        Assertions.assertTrue(script.contains("renderRelationshipDetail"));
        Assertions.assertTrue(script.contains("copySchemaOutput"));
        Assertions.assertTrue(script.contains("initializeSchemaHelp"));
        Assertions.assertTrue(script.contains("validationListEditor"));
        Assertions.assertTrue(script.contains("downloadSchemaOutput"));
        Assertions.assertTrue(script.contains("window.mermaid.render"));
        Assertions.assertTrue(script.contains("schemaDirty"));
        Assertions.assertTrue(script.contains("confirmSchemaSaveBefore"));
        Assertions.assertTrue(script.contains("schemaDiagramZoom"));
        Assertions.assertTrue(script.contains("schemaDiagramDirection"));
        Assertions.assertTrue(script.contains("mermaidSourceForCurrentLayout"));
        Assertions.assertTrue(script.contains("beginSchemaDiagramResize"));
        Assertions.assertFalse(script.contains("els.version.textContent"));
        Assertions.assertTrue(styles.contains(".view-nav"));
        Assertions.assertTrue(styles.contains(".schema-editor"));
        Assertions.assertTrue(styles.contains(".schema-workbench"));
        Assertions.assertTrue(styles.contains(".schema-tree-panel"));
        Assertions.assertTrue(styles.contains(".schema-detail-panel"));
        Assertions.assertTrue(styles.contains(".copy-button::before"));
        Assertions.assertTrue(styles.contains(".help-icon"));
        Assertions.assertTrue(styles.contains(".schema-code-input"));
        Assertions.assertTrue(styles.contains(".schema-mermaid-diagram"));
        Assertions.assertTrue(styles.contains(".schema-diagram-key"));
        Assertions.assertTrue(styles.contains(".schema-key-title"));
        Assertions.assertTrue(styles.contains(".schema-dirty-status"));
        Assertions.assertTrue(styles.contains(".schema-diagram-actions"));
        Assertions.assertTrue(styles.contains(".schema-diagram-resizer"));
        Assertions.assertTrue(styles.contains(".er-key-symbol"));
        Assertions.assertTrue(styles.contains(".relationship-manager"));
        Assertions.assertTrue(styles.contains(".relationship-manager-card"));
        Assertions.assertTrue(styles.contains(".related-instance-group"));
        Assertions.assertTrue(styles.contains(".tree-caret"));
        Assertions.assertTrue(styles.contains(".field-control-readonly"));
    }

    @Test
    public void faviconResourceIsAvailableAndSimple() {
        String favicon = new ResourceReader().read("/public/favicon.svg");

        Assertions.assertTrue(favicon.contains("<svg"));
        Assertions.assertTrue(favicon.contains("viewBox=\"0 0 32 32\""));
        Assertions.assertTrue(favicon.contains("#134f56"));
        Assertions.assertTrue(favicon.contains("#f1c84c"));
        Assertions.assertFalse(favicon.contains("<text"));
    }
}
