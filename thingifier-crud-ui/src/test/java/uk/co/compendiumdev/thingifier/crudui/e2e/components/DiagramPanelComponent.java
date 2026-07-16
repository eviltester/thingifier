package uk.co.compendiumdev.thingifier.crudui.e2e.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class DiagramPanelComponent {

    private final Page page;

    public DiagramPanelComponent(final Page page) {
        this.page = page;
    }

    public Locator content() {
        return TestSelectors.byTestId(page, "schema-diagram-content");
    }

    public Locator diagram() {
        return TestSelectors.byTestId(page, "schema-mermaid-diagram");
    }

    public Locator key() {
        return TestSelectors.byTestId(page, "schema-diagram-key");
    }

    public Locator resizer() {
        return TestSelectors.byTestId(page, "schema-diagram-resizer");
    }

    public void toggleVisible() {
        TestSelectors.byTestId(page, "schema-toggle-diagram-button").click();
    }

    public void zoomIn() {
        TestSelectors.byTestId(page, "schema-zoom-in-button").click();
    }

    public void zoomOut() {
        TestSelectors.byTestId(page, "schema-zoom-out-button").click();
    }

    public void toggleLayout() {
        TestSelectors.byTestId(page, "schema-layout-toggle-button").click();
    }
}
