package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyField;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.application.command.AmendThingCommand;
import uk.co.compendiumdev.thingifier.application.command.BodyFieldValue;
import uk.co.compendiumdev.thingifier.application.command.CreateAndConnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateThingCommand;
import uk.co.compendiumdev.thingifier.application.command.ReplaceThingCommand;
import uk.co.compendiumdev.thingifier.application.schema.EntityTypeRef;
import uk.co.compendiumdev.thingifier.application.schema.SchemaViewCatalog;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public final class ThingBodyCommandMapper {

    private final SchemaViewCatalog schema;

    public ThingBodyCommandMapper(final SchemaViewCatalog schema) {
        this.schema = schema;
    }

    public ThingWriteRequestMapping mapCreate(
            final ApiBodyFields bodyFields,
            final EntityTypeRef entity,
            final boolean validateFinalRelationships) {
        RelationshipBodyCommands relationships = parseRelationships(bodyFields, entity);
        if (!relationships.validationReport().isValid()) {
            return invalidRelationships(relationships.validationReport());
        }

        return ThingWriteRequestMapping.command(
                new CreateThingCommand(
                        entity.name(),
                        "",
                        fieldValuesExcludingRelationships(bodyFields, relationships),
                        bodyFieldValues(bodyFields),
                        relationships.references(),
                        validateFinalRelationships));
    }

    public ThingWriteRequestMapping mapCreateWithPrimaryKey(
            final String primaryKey, final ApiBodyFields bodyFields, final EntityTypeRef entity) {
        RelationshipBodyCommands relationships = parseRelationships(bodyFields, entity);
        if (!relationships.validationReport().isValid()) {
            return invalidRelationships(relationships.validationReport());
        }

        return ThingWriteRequestMapping.command(
                new CreateThingCommand(
                        entity.name(),
                        primaryKey,
                        fieldValuesExcludingRelationships(bodyFields, relationships),
                        bodyFieldValues(bodyFields),
                        relationships.references(),
                        true));
    }

    public ThingWriteRequestMapping mapAmend(
            final ApiBodyFields bodyFields,
            final EntityTypeRef entity,
            final String identifier,
            final boolean replaceExistingFieldsAndRelationships) {
        RelationshipBodyCommands relationships = parseRelationships(bodyFields, entity);
        if (!relationships.validationReport().isValid()) {
            return ThingWriteRequestMapping.error(
                    ApiMappingError.withMessages(
                            400, relationships.validationReport().getErrorMessages()));
        }

        return ThingWriteRequestMapping.command(
                new AmendThingCommand(
                        entity.name(),
                        identifier,
                        fieldValuesExcludingRelationships(bodyFields, relationships),
                        bodyFieldValues(bodyFields),
                        replaceExistingFieldsAndRelationships,
                        relationships.references()));
    }

    public ThingWriteRequestMapping mapPut(
            final ApiBodyFields bodyFields, final EntityTypeRef entity, final String identifier) {
        RelationshipBodyCommands relationships = parseRelationships(bodyFields, entity);
        if (!relationships.validationReport().isValid()) {
            return ThingWriteRequestMapping.error(
                    ApiMappingError.withMessages(
                            400, relationships.validationReport().getErrorMessages()));
        }

        return ThingWriteRequestMapping.command(
                new ReplaceThingCommand(
                        entity.name(),
                        identifier,
                        fieldValuesExcludingRelationships(bodyFields, relationships),
                        bodyFieldValues(bodyFields),
                        relationships.references()));
    }

    public ThingWriteRequestMapping mapCreateAndConnect(
            final ApiBodyFields bodyFields,
            final EntityTypeRef parentEntity,
            final String parentIdentifier,
            final String relationshipName,
            final EntityTypeRef childEntity) {
        RelationshipBodyCommands relationships = parseRelationships(bodyFields, childEntity);
        if (!relationships.validationReport().isValid()) {
            return invalidRelationships(relationships.validationReport());
        }

        return ThingWriteRequestMapping.command(
                new CreateAndConnectRelationshipCommand(
                        parentEntity.name(),
                        parentIdentifier,
                        relationshipName,
                        childEntity.name(),
                        fieldValuesExcludingRelationships(bodyFields, relationships),
                        bodyFieldValues(bodyFields),
                        relationships.references()));
    }

    public RelationshipBodyCommands parseRelationships(
            final ApiBodyFields bodyFields, final EntityTypeRef entity) {
        return new RelationshipBodyCommandParser(schema)
                .parse(bodyFields.asFlattenedStringMap(), entity);
    }

    private ThingWriteRequestMapping invalidRelationships(final ValidationReport validation) {
        return ThingWriteRequestMapping.error(
                ApiMappingError.withMessage(
                        400,
                        String.format(
                                "Invalid relationships: %s",
                                validation.getCombinedErrorMessages())));
    }

    public List<NamedValue> fieldValuesExcludingRelationships(
            final ApiBodyFields bodyFields, final RelationshipBodyCommands relationships) {
        List<Map.Entry<String, String>> flattenedArgs =
                new ArrayList<>(bodyFields.asFlattenedStringMap());
        flattenedArgs.removeAll(relationships.relationshipEntries());
        return FieldValues.fromListMapEntryStringString(flattenedArgs);
    }

    public List<BodyFieldValue> bodyFieldValues(final ApiBodyFields bodyFields) {
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
        return BodyFieldValue.SourceType.SOMETHING_ELSE;
    }
}
