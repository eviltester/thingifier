package uk.co.compendiumdev.thingifier.api.restapihandlers;

import java.util.List;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public class ThingCreation {

    private final Thingifier thingifier;

    public ThingCreation(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    public ApiResponse with(
            final BodyParser bodyargs, final EntityDefinition thing, final String database) {

        ValidationReport validated =
                new BodyRelationshipValidator(thingifier).validate(bodyargs, thing, database);

        if (!validated.isValid()) {
            return ApiResponse.error(
                    400,
                    String.format(
                            "Invalid relationships: %s", validated.getCombinedErrorMessages()));
        }

        validated = new BodyCreationValidator(thingifier).validate(bodyargs, thing);
        if (!validated.isValid()) {
            return ApiResponse.error(
                    400,
                    String.format("Invalid Creation: %s", validated.getCombinedErrorMessages()));
        }

        return addNewThingWithFields(
                bodyargs, EntityInstanceDraft.forEntity(thing), thing, database);
    }

    // create with GUID and IDs is normally associated with PUT or 'insert'
    public ApiResponse withPrimaryKey(
            final String primaryKey,
            final BodyParser bodyargs,
            final EntityDefinition thing,
            final String database) {

        ValidationReport validated;

        validated =
                new BodyCreationValidator(thingifier)
                        .areFieldsUnique(
                                bodyargs,
                                thing,
                                thingifier.getRepository(database),
                                thing.getFieldNamesOfType(
                                        FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID));
        if (!validated.isValid()) {
            return ApiResponse.error(
                    409,
                    "Cannot Create with duplicate values: " + validated.getCombinedErrorMessages());
        }

        EntityInstanceDraft draft = EntityInstanceDraft.forEntity(thing);
        if (isProtectedField(thing.getPrimaryKeyField().getType())) {
            draft.withProtectedField(thing.getPrimaryKeyField().getName(), primaryKey);
        } else {
            draft.withField(thing.getPrimaryKeyField().getName(), primaryKey);
        }

        validated = new BodyRelationshipValidator(thingifier).validate(bodyargs, thing, database);

        if (!validated.isValid()) {
            return ApiResponse.error(
                    400,
                    String.format(
                            "Invalid relationships: %s", validated.getCombinedErrorMessages()));
        }

        // any next id counts should be set higher than the ids mentioned in here
        List<NamedValue> fieldValues =
                FieldValues.fromListMapEntryStringString(bodyargs.getFlattenedStringMap());

        thingifier.getRepository(database).setNextIdCountersToAccomodate(thing, fieldValues);

        return insertNewThingWithFields(bodyargs, draft, thing, database);
    }

    private ApiResponse addNewThingWithFields(
            final BodyParser bodyargs,
            final EntityInstanceDraft baseDraft,
            final EntityDefinition thing,
            final String database) {

        if (thingifier.apiConfig().willApiEnforceDeclaredTypesInInput()) {
            ValidationReport validatedTypes = bodyargs.validateAgainstType(thing);
            if (!validatedTypes.isValid()) {
                return ApiResponse.error(400, validatedTypes.getCombinedErrorMessages());
            }
        }

        // todo: need to separate relationships from field in the path prior to sending through to
        // setFields
        EntityInstanceDraft draft;

        try {
            // if any guids or ids then throw an error if they are not the same
            List<NamedValue> fieldValues =
                    FieldValues.fromListMapEntryStringString(
                            new BodyArgsProcessor(thingifier, bodyargs)
                                    .removeRelationshipsFrom(thing, database));

            draft = new EntityInstanceBulkUpdater(thing).setFieldValuesFrom(fieldValues);
            copyBaseDraftValues(baseDraft, draft);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }

        return addValidatedInstance(bodyargs, draft, thing, database);
    }

    private ApiResponse insertNewThingWithFields(
            final BodyParser bodyargs,
            final EntityInstanceDraft baseDraft,
            final EntityDefinition thing,
            final String database) {

        if (thingifier.apiConfig().willApiEnforceDeclaredTypesInInput()) {
            ValidationReport validatedTypes = bodyargs.validateAgainstType(thing);
            if (!validatedTypes.isValid()) {
                return ApiResponse.error(400, validatedTypes.getCombinedErrorMessages());
            }
        }

        EntityInstanceDraft draft;
        try {
            // set all the fields and values, except guids
            List<String> ignoreFields = thing.getFieldNamesOfType(FieldType.AUTO_GUID);

            List<NamedValue> fieldValues =
                    FieldValues.fromListMapEntryStringString(
                            new BodyArgsProcessor(thingifier, bodyargs)
                                    .removeRelationshipsFrom(thing, database));

            draft =
                    new EntityInstanceBulkUpdater(thing)
                            .overrideFieldValuesFromArgsIgnoring(fieldValues, ignoreFields);
            copyBaseDraftValues(baseDraft, draft);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }

        return addValidatedInstance(bodyargs, draft, thing, database);
    }

    private ApiResponse addValidatedInstance(
            BodyParser bodyargs,
            EntityInstanceDraft draft,
            EntityDefinition thing,
            String database) {

        try {
            EntityInstance instance = thingifier.getRepository(database).createInstance(draft);
            return new RelationshipCreator(thingifier)
                    .createRelationships(bodyargs, instance, database);
        } catch (Exception e) {
            return ApiResponse.error(400, creationErrorMessage(e));
        }
    }

    private String creationErrorMessage(final Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return "";
        }

        String validationPrefix = "Failed Validation: ";
        if (message.startsWith(validationPrefix)
                && message.substring(validationPrefix.length()).endsWith(" : field is mandatory")
                && !message.substring(validationPrefix.length()).contains(", ")) {
            return message.substring(validationPrefix.length());
        }
        return message;
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
