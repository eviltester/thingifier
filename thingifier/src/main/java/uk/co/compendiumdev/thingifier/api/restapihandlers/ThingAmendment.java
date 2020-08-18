package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

import java.util.List;
import java.util.Map;

public class ThingAmendment {

    private final Thingifier thingifier;

    public ThingAmendment(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    public ApiResponse amendInstance(final BodyParser bodyargs, final ThingInstance instance,
                                     final Boolean clearFieldsBeforeSettingFromArgs) {

        Map<String, String> args = bodyargs.getStringMap();

        if(thingifier.apiConfig().willApiEnforceDeclaredTypesInInput()) {
            ValidationReport validatedTypes = bodyargs.validateAgainstType(instance.getEntity());
            if(!validatedTypes.isValid()){
                return ApiResponse.error(400, validatedTypes.getCombinedErrorMessages());
            }
        }

        ThingInstance cloned = null;

        try {

            cloned = instance.createDuplicateWithoutRelationships();

            if(clearFieldsBeforeSettingFromArgs){
                // if you want an idempotent amend then clear it down prior to amending
                cloned.clearAllFields();

            }
            List<FieldValue> fieldValues = FieldValues.
                                        fromListMapEntryStringString(
                                                new BodyArgsProcessor(thingifier, bodyargs).
                                                        removeRelationshipsFrom(instance));
            cloned.setFieldValuesFrom(fieldValues);

        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }

        final List<String> protectedFieldNames = cloned.getEntity().getFieldNamesOfType(FieldType.ID, FieldType.GUID);
        ValidationReport validation = cloned.validateFieldValues(protectedFieldNames, false);

        // validate the relationships as well
        ValidationReport relationshipsValidation = new BodyRelationshipValidator(thingifier).validate(bodyargs, cloned.getEntity());
        validation.combine(relationshipsValidation);

        if (validation.isValid()) {
            if(clearFieldsBeforeSettingFromArgs){
                instance.clearAllFields();
                // delete all existing relationships for idempotent amend
                // todo: this returns a list of 'items' to be removed based on relationship
                instance.getRelationships().removeAllRelationships();
            }
            List<FieldValue> fieldValues = FieldValues.
                    fromListMapEntryStringString(
                            new BodyArgsProcessor(thingifier, bodyargs).
                                    removeRelationshipsFrom(instance));
            instance.setFieldValuesFrom(fieldValues);

            // todo: should we check that this was actually a success?
            final ApiResponse relresponse = new RelationshipCreator(thingifier).createRelationships(bodyargs, instance);
            // todo: should check if any of the 'removed items due to relationship removal' need to be removed
            // and remove them if we do
            return ApiResponse.success().returnSingleInstance(instance);
        } else {
            // do not add it, report the errors
            return ApiResponse.error(400, validation.getErrorMessages());
        }

    }
}
