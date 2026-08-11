package uk.co.compendiumdev.thingifier.crudui.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.crudui.e2e.pages.WorkspacePage;

public class WorkspaceUiIT extends BrowserTestBase {

    private WorkspacePage workspace;

    @BeforeEach
    void openWorkspace() {
        resetToProjectTasks();
        workspace = new WorkspacePage(page, server().baseUrl()).open();
    }

    @Test
    public void workspaceLoadsDefaultMetadataAndRelationshipOutlineCanExpand() {
        assertThat(workspace.topBar().title()).containsText("Project Tasks");
        assertThat(workspace.outline().entity("project")).isVisible();
        assertThat(workspace.outline().entity("todo")).isVisible();

        api().postJson("/api/projects", "{\"title\":\"Smoke Project\"}");
        api().postJson("/api/todos", "{\"title\":\"Do this\"}");
        api().postJson("/api/todos", "{\"title\":\"Do that\"}");
        api().postJson("/api/projects/1/tasks", "{\"id\":\"1\"}");
        api().postJson("/api/projects/1/tasks", "{\"id\":\"2\"}");
        workspace.grid().refresh();

        workspace.outline().expandEntity("project");
        assertThat(workspace.outline().instance("project", "1")).containsText("Smoke Project");
        workspace.outline().expandInstance("project", "1");
        assertThat(workspace.outline().relationship("project", "1", "tasks")).isVisible();
        workspace.outline().expandRelationship("project", "1", "tasks");
        assertThat(workspace.outline().relatedInstance("todo", "1")).containsText("Do this");
        assertThat(workspace.outline().relatedInstance("todo", "2")).containsText("Do that");
    }

    @Test
    public void entityCrudSearchRefreshAndDocumentationLinksWorkFromWorkspace() {
        workspace.outline().selectEntity("todo");
        assertThat(workspace.grid().title()).containsText("todos");
        workspace.grid().clickNew();
        assertThat(workspace.editor().field("id")).isDisabled();
        assertThat(workspace.editor().field("id")).hasValue("<auto-assigned>");
        workspace.editor().fillField("title", "Do this");
        workspace.editor().save();
        assertThat(workspace.messages().root()).containsText("todo saved.");
        assertThat(workspace.grid().row("todo", "1")).containsText("Do this");

        workspace.grid().row("todo", "1").click();
        workspace.editor().fillField("title", "Do that");
        workspace.editor().save();
        assertThat(workspace.grid().row("todo", "1")).containsText("Do that");
        workspace.grid().globalSearch("Do that");
        assertThat(workspace.grid().row("todo", "1")).isVisible();
        Assertions.assertTrue(api().get("/api/todos/1").body().contains("Do that"));

        workspace.grid().row("todo", "1").click();
        workspace.editor().delete();
        assertThat(workspace.messages().root()).containsText("todo deleted.");
        Assertions.assertEquals(404, api().get("/api/todos/1").statusCode());

        workspace.topBar().openApiDocs();
        page.waitForURL("**/docs");
        assertThat(page.locator("body")).containsText("Project Tasks API Documentation");
        page.navigate(server().baseUrl() + "/");
        workspace.topBar().openSwagger();
        page.waitForURL("**/swagger");
        assertThat(page.locator("body")).containsText("Select a definition");
        page.navigate(server().baseUrl() + "/");
        Assertions.assertTrue(
                workspace.topBar().downloadOpenApi().suggestedFilename().endsWith(".json"));
    }

    @Test
    public void booleanFieldsAreSubmittedAsJsonBooleansFromWorkspaceEditor() {
        api().postText("/ui/model/yaml", E2eResource.text("/models/todo-manager.yaml"));
        workspace = new WorkspacePage(page, server().baseUrl()).open();
        List<String> projectPostBodies = new ArrayList<>();
        page.onRequest(
                request -> {
                    if ("POST".equals(request.method())
                            && request.url().endsWith("/api/projects")) {
                        projectPostBodies.add(request.postData());
                    }
                });

        workspace.outline().selectEntity("project");
        workspace.grid().clickNew();
        workspace.editor().fillField("title", "Boolean Smoke Project");
        workspace.editor().save();

        assertThat(workspace.messages().root()).containsText("project saved.");
        Assertions.assertFalse(projectPostBodies.isEmpty());
        Assertions.assertTrue(projectPostBodies.get(0).contains("\"completed\":false"));
        Assertions.assertTrue(projectPostBodies.get(0).contains("\"active\":false"));
        String savedProject = api().get("/api/projects/1").body();
        Assertions.assertTrue(
                savedProject.matches("(?s).*\"completed\"\\s*:\\s*false.*"), savedProject);
        Assertions.assertTrue(
                savedProject.matches("(?s).*\"active\"\\s*:\\s*false.*"), savedProject);
    }

    @Test
    public void stringFieldsWithoutMaxLengthRenderAsTextareas() {
        workspace.outline().selectEntity("project");
        workspace.grid().clickNew();
        Assertions.assertEquals("textarea", workspace.editor().fieldTagName("title"));

        api().postText("/ui/model/yaml", E2eResource.text("/models/validations.yaml"));
        workspace = new WorkspacePage(page, server().baseUrl()).open();
        workspace.outline().selectEntity("item");
        workspace.grid().clickNew();
        Assertions.assertEquals("input", workspace.editor().fieldTagName("title"));
    }

    @Test
    public void yamlUploadReplacesSchemaAndClearsData() {
        api().postJson("/api/projects", "{\"title\":\"Smoke Project\"}");
        page.locator("[data-testid='yaml-file']")
                .setInputFiles(
                        Path.of("src/test/resources/models/minimal-todo.yaml").toAbsolutePath());

        assertThat(workspace.topBar().title()).containsText("Minimal Todo");
        assertThat(workspace.outline().entity("todo")).isVisible();
        Assertions.assertEquals(404, api().get("/api/projects").statusCode());
        Assertions.assertFalse(api().get("/api/todos").body().contains("Smoke Project"));
    }
}
