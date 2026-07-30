package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.JsonPatch;
import java.util.Map;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.InstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public final class EntityPatchDocumentMapper {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final ThingifierApiRuntime runtime;
    private final ThingWriteRequestMapper writeMapper;

    public EntityPatchDocumentMapper(final ThingifierApiRuntime runtime) {
        this.runtime = runtime;
        this.writeMapper = new ThingWriteRequestMapper(runtime.schema());
    }

    public ThingWriteRequestMapping map(
            final EntityPatchUpdateStyle style,
            final ThingRoute route,
            final String rawBody,
            final ThingifierRequestContext context) {
        if (!(route instanceof InstanceRoute)) {
            return writeMapper.mapPatch(route, ApiBodyFields.empty());
        }

        InstanceRoute instanceRoute = (InstanceRoute) route;
        if (style == EntityPatchUpdateStyle.PARTIAL_JSON_UPDATE) {
            return mapPartialJsonUpdate(instanceRoute, rawBody);
        }
        if (style == EntityPatchUpdateStyle.JSON_MERGE_PATCH_RFC7396) {
            return mapJsonMergePatch(instanceRoute, rawBody, context);
        }
        if (style == EntityPatchUpdateStyle.JSON_PATCH_RFC6902) {
            return mapJsonPatch(instanceRoute, rawBody, context);
        }

        return ThingWriteRequestMapping.error(
                ApiMappingError.withMessage(400, "Unsupported PATCH update style"));
    }

    private ThingWriteRequestMapping mapPartialJsonUpdate(
            final InstanceRoute route, final String rawBody) {
        ParseResult parsed = parseJsonObject(rawBody, true);
        if (parsed.error != null) {
            return parsed.error;
        }
        return writeMapper.mapPatch(route, parsed.bodyFields());
    }

    private ThingWriteRequestMapping mapJsonMergePatch(
            final InstanceRoute route,
            final String rawBody,
            final ThingifierRequestContext context) {
        EntityInstance instance = findInstance(route, context);
        if (instance == null) {
            return missingInstance(route);
        }

        JsonNode patchDocument;
        try {
            patchDocument = JSON.readTree(rawBody);
        } catch (JsonProcessingException e) {
            return malformedPatch("Malformed JSON Merge Patch document");
        }

        if (patchDocument == null || !patchDocument.isObject()) {
            return unprocessablePatch("JSON Merge Patch for entity resources must be an object");
        }

        JsonNode patchedDocument = applyMergePatch(jsonFor(instance), patchDocument);
        return mapReplacement(route, patchedDocument);
    }

    private ThingWriteRequestMapping mapJsonPatch(
            final InstanceRoute route,
            final String rawBody,
            final ThingifierRequestContext context) {
        EntityInstance instance = findInstance(route, context);
        if (instance == null) {
            return missingInstance(route);
        }

        JsonNode patchDocument;
        try {
            patchDocument = JSON.readTree(rawBody);
        } catch (JsonProcessingException e) {
            return malformedPatch("Malformed JSON Patch document");
        }

        if (patchDocument == null || !patchDocument.isArray()) {
            return malformedPatch("JSON Patch document must be an array of operations");
        }

        JsonNode patchedDocument;
        try {
            patchedDocument = JsonPatch.apply(patchDocument, jsonFor(instance));
        } catch (RuntimeException e) {
            return conflictingPatch("JSON Patch could not be applied");
        }

        return mapReplacement(route, patchedDocument);
    }

    private JsonNode applyMergePatch(final JsonNode target, final JsonNode patch) {
        if (!patch.isObject()) {
            return patch;
        }

        ObjectNode result =
                target != null && target.isObject()
                        ? ((ObjectNode) target).deepCopy()
                        : JSON.createObjectNode();
        patch.fields()
                .forEachRemaining(
                        entry -> {
                            if (entry.getValue().isNull()) {
                                result.remove(entry.getKey());
                            } else {
                                result.set(
                                        entry.getKey(),
                                        applyMergePatch(
                                                result.get(entry.getKey()), entry.getValue()));
                            }
                        });
        return result;
    }

    private ThingWriteRequestMapping mapReplacement(
            final InstanceRoute route, final JsonNode patchedDocument) {
        if (patchedDocument == null || !patchedDocument.isObject()) {
            return unprocessablePatch("PATCH result for entity resources must be an object");
        }

        return writeMapper.mapPatchReplacingFields(
                route, ApiBodyFields.fromMap(JSON.convertValue(patchedDocument, Map.class)));
    }

    private JsonNode jsonFor(final EntityInstance instance) {
        try {
            return JSON.readTree(
                    new JsonThing(runtime.apiConfig().jsonOutput())
                            .asJsonObject(instance)
                            .toString());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not render entity instance as JSON", e);
        }
    }

    private EntityInstance findInstance(
            final InstanceRoute route, final ThingifierRequestContext context) {
        EntityDefinition entity =
                runtime.schema().definitionWithSingularOrPluralNamed(route.entity().name());
        if (entity == null) {
            return null;
        }
        return context.store().entityQueries().findByQueryIdentifier(entity, route.identifier());
    }

    private ThingWriteRequestMapping missingInstance(final InstanceRoute route) {
        return ThingWriteRequestMapping.error(
                ApiMappingError.withMessage(
                        404,
                        String.format(
                                "No such %s entity instance with %s == %s found",
                                route.entity().name(),
                                route.entity().primaryKeyFieldName(),
                                route.identifier())));
    }

    private ParseResult parseJsonObject(final String rawBody, final boolean allowEmpty) {
        String body = rawBody == null ? "" : rawBody.trim();
        if (body.isEmpty()) {
            return allowEmpty
                    ? ParseResult.bodyFields(ApiBodyFields.empty())
                    : ParseResult.error(malformedPatch("Malformed JSON document"));
        }

        JsonNode document;
        try {
            document = JSON.readTree(body);
        } catch (JsonProcessingException e) {
            return ParseResult.error(malformedPatch("Malformed JSON document"));
        }

        if (document == null || !document.isObject()) {
            return ParseResult.error(
                    malformedPatch("PATCH partial JSON update document must be an object"));
        }

        return ParseResult.bodyFields(
                ApiBodyFields.fromMap(JSON.convertValue(document, Map.class)));
    }

    private ThingWriteRequestMapping malformedPatch(final String message) {
        return ThingWriteRequestMapping.error(ApiMappingError.withMessage(400, message));
    }

    private ThingWriteRequestMapping unprocessablePatch(final String message) {
        return ThingWriteRequestMapping.error(ApiMappingError.withMessage(422, message));
    }

    private ThingWriteRequestMapping conflictingPatch(final String message) {
        return ThingWriteRequestMapping.error(ApiMappingError.withMessage(409, message));
    }

    private static final class ParseResult {
        private final ApiBodyFields bodyFields;
        private final ThingWriteRequestMapping error;

        private ParseResult(final ApiBodyFields bodyFields, final ThingWriteRequestMapping error) {
            this.bodyFields = bodyFields;
            this.error = error;
        }

        static ParseResult bodyFields(final ApiBodyFields bodyFields) {
            return new ParseResult(bodyFields, null);
        }

        static ParseResult error(final ThingWriteRequestMapping error) {
            return new ParseResult(ApiBodyFields.empty(), error);
        }

        ApiBodyFields bodyFields() {
            return bodyFields;
        }
    }
}
