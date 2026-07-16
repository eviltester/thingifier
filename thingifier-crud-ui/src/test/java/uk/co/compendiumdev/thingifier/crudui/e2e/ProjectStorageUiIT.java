package uk.co.compendiumdev.thingifier.crudui.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.awt.GraphicsEnvironment;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.co.compendiumdev.thingifier.crudui.e2e.pages.WorkspacePage;

public class ProjectStorageUiIT extends BrowserTestBase {

    @TempDir Path temp;

    private WorkspacePage workspace;

    @BeforeEach
    void resetWorkspace() {
        resetToProjectTasks();
    }

    @Test
    public void typedProjectSaveLoadAndRecentPathWorkFromWorkspace() {
        workspace = new WorkspacePage(page, server().baseUrl()).open();
        api().postJson("/api/todos", "{\"title\":\"Saved Task\"}");
        Path projectFolder = temp.resolve("typed-project");

        workspace.projectDialog().openSave();
        workspace.projectDialog().fillPath(projectFolder.toString());
        workspace.projectDialog().validatePath();
        assertThat(workspace.projectDialog().status())
                .containsText("Project folder can be created");
        workspace.projectDialog().confirm();
        assertThat(workspace.messages().root()).containsText("Project saved.");
        Assertions.assertTrue(Files.exists(projectFolder.resolve("projectfile.erproj")));
        Assertions.assertTrue(Files.exists(projectFolder.resolve("schema.yaml")));
        Assertions.assertTrue(Files.exists(projectFolder.resolve("data.json")));

        api().postText("/ui/model/yaml", E2eResource.text("/models/minimal-todo.yaml"));
        workspace = new WorkspacePage(page, server().baseUrl()).open();
        workspace.projectDialog().openLoad();
        workspace.projectDialog().fillPath(projectFolder.toString());
        workspace.projectDialog().confirm();
        assertThat(workspace.messages().root()).containsText("Project loaded.");
        Assertions.assertTrue(api().get("/api/todos").body().contains("Saved Task"));
        Assertions.assertTrue(
                String.valueOf(
                                page.evaluate(
                                        "() => localStorage.getItem('thingifier-crud-ui.projectPaths')"))
                        .contains("typed-project"));
    }

    @Test
    public void saveProjectStorageChoiceSwitchesActiveWorkspaceStorage() {
        workspace = new WorkspacePage(page, server().baseUrl()).open();
        api().postJson("/api/todos", "{\"title\":\"Saved Storage Task\"}");
        Path sqliteProject = temp.resolve("save-as-sqlite-project");
        Path jsonProject = temp.resolve("save-as-json-project");

        workspace.projectDialog().openSave();
        assertThat(workspace.projectDialog().saveStorageOptions()).isVisible();
        workspace.projectDialog().chooseSqliteProject();
        workspace.projectDialog().fillPath(sqliteProject.toString());
        workspace.projectDialog().confirm();

        assertThat(workspace.messages().root()).containsText("Project saved.");
        assertThat(workspace.topBar().description()).containsText("SQLite File");
        Assertions.assertTrue(Files.exists(sqliteProject.resolve("data.sqlite")));
        Assertions.assertFalse(Files.exists(sqliteProject.resolve("data.json")));
        Assertions.assertTrue(api().get("/api/todos").body().contains("Saved Storage Task"));

        workspace.projectDialog().openSave();
        workspace.projectDialog().chooseJsonProject();
        workspace.projectDialog().fillPath(jsonProject.toString());
        workspace.projectDialog().confirm();

        assertThat(workspace.messages().root()).containsText("Project saved.");
        assertThat(workspace.topBar().description()).containsText("In Memory");
        Assertions.assertTrue(Files.exists(jsonProject.resolve("data.json")));
        Assertions.assertFalse(Files.exists(jsonProject.resolve("data.sqlite")));
        Assertions.assertTrue(api().get("/api/todos").body().contains("Saved Storage Task"));
    }

    @Test
    public void browserFolderProjectSaveAndLoadUseInjectedFolderPicker() {
        BrowserFolderPickerStub savePicker = new BrowserFolderPickerStub(page);
        savePicker.install("browser-project", new LinkedHashMap<>());
        workspace = new WorkspacePage(page, server().baseUrl()).open();
        api().postJson("/api/todos", "{\"title\":\"Browser Saved Task\"}");

        workspace.projectDialog().openSave();
        workspace.projectDialog().browserSave();
        assertThat(workspace.projectDialog().status()).containsText("Browser project saved");
        Assertions.assertEquals("browser-project", workspace.projectDialog().pathValue());
        Map<String, String> savedFiles = savePicker.files();
        Assertions.assertTrue(savedFiles.containsKey("projectfile.erproj"));
        Assertions.assertTrue(savedFiles.containsKey("schema.yaml"));
        Assertions.assertTrue(savedFiles.containsKey("data.json"));

        page.close();
        page = context.newPage();
        BrowserFolderPickerStub loadPicker = new BrowserFolderPickerStub(page);
        loadPicker.install("browser-project", savedFiles);
        api().postText("/ui/model/yaml", E2eResource.text("/models/minimal-todo.yaml"));
        workspace = new WorkspacePage(page, server().baseUrl()).open();
        workspace.projectDialog().openLoad();
        workspace.projectDialog().browserLoad();
        assertThat(workspace.messages().root()).containsText("Browser project loaded");
        Assertions.assertTrue(api().get("/api/todos").body().contains("Browser Saved Task"));
    }

    @Test
    public void storageSelectorSwitchesModesAndPreservesData() {
        workspace = new WorkspacePage(page, server().baseUrl()).open();
        api().postJson("/api/todos", "{\"title\":\"Stored Task\"}");

        workspace.storage().switchTo("sqlite-memory");
        assertThat(workspace.messages().root()).containsText("Storage switched.");
        Assertions.assertTrue(api().get("/api/todos").body().contains("Stored Task"));

        Path database = temp.resolve("workspace.sqlite");
        workspace.storage().switchToFile(database.toString());
        assertThat(workspace.messages().root()).containsText("Storage switched.");
        assertThat(workspace.topBar().description()).containsText("SQLite File");
        Assertions.assertTrue(api().get("/api/todos").body().contains("Stored Task"));
        workspace.storage().switchTo("memory");
        assertThat(workspace.messages().root()).containsText("Storage switched.");
    }

    @Test
    public void nativeBrowseReportsHeadlessFallbackWhenRunningHeadless() {
        Assumptions.assumeTrue(GraphicsEnvironment.isHeadless());
        workspace = new WorkspacePage(page, server().baseUrl()).open();

        workspace.projectDialog().openSave();
        workspace.projectDialog().browse();
        assertThat(workspace.projectDialog().status()).containsText("Native project browsing");
    }

    @Test
    public void jsonImportExportRoundTripWorksThroughBrowserControls() {
        workspace = new WorkspacePage(page, server().baseUrl()).open();
        api().postJson("/api/todos", "{\"title\":\"Downloaded Task\"}");
        Path downloadPath = temp.resolve("thingifier-workspace.json");

        com.microsoft.playwright.Download download =
                page.waitForDownload(() -> page.locator("[data-testid='export-button']").click());
        download.saveAs(downloadPath);

        api().postText("/ui/model/yaml", E2eResource.text("/models/minimal-todo.yaml"));
        page.locator("[data-testid='import-file']").setInputFiles(downloadPath);
        assertThat(workspace.messages().root()).containsText("Workspace imported.");
        Assertions.assertTrue(api().get("/api/todos").body().contains("Downloaded Task"));
    }
}
