package uk.co.compendiumdev.thingifier.crudui.e2e.components;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class SchemaExportsComponent {

    private final Page page;

    public SchemaExportsComponent(final Page page) {
        this.page = page;
    }

    public Locator root() {
        return TestSelectors.byTestId(page, "schema-exports-section");
    }

    public Locator canonicalYaml() {
        return TestSelectors.byTestId(page, "schema-canonical-yaml");
    }

    public Locator mermaid() {
        return TestSelectors.byTestId(page, "schema-mermaid-output");
    }

    public Locator graphviz() {
        return TestSelectors.byTestId(page, "schema-graphviz-output");
    }

    public void toggle() {
        TestSelectors.byTestId(page, "schema-toggle-exports-button").click();
    }

    public void copyYaml() {
        TestSelectors.byTestId(page, "schema-copy-yaml").click();
    }

    public Download downloadMermaid() {
        return page.waitForDownload(
                () -> TestSelectors.byTestId(page, "schema-download-mermaid").click());
    }
}
