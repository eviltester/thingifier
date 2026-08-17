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

/**
 * Validates and applies JSON Patch and JSON Merge Patch document commands.
 *
 * <p>The handler turns a patch document into a replacement amend command. Keeping this conversion
 * separate from mutation lets lifecycle hooks inspect validation before the patched document is
 * applied to the store.
 */
final class PatchThingDocumentHandler {

    private final ThingDefinitionResolver definitions;
    private final AmendThingHandler amendHandler;
    private final JsonOutputConfig jsonOutput;

    /**
     * Creates the patch document handler.
     *
     * @param definitions resolver for model definitions and instances
     * @param amendHandler handler used to validate and apply the replacement command
     * @param jsonOutput JSON output configuration used to render the current instance before
     *     patching
     */
    PatchThingDocumentHandler(
            final ThingDefinitionResolver definitions,
            final AmendThingHandler amendHandler,
            final JsonOutputConfig jsonOutput) {
        this.definitions = definitions;
        this.amendHandler = amendHandler;
        this.jsonOutput = jsonOutput == null ? new JsonOutputConfig() : jsonOutput;
    }

    /**
     * Validates and applies a patch document command in one call.
     *
     * @param command patch document command to handle
     * @return validation error or successful amend result
     */
    ThingCommandResult handle(final PatchThingDocumentCommand command) {
        ThingCommandResult validationResult = validate(command);
        if (validationResult != null) {
            return validationResult;
        }
        return apply(command);
    }

    /**
     * Validates a patch document without mutating the store.
     *
     * <p>Validation applies the patch to an in-memory representation and validates the equivalent
     * replacement command.
     *
     * @param command patch document command to validate
     * @return validation error, or null when validation succeeds
     */
    ThingCommandResult validate(final PatchThingDocumentCommand command) {
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

        return amendHandler.validate(replacementCommand(command, patchedDocument));
    }

    /**
     * Applies a patch document after validation.
     *
     * @param command validated patch document command
     * @return command result from applying the equivalent amend command
     */
    ThingCommandResult apply(final PatchThingDocumentCommand command) {
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

        return amendHandler.apply(replacementCommand(command, patchedDocument));
    }

    /**
     * Parses the raw patch document into JSON.
     *
     * @param command patch document command
     * @return parsed document, or null when parsing fails
     */
    private JsonNode parsePatchDocument(final PatchThingDocumentCommand command) {
        try {
            return JsonBodyValueConverter.readTree(command.getRawDocument());
        } catch (JsonProcessingException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Creates the style-specific malformed patch error.
     *
     * @param command patch document command
     * @return command result containing the malformed document error
     */
    private ThingCommandResult malformedPatch(final PatchThingDocumentCommand command) {
        if (command.getStyle()
                == PatchThingDocumentCommand.DocumentStyle.JSON_MERGE_PATCH_RFC7396) {
            return ThingCommandResult.error(
                    ApplicationError.badRequest("Malformed JSON Merge Patch document"));
        }
        return ThingCommandResult.error(
                ApplicationError.badRequest("Malformed JSON Patch document"));
    }

    /**
     * Validates the top-level shape required by the selected patch document style.
     *
     * @param command patch document command
     * @param patchDocument parsed patch document
     * @return validation error, or null when the shape is valid
     */
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

    /**
     * Applies the parsed patch document to the current instance JSON.
     *
     * @param command patch document command
     * @param instance current entity instance
     * @param patchDocument parsed patch document
     * @return patched JSON document, or null when JSON Patch application fails
     */
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

    /**
     * Applies RFC 7396 merge patch semantics to a JSON target.
     *
     * @param target current JSON target
     * @param patch merge patch document
     * @return merged JSON document
     */
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

    /**
     * Renders an entity instance as JSON so patch documents can be applied in memory.
     *
     * @param instance current entity instance
     * @return JSON representation of the instance
     */
    private JsonNode jsonFor(final EntityInstance instance) {
        try {
            return JsonBodyValueConverter.readTree(
                    new JsonThing(jsonOutput).asJsonObject(instance).toString());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not render entity instance as JSON", e);
        }
    }

    /**
     * Builds the amend command represented by a successfully patched document.
     *
     * @param command original patch document command
     * @param patchedDocument patched JSON object
     * @return amend command replacing fields from the patched document
     */
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

    /**
     * Converts parsed body fields into command named values.
     *
     * @param bodyFields parsed body fields
     * @return flattened named values
     */
    private List<NamedValue> fieldValues(final ApiBodyFields bodyFields) {
        List<NamedValue> values = new ArrayList<>();
        for (Map.Entry<String, String> entry : bodyFields.asFlattenedStringMap()) {
            values.add(new NamedValue(entry.getKey(), entry.getValue()));
        }
        return values;
    }

    /**
     * Converts parsed body fields into typed command body field values.
     *
     * @param bodyFields parsed body fields
     * @return top-level typed body values
     */
    private List<BodyFieldValue> bodyFieldValues(final ApiBodyFields bodyFields) {
        List<BodyFieldValue> values = new ArrayList<>();
        for (ApiBodyField field : bodyFields.topLevelFields()) {
            values.add(
                    new BodyFieldValue(
                            field.name(), field.value(), sourceTypeFor(field.sourceType())));
        }
        return values;
    }

    /**
     * Maps parser source type names to command source type values.
     *
     * @param sourceType source type name from parsed body fields
     * @return command source type
     */
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
