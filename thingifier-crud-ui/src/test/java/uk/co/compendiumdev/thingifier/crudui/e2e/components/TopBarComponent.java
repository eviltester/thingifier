package uk.co.compendiumdev.thingifier.crudui.e2e.components;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class TopBarComponent {

    private final Page page;

    public TopBarComponent(final Page page) {
        this.page = page;
    }

    public Locator title() {
        return TestSelectors.byTestId(page, "model-title");
    }

    public Locator description() {
        return TestSelectors.byTestId(page, "model-description");
    }

    public void openWorkspace() {
        TestSelectors.byTestId(page, "schema-workspace-link").click();
    }

    public void openSchemaEdit() {
        TestSelectors.byTestId(page, "schema-link").click();
    }

    public void openApiDocs() {
        TestSelectors.byTestId(page, "docs-link").click();
    }

    public void openSwagger() {
        TestSelectors.byTestId(page, "swagger-link").click();
    }

    public Download downloadOpenApi() {
        return page.waitForDownload(
                () -> TestSelectors.byTestId(page, "download-openapi-link").click());
    }
}
