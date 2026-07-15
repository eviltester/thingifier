package uk.co.compendiumdev.thingifier.crudui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DynamicThingifierApiProxyTest {

    @Test
    public void forwardsCrudRequestsToActiveThingifier() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/minimal-todo.yaml"));
            DynamicThingifierApiProxy proxy = new DynamicThingifierApiProxy(workspace);

            UiHttpResponse create = proxy.postJson("todos", "{\"title\":\"write tests\"}");
            Assertions.assertEquals(201, create.statusCode());
            String id = field(create.body(), "id");

            UiHttpResponse read = proxy.getJson("todos");
            Assertions.assertEquals(200, read.statusCode());
            Assertions.assertTrue(read.body().contains("write tests"));

            UiHttpResponse update =
                    proxy.forward(
                            new uk.co.compendiumdev.thingifier.adapter.internalhttp
                                            .InternalHttpRequest("/api/todos/" + id)
                                    .setVerb(
                                            uk.co.compendiumdev.thingifier.adapter.internalhttp
                                                    .InternalHttpMethod.PUT)
                                    .setBody("{\"title\":\"updated\"}")
                                    .addHeader("Accept", "application/json")
                                    .addHeader("Content-Type", "application/json"));
            Assertions.assertEquals(200, update.statusCode());
            Assertions.assertTrue(update.body().contains("updated"));

            UiHttpResponse delete =
                    proxy.forward(
                            new uk.co.compendiumdev.thingifier.adapter.internalhttp
                                            .InternalHttpRequest("/api/todos/" + id)
                                    .setVerb(
                                            uk.co.compendiumdev.thingifier.adapter.internalhttp
                                                    .InternalHttpMethod.DELETE)
                                    .addHeader("Accept", "application/json"));
            Assertions.assertEquals(200, delete.statusCode());
        }
    }

    @Test
    public void activeWorkspaceCanChangeWithoutRegeneratingRoutes() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            DynamicThingifierApiProxy proxy = new DynamicThingifierApiProxy(workspace);
            Assertions.assertEquals(200, proxy.getJson("projects").statusCode());

            workspace.replaceWithYaml(TestResources.text("/models/minimal-todo.yaml"));

            Assertions.assertEquals(404, proxy.getJson("projects").statusCode());
            Assertions.assertEquals(200, proxy.getJson("todos").statusCode());
        }
    }

    @Test
    public void forwardsRelationshipConnectCreateAndDisconnectRequests() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
            DynamicThingifierApiProxy proxy = new DynamicThingifierApiProxy(workspace);
            String projectId =
                    field(proxy.postJson("projects", "{\"title\":\"Project A\"}").body(), "id");
            String existingTodoId =
                    field(proxy.postJson("todos", "{\"title\":\"Existing Task\"}").body(), "id");

            UiHttpResponse connectExisting =
                    proxy.postJson(
                            "projects/" + projectId + "/tasks",
                            "{\"id\":\"" + existingTodoId + "\"}");
            Assertions.assertEquals(201, connectExisting.statusCode());
            Assertions.assertTrue(
                    proxy.getJson("projects/" + projectId + "/tasks")
                            .body()
                            .contains("Existing Task"));

            UiHttpResponse createAndConnect =
                    proxy.postJson("projects/" + projectId + "/tasks", "{\"title\":\"New Task\"}");
            Assertions.assertEquals(201, createAndConnect.statusCode());
            Assertions.assertTrue(
                    proxy.getJson("projects/" + projectId + "/tasks").body().contains("New Task"));

            UiHttpResponse disconnect =
                    proxy.deleteJson("projects/" + projectId + "/tasks/" + existingTodoId);
            Assertions.assertEquals(200, disconnect.statusCode());
            Assertions.assertFalse(
                    proxy.getJson("projects/" + projectId + "/tasks")
                            .body()
                            .contains("Existing Task"));
            Assertions.assertTrue(proxy.getJson("todos").body().contains("Existing Task"));
        }
    }

    private String field(final String json, final String fieldName) {
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        return object.get(fieldName).getAsString();
    }
}
