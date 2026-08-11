package uk.co.compendiumdev.thingifier.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.JsonPatch;
import com.flipkart.zjsonpatch.JsonPatchApplicationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyField;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.JsonBodyValueConverter;
import uk.co.compendiumdev.thingifier.apiconfig.JsonOutputConfig;
import uk.co.compendiumdev.thingifier.application.command.AmendThingCommand;
import uk.co.compendiumdev.thingifier.application.command.BodyFieldValue;
import uk.co.compendiumdev.thingifier.application.command.PatchThingDocumentCommand;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

final class PatchThingDocumentHandler {

    private final ThingDefinitionResolver definitions;
    private final AmendThingHandler amendHandler;
    private final JsonOutputConfig jsonOutput;

    PatchThingDocumentHandler(
            final ThingDefinitionResolver definitions,
            final AmendThingHandler amendHandler,
            final JsonOutputConfig jsonOutput) {
        this.definitions = definitions;
        this.amendHandler = amendHandler;
        this.jsonOutput = jsonOutput == null ? new JsonOutputConfig() : jsonOutput;
    }

    ThingCommandResult handle(final PatchThingDocumentCommand command) {
        EntityDefinition entity = definitions.entityNamed(command.getEntityName());
        EntityInstance instance = definitions.resolveInstance(entity, command.getIdentifier());
        if (instance == null) {
            return ThingCommandResult.error(
                    ApplicationError.instanceNotFound(
                            command.getEntityName(), command.getIdentifier()));
        }

        JsonNode patchDocument = parsePatchDocument(command);
        if (patchDocument == null) {
            return malformedPatch(command);
        }

        ThingCommandResult shapeValidation = validateDocumentShape(command, patchDocument);
        if (shapeValidation != null) {
            return shapeValidation;
        }

        JsonNode patchedDocument = patchedDocument(command, instance, patchDocument);
        if (patchedDocument == null) {
            return ThingCommandResult.error(
                    ApplicationError.conflict("JSON Patch could not be applied"));
        }
        if (!patchedDocument.isObject()) {
            return ThingCommandResult.error(ApplicationError.patchResultNotObject());
        }

        return amendHandler.handle(replacementCommand(command, patchedDocument));
    }

    private JsonNode parsePatchDocument(final PatchThingDocumentCommand command) {
        try {
            return JsonBodyValueConverter.readTree(command.getRawDocument());
        } catch (JsonProcessingException | IllegalArgumentException e) {
            return null;
        }
    }

    private ThingCommandResult malformedPatch(final PatchThingDocumentCommand command) {
        if (command.getStyle()
                == PatchThingDocumentCommand.DocumentStyle.JSON_MERGE_PATCH_RFC7396) {
            return ThingCommandResult.error(
                    ApplicationError.badRequest("Malformed JSON Merge Patch document"));
        }
        return ThingCommandResult.error(
                ApplicationError.badRequest("Malformed JSON Patch document"));
    }

    private ThingCommandResult validateDocumentShape(
            final PatchThingDocumentCommand command, final JsonNode patchDocument) {
        if (command.getStyle()
                == PatchThingDocumentCommand.DocumentStyle.JSON_MERGE_PATCH_RFC7396) {
            if (!patchDocument.isObject()) {
                return ThingCommandResult.error(
                        ApplicationError.validation(
                                "JSON Merge Patch for entity resources must be an object"));
            }
            return null;
        }

        if (!patchDocument.isArray()) {
            return ThingCommandResult.error(
                    ApplicationError.badRequest(
                            "JSON Patch document must be an array of operations"));
        }
        return null;
    }

    private JsonNode patchedDocument(
            final PatchThingDocumentCommand command,
            final EntityInstance instance,
            final JsonNode patchDocument) {
        JsonNode currentDocument = jsonFor(instance);
        if (command.getStyle()
                == PatchThingDocumentCommand.DocumentStyle.JSON_MERGE_PATCH_RFC7396) {
            return applyMergePatch(currentDocument, patchDocument);
        }

        try {
            return JsonPatch.apply(patchDocument, currentDocument);
        } catch (JsonPatchApplicationException | IllegalArgumentException e) {
            return null;
        }
    }

    private JsonNode applyMergePatch(final JsonNode target, final JsonNode patch) {
        if (!patch.isObject()) {
            return patch;
        }

        ObjectNode result =
                target != null && target.isObject()
                        ? ((ObjectNode) target).deepCopy()
                        : JsonNodeFactory.instance.objectNode();
        for (Map.Entry<String, JsonNode> entry : patch.properties()) {
            if (entry.getValue().isNull()) {
                result.remove(entry.getKey());
            } else {
                result.set(
                        entry.getKey(),
                        applyMergePatch(result.get(entry.getKey()), entry.getValue()));
            }
        }
        return result;
    }

    private JsonNode jsonFor(final EntityInstance instance) {
        try {
            return JsonBodyValueConverter.readTree(
                    new JsonThing(jsonOutput).asJsonObject(instance).toString());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not render entity instance as JSON", e);
        }
    }

    private AmendThingCommand replacementCommand(
            final PatchThingDocumentCommand command, final JsonNode patchedDocument) {
        ApiBodyFields bodyFields =
                ApiBodyFields.fromMap(JsonBodyValueConverter.objectNodeAsMap(patchedDocument));
        return new AmendThingCommand(
                command.getEntityName(),
                command.getIdentifier(),
                fieldValues(bodyFields),
                bodyFieldValues(bodyFields),
                true,
                false,
                List.of());
    }

    private List<NamedValue> fieldValues(final ApiBodyFields bodyFields) {
        List<NamedValue> values = new ArrayList<>();
        for (Map.Entry<String, String> entry : bodyFields.asFlattenedStringMap()) {
            values.add(new NamedValue(entry.getKey(), entry.getValue()));
        }
        return values;
    }

    private List<BodyFieldValue> bodyFieldValues(final ApiBodyFields bodyFields) {
        List<BodyFieldValue> values = new ArrayList<>();
        for (ApiBodyField field : bodyFields.topLevelFields()) {
            values.add(
                    new BodyFieldValue(
                            field.name(), field.value(), sourceTypeFor(field.sourceType())));
        }
        return values;
    }

    private BodyFieldValue.SourceType sourceTypeFor(final String sourceType) {
        if ("STRING".equals(sourceType)) {
            return BodyFieldValue.SourceType.STRING;
        }
        if ("BOOLEAN".equals(sourceType)) {
            return BodyFieldValue.SourceType.BOOLEAN;
        }
        if ("INTEGER".equals(sourceType)) {
            return BodyFieldValue.SourceType.INTEGER;
        }
        if ("NUMERIC".equals(sourceType)) {
            return BodyFieldValue.SourceType.NUMERIC;
        }
        if ("OBJECT".equals(sourceType)) {
            return BodyFieldValue.SourceType.OBJECT;
        }
        if ("ARRAY".equals(sourceType)) {
            return BodyFieldValue.SourceType.ARRAY;
        }
        if ("NULL".equals(sourceType)) {
            return BodyFieldValue.SourceType.NULL;
        }
        return BodyFieldValue.SourceType.SOMETHING_ELSE;
    }
}
