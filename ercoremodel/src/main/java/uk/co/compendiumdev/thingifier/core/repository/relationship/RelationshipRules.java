package uk.co.compendiumdev.thingifier.core.repository.relationship;

import static uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality.MANDATORY_RELATIONSHIP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

public final class RelationshipRules {

    public RelationshipVectorDefinition relationshipVectorForConnection(
            final EntityInstance from, final String relationshipName, final EntityInstance to) {
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
        return vector;
    }

    public void validateConnection(
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

    public boolean relationshipExistsBetween(
            final Collection<RelationshipRow> rows,
            final EntityInstance first,
            final EntityInstance second,
            final String relationshipName) {
        for (RelationshipRow row : rows) {
            if (row.relationshipDefinitionIsKnownAs(relationshipName)
                    && row.connects(first, second)) {
                return true;
            }
        }
        return false;
    }

    public boolean relationshipExistsBetween(
            final Collection<RelationshipVectorDefinition> vectors,
            final EntityInstance first,
            final EntityInstance second,
            final String relationshipName,
            final RelationshipRowLookup lookup) {
        for (RelationshipVectorDefinition vector : vectors) {
            if (!vector.getRelationshipDefinition().isKnownAs(relationshipName)) {
                continue;
            }
            if (lookup.exists(vector, first.getInternalId(), second.getInternalId())) {
                return true;
            }
            if (lookup.exists(vector, second.getInternalId(), first.getInternalId())) {
                return true;
            }
        }
        return false;
    }

    public void assertCardinalityAllows(
            final RelationshipVectorDefinition vector,
            final EntityInstance from,
            final EntityInstance to,
            final RelationshipEndpointCounter counter) {
        if (vector.getCardinality().hasMaximumLimit()
                && countRowsForVector(from, vector, counter)
                        >= vector.getCardinality().maximumLimit()) {
            throw new RuntimeException(
                    String.format(
                            "Cannot add relationship type %s, exceeds maximum %d",
                            vector.getName(), vector.getCardinality().maximumLimit()));
        }

        if (!vector.getRelationshipDefinition().isTwoWay()) {
            return;
        }

        RelationshipVectorDefinition reverse =
                vector.getRelationshipDefinition().otherVectorOf(vector);
        if (reverse != null
                && reverse.getCardinality().hasMaximumLimit()
                && countRowsForVector(to, reverse, counter)
                        >= reverse.getCardinality().maximumLimit()) {
            throw new RuntimeException(
                    String.format(
                            "Cannot add relationship type %s, exceeds maximum %d",
                            reverse.getName(), reverse.getCardinality().maximumLimit()));
        }
    }

    public int countRowsForVector(
            final EntityInstance instance,
            final RelationshipVectorDefinition vector,
            final RelationshipEndpointCounter counter) {
        int count = counter.count(vector, RelationshipEndpoint.FROM, instance.getInternalId());

        if (vector.getRelationshipDefinition().isTwoWay()) {
            RelationshipVectorDefinition reverse =
                    vector.getRelationshipDefinition().otherVectorOf(vector);
            if (reverse != null) {
                count += counter.count(reverse, RelationshipEndpoint.TO, instance.getInternalId());
            }
        }

        return count;
    }

    public ValidationReport validateRelationships(
            final EntityInstance instance,
            final Collection<RelationshipRow> rowsInvolvingInstance,
            final RelationshipInstanceResolver resolver,
            final RelationshipEndpointCounter counter) {
        ValidationReport report = new ValidationReport();

        for (RelationshipVectorDefinition vector :
                instance.getEntity().related().getRelationships()) {
            int count = countRowsForVector(instance, vector, counter);

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

        for (RelationshipRow row : rowsInvolvingInstance) {
            ValidationReport rowReport = validateRelationshipRow(row, resolver);
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

    public ValidationReport validateRelationshipRow(
            final RelationshipRow row, final RelationshipInstanceResolver resolver) {
        ValidationReport report = new ValidationReport();
        EntityInstance from = resolver.resolve(row.getVector().getFrom(), row.getFromInternalId());
        EntityInstance to = resolver.resolve(row.getVector().getTo(), row.getToInternalId());

        if (from == null) {
            report.setValid(false).addErrorMessage("No From Instance found");
        }
        if (to == null) {
            report.setValid(false).addErrorMessage("No To Instance found");
        }
        if (!report.isValid()) {
            return report;
        }
        if (from.getEntity() != row.getVector().getFrom()) {
            report.setValid(false)
                    .addErrorMessage(
                            String.format(
                                    "Found from EntityInstance types %s but expected of type %s",
                                    from.getEntity().getName(),
                                    row.getVector().getFrom().getName()));
        }
        if (to.getEntity() != row.getVector().getTo()) {
            report.setValid(false)
                    .addErrorMessage(
                            String.format(
                                    "Found to EntityInstance types %s but expected of type %s",
                                    to.getEntity().getName(), row.getVector().getTo().getName()));
        }
        return report;
    }

    public List<EntityInstance> instancesSubjectToMandatoryRelationship(
            final RelationshipRow row, final RelationshipInstanceResolver resolver) {
        List<EntityInstance> deleteThese = new ArrayList<>();

        if (row.getVector().getOptionality() == MANDATORY_RELATIONSHIP) {
            EntityInstance from =
                    resolver.resolve(row.getVector().getFrom(), row.getFromInternalId());
            if (from != null) {
                deleteThese.add(from);
            }
        }

        if (row.getVector().getRelationshipDefinition().isTwoWay()) {
            RelationshipVectorDefinition otherVector =
                    row.getVector().getRelationshipDefinition().otherVectorOf(row.getVector());
            if (otherVector != null && otherVector.getOptionality() == MANDATORY_RELATIONSHIP) {
                EntityInstance to =
                        resolver.resolve(row.getVector().getTo(), row.getToInternalId());
                if (to != null) {
                    deleteThese.add(to);
                }
            }
        }

        return deleteThese;
    }

    public EntityInstance relatedInstance(
            final EntityInstance instance,
            final RelationshipRow row,
            final RelationshipInstanceResolver resolver) {
        if (row.getFromInternalId().equals(instance.getInternalId())) {
            return resolver.resolve(row.getVector().getTo(), row.getToInternalId());
        }
        if (row.getToInternalId().equals(instance.getInternalId())
                && row.getVector().getRelationshipDefinition().isTwoWay()) {
            return resolver.resolve(row.getVector().getFrom(), row.getFromInternalId());
        }
        return null;
    }
}
