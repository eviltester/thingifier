package uk.co.compendiumdev.thingifier.core.domain.instances;

import java.util.Collection;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public final class EntityInstanceRepositoryAccess {

    private EntityInstanceRepositoryAccess() {}

    public static EntityInstance patch(
            final EntityInstance existing, final EntityInstanceDraft draft) {
        EntityInstance patched =
                existing.createDuplicateWithoutRelationships(existing.getInternalId());
        return apply(patched, draft);
    }

    public static EntityInstance replace(
            final EntityInstance existing, final EntityInstanceDraft draft) {
        EntityInstance replacement =
                existing.createDuplicateWithoutRelationships(existing.getInternalId());
        clearAllFields(replacement);
        return apply(replacement, draft);
    }

    public static EntityInstance apply(
            final EntityInstance existing, final EntityInstanceDraft draft) {
        return existing.applyDraftFromRepository(draft);
    }

    public static EntityInstance empty(final EntityDefinition entity) {
        return new EntityInstance(entity);
    }

    public static EntityInstance empty(
            final EntityDefinition entity, final java.util.UUID internalId) {
        return new EntityInstance(entity, internalId);
    }

    public static void setValue(
            final EntityInstance instance, final String fieldName, final String value) {
        instance.setValueFromRepository(fieldName, value);
    }

    public static void overrideValue(
            final EntityInstance instance, final String fieldName, final String value) {
        instance.overrideValueFromRepository(fieldName, value);
    }

    public static void clearAllFields(final EntityInstance instance) {
        instance.clearAllFieldsFromRepository();
    }

    public static InstanceFields fields(final EntityInstance instance) {
        return instance.getFields();
    }

    public static void connectRelationship(
            final EntityInstance from, final String relationshipName, final EntityInstance to) {
        from.getRelationships().connectFromRepository(relationshipName, to);
    }

    public static List<EntityInstance> removeRelationshipsInvolving(
            final EntityInstance parent,
            final EntityInstance child,
            final String relationshipName) {
        return parent.getRelationships()
                .removeRelationshipsInvolvingFromRepository(child, relationshipName);
    }

    public static List<EntityInstance> removeAllRelationships(final EntityInstance instance) {
        return instance.getRelationships().removeAllRelationshipsFromRepository();
    }

    public static Collection<EntityInstance> connectedItems(
            final EntityInstance instance, final String relationshipName) {
        return instance.getRelationships().getConnectedItems(relationshipName);
    }

    public static ValidationReport validateFieldValues(
            final EntityInstance instance,
            final List<String> excluding,
            final boolean allowedToSetIds) {
        return instance.validateFieldValues(excluding, allowedToSetIds);
    }

    public static ValidationReport validateRelationships(final EntityInstance instance) {
        return instance.validateRelationships();
    }

    public static List<String> findAnyGuidOrIdDifferences(
            final EntityInstance instance, final List<NamedValue> fieldValues) {
        return instance.getFields().findAnyGuidOrIdDifferences(fieldValues);
    }

    public static EntityInstance lock(final EntityInstance instance) {
        instance.lockForRepository();
        return instance;
    }
}
