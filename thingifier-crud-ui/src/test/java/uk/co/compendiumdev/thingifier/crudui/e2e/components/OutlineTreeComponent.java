package uk.co.compendiumdev.thingifier.crudui.e2e.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class OutlineTreeComponent {

    private final Page page;

    public OutlineTreeComponent(final Page page) {
        this.page = page;
    }

    public Locator root() {
        return TestSelectors.byTestId(page, "outline-tree");
    }

    public Locator entity(final String entityName) {
        return TestSelectors.byTestId(page, TestSelectors.id("outline-entity", entityName));
    }

    public Locator instance(final String entityName, final String id) {
        return TestSelectors.byTestId(page, TestSelectors.id("outline-instance", entityName, id));
    }

    public Locator relationship(final String entityName, final String sourceId, final String name) {
        return TestSelectors.byTestId(
                page, TestSelectors.id("outline-relationship", entityName, sourceId, name));
    }

    public Locator relatedInstance(final String entityName, final String id) {
        return TestSelectors.byTestId(
                page, TestSelectors.id("outline-related-instance", entityName, id));
    }

    public void expandEntity(final String entityName) {
        entity(entityName).locator(".tree-caret").click();
    }

    public void expandInstance(final String entityName, final String id) {
        instance(entityName, id).locator(".tree-caret").click();
    }

    public void expandRelationship(
            final String entityName, final String sourceId, final String relationshipName) {
        relationship(entityName, sourceId, relationshipName).locator(".tree-caret").click();
    }

    public void selectEntity(final String entityName) {
        entity(entityName).click();
    }

    public void selectInstance(final String entityName, final String id) {
        instance(entityName, id).click();
    }

    public void selectRelationship(
            final String entityName, final String sourceId, final String relationshipName) {
        relationship(entityName, sourceId, relationshipName).click();
    }
}
