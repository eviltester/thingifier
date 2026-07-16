package uk.co.compendiumdev.thingifier.crudui.e2e.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class SchemaDetailComponent {

    private final Page page;

    public SchemaDetailComponent(final Page page) {
        this.page = page;
    }

    public Locator title() {
        return TestSelectors.byTestId(page, "schema-detail-title");
    }

    public Locator input(final String label) {
        return page.locator("[data-testid='" + TestSelectors.id("schema-input", label) + "']")
                .first();
    }

    public Locator action(final String label) {
        return page.locator("[data-testid='" + TestSelectors.id("schema-action", label) + "']")
                .first();
    }

    public void fill(final String label, final String value) {
        input(label).fill(value);
    }

    public void select(final String label, final String value) {
        input(label).selectOption(value);
    }

    public void clickAction(final String label) {
        action(label).click();
    }
}
