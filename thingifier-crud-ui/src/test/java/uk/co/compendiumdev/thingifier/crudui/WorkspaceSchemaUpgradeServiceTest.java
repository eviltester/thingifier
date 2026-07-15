package uk.co.compendiumdev.thingifier.crudui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WorkspaceSchemaUpgradeServiceTest {

    @Test
    public void previewDoesNotMutateWorkspaceSchemaVersionOrData() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
            DynamicThingifierApiProxy proxy = new DynamicThingifierApiProxy(workspace);
            createProject(proxy, "Project A");
            CrudUiController controller = new CrudUiController(workspace);
            long version = workspace.snapshot().version();

            UiHttpResponse response =
                    controller.previewSchemaUpgrade(
                            upgradeRequest(draftFromYaml("/models/minimal-todo.yaml"), null, null));
            JsonObject body = json(response);

            Assertions.assertEquals(200, response.statusCode(), response.body());
            Assertions.assertTrue(body.get("valid").getAsBoolean());
            Assertions.assertTrue(body.get("canApply").getAsBoolean());
            Assertions.assertEquals(version, workspace.snapshot().version());
            Assertions.assertEquals("Project Tasks", workspace.snapshot().definition().title());
            Assertions.assertTrue(proxy.getJson("projects").body().contains("Project A"));
        }
    }

    @Test
    public void applyRejectsInvalidSchemaDraft() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
            CrudUiController controller = new CrudUiController(workspace);
            JsonObject draft = draftFromYaml("/models/project-tasks.yaml");
            entityNamed(draft, "todo").addProperty("primaryKey", "missing");
            long version = workspace.snapshot().version();

            UiHttpResponse response =
                    controller.applySchemaUpgrade(upgradeRequest(draft, version, new JsonObject()));
            JsonObject body = json(response);

            Assertions.assertEquals(400, response.statusCode());
            Assertions.assertFalse(body.get("valid").getAsBoolean());
            Assertions.assertEquals(version, workspace.snapshot().version());
            Assertions.assertTrue(body.get("errors").toString().contains("primaryKey"));
        }
    }

    @Test
    public void applyRejectsStaleWorkspaceVersion() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
            long staleVersion = workspace.snapshot().version();
            workspace.replaceWithYaml(TestResources.text("/models/minimal-todo.yaml"));
            CrudUiController controller = new CrudUiController(workspace);

            UiHttpResponse response =
                    controller.applySchemaUpgrade(
                            upgradeRequest(
                                    draftFromYaml("/models/minimal-todo.yaml"),
                                    staleVersion,
                                    null));

            Assertions.assertEquals(409, response.statusCode());
            Assertions.assertTrue(response.body().contains("Workspace changed"));
        }
    }

    @Test
    public void addFieldWithDefaultMigratesExistingRowsAndPreservesAutoKey() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
            DynamicThingifierApiProxy proxy = new DynamicThingifierApiProxy(workspace);
            String todoId = createTodo(proxy, "Task A");
            CrudUiController controller = new CrudUiController(workspace);
            JsonObject draft = draftFromYaml("/models/project-tasks.yaml");
            JsonArray fields = entityNamed(draft, "todo").getAsJsonArray("fields");
            fields.add(field("status", "string", "open"));

            UiHttpResponse response =
                    controller.applySchemaUpgrade(
                            upgradeRequest(draft, workspace.snapshot().version(), null));
            JsonObject body = json(response);

            Assertions.assertEquals(200, response.statusCode(), response.body());
            Assertions.assertTrue(body.get("canApply").getAsBoolean());
            Assertions.assertTrue(
                    proxy.getJson("todos/" + todoId).body().contains("\"status\":\"open\""));
            Assertions.assertEquals(
                    "2",
                    fieldValue(proxy.postJson("todos", "{\"title\":\"Task B\"}").body(), "id"));
        }
    }

    @Test
    public void droppedFieldIsRemovedAndReported() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
            DynamicThingifierApiProxy proxy = new DynamicThingifierApiProxy(workspace);
            createTodo(proxy, "Task A");
            CrudUiController controller = new CrudUiController(workspace);
            JsonObject draft = draftFromYaml("/models/project-tasks.yaml");
            JsonArray fields = entityNamed(draft, "todo").getAsJsonArray("fields");
            fields.remove(1);

            UiHttpResponse response =
                    controller.applySchemaUpgrade(
                            upgradeRequest(draft, workspace.snapshot().version(), null));

            Assertions.assertEquals(200, response.statusCode(), response.body());
            Assertions.assertTrue(
                    response.body().contains("Source field todo.title will be dropped"));
            Assertions.assertFalse(proxy.getJson("todos").body().contains("Task A"));
        }
    }

    @Test
    public void manualEntityAndFieldMappingsPreserveRenamedRows() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/minimal-todo.yaml"));
            DynamicThingifierApiProxy proxy = new DynamicThingifierApiProxy(workspace);
            String id = createTodo(proxy, "Task A");
            CrudUiController controller = new CrudUiController(workspace);
            JsonObject draft = draftFromYaml("/models/minimal-todo.yaml");
            JsonObject todo = entityNamed(draft, "todo");
            todo.addProperty("name", "item");
            todo.addProperty("plural", "items");
            fieldNamed(todo, "title").addProperty("name", "name");
            JsonObject mappings = new JsonObject();
            JsonObject entityMappings = new JsonObject();
            entityMappings.addProperty("item", "todo");
            JsonObject fieldMappings = new JsonObject();
            JsonObject itemFieldMappings = new JsonObject();
            itemFieldMappings.addProperty("name", "title");
            fieldMappings.add("item", itemFieldMappings);
            mappings.add("entityMappings", entityMappings);
            mappings.add("fieldMappings", fieldMappings);

            UiHttpResponse response =
                    controller.applySchemaUpgrade(
                            upgradeRequest(draft, workspace.snapshot().version(), mappings));

            Assertions.assertEquals(200, response.statusCode(), response.body());
            Assertions.assertTrue(
                    proxy.getJson("items/" + id).body().contains("\"name\":\"Task A\""));
            Assertions.assertEquals(404, proxy.getJson("todos").statusCode());
        }
    }

    @Test
    public void manualRelationshipMappingPreservesRenamedRelationshipEdges() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
            DynamicThingifierApiProxy proxy = new DynamicThingifierApiProxy(workspace);
            String projectId = createProject(proxy, "Project A");
            String todoId = createTodo(proxy, "Task A");
            Assertions.assertEquals(
                    201,
                    proxy.postJson(
                                    "projects/" + projectId + "/tasks",
                                    "{\"id\":\"" + todoId + "\"}")
                            .statusCode());
            CrudUiController controller = new CrudUiController(workspace);
            JsonObject draft = draftFromYaml("/models/project-tasks.yaml");
            JsonObject relationship =
                    draft.getAsJsonArray("relationships").get(0).getAsJsonObject();
            relationship.addProperty("name", "todos");
            relationship.getAsJsonObject("reverse").addProperty("name", "projectsof");
            JsonObject mappings = new JsonObject();
            JsonArray relationshipMappings = new JsonArray();
            JsonObject mapping = new JsonObject();
            mapping.addProperty("targetFromEntity", "project");
            mapping.addProperty("targetName", "todos");
            mapping.addProperty("sourceFromEntity", "project");
            mapping.addProperty("sourceName", "tasks");
            relationshipMappings.add(mapping);
            mappings.add("relationshipMappings", relationshipMappings);

            UiHttpResponse response =
                    controller.applySchemaUpgrade(
                            upgradeRequest(draft, workspace.snapshot().version(), mappings));

            Assertions.assertEquals(200, response.statusCode(), response.body());
            Assertions.assertTrue(
                    proxy.getJson("projects/" + projectId + "/todos").body().contains("Task A"));
            Assertions.assertEquals(
                    404, proxy.getJson("projects/" + projectId + "/tasks").statusCode());
        }
    }

    @Test
    public void typeCoercionsAreReported() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
            DynamicThingifierApiProxy proxy = new DynamicThingifierApiProxy(workspace);
            createTodo(proxy, "123");
            CrudUiController controller = new CrudUiController(workspace);
            JsonObject draft = draftFromYaml("/models/project-tasks.yaml");
            fieldNamed(entityNamed(draft, "todo"), "title").addProperty("type", "integer");

            UiHttpResponse response =
                    controller.applySchemaUpgrade(
                            upgradeRequest(draft, workspace.snapshot().version(), null));
            JsonObject body = json(response);

            Assertions.assertEquals(200, response.statusCode(), response.body());
            Assertions.assertTrue(body.get("coercions").toString().contains("string to integer"));
            Assertions.assertTrue(proxy.getJson("todos").body().contains("\"title\":123"));
        }
    }

    @Test
    public void newFieldFallbacksAreReportedAsAssignmentsNotCoercions() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
            DynamicThingifierApiProxy proxy = new DynamicThingifierApiProxy(workspace);
            createProject(proxy, "Smoke Project");
            CrudUiController controller = new CrudUiController(workspace);
            JsonObject draft = draftFromYaml("/models/project-tasks.yaml");
            entityNamed(draft, "project")
                    .getAsJsonArray("fields")
                    .add(field("risk", "integer", null));

            UiHttpResponse response =
                    controller.previewSchemaUpgrade(
                            upgradeRequest(draft, workspace.snapshot().version(), null));
            JsonObject body = json(response);

            Assertions.assertEquals(200, response.statusCode(), response.body());
            Assertions.assertTrue(
                    body.get("valueAssignments").toString().contains("Used target type fallback"));
            Assertions.assertTrue(body.get("valueAssignments").toString().contains("risk"));
            Assertions.assertEquals("[]", body.get("coercions").toString());
        }
    }

    @Test
    public void remainingValidationErrorsBlockApplyAndLeaveWorkspaceUnchanged() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
            DynamicThingifierApiProxy proxy = new DynamicThingifierApiProxy(workspace);
            createTodo(proxy, "Task A");
            CrudUiController controller = new CrudUiController(workspace);
            JsonObject draft = draftFromYaml("/models/project-tasks.yaml");
            JsonObject requiredCode = field("code", "string", null);
            requiredCode.addProperty("required", true);
            JsonArray validations = new JsonArray();
            JsonObject notEmpty = new JsonObject();
            notEmpty.addProperty("type", "notEmpty");
            validations.add(notEmpty);
            requiredCode.add("validations", validations);
            entityNamed(draft, "todo").getAsJsonArray("fields").add(requiredCode);
            long version = workspace.snapshot().version();

            UiHttpResponse response =
                    controller.applySchemaUpgrade(upgradeRequest(draft, version, null));

            Assertions.assertEquals(400, response.statusCode());
            Assertions.assertEquals(version, workspace.snapshot().version());
            Assertions.assertTrue(response.body().contains("Failed Validation"));
            Assertions.assertTrue(proxy.getJson("todos").body().contains("Task A"));
        }
    }

    private String createProject(final DynamicThingifierApiProxy proxy, final String title) {
        return fieldValue(proxy.postJson("projects", "{\"title\":\"" + title + "\"}").body(), "id");
    }

    private String createTodo(final DynamicThingifierApiProxy proxy, final String title) {
        return fieldValue(proxy.postJson("todos", "{\"title\":\"" + title + "\"}").body(), "id");
    }

    private String upgradeRequest(
            final JsonObject draft,
            final Long expectedWorkspaceVersion,
            final JsonObject mappings) {
        JsonObject request = new JsonObject();
        request.add("draft", draft);
        if (expectedWorkspaceVersion != null) {
            request.addProperty("expectedWorkspaceVersion", expectedWorkspaceVersion);
        }
        request.add("mappings", mappings == null ? new JsonObject() : mappings);
        return request.toString();
    }

    private JsonObject draftFromYaml(final String resourceName) {
        SchemaPreviewService previewService = new SchemaPreviewService();
        UiHttpResponse response = previewService.fromYaml(TestResources.text(resourceName));
        return json(response).getAsJsonObject("draft");
    }

    private JsonObject field(final String name, final String type, final String defaultValue) {
        JsonObject field = new JsonObject();
        field.addProperty("name", name);
        field.addProperty("type", type);
        if (defaultValue != null) {
            field.addProperty("defaultValue", defaultValue);
        }
        return field;
    }

    private JsonObject entityNamed(final JsonObject draft, final String name) {
        for (var element : draft.getAsJsonArray("entities")) {
            JsonObject entity = element.getAsJsonObject();
            if (name.equals(entity.get("name").getAsString())) {
                return entity;
            }
        }
        throw new AssertionError("No entity named " + name);
    }

    private JsonObject fieldNamed(final JsonObject entity, final String name) {
        for (var element : entity.getAsJsonArray("fields")) {
            JsonObject field = element.getAsJsonObject();
            if (name.equals(field.get("name").getAsString())) {
                return field;
            }
        }
        throw new AssertionError("No field named " + name);
    }

    private String fieldValue(final String json, final String fieldName) {
        return json(json).get(fieldName).getAsString();
    }

    private JsonObject json(final UiHttpResponse response) {
        return json(response.body());
    }

    private JsonObject json(final String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }
}
