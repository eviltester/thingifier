package uk.co.compendiumdev.thingifier.crudui.e2e.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class StorageControlsComponent {

    private final Page page;

    public StorageControlsComponent(final Page page) {
        this.page = page;
    }

    public Locator modeSelect() {
        return TestSelectors.byTestId(page, "storage-mode-select");
    }

    public Locator sqliteFileInput() {
        return TestSelectors.byTestId(page, "storage-file-input");
    }

    public void switchTo(final String mode) {
        modeSelect().selectOption(mode);
        TestSelectors.byTestId(page, "switch-storage-button").click();
    }

    public void switchToFile(final String path) {
        modeSelect().selectOption("sqlite-file");
        sqliteFileInput().fill(path);
        TestSelectors.byTestId(page, "switch-storage-button").click();
    }
}
