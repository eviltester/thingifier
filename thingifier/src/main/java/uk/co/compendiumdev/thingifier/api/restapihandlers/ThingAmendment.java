package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.Map;

public class ThingAmendment {

    public ApiResponse amendInstance(final Map<String, String> args, final ThingInstance instance, final Boolean clearFieldsBeforeSettingFromArgs) {


        ThingInstance cloned = null;

        try {

            cloned = instance.createDuplicateWithoutRelationships();

            if(clearFieldsBeforeSettingFromArgs){
                // if you want an idempotent amend then clear it down prior to amending
                cloned.clearAllFields();
            }
            cloned.setFieldValuesFrom(args);

        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }

        ValidationReport validation = cloned.validate();

        if (validation.isValid()) {
            if(clearFieldsBeforeSettingFromArgs){
                instance.clearAllFields();
            }
            instance.setFieldValuesFrom(args);
            return ApiResponse.success().returnSingleInstance(instance);
        } else {
            // do not add it, report the errors
            return ApiResponse.error(400, validation.getErrorMessages());
        }

    }
}
