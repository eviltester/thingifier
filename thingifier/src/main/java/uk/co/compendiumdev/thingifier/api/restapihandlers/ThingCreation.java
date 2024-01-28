package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ThingCreation {

    private final Thingifier thingifier;

    public ThingCreation(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    public ApiResponse with(final BodyParser bodyargs, final EntityInstanceCollection thing, final String database) {

        ValidationReport validated = new BodyRelationshipValidator(thingifier).validate(bodyargs, thing, database);

        if(!validated.isValid()){
            return ApiResponse.error(400, String.format("Invalid relationships: %s",validated.getCombinedErrorMessages()));
        }

        validated = new BodyCreationValidator(thingifier).validate(bodyargs, thing);
        if(!validated.isValid()){
            return ApiResponse.error(400, String.format("Invalid Creation: %s",validated.getCombinedErrorMessages()));
        }

        // todo: separate validation for creation of 'cannot' create with ID, or cannot create with GUID
        EntityInstance instance = new EntityInstance(thing.definition());
        instance.addGUIDtoInstance();
        instance.addIdsToInstance();
        return addNewThingWithFields(bodyargs, instance, thing, database);
    }

    // create with GUID and IDs is normally associated with PUT or 'insert'
    public ApiResponse withGuid(final String instanceGuid, final BodyParser bodyargs,
                                final EntityInstanceCollection thing, final String database) {

        final Map<String, String> args = bodyargs.getStringMap();

        EntityInstance instance;
        ValidationReport validated;

        validated = new BodyCreationValidator(thingifier).
                areFieldsUnique(bodyargs, thing,
                        thing.definition().getFieldNamesOfType(FieldType.ID, FieldType.GUID));
        if(!validated.isValid()){
            return ApiResponse.error(409,"Cannot Create with duplicate values: "+
                    validated.getCombinedErrorMessages());
        }

        String aGUID="";

        try {
             aGUID= UUID.fromString(instanceGuid).toString();
        } catch (Exception e) {
            // that is not a valid guid
            System.out.println(e.getMessage());
            return ApiResponse.error404(String.format("Invalid GUID for %s entity %s", instanceGuid, thing.definition().getName()));
        }

        instance = new EntityInstance(thing.definition());
        instance.overrideValue("guid", aGUID);
        instance.addIdsToInstance();

        validated = new BodyRelationshipValidator(thingifier).validate(bodyargs, thing, database);

        if(!validated.isValid()){
            return ApiResponse.error(400,
                    String.format("Invalid relationships: %s",
                            validated.getCombinedErrorMessages()));
        }


        // any next id counts should be set higher than the ids mentioned in here
        List<FieldValue> fieldValues = FieldValues.
                            fromListMapEntryStringString(
                                    bodyargs.getFlattenedStringMap());

        thing.definition().setNextIdsToAccomodate(fieldValues);

        return insertNewThingWithFields(bodyargs, instance, thing, database);
    }


    private ApiResponse addNewThingWithFields(final BodyParser bodyargs, final EntityInstance instance,
                                              final EntityInstanceCollection thing, final String database) {

        if(thingifier.apiConfig().willApiEnforceDeclaredTypesInInput()) {
            ValidationReport validatedTypes = bodyargs.validateAgainstType(instance.getEntity());
            if(!validatedTypes.isValid()){
                return ApiResponse.error(400, validatedTypes.getCombinedErrorMessages());
            }
        }

        // todo: need to separate relationships from field in the path prior to sending through to setFields

        try {
            // if any guids or ids then throw an error if they are not the same
            List<FieldValue> fieldValues = FieldValues.
                    fromListMapEntryStringString(
                            new BodyArgsProcessor(thingifier, bodyargs).
                                    removeRelationshipsFrom(instance, database));

            instance.setFieldValuesFrom(fieldValues);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }


        final List<String> protectedFieldNames = instance.getEntity().
                                getFieldNamesOfType(FieldType.ID, FieldType.GUID);

        ValidationReport validation = instance.validateFieldValues(protectedFieldNames, false);

        return addValidatedInstance(bodyargs, instance, thing, database, validation);
    }

    private ApiResponse insertNewThingWithFields(final BodyParser bodyargs, final EntityInstance instance,
                                              final EntityInstanceCollection thing, final String database) {

        if(thingifier.apiConfig().willApiEnforceDeclaredTypesInInput()) {
            ValidationReport validatedTypes = bodyargs.validateAgainstType(instance.getEntity());
            if(!validatedTypes.isValid()){
                return ApiResponse.error(400, validatedTypes.getCombinedErrorMessages());
            }
        }

        try {
            // set all the fields and values, except guids
            List<String> ignoreFields = instance.getEntity().
                                getFieldNamesOfType(FieldType.GUID);

            List<FieldValue> fieldValues = FieldValues.
                    fromListMapEntryStringString(
                            new BodyArgsProcessor(thingifier, bodyargs).
                                    removeRelationshipsFrom(instance, database));

            instance.overrideFieldValuesFromArgsIgnoring(fieldValues, ignoreFields);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }

        ValidationReport validation = instance.validateFieldValues(new ArrayList<>(), true);

        return addValidatedInstance(bodyargs, instance, thing, database, validation);
    }

    private ApiResponse addValidatedInstance(BodyParser bodyargs, EntityInstance instance, EntityInstanceCollection thing, String database, ValidationReport validation) {
        if (validation.isValid()) {
            try {
                thing.addInstance(instance);
            }catch(Exception e){
                return ApiResponse.error(400, e.getMessage());
            }

            return new RelationshipCreator(thingifier).createRelationships(bodyargs, instance, database);

        } else {
            // do not add it, report the errors
            return ApiResponse.error(400, validation.getErrorMessages());
        }
    }

}

