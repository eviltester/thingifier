package uk.co.compendiumdev.thingifier.crudui.e2e.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class SwaggerCopyComponent {

    private final Page page;

    public SwaggerCopyComponent(final Page page) {
        this.page = page;
    }

    public void installClipboardStub() {
        page.addInitScript(
                "window.__copiedText = '';\n"
                        + "Object.defineProperty(navigator, 'clipboard', {\n"
                        + "  configurable: true,\n"
                        + "  value: { writeText: async function(text) { window.__copiedText = text; } }\n"
                        + "});");
    }

    public Locator fullApiButton() {
        return TestSelectors.byTestId(page, "swagger-copy-full-ai");
    }

    public Locator firstOperationButton() {
        return TestSelectors.byTestId(page, "swagger-copy-operation-ai").first();
    }

    public Locator operationButton(final String method, final String path) {
        return page.locator(".opblock")
                .filter(new Locator.FilterOptions().setHasText(method.toUpperCase()))
                .filter(new Locator.FilterOptions().setHasText(path))
                .locator("[data-testid='swagger-copy-operation-ai']")
                .first();
    }

    public String copiedText() {
        return String.valueOf(page.evaluate("() => window.__copiedText || ''"));
    }

    public void waitForCopiedTextContaining(final String expectedText) {
        page.waitForFunction(
                "expected => (window.__copiedText || '').includes(expected)", expectedText);
    }
}
