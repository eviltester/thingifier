package uk.co.compendiumdev.thingifier.crudui.e2e;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

final class E2eResource {

    private E2eResource() {}

    static String text(final String resourcePath) {
        try (InputStream stream = E2eResource.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalArgumentException("Missing test resource " + resourcePath);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
