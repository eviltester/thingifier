package uk.co.compendiumdev.thingifier.swaggerizer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SwaggerCopyForAiAssetTest {

    @Test
    void javascriptContainsCopyForAiBehaviour() {
        final String script = resource("/public/js/swagger-copy-for-ai.js");

        Assertions.assertTrue(script.contains("window.location.origin"));
        Assertions.assertTrue(script.contains("navigator.clipboard.writeText"));
        Assertions.assertTrue(script.contains("MutationObserver"));
        Assertions.assertTrue(script.contains("Copy for AI"));
        Assertions.assertTrue(script.contains("Copy full API for AI"));
        Assertions.assertTrue(script.contains("fullApiMarkdown"));
        Assertions.assertTrue(script.contains("operationMarkdown"));
    }

    @Test
    void cssContainsCopyForAiStyles() {
        final String css = resource("/public/css/swagger-copy-for-ai.css");

        Assertions.assertTrue(css.contains(".swagger-copy-ai-toolbar"));
        Assertions.assertTrue(css.contains(".swagger-copy-ai-button"));
        Assertions.assertTrue(css.contains(".swagger-copy-ai-operation"));
    }

    private String resource(final String path) {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) {
                throw new AssertionError("Missing resource " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError("Could not read resource " + path, e);
        }
    }
}
