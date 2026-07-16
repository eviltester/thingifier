package uk.co.compendiumdev.thingifier.crudui.e2e.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class DataGridComponent {

    private final Page page;

    public DataGridComponent(final Page page) {
        this.page = page;
    }

    public Locator title() {
        return TestSelectors.byTestId(page, "grid-title");
    }

    public Locator context() {
        return TestSelectors.byTestId(page, "grid-context");
    }

    public Locator row(final String entityName, final String id) {
        return TestSelectors.byTestId(page, TestSelectors.id("grid-row", entityName, id));
    }

    public Locator columnFilter(final String entityName, final String fieldName) {
        return TestSelectors.byTestId(page, TestSelectors.id("grid-filter", entityName, fieldName));
    }

    public void globalSearch(final String text) {
        TestSelectors.byTestId(page, "search-input").fill(text);
    }

    public void clickNew() {
        TestSelectors.byTestId(page, "new-button").click();
    }

    public void refresh() {
        TestSelectors.byTestId(page, "refresh-button").click();
    }
}
