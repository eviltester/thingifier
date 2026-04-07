package uk.co.compendiumdev.challenge.gui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MarkdownContentManagerDateMetadataTest {

    @Test
    void resolveDateModifiedPrefersLastmodWhenPresent() {
        final String resolved = MarkdownContentManager.resolveDateModified("2026-02-18", "2021-01-01T09:00:00Z");
        Assertions.assertEquals("2026-02-18", resolved);
    }

    @Test
    void resolveDateModifiedFallsBackToDateWhenLastmodMissing() {
        final String resolved = MarkdownContentManager.resolveDateModified("", "2021-01-01T09:00:00Z");
        Assertions.assertEquals("2021-01-01T09:00:00Z", resolved);
    }

    @Test
    void resolveDateModifiedReturnsEmptyWhenBothValuesMissing() {
        final String resolved = MarkdownContentManager.resolveDateModified("", "");
        Assertions.assertEquals("", resolved);
    }
}
