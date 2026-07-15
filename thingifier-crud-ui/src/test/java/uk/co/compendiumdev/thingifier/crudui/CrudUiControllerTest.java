package uk.co.compendiumdev.thingifier.crudui;

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
    public void staticIndexResourceIsAvailable() {
        String index = new ResourceReader().read("/public/index.html");

        Assertions.assertTrue(index.contains("Thingifier CRUD UI"));
        Assertions.assertTrue(index.contains("/assets/app.js"));
        Assertions.assertTrue(index.contains("API Docs"));
        Assertions.assertTrue(index.contains("Swagger UI"));
        Assertions.assertTrue(index.contains("Download OpenAPI"));
        Assertions.assertFalse(index.contains("workspace-version"));
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
        Assertions.assertFalse(script.contains("els.version.textContent"));
        Assertions.assertTrue(styles.contains(".relationship-manager"));
        Assertions.assertTrue(styles.contains(".relationship-manager-card"));
        Assertions.assertTrue(styles.contains(".related-instance-group"));
        Assertions.assertTrue(styles.contains(".tree-caret"));
        Assertions.assertTrue(styles.contains(".field-control-readonly"));
    }
}
