package uk.co.compendiumdev.thingifier.api.http.headers.headerparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ApiKeyHeaderParserTest {

    @Test
    void validApiKeyHeaderExposesTrimmedCredential() {
        final ApiKeyHeaderParser parser = new ApiKeyHeaderParser("  Key-123  ");

        Assertions.assertTrue(parser.isValid());
        Assertions.assertEquals("Key-123", parser.credential());
    }

    @Test
    void missingApiKeyHeaderIsInvalid() {
        final ApiKeyHeaderParser parser = new ApiKeyHeaderParser(null);

        Assertions.assertFalse(parser.isValid());
        Assertions.assertEquals("", parser.credential());
    }

    @Test
    void blankApiKeyHeaderIsInvalid() {
        final ApiKeyHeaderParser parser = new ApiKeyHeaderParser("   ");

        Assertions.assertFalse(parser.isValid());
        Assertions.assertEquals("", parser.credential());
    }
}
