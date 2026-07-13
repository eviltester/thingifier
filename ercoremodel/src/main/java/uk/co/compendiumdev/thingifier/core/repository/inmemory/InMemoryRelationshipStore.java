package uk.co.compendiumdev.thingifier.core.repository.inmemory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.repository.relationship.RelationshipEndpoint;
import uk.co.compendiumdev.thingifier.core.repository.relationship.RelationshipRow;
import uk.co.compendiumdev.thingifier.core.repository.relationship.RelationshipRules;

final class InMemoryRelationshipStore {

    private final Map<String, RelationshipRow> rows = new LinkedHashMap<>();
    private final RelationshipRules rules = new RelationshipRules();

    void connect(
            final EntityInstance from,
            final String relationshipName,
            final EntityInstance to,
            final BiFunction<EntityDefinition, String, EntityInstance> resolver) {
        RelationshipVectorDefinition vector =
                rules.relationshipVectorForConnection(from, relationshipName, to);
        if (rules.relationshipExistsBetween(rows.values(), from, to, relationshipName)) {
            return;
        }
        rules.assertCardinalityAllows(vector, from, to, this::countRowsForEndpoint);
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
            EntityInstance item = rules.relatedInstance(instance, row, resolver::apply);
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
        return rules.validateRelationships(
                instance, rowsInvolving(instance), resolver::apply, this::countRowsForEndpoint);
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
            alsoDelete.addAll(rules.instancesSubjectToMandatoryRelationship(row, resolver::apply));
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
            alsoDelete.addAll(rules.instancesSubjectToMandatoryRelationship(row, resolver::apply));
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
                                internalIds.contains(entry.getValue().getFromInternalId())
                                        || internalIds.contains(
                                                entry.getValue().getToInternalId()));
    }

    void clear() {
        rows.clear();
    }

    private List<RelationshipRow> rowsInvolving(final EntityInstance instance) {
        List<RelationshipRow> involving = new ArrayList<>();
        for (RelationshipRow row : rows.values()) {
            if (row.involves(instance)) {
                involving.add(row);
            }
        }
        return involving;
    }

    private int countRowsForEndpoint(
            final RelationshipVectorDefinition vector,
            final RelationshipEndpoint endpoint,
            final String internalId) {
        int count = 0;
        for (RelationshipRow row : rows.values()) {
            if (row.matchesEndpoint(vector, endpoint, internalId)) {
                count++;
            }
        }
        return count;
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
}
