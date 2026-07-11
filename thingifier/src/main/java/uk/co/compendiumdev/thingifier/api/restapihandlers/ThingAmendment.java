package uk.co.compendiumdev.thingifier.api.restapihandlers;

import java.util.List;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

// TODO - there should be a generic API handlers package that does create, read, update, delete
// which I think this is, and they should not use http status codes
// also cloning instances feels like the wrong way to amend, we should create 'amend/patch' requests
// would also handle relationship

public class ThingAmendment {

    private final Thingifier thingifier;

    public ThingAmendment(final Thingifier thingifier) {
        this.thingifier = thingifier;
    }

    public ApiResponse amendInstance(
            final BodyParser bodyargs,
            final EntityInstance instance,
            final Boolean clearFieldsBeforeSettingFromArgs,
            final String database) {

        bodyargs.getMap();

        if (thingifier.apiConfig().willApiEnforceDeclaredTypesInInput()) {
            List<String> doNotValidateFields =
                    instance.getEntity()
                            .getFieldNamesOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID);
            ValidationReport validatedTypes =
                    bodyargs.validateAgainstTypeIgnoring(instance.getEntity(), doNotValidateFields);
            if (!validatedTypes.isValid()) {
                return ApiResponse.error(400, validatedTypes.getCombinedErrorMessages());
            }
        }

        EntityInstanceDraft draft;

        try {
            List<NamedValue> fieldValues =
                    FieldValues.fromListMapEntryStringString(
                            new BodyArgsProcessor(thingifier, bodyargs)
                                    .removeRelationshipsFrom(instance, database));

            draft = new EntityInstanceBulkUpdater(instance).setFieldValuesFrom(fieldValues);

        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }

        // validate the relationships as well
        ValidationReport validation =
                new BodyRelationshipValidator(thingifier)
                        .validate(bodyargs, instance.getEntity(), database);

        if (validation.isValid()) {
            final EntityInstance updated;
            try {
                if (clearFieldsBeforeSettingFromArgs) {
                    updated = thingifier.getStore(database).entities().replace(instance, draft);
                    // Reset repository-owned relationships for idempotent amend.
                    // TODO: handle mandatory dependents returned by relationship removal.
                    thingifier.getStore(database).relationships().removeAll(updated);
                } else {
                    updated = thingifier.getStore(database).entities().patch(instance, draft);
                }
            } catch (Exception e) {
                return ApiResponse.error(400, e.getMessage());
            }

            // todo: should we check that this was actually a success?
            new RelationshipCreator(thingifier).createRelationships(bodyargs, updated, database);
            // TODO: relationship removal can return mandatory dependents that may also need
            // deletion.
            return ApiResponse.success().returnSingleInstance(updated);
        } else {
            // do not add it, report the errors
            return ApiResponse.error(400, validation.getErrorMessages());
        }
    }
}
