package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

import java.util.List;
import java.util.Map;

// TODO - there should be a generic API handlers package that does create, read, update, delete
// which I think this is, and they should not use http status codes
// also cloning instances feels like the wrong way to amend, we should create 'amend/patch' requests
// would also handle relationship

public class ThingAmendment {

    private final Thingifier thingifier;

    public ThingAmendment(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    public ApiResponse amendInstance(final BodyParser bodyargs, final EntityInstance instance,
                                     final Boolean clearFieldsBeforeSettingFromArgs, final String database) {

        Map<String, String> args = bodyargs.getStringMap();

        if(thingifier.apiConfig().willApiEnforceDeclaredTypesInInput()) {
            List<String> doNotValidateFields = instance.getEntity().getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID);
            ValidationReport validatedTypes = bodyargs.validateAgainstTypeIgnoring(instance.getEntity(), doNotValidateFields);
            if(!validatedTypes.isValid()){
                return ApiResponse.error(400, validatedTypes.getCombinedErrorMessages());
            }
        }

        EntityInstanceDraft draft;

        try {
            List<NamedValue> fieldValues = FieldValues.
                                        fromListMapEntryStringString(
                                                new BodyArgsProcessor(thingifier, bodyargs).
                                                        removeRelationshipsFrom(instance, database));

            draft = new EntityInstanceBulkUpdater(instance).setFieldValuesFrom(fieldValues);

        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }

        // validate the relationships as well
        ValidationReport validation = new BodyRelationshipValidator(thingifier).
                validate(bodyargs, instance.getEntity(), database);

        if (validation.isValid()) {
            final EntityInstance updated;
            try {
                if(clearFieldsBeforeSettingFromArgs){
                    updated = thingifier.getRepository(database).replaceInstance(instance, draft);
                    // delete all existing relationships for idempotent amend
                    // todo: this returns a list of 'items' to be removed based on relationship
                    thingifier.getRepository(database).removeAllRelationships(updated);
                } else {
                    updated = thingifier.getRepository(database).patchInstance(instance, draft);
                }
            } catch (Exception e) {
                return ApiResponse.error(400, e.getMessage());
            }

            // todo: should we check that this was actually a success?
            final ApiResponse relresponse = new RelationshipCreator(thingifier).createRelationships(bodyargs, updated, database);
            // todo: should check if any of the 'removed items due to relationship removal' need to be removed
            // and remove them if we do
            return ApiResponse.success().returnSingleInstance(updated);
        } else {
            // do not add it, report the errors
            return ApiResponse.error(400, validation.getErrorMessages());
        }

    }
}
