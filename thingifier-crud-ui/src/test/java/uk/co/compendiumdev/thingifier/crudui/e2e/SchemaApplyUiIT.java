package uk.co.compendiumdev.thingifier.crudui.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.crudui.e2e.components.TestSelectors;
import uk.co.compendiumdev.thingifier.crudui.e2e.pages.SchemaEditPage;

public class SchemaApplyUiIT extends BrowserTestBase {

    private SchemaEditPage schema;

    @BeforeEach
    void openSchemaEditorWithData() {
        resetToProjectTasks();
        api().postJson("/api/todos", "{\"title\":\"Migrated Task\"}");
        schema = new SchemaEditPage(page, server().baseUrl()).open();
    }

    @Test
    public void applyDialogOpensOnlyOnApplyCancelLeavesWorkspaceAndConfirmMigratesData() {
        assertThat(schema.upgrade().root()).isHidden();
        addTodoStatusField();

        assertThat(TestSelectors.byTestId(page, "schema-apply-workspace-button")).isEnabled();
        schema.upgrade().open();
        assertThat(schema.upgrade().root()).isVisible();
        assertThat(schema.upgrade().report()).containsText("Upgrade can be applied");
        schema.upgrade().cancel();
        assertThat(schema.upgrade().root()).isHidden();
        Assertions.assertFalse(api().get("/api/todos/1").body().contains("status"));

        schema.upgrade().open();
        assertThat(schema.upgrade().report()).containsText("Upgrade can be applied");
        schema.upgrade().confirm();
        assertThat(schema.messages().root()).containsText("Schema upgrade applied");
        Assertions.assertTrue(api().get("/api/todos/1").body().contains("\"status\":\"open\""));
    }

    @Test
    public void staleWorkspaceApplyFailureIsReportedByUi() {
        addTodoStatusField();
        schema.upgrade().open();
        assertThat(schema.upgrade().root()).isVisible();
        api().postText("/ui/model/yaml", E2eResource.text("/models/minimal-todo.yaml"));
        schema.upgrade().confirm();
        assertThat(schema.messages().root()).containsText("Request failed with status 409");
    }

    private void addTodoStatusField() {
        schema.tree().selectEntity("todo");
        schema.detail().clickAction("Add Field");
        schema.detail().fill("Name", "status");
        schema.detail().select("Type", "string");
        schema.detail().fill("Default", "open");
        schema.validate();
        assertThat(TestSelectors.byTestId(page, "schema-validation"))
                .containsText("Schema is valid");
    }
}
