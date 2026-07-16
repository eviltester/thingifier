package uk.co.compendiumdev.thingifier.crudui.e2e.pages;

import com.microsoft.playwright.Page;
import uk.co.compendiumdev.thingifier.crudui.e2e.components.DiagramPanelComponent;
import uk.co.compendiumdev.thingifier.crudui.e2e.components.MessageBarComponent;
import uk.co.compendiumdev.thingifier.crudui.e2e.components.SchemaDetailComponent;
import uk.co.compendiumdev.thingifier.crudui.e2e.components.SchemaExportsComponent;
import uk.co.compendiumdev.thingifier.crudui.e2e.components.SchemaTreeComponent;
import uk.co.compendiumdev.thingifier.crudui.e2e.components.SchemaUpgradeDialogComponent;
import uk.co.compendiumdev.thingifier.crudui.e2e.components.TestSelectors;
import uk.co.compendiumdev.thingifier.crudui.e2e.components.TopBarComponent;

public final class SchemaEditPage {

    private final Page page;
    private final String baseUrl;
    private final TopBarComponent topBar;
    private final SchemaTreeComponent tree;
    private final SchemaDetailComponent detail;
    private final DiagramPanelComponent diagram;
    private final SchemaExportsComponent exports;
    private final SchemaUpgradeDialogComponent upgrade;
    private final MessageBarComponent messages;

    public SchemaEditPage(final Page page, final String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
        this.topBar = new TopBarComponent(page);
        this.tree = new SchemaTreeComponent(page);
        this.detail = new SchemaDetailComponent(page);
        this.diagram = new DiagramPanelComponent(page);
        this.exports = new SchemaExportsComponent(page);
        this.upgrade = new SchemaUpgradeDialogComponent(page);
        this.messages = new MessageBarComponent(page);
    }

    public SchemaEditPage open() {
        page.navigate(baseUrl + "/schema");
        TestSelectors.byTestId(page, "schema-page").waitFor();
        TestSelectors.byTestId(page, "schema-tree").waitFor();
        return this;
    }

    public TopBarComponent topBar() {
        return topBar;
    }

    public SchemaTreeComponent tree() {
        return tree;
    }

    public SchemaDetailComponent detail() {
        return detail;
    }

    public DiagramPanelComponent diagram() {
        return diagram;
    }

    public SchemaExportsComponent exports() {
        return exports;
    }

    public SchemaUpgradeDialogComponent upgrade() {
        return upgrade;
    }

    public MessageBarComponent messages() {
        return messages;
    }

    public void toggleYamlDraft() {
        TestSelectors.byTestId(page, "schema-toggle-yaml-button").click();
    }

    public void validate() {
        TestSelectors.byTestId(page, "schema-validate-button").click();
    }
}
