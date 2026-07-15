package uk.co.compendiumdev.thingifier.crudui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

final class TestResources {

    private TestResources() {}

    static String text(final String resourcePath) {
        try (InputStream stream = TestResources.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalArgumentException("Missing test resource " + resourcePath);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
