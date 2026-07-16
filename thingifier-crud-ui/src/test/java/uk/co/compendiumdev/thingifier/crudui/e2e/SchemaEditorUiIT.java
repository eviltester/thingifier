package uk.co.compendiumdev.thingifier.crudui.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Download;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.co.compendiumdev.thingifier.crudui.e2e.components.TestSelectors;
import uk.co.compendiumdev.thingifier.crudui.e2e.pages.SchemaEditPage;

public class SchemaEditorUiIT extends BrowserTestBase {

    @TempDir Path temp;

    private SchemaEditPage schema;

    @BeforeEach
    void openSchemaEditor() {
        resetToProjectTasks();
        schema = new SchemaEditPage(page, server().baseUrl()).open();
    }

    @Test
    public void schemaEditIsSeparateFromWorkspaceAndDiagramControlsWork() {
        assertThat(TestSelectors.byTestId(page, "schema-page")).isVisible();
        Assertions.assertEquals(0, page.locator("[data-testid='docs-link']").count());
        Assertions.assertEquals(0, page.locator("[data-testid='swagger-link']").count());
        Assertions.assertEquals(0, page.locator("[data-testid='download-openapi-link']").count());
        assertThat(schema.diagram().key()).containsText("Relationship Key");
        assertThat(schema.diagram().diagram()).isVisible();

        schema.diagram().zoomIn();
        assertThat(TestSelectors.byTestId(page, "schema-zoom-reset-button")).containsText("110%");
        schema.diagram().zoomOut();
        assertThat(TestSelectors.byTestId(page, "schema-zoom-reset-button")).containsText("100%");
        schema.diagram().toggleLayout();
        assertThat(TestSelectors.byTestId(page, "schema-layout-toggle-button"))
                .containsText("Horizontal Layout");
        schema.diagram().toggleVisible();
        assertThat(schema.diagram().content()).isHidden();
        schema.diagram().toggleVisible();
        assertThat(schema.diagram().content()).isVisible();
    }

    @Test
    public void schemaTreeSupportsEntityFieldValidationAndRelationshipEditing() {
        schema.tree().addEntity();
        schema.detail().fill("Name", "note");
        schema.detail().fill("Plural", "notes");
        schema.detail().fill("Primary Key", "id");
        schema.detail().clickAction("Add Field");
        schema.detail().fill("Name", "id");
        schema.detail().select("Type", "auto-increment");
        schema.tree().selectEntity("note");
        schema.detail().clickAction("Add Field");
        schema.detail().fill("Name", "body");
        schema.detail().select("Type", "string");
        schema.detail().clickAction("Add Validation");
        assertThat(TestSelectors.byTestId(page, "schema-dirty-status")).isVisible();

        page.locator(
                        "[data-testid='schema-section-relationships'] [data-testid='schema-action-add']")
                .click();
        schema.detail().select("From", "project");
        schema.detail().fill("Name", "notes");
        schema.detail().select("To", "note");
        schema.detail().select("Cardinality", "one-to-many");
        schema.detail().select("Optionality", "optional");
        schema.validate();
        assertThat(TestSelectors.byTestId(page, "schema-validation"))
                .containsText("Schema is valid");
    }

    @Test
    public void yamlParseValidateExportsCopyAndDownloadWorkFromSchemaEdit() throws Exception {
        schema.toggleYamlDraft();
        assertThat(TestSelectors.byTestId(page, "schema-yaml-section")).isVisible();
        TestSelectors.byTestId(page, "schema-yaml-input").fill("formatVersion: 1\nentities: [");
        TestSelectors.byTestId(page, "schema-parse-yaml-button").click();
        assertThat(schema.messages().root()).isVisible();

        TestSelectors.byTestId(page, "schema-yaml-input")
                .fill(E2eResource.text("/models/minimal-todo.yaml"));
        TestSelectors.byTestId(page, "schema-parse-yaml-button").click();
        assertThat(schema.tree().root()).containsText("todo");
        schema.validate();

        schema.exports().toggle();
        assertThat(schema.exports().root()).isVisible();
        Assertions.assertTrue(
                schema.exports().canonicalYaml().inputValue().contains("Minimal Todo"));
        Assertions.assertTrue(schema.exports().mermaid().inputValue().contains("erDiagram"));
        Assertions.assertTrue(schema.exports().graphviz().inputValue().contains("digraph"));
        schema.exports().copyYaml();
        assertThat(schema.messages().root()).containsText("YAML copied");

        Download download = schema.exports().downloadMermaid();
        Path mermaidFile = temp.resolve(download.suggestedFilename());
        download.saveAs(mermaidFile);
        Assertions.assertTrue(Files.readString(mermaidFile).contains("erDiagram"));
    }

    @Test
    public void dirtyResetPromptsBeforeDiscardingDraftChanges() {
        schema.tree().selectModel();
        schema.detail().fill("Title", "Changed Title");
        assertThat(TestSelectors.byTestId(page, "schema-dirty-status")).isVisible();

        page.onDialog(dialog -> dialog.accept());
        TestSelectors.byTestId(page, "schema-reset-button").click();
        Assertions.assertEquals(
                Boolean.TRUE,
                TestSelectors.byTestId(page, "schema-dirty-status")
                        .evaluate("element => element.hidden"));
        assertThat(schema.topBar().description()).containsText("Project Tasks");
    }
}
