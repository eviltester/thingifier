package uk.co.compendiumdev.thingifier.crudui.e2e.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class RelationshipManagerComponent {

    private final Page page;

    public RelationshipManagerComponent(final Page page) {
        this.page = page;
    }

    public Locator root() {
        return TestSelectors.byTestId(page, "relationship-manager");
    }

    public Locator connectSelect() {
        return TestSelectors.byTestId(page, "relationship-connect-select");
    }

    public void connectExisting(final String id) {
        connectSelect().selectOption(id);
        TestSelectors.byTestId(page, "relationship-connect-submit").click();
    }

    public Locator createField(final String fieldName) {
        return TestSelectors.byTestId(
                page, TestSelectors.id("field-input", "relationship-create", fieldName));
    }

    public void createAndConnect(final String title) {
        createField("title").fill(title);
        TestSelectors.byTestId(page, "relationship-create-submit").click();
    }

    public void removeRow(final String entityName, final String id) {
        TestSelectors.byTestId(page, TestSelectors.id("relationship-row-remove", entityName, id))
                .click();
    }
}
