package uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route;

import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.SchemaCatalog;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

public final class ThingRouteMapper {

    private final SchemaCatalog schema;

    public ThingRouteMapper(final SchemaCatalog schema) {
        this.schema = schema;
    }

    public ThingRoute map(final String path) {
        String[] parts = parts(path);
        if (parts.length == 0 || parts.length > 4) {
            return new UnmatchedRoute(path, parts);
        }

        EntityDefinition entity = schema.definitionWithSingularOrPluralNamed(parts[0]);
        if (entity == null) {
            return new UnmatchedRoute(path, parts);
        }

        if (parts.length == 1) {
            return new CollectionRoute(path, entity);
        }

        if (parts.length == 2) {
            return new InstanceRoute(path, entity, parts[1]);
        }

        if (!entity.related().hasRelationship(parts[2])) {
            return new UnmatchedRoute(path, parts);
        }

        if (parts.length == 3) {
            return new RelationshipCollectionRoute(path, entity, parts[1], parts[2]);
        }

        return new RelationshipInstanceRoute(path, entity, parts[1], parts[2], parts[3]);
    }

    public static String[] parts(final String path) {
        String normalized = path == null ? "" : path.trim();
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
