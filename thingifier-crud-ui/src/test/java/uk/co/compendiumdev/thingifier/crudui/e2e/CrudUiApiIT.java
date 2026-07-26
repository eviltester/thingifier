package uk.co.compendiumdev.thingifier.crudui.e2e;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CrudUiApiIT {

    private static CrudUiTestServer server;
    private CrudUiApiClient api;

    @BeforeAll
    static void startServer() {
        server = CrudUiTestServer.start();
    }

    @AfterAll
    static void stopServer() {
        server.close();
    }

    @BeforeEach
    void resetWorkspace() {
        api = server.api();
        server.resetToYaml("/models/project-tasks.yaml");
    }

    @Test
    public void staticPagesAndGeneratedDocumentationAreReachable() {
        assertStatusContains("/", 200, "Thingifier CRUD UI");
        assertStatusContains("/schema", 200, "Thingifier Schema Edit");
        assertStatusContains("/docs", 200, "API Documentation");
        assertStatusContains("/swagger", 200, "SwaggerUIBundle");
        assertStatusContains("/swagger", 200, "swagger-copy-for-ai.js");
        assertStatusContains("/openapi.json", 200, "\"openapi\"");
        assertStatusContains("/openapi-3.1.json", 200, "\"openapi\"");
        assertStatusContains("/openapi-3.0.json", 200, "\"openapi\"");
        assertStatusContains("/docs/swagger", 200, "\"openapi\"");

        CrudUiApiClient.ApiResult removedAlias = api.get("/openapi-3-1.json");
        Assertions.assertEquals(404, removedAlias.statusCode(), removedAlias.body());
    }

    @Test
    public void workspaceEndpointsImportExportAndSchemaPreviewWorkThroughHttp() {
        JsonObject workspace = api.get("/ui/workspace").jsonObject();
        Assertions.assertEquals(
                "Project Tasks", workspace.getAsJsonObject("model").get("title").getAsString());

        CrudUiApiClient.ApiResult fromYaml =
                api.postText("/ui/schema/from-yaml", E2eResource.text("/models/minimal-todo.yaml"));
        Assertions.assertEquals(200, fromYaml.statusCode(), fromYaml.body());
        JsonObject draft = fromYaml.jsonObject().getAsJsonObject("draft");

        CrudUiApiClient.ApiResult preview = api.postJson("/ui/schema/preview", draft.toString());
        Assertions.assertEquals(200, preview.statusCode(), preview.body());
        Assertions.assertTrue(preview.jsonObject().get("valid").getAsBoolean());

        CrudUiApiClient.ApiResult created =
                api.postJson("/api/todos", "{\"title\":\"Round trip\"}");
        Assertions.assertEquals(201, created.statusCode(), created.body());
        CrudUiApiClient.ApiResult exported = api.get("/ui/export");
        Assertions.assertEquals(200, exported.statusCode(), exported.body());
        Assertions.assertTrue(exported.body().contains("Round trip"));

        CrudUiApiClient.ApiResult imported = api.postJson("/ui/import", exported.body());
        Assertions.assertEquals(200, imported.statusCode(), imported.body());
        Assertions.assertTrue(api.get("/api/todos").body().contains("Round trip"));
    }

    @Test
    public void schemaUpgradePreviewAndApplyAreAvailableThroughHttp() {
        JsonObject projectDraft =
                api.postText("/ui/schema/from-yaml", E2eResource.text("/models/project-tasks.yaml"))
                        .jsonObject()
                        .getAsJsonObject("draft");
        entityNamed(projectDraft, "todo")
                .getAsJsonArray("fields")
                .add(field("status", "string", "open"));

        long version = api.get("/ui/workspace").jsonObject().get("workspaceVersion").getAsLong();
        CrudUiApiClient.ApiResult preview =
                api.postJson("/ui/schema/upgrade/preview", upgradeRequest(projectDraft, version));
        Assertions.assertEquals(200, preview.statusCode(), preview.body());
        Assertions.assertTrue(preview.jsonObject().get("canApply").getAsBoolean());

        CrudUiApiClient.ApiResult apply =
                api.postJson("/ui/schema/upgrade/apply", upgradeRequest(projectDraft, version));
        Assertions.assertEquals(200, apply.statusCode(), apply.body());
        Assertions.assertTrue(apply.body().contains("\"status\""));
    }

    @Test
    public void projectFilePayloadAndCheckEndpointsWorkThroughHttp() {
        CrudUiApiClient.ApiResult check =
                api.postJson(
                        "/ui/project/check",
                        "{\"action\":\"save\",\"path\":\"target/e2e-project\"}");
        Assertions.assertEquals(200, check.statusCode(), check.body());
        Assertions.assertTrue(check.jsonObject().get("canProceed").getAsBoolean());

        CrudUiApiClient.ApiResult exported = api.postJson("/ui/project/export-files", "{}");
        Assertions.assertEquals(200, exported.statusCode(), exported.body());
        JsonArray files = exported.jsonObject().getAsJsonArray("files");
        Assertions.assertTrue(files.toString().contains("projectfile.erproj"));

        JsonObject loadRequest = new JsonObject();
        loadRequest.addProperty("folderName", "browser-project");
        loadRequest.add("files", files);
        CrudUiApiClient.ApiResult loaded =
                api.postJson("/ui/project/load-files", loadRequest.toString());
        Assertions.assertEquals(200, loaded.statusCode(), loaded.body());
        Assertions.assertTrue(loaded.body().contains("Browser folder: browser-project"));
    }

    @Test
    public void storageEndpointAndDynamicApiProxySupportCrudAndRelationships() {
        CrudUiApiClient.ApiResult switched =
                api.postJson("/ui/storage/switch", "{\"mode\":\"sqlite-memory\"}");
        Assertions.assertEquals(200, switched.statusCode(), switched.body());
        Assertions.assertEquals(
                "sqlite-memory",
                switched.jsonObject().getAsJsonObject("storage").get("mode").getAsString());

        String projectId = create("/api/projects", "title", "Smoke Project");
        String todoId = create("/api/todos", "title", "Do this");
        CrudUiApiClient.ApiResult connected =
                api.postJson(
                        "/api/projects/" + projectId + "/tasks", "{\"id\":\"" + todoId + "\"}");
        Assertions.assertEquals(201, connected.statusCode(), connected.body());
        Assertions.assertTrue(
                api.get("/api/projects/" + projectId + "/tasks").body().contains("Do this"));

        CrudUiApiClient.ApiResult disconnected =
                api.delete("/api/projects/" + projectId + "/tasks/" + todoId);
        Assertions.assertEquals(200, disconnected.statusCode(), disconnected.body());
        Assertions.assertTrue(api.get("/api/todos/" + todoId).body().contains("Do this"));
    }

    private void assertStatusContains(
            final String path, final int statusCode, final String expectedText) {
        CrudUiApiClient.ApiResult result = api.get(path);
        Assertions.assertEquals(statusCode, result.statusCode(), result.body());
        Assertions.assertTrue(result.body().contains(expectedText), result.body());
    }

    private String create(final String path, final String field, final String value) {
        CrudUiApiClient.ApiResult created =
                api.postJson(path, JsonSupport.map(field, value).toString());
        Assertions.assertEquals(201, created.statusCode(), created.body());
        return created.jsonObject().get("id").getAsString();
    }

    private String upgradeRequest(final JsonObject draft, final long version) {
        JsonObject request = new JsonObject();
        request.add("draft", draft);
        request.addProperty("expectedWorkspaceVersion", version);
        request.add("mappings", new JsonObject());
        return request.toString();
    }

    private JsonObject field(final String name, final String type, final String defaultValue) {
        JsonObject field = new JsonObject();
        field.addProperty("name", name);
        field.addProperty("type", type);
        field.addProperty("defaultValue", defaultValue);
        return field;
    }

    private JsonObject entityNamed(final JsonObject draft, final String name) {
        for (var entityValue : draft.getAsJsonArray("entities")) {
            JsonObject entity = entityValue.getAsJsonObject();
            if (name.equals(entity.get("name").getAsString())) {
                return entity;
            }
        }
        throw new AssertionError("Missing entity " + name);
    }

    private static final class JsonSupport {

        private JsonSupport() {}

        static JsonObject map(final String key, final String value) {
            JsonObject object = new JsonObject();
            object.addProperty(key, value);
            return object;
        }
    }
}
