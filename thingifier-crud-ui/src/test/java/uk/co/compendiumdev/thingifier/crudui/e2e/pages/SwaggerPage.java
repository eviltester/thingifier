package uk.co.compendiumdev.thingifier.crudui.e2e.pages;

import com.microsoft.playwright.Page;
import uk.co.compendiumdev.thingifier.crudui.e2e.components.SwaggerCopyComponent;
import uk.co.compendiumdev.thingifier.crudui.e2e.components.TestSelectors;

public final class SwaggerPage {

    private final Page page;
    private final String baseUrl;
    private final SwaggerCopyComponent copy;

    public SwaggerPage(final Page page, final String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
        this.copy = new SwaggerCopyComponent(page);
    }

    public SwaggerPage open() {
        page.navigate(baseUrl + "/swagger");
        TestSelectors.byTestId(page, "swagger-copy-full-ai").waitFor();
        TestSelectors.byTestId(page, "swagger-copy-operation-ai").first().waitFor();
        return this;
    }

    public SwaggerCopyComponent copy() {
        return copy;
    }
}
