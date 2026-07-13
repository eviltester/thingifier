package uk.co.compendiumdev.thingifier.api.restapihandlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

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
            final ThingStore store = thingifier.getStore(database);
            final RelationshipSnapshot originalRelationships =
                    RelationshipSnapshot.capture(store, instance);
            try {
                if (clearFieldsBeforeSettingFromArgs) {
                    updated = store.entities().replace(instance, draft);
                    originalRelationships.disconnectFrom(store, updated);
                } else {
                    updated = store.entities().patch(instance, draft);
                }
            } catch (Exception e) {
                return ApiResponse.error(400, e.getMessage());
            }

            ApiResponse relationshipResponse;
            try {
                relationshipResponse =
                        new RelationshipCreator(thingifier)
                                .createRelationships(bodyargs, updated, database);
            } catch (Exception e) {
                rollbackAmendment(store, updated, instance, originalRelationships);
                return ApiResponse.error(400, e.getMessage());
            }

            if (relationshipResponse.isErrorResponse()) {
                rollbackAmendment(store, updated, instance, originalRelationships);
                return relationshipResponse;
            }

            ValidationReport finalRelationships = store.relationships().validate(updated);
            if (!finalRelationships.isValid()) {
                rollbackAmendment(store, updated, instance, originalRelationships);
                return ApiResponse.error(400, finalRelationships.getErrorMessages());
            }

            if (clearFieldsBeforeSettingFromArgs) {
                originalRelationships.deleteFormerDependentsMadeInvalidBy(store, updated);
            }

            return ApiResponse.success().returnSingleInstance(updated);
        } else {
            // do not add it, report the errors
            return ApiResponse.error(400, validation.getErrorMessages());
        }
    }

    private void rollbackAmendment(
            final ThingStore store,
            final EntityInstance current,
            final EntityInstance original,
            final RelationshipSnapshot originalRelationships) {
        RelationshipSnapshot.capture(store, current).disconnectFrom(store, current);
        EntityInstance restored = store.entities().replace(current, draftFrom(original));
        originalRelationships.restoreTo(store, restored);
    }

    private EntityInstanceDraft draftFrom(final EntityInstance instance) {
        EntityInstanceDraft draft = EntityInstanceDraft.forEntity(instance.getEntity());
        for (FieldValue value : instance.getAssignedFieldValues()) {
            Field field = instance.getEntity().getField(value.getName());
            if (field.getType() == FieldType.AUTO_INCREMENT
                    || field.getType() == FieldType.AUTO_GUID) {
                draft.withProtectedField(value.getName(), value.asString());
            } else {
                draft.withField(value.getName(), value.asString());
            }
        }
        return draft;
    }

    private static final class RelationshipSnapshot {

        private final List<RelationshipLink> links;

        private RelationshipSnapshot(final List<RelationshipLink> links) {
            this.links = links;
        }

        private static RelationshipSnapshot capture(
                final ThingStore store, final EntityInstance instance) {
            List<RelationshipLink> links = new ArrayList<>();
            Set<String> seenLinks = new HashSet<>();
            for (RelationshipVectorDefinition vector :
                    instance.getEntity().related().getRelationships()) {
                for (EntityInstance related :
                        store.relationships().listRelated(instance, vector.getName())) {
                    String key = vector.getName() + "|" + related.getInternalId();
                    if (seenLinks.add(key)) {
                        links.add(
                                new RelationshipLink(
                                        vector.getName(),
                                        related,
                                        store.relationships().validate(related).isValid()));
                    }
                }
            }
            return new RelationshipSnapshot(links);
        }

        private void disconnectFrom(final ThingStore store, final EntityInstance instance) {
            for (RelationshipLink link : links) {
                store.relationships()
                        .disconnectBetween(instance, link.related, link.relationshipName);
            }
        }

        private void restoreTo(final ThingStore store, final EntityInstance instance) {
            for (RelationshipLink link : links) {
                store.relationships().connect(instance, link.relationshipName, link.related);
            }
        }

        private void deleteFormerDependentsMadeInvalidBy(
                final ThingStore store, final EntityInstance instance) {
            for (RelationshipLink link : links) {
                if (link.relatedWasValid
                        && !link.isStillRelatedTo(store, instance)
                        && !store.relationships().validate(link.related).isValid()) {
                    store.entities().delete(link.related);
                }
            }
        }
    }

    private static final class RelationshipLink {

        private final String relationshipName;
        private final EntityInstance related;
        private final boolean relatedWasValid;

        private RelationshipLink(
                final String relationshipName,
                final EntityInstance related,
                final boolean relatedWasValid) {
            this.relationshipName = relationshipName;
            this.related = related;
            this.relatedWasValid = relatedWasValid;
        }

        private boolean isStillRelatedTo(final ThingStore store, final EntityInstance instance) {
            for (EntityInstance current :
                    store.relationships().listRelated(instance, relationshipName)) {
                if (current.getInternalId().equals(related.getInternalId())) {
                    return true;
                }
            }
            return false;
        }
    }
}
