package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.generic.definitions.RelationshipVector;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ThingCreation {

    private final Thingifier thingifier;

    public ThingCreation(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    public ApiResponse with(final BodyParser bodyargs, final Thing thing) {

        ValidationReport validated = new BodyRelationshipValidator(thingifier).validate(bodyargs, thing);

        if(!validated.isValid()){
            return ApiResponse.error(400, String.format("Invalid relationships: %s",validated.getCombinedErrorMessages()));
        }

        validated = new BodyCreationValidator(thingifier).validate(bodyargs, thing);
        if(!validated.isValid()){
            return ApiResponse.error(400, String.format("Invalid Creation: %s",validated.getCombinedErrorMessages()));
        }

        // todo: separate validation for creation of 'cannot' create with ID, or cannot create with GUID
        return addNewThingWithFields(bodyargs, thing.createInstance(), thing);
    }

    public ApiResponse withGuid(final String instanceGuid, final BodyParser bodyargs, final Thing thing) {

        final Map<String, String> args = bodyargs.getStringMap();

        ThingInstance instance;

        try {
            String aGUID = UUID.fromString(instanceGuid).toString();
            instance = thing.createInstance(aGUID);

        } catch (Exception e) {
            // that is not a valid guid
            System.out.println(e.getMessage());
            return ApiResponse.error404(String.format("Invalid GUID for %s entity %s", instanceGuid, thing.definition().getName()));
        }

        return addNewThingWithFields(bodyargs, instance, thing);
    }


    private ApiResponse addNewThingWithFields(final BodyParser bodyargs, final ThingInstance instance, final Thing thing) {

        final Map<String, String> args = bodyargs.getStringMap();

        try {
            instance.setFieldValuesFrom(args);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }


        ValidationReport validation = instance.validateNonProtectedFields();

        if (validation.isValid()) {
            thing.addInstance(instance);

            return new RelationshipCreator(thingifier).createRelationships(bodyargs, instance);

        } else {
            // do not add it, report the errors
            return ApiResponse.error(400, validation.getErrorMessages());
        }
    }


}

