package uk.co.compendiumdev.thingifier.crudui.e2e.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class MessageBarComponent {

    private final Page page;

    public MessageBarComponent(final Page page) {
        this.page = page;
    }

    public Locator root() {
        return TestSelectors.byTestId(page, "message-bar");
    }
}
