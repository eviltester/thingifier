package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class EntityUrlMatcher {

    private EntityUrlMatcher() {}

    static EntityDefinition entityFromCollectionUrl(final SchemaCatalog schema, final String url) {
        String[] parts = parts(url);
        if (parts.length != 1) {
            return null;
        }
        return schema.definitionWithSingularOrPluralNamed(parts[0]);
    }

    static EntityDefinition entityFromInstanceUrl(final SchemaCatalog schema, final String url) {
        String[] parts = parts(url);
        if (parts.length != 2) {
            return null;
        }
        return schema.definitionWithSingularOrPluralNamed(parts[0]);
    }

    static String identifierFromInstanceUrl(final String url) {
        String[] parts = parts(url);
        if (parts.length != 2) {
            return null;
        }
        return parts[1];
    }

    static String entityTermFromUrl(final String url) {
        String[] parts = parts(url);
        if (parts.length == 0) {
            return "";
        }
        return parts[0];
    }

    static boolean hasPartCount(final String url, final int count) {
        return parts(url).length == count;
    }

    static EntityInstance findInstanceFromUrl(
            final SchemaCatalog schema, final ThingStore store, final String url) {
        EntityDefinition entity = entityFromInstanceUrl(schema, url);
        String identifier = identifierFromInstanceUrl(url);
        if (entity == null || identifier == null) {
            return null;
        }
        return store.entityQueries().findByQueryIdentifier(entity, identifier);
    }

    static String[] parts(final String url) {
        String normalized = url == null ? "" : url.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.isEmpty()) {
            return new String[0];
        }
        return normalized.split("/");
    }
}
