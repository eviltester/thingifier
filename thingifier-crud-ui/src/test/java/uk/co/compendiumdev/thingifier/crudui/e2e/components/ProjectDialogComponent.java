package uk.co.compendiumdev.thingifier.crudui.e2e.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class ProjectDialogComponent {

    private final Page page;

    public ProjectDialogComponent(final Page page) {
        this.page = page;
    }

    public Locator root() {
        return TestSelectors.byTestId(page, "project-dialog");
    }

    public Locator status() {
        return TestSelectors.byTestId(page, "project-browser-status");
    }

    public Locator saveStorageOptions() {
        return TestSelectors.byTestId(page, "project-save-storage-options");
    }

    public void openSave() {
        TestSelectors.byTestId(page, "save-project-button").click();
    }

    public void openLoad() {
        TestSelectors.byTestId(page, "load-project-button").click();
    }

    public void fillPath(final String path) {
        TestSelectors.byTestId(page, "project-path-input").fill(path);
    }

    public String pathValue() {
        return TestSelectors.byTestId(page, "project-path-input").inputValue();
    }

    public void validatePath() {
        TestSelectors.byTestId(page, "project-validate-button").click();
    }

    public void browse() {
        TestSelectors.byTestId(page, "project-browse-button").click();
    }

    public void chooseJsonProject() {
        TestSelectors.byTestId(page, "project-save-storage-json").check();
    }

    public void chooseSqliteProject() {
        TestSelectors.byTestId(page, "project-save-storage-sqlite").check();
    }

    public void confirm() {
        TestSelectors.byTestId(page, "project-dialog-confirm").click();
    }

    public void browserSave() {
        TestSelectors.byTestId(page, "project-browser-save-button").click();
    }

    public void browserLoad() {
        TestSelectors.byTestId(page, "project-browser-load-button").click();
    }
}
