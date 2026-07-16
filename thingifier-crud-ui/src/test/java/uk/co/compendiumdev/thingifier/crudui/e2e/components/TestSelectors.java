package uk.co.compendiumdev.thingifier.crudui.e2e.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class TestSelectors {

    private TestSelectors() {}

    public static Locator byTestId(final Page page, final String testId) {
        return page.locator("[data-testid='" + testId + "']");
    }

    public static String idPart(final String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
    }

    public static String id(final String prefix, final String... parts) {
        StringBuilder builder = new StringBuilder(prefix);
        for (String part : parts) {
            builder.append('-').append(idPart(part));
        }
        return builder.toString();
    }
}
