package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

import java.util.List;

final class RepositoryBackedRelationshipUrlResolver {

    private final Thingifier thingifier;
    private final ThingRepository repository;

    RepositoryBackedRelationshipUrlResolver(
            final Thingifier thingifier,
            final String databaseName) {
        this.thingifier = thingifier;
        this.repository = thingifier.getRepository(databaseName);
    }

    RelationshipUrlResolution resolveCollection(final String url) {
        String[] parts = EntityUrlMatcher.parts(url);
        if (parts.length != 3) {
            return RelationshipUrlResolution.notMatched();
        }

        EntityDefinition parentEntity = entityFor(parts[0]);
        if (parentEntity == null || !parentEntity.related().hasRelationship(parts[2])) {
            return RelationshipUrlResolution.notMatched();
        }

        EntityInstance parent =
                repository.findInstanceByQueryIdentifier(parentEntity, parts[1]);

        return RelationshipUrlResolution.collection(
                parentEntity, parent, parts[2]);
    }

    RelationshipUrlResolution resolveRelationshipInstance(final String url) {
        String[] parts = EntityUrlMatcher.parts(url);
        if (parts.length != 4) {
            return RelationshipUrlResolution.notMatched();
        }

        RelationshipUrlResolution collection =
                resolveCollection(parts[0] + "/" + parts[1] + "/" + parts[2]);
        if (!collection.matchedRelationshipPath() || collection.parentInstance() == null) {
            return collection;
        }

        EntityInstance child = null;
        List<EntityInstance> relatedInstances = repository.listRelatedInstances(
                collection.parentInstance(), collection.relationshipName());
        for (EntityInstance relatedInstance : relatedInstances) {
            if (matchesQueryIdentifier(relatedInstance, parts[3])) {
                child = relatedInstance;
                break;
            }
        }

        return RelationshipUrlResolution.relationshipInstance(
                collection.parentEntity(),
                collection.parentInstance(),
                collection.relationshipName(),
                child);
    }

    private EntityDefinition entityFor(final String term) {
        return thingifier.getERmodel().getSchema().
                getDefinitionWithSingularOrPluralNamed(term);
    }

    private boolean matchesQueryIdentifier(
            final EntityInstance instance,
            final String identifier) {
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

    static final class RelationshipUrlResolution {
        private final boolean matchedRelationshipPath;
        private final boolean relationshipInstancePath;
        private final EntityDefinition parentEntity;
        private final EntityInstance parentInstance;
        private final String relationshipName;
        private final EntityInstance childInstance;

        private RelationshipUrlResolution(
                final boolean matchedRelationshipPath,
                final boolean relationshipInstancePath,
                final EntityDefinition parentEntity,
                final EntityInstance parentInstance,
                final String relationshipName,
                final EntityInstance childInstance) {
            this.matchedRelationshipPath = matchedRelationshipPath;
            this.relationshipInstancePath = relationshipInstancePath;
            this.parentEntity = parentEntity;
            this.parentInstance = parentInstance;
            this.relationshipName = relationshipName;
            this.childInstance = childInstance;
        }

        static RelationshipUrlResolution notMatched() {
            return new RelationshipUrlResolution(
                    false, false, null, null, null, null);
        }

        static RelationshipUrlResolution collection(
                final EntityDefinition parentEntity,
                final EntityInstance parentInstance,
                final String relationshipName) {
            return new RelationshipUrlResolution(
                    true, false, parentEntity, parentInstance, relationshipName, null);
        }

        static RelationshipUrlResolution relationshipInstance(
                final EntityDefinition parentEntity,
                final EntityInstance parentInstance,
                final String relationshipName,
                final EntityInstance childInstance) {
            return new RelationshipUrlResolution(
                    true, true, parentEntity, parentInstance, relationshipName, childInstance);
        }

        boolean matchedRelationshipPath() {
            return matchedRelationshipPath;
        }

        boolean relationshipInstancePath() {
            return relationshipInstancePath;
        }

        EntityDefinition parentEntity() {
            return parentEntity;
        }

        EntityInstance parentInstance() {
            return parentInstance;
        }

        String relationshipName() {
            return relationshipName;
        }

        EntityInstance childInstance() {
            return childInstance;
        }
    }
}
