package uk.co.compendiumdev.thingifier.api.restapihandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.command.AmendThingCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateAndConnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateThingCommand;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public final class ThingBodyCommandMapper {

    private final Thingifier thingifier;
    private final String database;

    public ThingBodyCommandMapper(final Thingifier thingifier, final String database) {
        this.thingifier = thingifier;
        this.database = database;
    }

    public ThingWriteRequestMapping mapCreate(
            final BodyParser bodyargs,
            final EntityDefinition entity,
            final boolean validateFinalRelationships) {
        RelationshipBodyCommands relationships = parseRelationships(bodyargs, entity);
        if (!relationships.validationReport().isValid()) {
            return invalidRelationships(relationships.validationReport());
        }

        ValidationReport validated = new BodyCreationValidator().validate(bodyargs, entity);
        if (!validated.isValid()) {
            return ThingWriteRequestMapping.error(
                    ApiResponse.error(
                            400,
                            String.format(
                                    "Invalid Creation: %s", validated.getCombinedErrorMessages())));
        }

        return mapCreateFromValidatedBody(
                bodyargs,
                entity,
                EntityInstanceDraft.forEntity(entity),
                relationships,
                validateFinalRelationships);
    }

    public ThingWriteRequestMapping mapCreateWithPrimaryKey(
            final String primaryKey, final BodyParser bodyargs, final EntityDefinition entity) {
        ValidationReport validated =
                new BodyCreationValidator()
                        .areFieldsUnique(
                                bodyargs,
                                entity,
                                thingifier.getStore(database).entityQueries(),
                                entity.getFieldNamesOfType(
                                        FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID));
        if (!validated.isValid()) {
            return ThingWriteRequestMapping.error(
                    ApiResponse.error(
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

        RelationshipBodyCommands relationships = parseRelationships(bodyargs, entity);
        if (!relationships.validationReport().isValid()) {
            return invalidRelationships(relationships.validationReport());
        }

        List<NamedValue> fieldValues =
                FieldValues.fromListMapEntryStringString(bodyargs.getFlattenedStringMap());
        thingifier.getStore(database).administration().accommodateProtectedIds(entity, fieldValues);

        if (thingifier.apiConfig().willApiEnforceDeclaredTypesInInput()) {
            ValidationReport validatedTypes = bodyargs.validateAgainstType(entity);
            if (!validatedTypes.isValid()) {
                return ThingWriteRequestMapping.error(
                        ApiResponse.error(400, validatedTypes.getCombinedErrorMessages()));
            }
        }

        try {
            List<String> ignoreFields = entity.getFieldNamesOfType(FieldType.AUTO_GUID);
            EntityInstanceDraft draft =
                    new EntityInstanceBulkUpdater(entity)
                            .overrideFieldValuesFromArgsIgnoring(
                                    FieldValues.fromListMapEntryStringString(
                                            fieldValuesExcludingRelationships(
                                                    bodyargs, relationships)),
                                    ignoreFields);
            copyBaseDraftValues(baseDraft, draft);
            return ThingWriteRequestMapping.command(
                    new CreateThingCommand(draft, relationships.connections(), true));
        } catch (Exception e) {
            return ThingWriteRequestMapping.error(ApiResponse.error(400, e.getMessage()));
        }
    }

    public ThingWriteRequestMapping mapAmend(
            final BodyParser bodyargs,
            final EntityInstance instance,
            final boolean replaceExistingFieldsAndRelationships) {
        bodyargs.getMap();

        if (thingifier.apiConfig().willApiEnforceDeclaredTypesInInput()) {
            List<String> doNotValidateFields =
                    instance.getEntity()
                            .getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID);
            ValidationReport validatedTypes =
                    bodyargs.validateAgainstTypeIgnoring(instance.getEntity(), doNotValidateFields);
            if (!validatedTypes.isValid()) {
                return ThingWriteRequestMapping.error(
                        ApiResponse.error(400, validatedTypes.getCombinedErrorMessages()));
            }
        }

        RelationshipBodyCommands relationships = parseRelationships(bodyargs, instance.getEntity());
        EntityInstanceDraft draft;
        try {
            List<NamedValue> fieldValues =
                    FieldValues.fromListMapEntryStringString(
                            fieldValuesExcludingRelationships(bodyargs, relationships));
            draft = new EntityInstanceBulkUpdater(instance).setFieldValuesFrom(fieldValues);
        } catch (Exception e) {
            return ThingWriteRequestMapping.error(ApiResponse.error(400, e.getMessage()));
        }

        if (!relationships.validationReport().isValid()) {
            return ThingWriteRequestMapping.error(
                    ApiResponse.error(400, relationships.validationReport().getErrorMessages()));
        }

        return ThingWriteRequestMapping.command(
                new AmendThingCommand(
                        instance,
                        draft,
                        replaceExistingFieldsAndRelationships,
                        relationships.connections()));
    }

    public ThingWriteRequestMapping mapCreateAndConnect(
            final BodyParser bodyargs,
            final EntityInstance parent,
            final String relationshipName,
            final EntityDefinition childEntity) {
        ThingWriteRequestMapping createMapping = mapCreate(bodyargs, childEntity, false);
        if (createMapping.isError()) {
            return createMapping;
        }

        CreateThingCommand create = (CreateThingCommand) createMapping.getCommand();
        return ThingWriteRequestMapping.command(
                new CreateAndConnectRelationshipCommand(
                        parent, relationshipName, create.getDraft(), create.getRelationships()));
    }

    private ThingWriteRequestMapping mapCreateFromValidatedBody(
            final BodyParser bodyargs,
            final EntityDefinition entity,
            final EntityInstanceDraft baseDraft,
            final RelationshipBodyCommands relationships,
            final boolean validateFinalRelationships) {
        if (thingifier.apiConfig().willApiEnforceDeclaredTypesInInput()) {
            ValidationReport validatedTypes = bodyargs.validateAgainstType(entity);
            if (!validatedTypes.isValid()) {
                return ThingWriteRequestMapping.error(
                        ApiResponse.error(400, validatedTypes.getCombinedErrorMessages()));
            }
        }

        try {
            List<NamedValue> fieldValues =
                    FieldValues.fromListMapEntryStringString(
                            fieldValuesExcludingRelationships(bodyargs, relationships));
            EntityInstanceDraft draft =
                    new EntityInstanceBulkUpdater(entity).setFieldValuesFrom(fieldValues);
            copyBaseDraftValues(baseDraft, draft);
            return ThingWriteRequestMapping.command(
                    new CreateThingCommand(
                            draft, relationships.connections(), validateFinalRelationships));
        } catch (Exception e) {
            return ThingWriteRequestMapping.error(ApiResponse.error(400, e.getMessage()));
        }
    }

    private RelationshipBodyCommands parseRelationships(
            final BodyParser bodyargs, final EntityDefinition entity) {
        return new RelationshipBodyCommandParser(thingifier).parse(bodyargs, entity, database);
    }

    private ThingWriteRequestMapping invalidRelationships(final ValidationReport validation) {
        return ThingWriteRequestMapping.error(
                ApiResponse.error(
                        400,
                        String.format(
                                "Invalid relationships: %s",
                                validation.getCombinedErrorMessages())));
    }

    private List<Map.Entry<String, String>> fieldValuesExcludingRelationships(
            final BodyParser bodyargs, final RelationshipBodyCommands relationships) {
        List<Map.Entry<String, String>> flattenedArgs =
                new ArrayList<>(bodyargs.getFlattenedStringMap());
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
