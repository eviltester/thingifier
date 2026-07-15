package uk.co.compendiumdev.thingifier.crudui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class ResourceReader {

    public String read(final String resourcePath) {
        try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new CrudUiException(404, "Resource not found");
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CrudUiException(500, "Could not read resource");
        }
    }
}
