package uk.co.compendiumdev.thingifier.crudui.e2e.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class EditorPanelComponent {

    private final Page page;

    public EditorPanelComponent(final Page page) {
        this.page = page;
    }

    public Locator root() {
        return TestSelectors.byTestId(page, "editor-panel");
    }

    public Locator field(final String fieldName) {
        return TestSelectors.byTestId(page, TestSelectors.id("field-input", "editor", fieldName));
    }

    public String fieldTagName(final String fieldName) {
        return String.valueOf(
                field(fieldName).evaluate("element => element.tagName.toLowerCase()"));
    }

    public void fillField(final String fieldName, final String value) {
        field(fieldName).fill(value);
    }

    public void save() {
        TestSelectors.byTestId(page, "editor-save-button").click();
    }

    public void delete() {
        TestSelectors.byTestId(page, "editor-delete-button").click();
    }

    public void removeFromRelationship() {
        TestSelectors.byTestId(page, "editor-remove-relationship-button").click();
    }
}
