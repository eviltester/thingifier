package uk.co.compendiumdev.thingifier.crudui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class WorkspaceProjectServiceTest {

    @TempDir Path temp;

    @Test
    public void saveCreatesProjectManifestSchemaAndDataFiles() throws Exception {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
            createRelatedProjectAndTodo(new DynamicThingifierApiProxy(workspace));
            CrudUiController controller = new CrudUiController(workspace);
            Path projectFolder = temp.resolve("project-bundle");

            UiHttpResponse response = controller.saveProject(request(projectFolder));

            Assertions.assertEquals(200, response.statusCode());
            Assertions.assertTrue(Files.exists(projectFolder.resolve("projectfile.erproj")));
            Assertions.assertTrue(Files.exists(projectFolder.resolve("schema.yaml")));
            Assertions.assertTrue(Files.exists(projectFolder.resolve("data.json")));
            Assertions.assertFalse(
                    Files.readString(projectFolder.resolve("data.json")).contains("schemaYaml"));
            Assertions.assertTrue(response.body().contains("\"projectStatus\": \"saved\""));
            Assertions.assertTrue(response.body().contains("\"active\": true"));
        }
    }

    @Test
    public void savedProjectReloadsSchemaDataAndRelationshipEdges() {
        Path projectFolder = temp.resolve("project-bundle");
        try (ActiveThingifierWorkspace source =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            source.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
            createRelatedProjectAndTodo(new DynamicThingifierApiProxy(source));
            new CrudUiController(source).saveProject(request(projectFolder));
        }

        try (ActiveThingifierWorkspace target =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            CrudUiController controller = new CrudUiController(target);

            UiHttpResponse response = controller.loadProject(request(projectFolder));
            DynamicThingifierApiProxy targetProxy = new DynamicThingifierApiProxy(target);

            Assertions.assertEquals(200, response.statusCode());
            Assertions.assertTrue(response.body().contains("\"projectStatus\": \"loaded\""));
            Assertions.assertEquals("Project Tasks", target.snapshot().definition().title());
            Assertions.assertTrue(targetProxy.getJson("projects").body().contains("Project A"));
            Assertions.assertTrue(targetProxy.getJson("todos").body().contains("Task A"));
            Assertions.assertEquals(
                    1,
                    root(targetProxy.getJson("projects/1/tasks").body())
                            .getAsJsonArray("todos")
                            .size());
        }
    }

    @Test
    public void loadAcceptsDirectProjectFilePath() {
        Path projectFolder = temp.resolve("project-bundle");
        try (ActiveThingifierWorkspace source =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            source.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
            new CrudUiController(source).saveProject(request(projectFolder));
        }

        try (ActiveThingifierWorkspace target =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            UiHttpResponse response =
                    new CrudUiController(target)
                            .loadProject(request(projectFolder.resolve("projectfile.erproj")));

            Assertions.assertEquals(200, response.statusCode());
            Assertions.assertEquals("Project Tasks", target.snapshot().definition().title());
        }
    }

    @Test
    public void invalidProjectLoadDoesNotMutateActiveWorkspace() throws Exception {
        Path projectFolder = temp.resolve("invalid-project");
        Files.createDirectories(projectFolder);
        Files.writeString(
                projectFolder.resolve("projectfile.erproj"),
                "formatVersion: 1\nschemaFile: schema.yaml\ndataFile: data.json\n");
        Files.writeString(
                projectFolder.resolve("schema.yaml"),
                TestResources.text("/models/project-tasks.yaml"));
        Files.writeString(projectFolder.resolve("data.json"), "{");

        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            long version = workspace.snapshot().version();

            UiHttpResponse response =
                    new CrudUiController(workspace).loadProject(request(projectFolder));

            Assertions.assertEquals(400, response.statusCode());
            Assertions.assertEquals(version, workspace.snapshot().version());
            Assertions.assertEquals("Todo Manager", workspace.snapshot().definition().title());
        }
    }

    @Test
    public void saveLeavesUnrelatedFilesUntouched() throws Exception {
        Path projectFolder = temp.resolve("project-bundle");
        Files.createDirectories(projectFolder);
        Path extraFile = projectFolder.resolve("validators.jar");
        Files.writeString(extraFile, "not really a jar");

        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            new CrudUiController(workspace).saveProject(request(projectFolder));
        }

        Assertions.assertEquals("not really a jar", Files.readString(extraFile));
    }

    private void createRelatedProjectAndTodo(final DynamicThingifierApiProxy proxy) {
        String projectId =
                field(proxy.postJson("projects", "{\"title\":\"Project A\"}").body(), "id");
        String todoId = field(proxy.postJson("todos", "{\"title\":\"Task A\"}").body(), "id");
        UiHttpResponse relationship =
                proxy.postJson("projects/" + projectId + "/tasks", "{\"id\":\"" + todoId + "\"}");
        Assertions.assertEquals(201, relationship.statusCode());
    }

    private String request(final Path path) {
        return JsonSupport.toJson(Map.of("path", path.toString()));
    }

    private String field(final String json, final String fieldName) {
        JsonObject object = root(json);
        return object.get(fieldName).getAsString();
    }

    private JsonObject root(final String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }
}
