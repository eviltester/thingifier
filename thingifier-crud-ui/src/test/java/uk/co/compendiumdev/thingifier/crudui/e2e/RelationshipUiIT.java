package uk.co.compendiumdev.thingifier.crudui.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.crudui.e2e.pages.WorkspacePage;

public class RelationshipUiIT extends BrowserTestBase {

    private WorkspacePage workspace;

    @BeforeEach
    void seedRelationshipWorkspace() {
        resetToProjectTasks();
        api().postJson("/api/projects", "{\"title\":\"Smoke Project\"}");
        api().postJson("/api/todos", "{\"title\":\"Do this\"}");
        api().postJson("/api/todos", "{\"title\":\"Do that\"}");
        workspace = new WorkspacePage(page, server().baseUrl()).open();
    }

    @Test
    public void relationshipManagerCanConnectCreateAndRemoveRelationshipEdges() {
        workspace.outline().expandEntity("project");
        workspace.outline().expandInstance("project", "1");
        workspace.outline().selectRelationship("project", "1", "tasks");
        assertThat(workspace.relationships().root()).isVisible();
        assertThat(workspace.relationships().connectSelect()).isVisible();

        workspace.relationships().connectExisting("1");
        assertThat(workspace.messages().root()).containsText("todo connected.");
        assertThat(workspace.grid().row("todo", "1")).containsText("Do this");
        Assertions.assertTrue(api().get("/api/projects/1/tasks").body().contains("Do this"));

        workspace.relationships().createAndConnect("Do from relationship");
        assertThat(workspace.messages().root()).containsText("todo created and connected.");
        assertThat(workspace.grid().row("todo", "3")).containsText("Do from relationship");
        Assertions.assertTrue(api().get("/api/todos/3").body().contains("Do from relationship"));

        workspace.relationships().removeRow("todo", "1");
        assertThat(workspace.messages().root()).containsText("todo removed from relationship.");
        Assertions.assertFalse(api().get("/api/projects/1/tasks").body().contains("Do this"));
        Assertions.assertTrue(api().get("/api/todos/1").body().contains("Do this"));
    }

    @Test
    public void editorRelationshipRemoveDoesNotDeleteTargetInstance() {
        api().postJson("/api/projects/1/tasks", "{\"id\":\"2\"}");
        page.reload();
        workspace.outline().expandEntity("project");
        workspace.outline().expandInstance("project", "1");
        workspace.outline().selectRelationship("project", "1", "tasks");
        workspace.grid().row("todo", "2").click();
        workspace.editor().removeFromRelationship();

        assertThat(workspace.messages().root()).containsText("todo removed from relationship.");
        Assertions.assertFalse(api().get("/api/projects/1/tasks").body().contains("Do that"));
        Assertions.assertTrue(api().get("/api/todos/2").body().contains("Do that"));
    }
}
