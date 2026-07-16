package uk.co.compendiumdev.thingifier.crudui.e2e.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class SchemaTreeComponent {

    private final Page page;

    public SchemaTreeComponent(final Page page) {
        this.page = page;
    }

    public Locator root() {
        return TestSelectors.byTestId(page, "schema-tree");
    }

    public void addEntity() {
        TestSelectors.byTestId(page, "schema-add-entity-button").click();
    }

    public void selectModel() {
        page.locator("[data-testid^='schema-tree-model']").click();
    }

    public void selectEntity(final String label) {
        page.locator("[data-testid^='schema-tree-entity']")
                .filter(new Locator.FilterOptions().setHasText(label))
                .first()
                .click();
    }

    public void selectField(final String label) {
        page.locator("[data-testid^='schema-tree-field']")
                .filter(new Locator.FilterOptions().setHasText(label))
                .first()
                .click();
    }

    public void selectRelationship(final String label) {
        page.locator("[data-testid^='schema-tree-relationship']")
                .filter(new Locator.FilterOptions().setHasText(label))
                .first()
                .click();
    }
}
