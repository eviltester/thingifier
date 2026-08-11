package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.InstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.JsonBodyValueConverter;
import uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle;
import uk.co.compendiumdev.thingifier.application.command.PatchThingDocumentCommand;

public final class EntityPatchDocumentMapper {

    private final ThingWriteRequestMapper writeMapper;

    public EntityPatchDocumentMapper(final SchemaCatalog schema) {
        this.writeMapper = new ThingWriteRequestMapper(schema);
    }

    public ThingWriteRequestMapping map(
            final EntityPatchUpdateStyle style, final ThingRoute route, final String rawBody) {
        if (!(route instanceof InstanceRoute)) {
            return writeMapper.mapPatch(route, ApiBodyFields.empty());
        }

        InstanceRoute instanceRoute = (InstanceRoute) route;
        if (style == EntityPatchUpdateStyle.PARTIAL_JSON_UPDATE) {
            return mapPartialJsonUpdate(instanceRoute, rawBody);
        }
        if (style == EntityPatchUpdateStyle.JSON_MERGE_PATCH_RFC7396) {
            return mapJsonDocumentPatch(
                    instanceRoute,
                    rawBody,
                    PatchThingDocumentCommand.DocumentStyle.JSON_MERGE_PATCH_RFC7396);
        }
        if (style == EntityPatchUpdateStyle.JSON_PATCH_RFC6902) {
            return mapJsonDocumentPatch(
                    instanceRoute,
                    rawBody,
                    PatchThingDocumentCommand.DocumentStyle.JSON_PATCH_RFC6902);
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

    private ThingWriteRequestMapping mapJsonDocumentPatch(
            final InstanceRoute route,
            final String rawBody,
            final PatchThingDocumentCommand.DocumentStyle style) {
        return ThingWriteRequestMapping.command(
                new PatchThingDocumentCommand(
                        route.entity().name(), route.identifier(), rawBody, style),
                ApiRouteDisplay.missingInstanceMessage(
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
            document = JsonBodyValueConverter.readTree(body);
        } catch (JsonProcessingException e) {
            return ParseResult.error(malformedPatch("Malformed JSON document"));
        }

        if (document == null || !document.isObject()) {
            return ParseResult.error(
                    malformedPatch("PATCH partial JSON update document must be an object"));
        }

        return ParseResult.bodyFields(
                ApiBodyFields.fromMap(JsonBodyValueConverter.objectNodeAsMap(document)));
    }

    private ThingWriteRequestMapping malformedPatch(final String message) {
        return ThingWriteRequestMapping.error(ApiMappingError.withMessage(400, message));
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
