package uk.co.compendiumdev.thingifier.crudui.e2e.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class SchemaUpgradeDialogComponent {

    private final Page page;

    public SchemaUpgradeDialogComponent(final Page page) {
        this.page = page;
    }

    public Locator root() {
        return TestSelectors.byTestId(page, "schema-upgrade-dialog");
    }

    public Locator report() {
        return TestSelectors.byTestId(page, "schema-upgrade-report");
    }

    public void open() {
        TestSelectors.byTestId(page, "schema-apply-workspace-button").click();
    }

    public void preview() {
        TestSelectors.byTestId(page, "schema-upgrade-preview-button").click();
    }

    public void confirm() {
        TestSelectors.byTestId(page, "schema-upgrade-confirm-button").click();
    }

    public void cancel() {
        TestSelectors.byTestId(page, "schema-upgrade-cancel-button").click();
    }
}
