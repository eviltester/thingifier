package uk.co.compendiumdev.thingifier.crudui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ActiveThingifierWorkspaceTest {

    @TempDir Path temp;

    @Test
    public void defaultWorkspaceStartsWithTodoManager() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            WorkspaceSnapshot snapshot = workspace.snapshot();

            Assertions.assertEquals(1L, snapshot.version());
            Assertions.assertEquals("Todo Manager", snapshot.definition().title());
            Assertions.assertNotNull(snapshot.definition().entityNamed("todo"));
            Assertions.assertTrue(snapshot.schemaYaml().contains("title: Todo Manager"));
        }
    }

    @Test
    public void yamlLoadReplacesSchemaAndIncrementsVersion() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/minimal-todo.yaml"));

            WorkspaceSnapshot snapshot = workspace.snapshot();
            Assertions.assertEquals(2L, snapshot.version());
            Assertions.assertEquals("Minimal Todo", snapshot.definition().title());
            Assertions.assertNotNull(snapshot.definition().entityNamed("todo"));
            Assertions.assertNull(snapshot.definition().entityNamed("project"));
        }
    }

    @Test
    public void switchToSqliteMemoryMigratesDataAndRelationships() {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
            createRelatedProjectAndTodo(new DynamicThingifierApiProxy(workspace));

            workspace.switchStorage(WorkspaceStorage.sqliteMemory());

            DynamicThingifierApiProxy proxy = new DynamicThingifierApiProxy(workspace);
            Assertions.assertEquals("sqlite-memory", workspace.snapshot().storage().mode());
            Assertions.assertTrue(proxy.getJson("projects").body().contains("Project A"));
            Assertions.assertTrue(proxy.getJson("todos").body().contains("Task A"));
            Assertions.assertEquals(
                    1,
                    root(proxy.getJson("projects/1/tasks").body()).getAsJsonArray("todos").size());
        }
    }

    @Test
    public void switchToSqliteFileCreatesFileAndMigratesData() {
        Path databaseFile = temp.resolve("workspace.sqlite");
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
            createRelatedProjectAndTodo(new DynamicThingifierApiProxy(workspace));

            workspace.switchStorage(WorkspaceStorage.sqliteFile(databaseFile));

            DynamicThingifierApiProxy proxy = new DynamicThingifierApiProxy(workspace);
            Assertions.assertTrue(Files.exists(databaseFile));
            Assertions.assertEquals("sqlite-file", workspace.snapshot().storage().mode());
            Assertions.assertTrue(proxy.getJson("projects").body().contains("Project A"));
            Assertions.assertEquals(
                    1,
                    root(proxy.getJson("projects/1/tasks").body()).getAsJsonArray("todos").size());
        }
    }

    @Test
    public void switchFromSqliteFileBackToMemoryMigratesData() {
        Path databaseFile = temp.resolve("workspace.sqlite");
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
            createRelatedProjectAndTodo(new DynamicThingifierApiProxy(workspace));
            workspace.switchStorage(WorkspaceStorage.sqliteFile(databaseFile));

            workspace.switchStorage(WorkspaceStorage.memory());

            DynamicThingifierApiProxy proxy = new DynamicThingifierApiProxy(workspace);
            Assertions.assertEquals("memory", workspace.snapshot().storage().mode());
            Assertions.assertTrue(proxy.getJson("projects").body().contains("Project A"));
            Assertions.assertEquals(
                    1,
                    root(proxy.getJson("projects/1/tasks").body()).getAsJsonArray("todos").size());
        }
    }

    private void createRelatedProjectAndTodo(final DynamicThingifierApiProxy proxy) {
        String projectId =
                field(proxy.postJson("projects", "{\"title\":\"Project A\"}").body(), "id");
        String todoId = field(proxy.postJson("todos", "{\"title\":\"Task A\"}").body(), "id");
        UiHttpResponse relationship =
                proxy.postJson("projects/" + projectId + "/tasks", "{\"id\":\"" + todoId + "\"}");
        Assertions.assertEquals(201, relationship.statusCode());
    }

    private String field(final String json, final String fieldName) {
        return root(json).get(fieldName).getAsString();
    }

    private JsonObject root(final String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }
}
