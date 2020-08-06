package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

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

            // todo: this 'clone' is actually just a cloned instance fields not the Instance
            cloned = instance.createDuplicateWithoutRelationships();

            if(clearFieldsBeforeSettingFromArgs){
                // if you want an idempotent amend then clear it down prior to amending
                cloned.clearAllFields();

            }
            cloned.setFieldValuesFrom(bodyargs);

        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }

        ValidationReport validation = cloned.validateNonProtectedFields();

        // validate the relationships as well
        ValidationReport relationshipsValidation = new BodyRelationshipValidator(thingifier).validate(bodyargs, cloned.getEntity());
        validation.combine(relationshipsValidation);

        if (validation.isValid()) {
            if(clearFieldsBeforeSettingFromArgs){
                instance.clearAllFields();
                // delete all existing relationships for idempotent amend
                instance.removeAllRelationships();
            }
            instance.setFieldValuesFrom(bodyargs);

            final ApiResponse relresponse = new RelationshipCreator(thingifier).createRelationships(bodyargs, instance);

            return ApiResponse.success().returnSingleInstance(instance);
        } else {
            // do not add it, report the errors
            return ApiResponse.error(400, validation.getErrorMessages());
        }

    }
}
