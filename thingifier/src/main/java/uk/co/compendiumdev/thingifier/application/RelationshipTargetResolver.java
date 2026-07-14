package uk.co.compendiumdev.thingifier.application;

import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class RelationshipTargetResolver {

    private final ThingStore store;

    RelationshipTargetResolver(final ThingStore store) {
        this.store = store;
    }

    RelatedItemResolution resolveRelatedItemFromReferenceFields(
            final EntityInstance parent,
            final String relationshipName,
            final List<NamedValue> childReferenceFields) {
        List<RelationshipVectorDefinition> possibleRelationships =
                parent.getEntity().related().getRelationships(relationshipName);
        RelationshipVectorDefinition relationshipToUse = possibleRelationships.get(0);
        EntityDefinition targetEntity = relationshipToUse.getTo();

        EntityInstance relatedItem = null;
        boolean expectingRelatedItem = false;
        String matchingFieldNames = "";
        for (NamedValue fieldValue : childReferenceFields) {
            final Field field = targetEntity.getField(fieldValue.getName());
            if (field == null) {
                continue;
            }
            if (field.getType() == FieldType.AUTO_GUID
                    || field.getType() == FieldType.AUTO_INCREMENT) {
                expectingRelatedItem = true;
                if (!matchingFieldNames.contains(fieldValue.getName() + " ")) {
                    matchingFieldNames = matchingFieldNames + fieldValue.getName() + " ";
                }
                relatedItem =
                        store.entityQueries()
                                .findByField(
                                        targetEntity, fieldValue.getName(), fieldValue.asString());
                if (relatedItem != null) {
                    break;
                }
            }
        }
        if (expectingRelatedItem && relatedItem == null) {
            matchingFieldNames = matchingFieldNames.trim().replace(" ", ", ");
            return RelatedItemResolution.error(
                    ApplicationError.notFound(
                            String.format(
                                    "Could not find thing matching value for %s",
                                    matchingFieldNames)));
        }

        if (relatedItem == null) {
            return RelatedItemResolution.error(
                    ApplicationError.validation(
                            String.format(
                                    "No related item reference supplied for %s",
                                    relationshipName)));
        }

        return RelatedItemResolution.success(relatedItem);
    }

    RelationshipVectorDefinition firstRelationshipVector(
            final EntityDefinition entity, final String relationshipName) {
        List<RelationshipVectorDefinition> vectors =
                entity.related().getRelationships(relationshipName);
        if (vectors.isEmpty()) {
            return null;
        }
        return vectors.get(0);
    }

    boolean bodyReferencesExistingRelatedItem(
            final EntityDefinition targetEntity, final List<NamedValue> bodyFields) {
        for (NamedValue fieldValue : bodyFields) {
            Field field = targetEntity.getField(fieldValue.getName());
            if (field == null) {
                continue;
            }
            if (field.getType() == FieldType.AUTO_GUID
                    || field.getType() == FieldType.AUTO_INCREMENT) {
                return true;
            }
        }
        return false;
    }

    EntityInstance relatedInstanceMatchingIdentifier(
            final EntityInstance parent, final String relationshipName, final String identifier) {
        for (EntityInstance related : store.relationships().listRelated(parent, relationshipName)) {
            if (matchesQueryIdentifier(related, identifier)) {
                return related;
            }
        }
        return null;
    }

    private boolean matchesQueryIdentifier(final EntityInstance instance, final String identifier) {
        for (Field autoIncrementField :
                instance.getEntity().getFieldsOfType(FieldType.AUTO_INCREMENT)) {
            String idValue = instance.getFieldValue(autoIncrementField.getName()).asString();
            if (idValue.contentEquals(identifier)) {
                return true;
            }
            break;
        }

        String primaryKeyValue = instance.getPrimaryKeyValue();
        return primaryKeyValue != null && primaryKeyValue.contentEquals(identifier);
    }

    static final class RelatedItemResolution {

        private final EntityInstance instance;
        private final ApplicationError error;

        private RelatedItemResolution(final EntityInstance instance, final ApplicationError error) {
            this.instance = instance;
            this.error = error;
        }

        static RelatedItemResolution success(final EntityInstance instance) {
            return new RelatedItemResolution(instance, null);
        }

        static RelatedItemResolution error(final ApplicationError error) {
            return new RelatedItemResolution(null, error);
        }

        EntityInstance instance() {
            return instance;
        }

        ApplicationError error() {
            return error;
        }
    }
}
