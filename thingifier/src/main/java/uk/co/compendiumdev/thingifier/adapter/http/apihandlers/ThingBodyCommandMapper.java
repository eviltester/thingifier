package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.EntityInstanceDraftBuilder;
import uk.co.compendiumdev.thingifier.application.command.AmendThingCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateAndConnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateThingCommand;
import uk.co.compendiumdev.thingifier.application.command.PutThingCommand;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public final class ThingBodyCommandMapper {

    private final SchemaCatalog schema;
    private final ThingifierApiConfig apiConfig;
    private final ThingStore store;

    public ThingBodyCommandMapper(
            final SchemaCatalog schema,
            final ThingifierApiConfig apiConfig,
            final ThingStore store) {
        this.schema = schema;
        this.apiConfig = apiConfig;
        this.store = store;
    }

    public ThingWriteRequestMapping mapCreate(
            final ApiBodyFields bodyFields,
            final EntityDefinition entity,
            final boolean validateFinalRelationships) {
        RelationshipBodyCommands relationships = parseRelationships(bodyFields, entity);
        if (!relationships.validationReport().isValid()) {
            return invalidRelationships(relationships.validationReport());
        }

        ValidationReport validated = new BodyCreationValidator().validate(bodyFields, entity);
        if (!validated.isValid()) {
            return ThingWriteRequestMapping.error(
                    ApiMappingError.withMessage(
                            400,
                            String.format(
                                    "Invalid Creation: %s", validated.getCombinedErrorMessages())));
        }

        return mapCreateFromValidatedBody(
                bodyFields,
                entity,
                EntityInstanceDraft.forEntity(entity),
                relationships,
                validateFinalRelationships);
    }

    public ThingWriteRequestMapping mapCreateWithPrimaryKey(
            final String primaryKey,
            final ApiBodyFields bodyFields,
            final EntityDefinition entity) {
        ValidationReport validated =
                new BodyCreationValidator()
                        .areFieldsUnique(
                                bodyFields,
                                entity,
                                store.entityQueries(),
                                entity.getFieldNamesOfType(
                                        FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID));
        if (!validated.isValid()) {
            return ThingWriteRequestMapping.error(
                    ApiMappingError.withMessage(
                            409,
                            "Cannot Create with duplicate values: "
                                    + validated.getCombinedErrorMessages()));
        }

        EntityInstanceDraft baseDraft = EntityInstanceDraft.forEntity(entity);
        if (isProtectedField(entity.getPrimaryKeyField().getType())) {
            baseDraft.withProtectedField(entity.getPrimaryKeyField().getName(), primaryKey);
        } else {
            baseDraft.withField(entity.getPrimaryKeyField().getName(), primaryKey);
        }

        RelationshipBodyCommands relationships = parseRelationships(bodyFields, entity);
        if (!relationships.validationReport().isValid()) {
            return invalidRelationships(relationships.validationReport());
        }

        List<NamedValue> fieldValues =
                FieldValues.fromListMapEntryStringString(bodyFields.asFlattenedStringMap());
        store.administration().accommodateProtectedIds(entity, fieldValues);

        if (apiConfig.willApiEnforceDeclaredTypesInInput()) {
            ValidationReport validatedTypes = bodyFields.validateAgainstType(entity);
            if (!validatedTypes.isValid()) {
                return ThingWriteRequestMapping.error(
                        ApiMappingError.withMessage(
                                400, validatedTypes.getCombinedErrorMessages()));
            }
        }

        try {
            List<String> ignoreFields = entity.getFieldNamesOfType(FieldType.AUTO_GUID);
            EntityInstanceDraft draft =
                    new EntityInstanceDraftBuilder(entity)
                            .overrideFieldValuesFromArgsIgnoring(
                                    FieldValues.fromListMapEntryStringString(
                                            fieldValuesExcludingRelationships(
                                                    bodyFields, relationships)),
                                    ignoreFields);
            copyBaseDraftValues(baseDraft, draft);
            return ThingWriteRequestMapping.command(
                    new CreateThingCommand(draft, relationships.references(), true));
        } catch (Exception e) {
            return ThingWriteRequestMapping.error(ApiMappingError.withMessage(400, e.getMessage()));
        }
    }

    public ThingWriteRequestMapping mapAmend(
            final ApiBodyFields bodyFields,
            final EntityDefinition entity,
            final String identifier,
            final boolean replaceExistingFieldsAndRelationships) {
        return mapAmend(bodyFields, entity, identifier, replaceExistingFieldsAndRelationships, "");
    }

    public ThingWriteRequestMapping mapAmend(
            final ApiBodyFields bodyFields,
            final EntityDefinition entity,
            final String identifier,
            final boolean replaceExistingFieldsAndRelationships,
            final String missingInstanceMessage) {
        if (apiConfig.willApiEnforceDeclaredTypesInInput()) {
            List<String> doNotValidateFields =
                    entity.getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID);
            ValidationReport validatedTypes =
                    bodyFields.validateAgainstTypeIgnoring(entity, doNotValidateFields);
            if (!validatedTypes.isValid()) {
                return ThingWriteRequestMapping.error(
                        ApiMappingError.withMessage(
                                400, validatedTypes.getCombinedErrorMessages()));
            }
        }

        RelationshipBodyCommands relationships = parseRelationships(bodyFields, entity);
        List<NamedValue> fieldValues =
                FieldValues.fromListMapEntryStringString(
                        fieldValuesExcludingRelationships(bodyFields, relationships));

        if (!relationships.validationReport().isValid()) {
            return ThingWriteRequestMapping.error(
                    ApiMappingError.withMessages(
                            400, relationships.validationReport().getErrorMessages()));
        }

        return ThingWriteRequestMapping.command(
                new AmendThingCommand(
                        entity,
                        identifier,
                        fieldValues,
                        replaceExistingFieldsAndRelationships,
                        relationships.references(),
                        missingInstanceMessage));
    }

    public ThingWriteRequestMapping mapPut(
            final ApiBodyFields bodyFields,
            final EntityDefinition entity,
            final String identifier) {
        if (apiConfig.willApiEnforceDeclaredTypesInInput()) {
            List<String> doNotValidateFields =
                    entity.getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID);
            ValidationReport validatedTypes =
                    bodyFields.validateAgainstTypeIgnoring(entity, doNotValidateFields);
            if (!validatedTypes.isValid()) {
                return ThingWriteRequestMapping.error(
                        ApiMappingError.withMessage(
                                400, validatedTypes.getCombinedErrorMessages()));
            }
        }

        RelationshipBodyCommands relationships = parseRelationships(bodyFields, entity);
        List<NamedValue> fieldValues =
                FieldValues.fromListMapEntryStringString(
                        fieldValuesExcludingRelationships(bodyFields, relationships));

        if (!relationships.validationReport().isValid()) {
            return ThingWriteRequestMapping.error(
                    ApiMappingError.withMessages(
                            400, relationships.validationReport().getErrorMessages()));
        }

        return ThingWriteRequestMapping.command(
                new PutThingCommand(entity, identifier, fieldValues, relationships.references()));
    }

    public ThingWriteRequestMapping mapCreateAndConnect(
            final ApiBodyFields bodyFields,
            final EntityDefinition parentEntity,
            final String parentIdentifier,
            final String relationshipName,
            final EntityDefinition childEntity,
            final String routeDisplay) {
        ThingWriteRequestMapping createMapping = mapCreate(bodyFields, childEntity, false);
        if (createMapping.isError()) {
            return createMapping;
        }

        CreateThingCommand create = (CreateThingCommand) createMapping.getCommand();
        return ThingWriteRequestMapping.command(
                new CreateAndConnectRelationshipCommand(
                        parentEntity,
                        parentIdentifier,
                        relationshipName,
                        create.getDraft(),
                        create.getRelationships(),
                        routeDisplay));
    }

    private ThingWriteRequestMapping mapCreateFromValidatedBody(
            final ApiBodyFields bodyFields,
            final EntityDefinition entity,
            final EntityInstanceDraft baseDraft,
            final RelationshipBodyCommands relationships,
            final boolean validateFinalRelationships) {
        if (apiConfig.willApiEnforceDeclaredTypesInInput()) {
            ValidationReport validatedTypes = bodyFields.validateAgainstType(entity);
            if (!validatedTypes.isValid()) {
                return ThingWriteRequestMapping.error(
                        ApiMappingError.withMessage(
                                400, validatedTypes.getCombinedErrorMessages()));
            }
        }

        try {
            List<NamedValue> fieldValues =
                    FieldValues.fromListMapEntryStringString(
                            fieldValuesExcludingRelationships(bodyFields, relationships));
            EntityInstanceDraft draft =
                    new EntityInstanceDraftBuilder(entity).setFieldValuesFrom(fieldValues);
            copyBaseDraftValues(baseDraft, draft);
            return ThingWriteRequestMapping.command(
                    new CreateThingCommand(
                            draft, relationships.references(), validateFinalRelationships));
        } catch (Exception e) {
            return ThingWriteRequestMapping.error(ApiMappingError.withMessage(400, e.getMessage()));
        }
    }

    private RelationshipBodyCommands parseRelationships(
            final ApiBodyFields bodyFields, final EntityDefinition entity) {
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

    private List<Map.Entry<String, String>> fieldValuesExcludingRelationships(
            final ApiBodyFields bodyFields, final RelationshipBodyCommands relationships) {
        List<Map.Entry<String, String>> flattenedArgs =
                new ArrayList<>(bodyFields.asFlattenedStringMap());
        flattenedArgs.removeAll(relationships.relationshipEntries());
        return flattenedArgs;
    }

    private void copyBaseDraftValues(
            final EntityInstanceDraft baseDraft, final EntityInstanceDraft draft) {
        for (NamedValue value : baseDraft.getFieldValues()) {
            draft.withField(value.getName(), value.asString());
        }
        for (NamedValue protectedValue : baseDraft.getProtectedFieldValues()) {
            draft.withProtectedField(protectedValue.getName(), protectedValue.asString());
        }
    }

    private boolean isProtectedField(final FieldType fieldType) {
        return fieldType == FieldType.AUTO_INCREMENT || fieldType == FieldType.AUTO_GUID;
    }
}
