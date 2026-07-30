package uk.co.compendiumdev.thingifier.crudui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class WorkspaceProjectServiceTest {

    @TempDir Path temp;

    @Test
    public void saveCreatesProjectManifestSchemaAndDataFiles() throws Exception {
        withDefaultWorkspace(
                (workspace, controller, proxy) -> {
                    workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
                    createRelatedProjectAndTodo(proxy);
                    Path projectFolder = temp.resolve("project-bundle");

                    UiHttpResponse response = controller.saveProject(request(projectFolder));

                    Assertions.assertEquals(200, response.statusCode());
                    Assertions.assertTrue(
                            Files.exists(projectFolder.resolve("projectfile.erproj")));
                    Assertions.assertTrue(Files.exists(projectFolder.resolve("schema.yaml")));
                    Assertions.assertTrue(Files.exists(projectFolder.resolve("data.json")));
                    Assertions.assertFalse(
                            Files.readString(projectFolder.resolve("data.json"))
                                    .contains("schemaYaml"));
                    Assertions.assertTrue(response.body().contains("\"projectStatus\": \"saved\""));
                    Assertions.assertTrue(response.body().contains("\"active\": true"));
                });
    }

    @Test
    public void savedProjectReloadsSchemaDataAndRelationshipEdges() throws Exception {
        Path projectFolder = temp.resolve("project-bundle");
        withDefaultWorkspace(
                (source, controller, proxy) -> {
                    source.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
                    createRelatedProjectAndTodo(proxy);
                    controller.saveProject(request(projectFolder));
                });

        withDefaultWorkspace(
                (target, controller, proxy) -> {
                    UiHttpResponse response = controller.loadProject(request(projectFolder));

                    Assertions.assertEquals(200, response.statusCode());
                    Assertions.assertTrue(
                            response.body().contains("\"projectStatus\": \"loaded\""));
                    Assertions.assertEquals(
                            "Project Tasks", target.snapshot().definition().title());
                    Assertions.assertTrue(proxy.getJson("projects").body().contains("Project A"));
                    Assertions.assertTrue(proxy.getJson("todos").body().contains("Task A"));
                    Assertions.assertEquals(
                            1,
                            root(proxy.getJson("projects/1/tasks").body())
                                    .getAsJsonArray("todos")
                                    .size());
                });
    }

    @Test
    public void sqliteBackedSaveCreatesManifestSchemaAndDatabaseFile() throws Exception {
        Path projectFolder = temp.resolve("sqlite-project-bundle");
        withDefaultWorkspace(
                (workspace, controller, proxy) -> {
                    workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
                    createRelatedProjectAndTodo(proxy);
                    workspace.switchStorage(
                            WorkspaceStorage.sqliteFile(temp.resolve("source.sqlite")));

                    UiHttpResponse response = controller.saveProject(request(projectFolder));

                    Assertions.assertEquals(200, response.statusCode());
                    Assertions.assertTrue(
                            Files.exists(projectFolder.resolve("projectfile.erproj")));
                    Assertions.assertTrue(Files.exists(projectFolder.resolve("schema.yaml")));
                    Assertions.assertTrue(Files.exists(projectFolder.resolve("data.sqlite")));
                    Assertions.assertFalse(Files.exists(projectFolder.resolve("data.json")));
                    Assertions.assertTrue(
                            Files.readString(projectFolder.resolve("projectfile.erproj"))
                                    .contains("dataFile: data.sqlite"));
                    Assertions.assertFalse(
                            Files.readString(projectFolder.resolve("projectfile.erproj"))
                                    .contains("storage:"));
                });
    }

    @Test
    public void saveAsSqliteFromMemorySwitchesWorkspaceToProjectDatabaseFile() throws Exception {
        Path projectFolder = temp.resolve("sqlite-save-as-project");
        withDefaultWorkspace(
                (workspace, controller, proxy) -> {
                    workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
                    createRelatedProjectAndTodo(proxy);

                    UiHttpResponse response =
                            controller.saveProject(request(projectFolder, "sqlite"));

                    Assertions.assertEquals(200, response.statusCode(), response.body());
                    Assertions.assertTrue(Files.exists(projectFolder.resolve("data.sqlite")));
                    Assertions.assertFalse(Files.exists(projectFolder.resolve("data.json")));
                    Assertions.assertEquals("sqlite-file", workspace.snapshot().storage().mode());
                    Assertions.assertEquals(
                            projectFolder
                                    .resolve("data.sqlite")
                                    .toAbsolutePath()
                                    .normalize()
                                    .toString(),
                            workspace.snapshot().storage().sqliteFilePath());
                    Assertions.assertTrue(proxy.getJson("projects").body().contains("Project A"));
                });
    }

    @Test
    public void saveAsJsonFromSqliteSwitchesWorkspaceToInMemoryStorage() throws Exception {
        Path projectFolder = temp.resolve("json-save-as-project");
        withDefaultWorkspace(
                (workspace, controller, proxy) -> {
                    workspace.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
                    createRelatedProjectAndTodo(proxy);
                    workspace.switchStorage(
                            WorkspaceStorage.sqliteFile(temp.resolve("source.sqlite")));

                    UiHttpResponse response =
                            controller.saveProject(request(projectFolder, "json"));

                    Assertions.assertEquals(200, response.statusCode(), response.body());
                    Assertions.assertTrue(Files.exists(projectFolder.resolve("data.json")));
                    Assertions.assertFalse(
                            Files.readString(projectFolder.resolve("projectfile.erproj"))
                                    .contains("sqlite-file"));
                    Assertions.assertEquals("memory", workspace.snapshot().storage().mode());
                    Assertions.assertTrue(proxy.getJson("projects").body().contains("Project A"));
                });
    }

    @Test
    public void sqliteBackedProjectReloadsDataAndSwitchesStorageMode() throws Exception {
        Path projectFolder = temp.resolve("sqlite-project-bundle");
        withDefaultWorkspace(
                (source, controller, proxy) -> {
                    source.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
                    createRelatedProjectAndTodo(proxy);
                    source.switchStorage(
                            WorkspaceStorage.sqliteFile(temp.resolve("source.sqlite")));
                    controller.saveProject(request(projectFolder));
                });

        withDefaultWorkspace(
                (target, controller, proxy) -> {
                    UiHttpResponse response = controller.loadProject(request(projectFolder));

                    Assertions.assertEquals(200, response.statusCode());
                    Assertions.assertEquals("sqlite-file", target.snapshot().storage().mode());
                    Assertions.assertTrue(
                            target.snapshot().storage().sqliteFilePath().endsWith("data.sqlite"));
                    Assertions.assertTrue(proxy.getJson("projects").body().contains("Project A"));
                    Assertions.assertEquals(
                            1,
                            root(proxy.getJson("projects/1/tasks").body())
                                    .getAsJsonArray("todos")
                                    .size());
                });
    }

    @Test
    public void loadProjectTreatsSqliteDataFileAsSqliteStorageWithoutStorageBlock()
            throws Exception {
        Path projectFolder = temp.resolve("edited-sqlite-project-bundle");
        withDefaultWorkspace(
                (source, controller, proxy) -> {
                    source.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
                    createRelatedProjectAndTodo(proxy);
                    source.switchStorage(
                            WorkspaceStorage.sqliteFile(temp.resolve("source.sqlite")));
                    controller.saveProject(request(projectFolder));
                });
        Files.move(
                projectFolder.resolve("data.sqlite"), projectFolder.resolve("todomanager.sqlite"));
        Files.writeString(
                projectFolder.resolve("projectfile.erproj"),
                "formatVersion: 1\n"
                        + "project:\n"
                        + "  title: Edited SQLite Project\n"
                        + "schemaFile: schema.yaml\n"
                        + "dataFile: todomanager.sqlite\n");

        withDefaultWorkspace(
                (target, controller, proxy) -> {
                    UiHttpResponse response = controller.loadProject(request(projectFolder));

                    Assertions.assertEquals(200, response.statusCode(), response.body());
                    Assertions.assertEquals("sqlite-file", target.snapshot().storage().mode());
                    Assertions.assertTrue(
                            target.snapshot()
                                    .storage()
                                    .sqliteFilePath()
                                    .endsWith("todomanager.sqlite"));
                    Assertions.assertTrue(proxy.getJson("projects").body().contains("Project A"));
                });
    }

    @Test
    public void loadProjectSniffsSqliteDataFileWhenFilenameHasNoSqliteExtension() throws Exception {
        Path projectFolder = temp.resolve("sniffed-sqlite-project-bundle");
        withDefaultWorkspace(
                (source, controller, proxy) -> {
                    source.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
                    createRelatedProjectAndTodo(proxy);
                    source.switchStorage(
                            WorkspaceStorage.sqliteFile(temp.resolve("source.sqlite")));
                    controller.saveProject(request(projectFolder));
                });
        Files.move(projectFolder.resolve("data.sqlite"), projectFolder.resolve("data.bin"));
        Files.writeString(
                projectFolder.resolve("projectfile.erproj"),
                "formatVersion: 1\n" + "schemaFile: schema.yaml\n" + "dataFile: data.bin\n");

        withDefaultWorkspace(
                (target, controller, proxy) -> {
                    UiHttpResponse response = controller.loadProject(request(projectFolder));

                    Assertions.assertEquals(200, response.statusCode(), response.body());
                    Assertions.assertEquals("sqlite-file", target.snapshot().storage().mode());
                    Assertions.assertTrue(
                            target.snapshot().storage().sqliteFilePath().endsWith("data.bin"));
                });
    }

    @Test
    public void loadAcceptsDirectProjectFilePath() throws Exception {
        Path projectFolder = temp.resolve("project-bundle");
        withDefaultWorkspace(
                (source, controller, proxy) -> {
                    source.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
                    controller.saveProject(request(projectFolder));
                });

        withDefaultWorkspace(
                (target, controller, proxy) -> {
                    UiHttpResponse response =
                            controller.loadProject(
                                    request(projectFolder.resolve("projectfile.erproj")));

                    Assertions.assertEquals(200, response.statusCode());
                    Assertions.assertEquals(
                            "Project Tasks", target.snapshot().definition().title());
                });
    }

    @Test
    public void checkSavePathReportsCreatableFolderAndManagedFiles() throws Exception {
        Path creatableFolder = temp.resolve("new-project");
        withDefaultWorkspace(
                (workspace, controller, proxy) -> {
                    JsonObject creatable =
                            root(
                                    controller
                                            .checkProject(actionRequest("save", creatableFolder))
                                            .body());

                    Assertions.assertTrue(creatable.get("canProceed").getAsBoolean());
                    Assertions.assertEquals(
                            "creatable-folder", creatable.get("kind").getAsString());

                    Path existingFolder = temp.resolve("existing-project");
                    Files.createDirectories(existingFolder);
                    Files.writeString(existingFolder.resolve("projectfile.erproj"), "managed");
                    Files.writeString(existingFolder.resolve("schema.yaml"), "managed");

                    JsonObject existing =
                            root(
                                    controller
                                            .checkProject(actionRequest("save", existingFolder))
                                            .body());

                    Assertions.assertTrue(existing.get("canProceed").getAsBoolean());
                    Assertions.assertTrue(
                            existing.get("warning").getAsString().contains("projectfile.erproj"));
                    Assertions.assertTrue(
                            existing.get("warning").getAsString().contains("schema.yaml"));
                });
    }

    @Test
    public void checkLoadPathAcceptsFolderAndDirectProjectFile() throws Exception {
        Path projectFolder = temp.resolve("project-bundle");
        withDefaultWorkspace(
                (source, controller, proxy) -> controller.saveProject(request(projectFolder)));

        withDefaultWorkspace(
                (workspace, controller, proxy) -> {
                    JsonObject folderCheck =
                            root(
                                    controller
                                            .checkProject(actionRequest("load", projectFolder))
                                            .body());
                    JsonObject fileCheck =
                            root(
                                    controller
                                            .checkProject(
                                                    actionRequest(
                                                            "load",
                                                            projectFolder.resolve(
                                                                    "projectfile.erproj")))
                                            .body());

                    Assertions.assertTrue(folderCheck.get("canProceed").getAsBoolean());
                    Assertions.assertEquals("folder", folderCheck.get("kind").getAsString());
                    Assertions.assertTrue(fileCheck.get("canProceed").getAsBoolean());
                    Assertions.assertEquals("project-file", fileCheck.get("kind").getAsString());
                });
    }

    @Test
    public void browserExportFilesEmitsJsonBackedProjectFiles() throws Exception {
        withDefaultWorkspace(
                (workspace, controller, proxy) -> {
                    UiHttpResponse response = controller.exportProjectFiles();
                    JsonObject body = root(response.body());

                    Assertions.assertEquals(200, response.statusCode());
                    Assertions.assertTrue(
                            response.body().contains("\"projectStatus\": \"exported\""));
                    Assertions.assertTrue(response.body().contains("\"projectfile.erproj\""));
                    Assertions.assertTrue(response.body().contains("\"schema.yaml\""));
                    Assertions.assertTrue(response.body().contains("\"data.json\""));
                    Assertions.assertFalse(response.body().contains("\"data.sqlite\""));
                    Assertions.assertEquals("memory", body.get("storageMode").getAsString());
                    Assertions.assertEquals("json", body.get("projectStorageMode").getAsString());
                });
    }

    @Test
    public void browserExportFilesEmitsSqliteBackedProjectFiles() throws Exception {
        withDefaultWorkspace(
                (workspace, controller, proxy) -> {
                    workspace.switchStorage(
                            WorkspaceStorage.sqliteFile(temp.resolve("source.sqlite")));

                    UiHttpResponse response = controller.exportProjectFiles();
                    JsonObject body = root(response.body());

                    Assertions.assertEquals(200, response.statusCode());
                    Assertions.assertTrue(response.body().contains("\"projectfile.erproj\""));
                    Assertions.assertTrue(response.body().contains("\"schema.yaml\""));
                    Assertions.assertTrue(response.body().contains("\"data.sqlite\""));
                    Assertions.assertFalse(response.body().contains("\"data.json\""));
                    Assertions.assertEquals("sqlite-file", body.get("storageMode").getAsString());
                    Assertions.assertEquals("sqlite", body.get("projectStorageMode").getAsString());
                });
    }

    @Test
    public void browserExportFilesCanExportSqliteProjectFromMemoryWorkspace() throws Exception {
        withDefaultWorkspace(
                (workspace, controller, proxy) -> {
                    UiHttpResponse response =
                            controller.exportProjectFiles(
                                    JsonSupport.toJson(Map.of("projectStorageMode", "sqlite")));
                    JsonObject body = root(response.body());

                    Assertions.assertEquals(200, response.statusCode());
                    Assertions.assertTrue(response.body().contains("\"data.sqlite\""));
                    Assertions.assertFalse(response.body().contains("\"data.json\""));
                    Assertions.assertEquals("sqlite-file", body.get("storageMode").getAsString());
                    Assertions.assertEquals("sqlite", body.get("projectStorageMode").getAsString());
                    Assertions.assertEquals("memory", workspace.snapshot().storage().mode());
                });
    }

    @Test
    public void browserLoadFilesLoadsJsonBackedProjectWithoutServerPath() throws Exception {
        Path projectFolder = temp.resolve("browser-json-project");
        withDefaultWorkspace(
                (source, controller, proxy) -> {
                    source.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
                    createRelatedProjectAndTodo(proxy);
                    controller.saveProject(request(projectFolder));
                });

        withDefaultWorkspace(
                (target, controller, proxy) -> {
                    UiHttpResponse response =
                            controller.loadProjectFiles(
                                    browserFilePayload(projectFolder, "browser-json"));

                    Assertions.assertEquals(200, response.statusCode(), response.body());
                    Assertions.assertEquals(
                            "Project Tasks", target.snapshot().definition().title());
                    Assertions.assertEquals(
                            "Browser folder: browser-json", target.snapshot().projectPath());
                    Assertions.assertTrue(proxy.getJson("projects").body().contains("Project A"));
                    Assertions.assertEquals(
                            1,
                            root(proxy.getJson("projects/1/tasks").body())
                                    .getAsJsonArray("todos")
                                    .size());
                });
    }

    @Test
    public void browserLoadFilesLoadsSqliteBackedProject() throws Exception {
        Path projectFolder = temp.resolve("browser-sqlite-project");
        withDefaultWorkspace(
                (source, controller, proxy) -> {
                    source.replaceWithYaml(TestResources.text("/models/project-tasks.yaml"));
                    createRelatedProjectAndTodo(proxy);
                    source.switchStorage(
                            WorkspaceStorage.sqliteFile(temp.resolve("source.sqlite")));
                    controller.saveProject(request(projectFolder));
                });

        withDefaultWorkspace(
                (target, controller, proxy) -> {
                    UiHttpResponse response =
                            controller.loadProjectFiles(
                                    browserFilePayload(projectFolder, "browser-sqlite"));

                    Assertions.assertEquals(200, response.statusCode(), response.body());
                    Assertions.assertEquals("sqlite-file", target.snapshot().storage().mode());
                    Assertions.assertEquals(
                            "Browser folder: browser-sqlite", target.snapshot().projectPath());
                    Assertions.assertTrue(proxy.getJson("projects").body().contains("Project A"));
                    Assertions.assertEquals(
                            1,
                            root(proxy.getJson("projects/1/tasks").body())
                                    .getAsJsonArray("todos")
                                    .size());
                });
    }

    @Test
    public void invalidBrowserLoadFilesDoesNotMutateWorkspace() throws Exception {
        withDefaultWorkspace(
                (workspace, controller, proxy) -> {
                    long version = workspace.snapshot().version();
                    List<Map<String, Object>> files = new ArrayList<>();
                    files.add(textPayload("projectfile.erproj", "not: valid: yaml"));
                    UiHttpResponse response =
                            controller.loadProjectFiles(
                                    JsonSupport.toJson(
                                            Map.of("folderName", "bad-project", "files", files)));

                    Assertions.assertEquals(400, response.statusCode());
                    Assertions.assertEquals(version, workspace.snapshot().version());
                    Assertions.assertEquals(
                            "Todo Manager", workspace.snapshot().definition().title());
                });
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

        withDefaultWorkspace(
                (workspace, controller, proxy) -> {
                    long version = workspace.snapshot().version();

                    UiHttpResponse response = controller.loadProject(request(projectFolder));

                    Assertions.assertEquals(400, response.statusCode());
                    Assertions.assertEquals(version, workspace.snapshot().version());
                    Assertions.assertEquals(
                            "Todo Manager", workspace.snapshot().definition().title());
                });
    }

    @Test
    public void saveLeavesUnrelatedFilesUntouched() throws Exception {
        Path projectFolder = temp.resolve("project-bundle");
        Files.createDirectories(projectFolder);
        Path extraFile = projectFolder.resolve("validators.jar");
        Files.writeString(extraFile, "not really a jar");

        withDefaultWorkspace(
                (workspace, controller, proxy) -> controller.saveProject(request(projectFolder)));

        Assertions.assertEquals("not really a jar", Files.readString(extraFile));
    }

    private void withDefaultWorkspace(final WorkspaceScenario scenario) throws Exception {
        try (ActiveThingifierWorkspace workspace =
                ActiveThingifierWorkspace.defaultTodoManagerWorkspace()) {
            scenario.run(
                    workspace,
                    new CrudUiController(workspace),
                    new DynamicThingifierApiProxy(workspace));
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

    private String request(final Path path) {
        return JsonSupport.toJson(Map.of("path", path.toString()));
    }

    private String request(final Path path, final String projectStorageMode) {
        return JsonSupport.toJson(
                Map.of("path", path.toString(), "projectStorageMode", projectStorageMode));
    }

    private String actionRequest(final String action, final Path path) {
        return JsonSupport.toJson(Map.of("action", action, "path", path.toString()));
    }

    private String browserFilePayload(final Path folder, final String folderName) throws Exception {
        List<Map<String, Object>> files = new ArrayList<>();
        files.add(
                textPayload(
                        "projectfile.erproj",
                        Files.readString(folder.resolve("projectfile.erproj"))));
        files.add(textPayload("schema.yaml", Files.readString(folder.resolve("schema.yaml"))));
        Path dataJson = folder.resolve("data.json");
        if (Files.exists(dataJson)) {
            files.add(textPayload("data.json", Files.readString(dataJson)));
        }
        Path sqlite = folder.resolve("data.sqlite");
        if (Files.exists(sqlite)) {
            files.add(binaryPayload("data.sqlite", Files.readAllBytes(sqlite)));
        }
        return JsonSupport.toJson(Map.of("folderName", folderName, "files", files));
    }

    private Map<String, Object> textPayload(final String name, final String content) {
        Map<String, Object> file = new LinkedHashMap<>();
        file.put("name", name);
        file.put("type", "text");
        file.put("content", content);
        return file;
    }

    private Map<String, Object> binaryPayload(final String name, final byte[] content) {
        Map<String, Object> file = new LinkedHashMap<>();
        file.put("name", name);
        file.put("type", "base64");
        file.put("content", Base64.getEncoder().encodeToString(content));
        return file;
    }

    private String field(final String json, final String fieldName) {
        JsonObject object = root(json);
        return object.get(fieldName).getAsString();
    }

    private JsonObject root(final String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    @FunctionalInterface
    private interface WorkspaceScenario {
        void run(
                ActiveThingifierWorkspace workspace,
                CrudUiController controller,
                DynamicThingifierApiProxy proxy)
                throws Exception;
    }
}
