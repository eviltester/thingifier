package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.Map;
import java.util.UUID;

public class ThingCreation {

    public ApiResponse with(final Map<String, String> args, final Thing thing) {
        return addNewThingWithFields(args, thing.createInstance(), thing);
    }

    public ApiResponse withGuid(final String instanceGuid, final Map<String, String> args, final Thing thing) {

        ThingInstance instance;

        try {
            String aGUID = UUID.fromString(instanceGuid).toString();
            instance = thing.createInstance(aGUID);

        } catch (Exception e) {
            // that is not a valid guid
            System.out.println(e.getMessage());
            return ApiResponse.error404(String.format("Invalid GUID for %s entity %s", instanceGuid, thing.definition().getName()));
        }

        return addNewThingWithFields(args, instance, thing);
    }


    /**
     * Because we are creating a new thing we can only validate fields and not the relationships
     *
     * @param args
     * @param instance
     * @param thing
     * @return
     */
    private ApiResponse addNewThingWithFields(final Map<String, String> args, final ThingInstance instance, final Thing thing) {

        try {
            instance.setFieldValuesFrom(args);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }

        ValidationReport validation = instance.validateFields();

        if (validation.isValid()) {
            thing.addInstance(instance);
            return ApiResponse.created(instance);
        } else {
            // do not add it, report the errors
            return ApiResponse.error(400, validation.getErrorMessages());
        }
    }
}
