package uk.co.compendiumdev.thingifier.core.repository.inmemory;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality.MANDATORY_RELATIONSHIP;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

final class InMemoryRelationshipStore {

    private final Map<String, RelationshipRow> rows = new LinkedHashMap<>();

    void connect(
            final EntityInstance from,
            final String relationshipName,
            final EntityInstance to,
            final BiFunction<EntityDefinition, String, EntityInstance> resolver) {
        RelationshipVectorDefinition vector =
                from.getEntity().getNamedRelationshipTo(relationshipName, to.getEntity());
        if (vector == null) {
            throw new IllegalArgumentException(
                    "Unknown relationship "
                            + relationshipName
                            + " between "
                            + from.getEntity().getName()
                            + " and "
                            + to.getEntity().getName());
        }

        validateConnection(vector, from, to);
        if (relationshipExistsBetween(from, to, relationshipName)) {
            return;
        }
        assertCardinalityAllows(vector, from, to);
        rows.putIfAbsent(
                rowKey(vector, from.getInternalId(), to.getInternalId()), row(vector, from, to));
    }

    List<EntityInstance> listRelatedInstances(
            final EntityInstance instance,
            final String relationshipName,
            final BiFunction<EntityDefinition, String, EntityInstance> resolver) {
        Map<String, EntityInstance> related = new LinkedHashMap<>();
        for (RelationshipRow row : rows.values()) {
            if (!row.relationshipDefinitionIsKnownAs(relationshipName)) {
                continue;
            }
            EntityInstance item = relatedInstance(instance, row, resolver);
            if (item != null) {
                related.put(item.getInternalId(), item);
            }
        }
        return new ArrayList<>(related.values());
    }

    boolean hasRelationshipInstances(final EntityInstance instance) {
        for (RelationshipRow row : rows.values()) {
            if (row.involves(instance)) {
                return true;
            }
        }
        return false;
    }

    ValidationReport validateRelationships(
            final EntityInstance instance,
            final BiFunction<EntityDefinition, String, EntityInstance> resolver) {
        ValidationReport report = new ValidationReport();

        for (RelationshipVectorDefinition vector :
                instance.getEntity().related().getRelationships()) {
            int count = countRowsForVector(instance, vector);

            if (vector.getOptionality() == MANDATORY_RELATIONSHIP && count == 0) {
                report.setValid(false)
                        .addErrorMessage(
                                String.format(
                                        "Mandatory Relationship not found %s", vector.getName()));
            }

            if (vector.getCardinality().hasMaximumLimit()
                    && count > vector.getCardinality().maximumLimit()) {
                report.setValid(false)
                        .addErrorMessage(
                                String.format(
                                        "Maximum related instances exceeded for %s at %d",
                                        vector.getName(), vector.getCardinality().maximumLimit()));
            }
        }

        for (RelationshipRow row : rows.values()) {
            if (!row.involves(instance)) {
                continue;
            }
            ValidationReport rowReport = validateRow(row, resolver);
            if (!rowReport.isValid()) {
                for (String errorMessage : rowReport.getErrorMessages()) {
                    report.setValid(false)
                            .addErrorMessage(
                                    String.format(
                                            "Error with EntityInstance relationship %s - %s",
                                            instance.getInternalId(), errorMessage));
                }
            }
        }

        return report;
    }

    List<EntityInstance> removeRelationshipsInvolving(
            final EntityInstance parent,
            final EntityInstance child,
            final String relationshipName,
            final BiFunction<EntityDefinition, String, EntityInstance> resolver) {
        List<EntityInstance> alsoDelete = new ArrayList<>();
        List<String> toDelete = new ArrayList<>();

        for (Map.Entry<String, RelationshipRow> entry : rows.entrySet()) {
            RelationshipRow row = entry.getValue();
            if (!row.relationshipDefinitionIsKnownAs(relationshipName)) {
                continue;
            }
            if (!row.connects(parent, child)) {
                continue;
            }
            alsoDelete.addAll(instancesSubjectToMandatoryRelationship(row, resolver));
            toDelete.add(entry.getKey());
        }

        for (String key : toDelete) {
            rows.remove(key);
        }
        return alsoDelete;
    }

    List<EntityInstance> removeAllRelationships(
            final EntityInstance instance,
            final BiFunction<EntityDefinition, String, EntityInstance> resolver) {
        List<EntityInstance> alsoDelete = new ArrayList<>();
        List<String> toDelete = new ArrayList<>();

        for (Map.Entry<String, RelationshipRow> entry : rows.entrySet()) {
            RelationshipRow row = entry.getValue();
            if (!row.involves(instance)) {
                continue;
            }
            alsoDelete.addAll(instancesSubjectToMandatoryRelationship(row, resolver));
            toDelete.add(entry.getKey());
        }

        for (String key : toDelete) {
            rows.remove(key);
        }
        return alsoDelete;
    }

    void removeRowsInvolving(final List<String> internalIds) {
        rows.entrySet()
                .removeIf(
                        entry ->
                                internalIds.contains(entry.getValue().fromInternalId)
                                        || internalIds.contains(entry.getValue().toInternalId));
    }

    void clear() {
        rows.clear();
    }

    private void validateConnection(
            final RelationshipVectorDefinition vector,
            final EntityInstance from,
            final EntityInstance to) {
        if (from.getEntity() != vector.getFrom()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Found from EntityInstance types %s but expected of type %s",
                            from.getEntity().getName(), vector.getFrom().getName()));
        }
        if (to.getEntity() != vector.getTo()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Found to EntityInstance types %s but expected of type %s",
                            to.getEntity().getName(), vector.getTo().getName()));
        }
    }

    private boolean relationshipExistsBetween(
            final EntityInstance first,
            final EntityInstance second,
            final String relationshipName) {
        for (RelationshipRow row : rows.values()) {
            if (row.relationshipDefinitionIsKnownAs(relationshipName)
                    && row.connects(first, second)) {
                return true;
            }
        }
        return false;
    }

    private void assertCardinalityAllows(
            final RelationshipVectorDefinition vector,
            final EntityInstance from,
            final EntityInstance to) {
        if (!vector.getCardinality().hasMaximumLimit()) {
            assertReverseCardinalityAllows(vector, to);
            return;
        }

        int count = countRowsForVector(from, vector);
        if (!rows.containsKey(rowKey(vector, from.getInternalId(), to.getInternalId()))
                && count >= vector.getCardinality().maximumLimit()) {
            throw new RuntimeException(
                    String.format(
                            "Cannot add relationship type %s, exceeds maximum %d",
                            vector.getName(), vector.getCardinality().maximumLimit()));
        }
        assertReverseCardinalityAllows(vector, to);
    }

    private void assertReverseCardinalityAllows(
            final RelationshipVectorDefinition vector, final EntityInstance to) {
        if (!vector.getRelationshipDefinition().isTwoWay()) {
            return;
        }
        RelationshipVectorDefinition reverse =
                vector.getRelationshipDefinition().otherVectorOf(vector);
        if (reverse == null || !reverse.getCardinality().hasMaximumLimit()) {
            return;
        }

        int count = countRowsForVector(to, reverse);
        if (count >= reverse.getCardinality().maximumLimit()) {
            throw new RuntimeException(
                    String.format(
                            "Cannot add relationship type %s, exceeds maximum %d",
                            reverse.getName(), reverse.getCardinality().maximumLimit()));
        }
    }

    private int countRowsForVector(
            final EntityInstance instance, final RelationshipVectorDefinition vector) {
        int count = 0;
        for (RelationshipRow row : rows.values()) {
            if (rowRepresentsVectorForInstance(row, vector, instance)) {
                count++;
            }
        }
        return count;
    }

    private boolean rowRepresentsVectorForInstance(
            final RelationshipRow row,
            final RelationshipVectorDefinition vector,
            final EntityInstance instance) {
        if (row.vector == vector && row.fromInternalId.equals(instance.getInternalId())) {
            return true;
        }
        if (row.vector.getRelationshipDefinition().isTwoWay()) {
            RelationshipVectorDefinition reverse =
                    row.vector.getRelationshipDefinition().otherVectorOf(row.vector);
            return reverse == vector && row.toInternalId.equals(instance.getInternalId());
        }
        return false;
    }

    private EntityInstance relatedInstance(
            final EntityInstance instance,
            final RelationshipRow row,
            final BiFunction<EntityDefinition, String, EntityInstance> resolver) {
        if (row.fromInternalId.equals(instance.getInternalId())) {
            return resolver.apply(row.vector.getTo(), row.toInternalId);
        }
        if (row.toInternalId.equals(instance.getInternalId())
                && row.vector.getRelationshipDefinition().isTwoWay()) {
            return resolver.apply(row.vector.getFrom(), row.fromInternalId);
        }
        return null;
    }

    private ValidationReport validateRow(
            final RelationshipRow row,
            final BiFunction<EntityDefinition, String, EntityInstance> resolver) {
        ValidationReport report = new ValidationReport();
        EntityInstance from = resolver.apply(row.vector.getFrom(), row.fromInternalId);
        EntityInstance to = resolver.apply(row.vector.getTo(), row.toInternalId);

        if (from == null) {
            report.setValid(false).addErrorMessage("No From Instance found");
        }
        if (to == null) {
            report.setValid(false).addErrorMessage("No To Instance found");
        }
        if (!report.isValid()) {
            return report;
        }
        if (from.getEntity() != row.vector.getFrom()) {
            report.setValid(false)
                    .addErrorMessage(
                            String.format(
                                    "Found from EntityInstance types %s but expected of type %s",
                                    from.getEntity().getName(), row.vector.getFrom().getName()));
        }
        if (to.getEntity() != row.vector.getTo()) {
            report.setValid(false)
                    .addErrorMessage(
                            String.format(
                                    "Found to EntityInstance types %s but expected of type %s",
                                    to.getEntity().getName(), row.vector.getTo().getName()));
        }
        return report;
    }

    private List<EntityInstance> instancesSubjectToMandatoryRelationship(
            final RelationshipRow row,
            final BiFunction<EntityDefinition, String, EntityInstance> resolver) {
        List<EntityInstance> deleteThese = new ArrayList<>();

        if (row.vector.getOptionality() == MANDATORY_RELATIONSHIP) {
            EntityInstance from = resolver.apply(row.vector.getFrom(), row.fromInternalId);
            if (from != null) {
                deleteThese.add(from);
            }
        }

        if (row.vector.getRelationshipDefinition().isTwoWay()) {
            RelationshipVectorDefinition otherVector =
                    row.vector.getRelationshipDefinition().otherVectorOf(row.vector);
            if (otherVector != null && otherVector.getOptionality() == MANDATORY_RELATIONSHIP) {
                EntityInstance to = resolver.apply(row.vector.getTo(), row.toInternalId);
                if (to != null) {
                    deleteThese.add(to);
                }
            }
        }

        return deleteThese;
    }

    private RelationshipRow row(
            final RelationshipVectorDefinition vector,
            final EntityInstance from,
            final EntityInstance to) {
        return new RelationshipRow(vector, from.getInternalId(), to.getInternalId());
    }

    private String rowKey(
            final RelationshipVectorDefinition vector,
            final String fromInternalId,
            final String toInternalId) {
        return vector.getFrom().getName()
                + "|"
                + vector.getName()
                + "|"
                + vector.getTo().getName()
                + "|"
                + fromInternalId
                + "|"
                + toInternalId;
    }

    private static final class RelationshipRow {
        private final RelationshipVectorDefinition vector;
        private final String fromInternalId;
        private final String toInternalId;

        private RelationshipRow(
                final RelationshipVectorDefinition vector,
                final String fromInternalId,
                final String toInternalId) {
            this.vector = vector;
            this.fromInternalId = fromInternalId;
            this.toInternalId = toInternalId;
        }

        private boolean involves(final EntityInstance instance) {
            return fromInternalId.equals(instance.getInternalId())
                    || toInternalId.equals(instance.getInternalId());
        }

        private boolean connects(final EntityInstance first, final EntityInstance second) {
            return (fromInternalId.equals(first.getInternalId())
                            && toInternalId.equals(second.getInternalId()))
                    || (fromInternalId.equals(second.getInternalId())
                            && toInternalId.equals(first.getInternalId()));
        }

        private boolean relationshipDefinitionIsKnownAs(final String relationshipName) {
            return vector.getRelationshipDefinition().isKnownAs(relationshipName);
        }
    }
}
