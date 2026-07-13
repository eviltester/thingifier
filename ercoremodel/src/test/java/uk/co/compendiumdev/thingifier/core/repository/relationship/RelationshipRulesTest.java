package uk.co.compendiumdev.thingifier.core.repository.relationship;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.repository.MutableEntityInstance;

class RelationshipRulesTest {

    private final RelationshipRules rules = new RelationshipRules();

    @Test
    void unknownRelationshipNamesFailConnectionLookup() {

        TwoWayModel model = twoWayModel();

        IllegalArgumentException error =
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                rules.relationshipVectorForConnection(
                                        model.task, "missing", model.project));

        Assertions.assertTrue(
                error.getMessage()
                        .contains("Unknown relationship missing between task and project"));
    }

    @Test
    void connectionValidationFailsWhenInstanceTypesDoNotMatchTheVector() {

        TwoWayModel model = twoWayModel();

        IllegalArgumentException error =
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> rules.validateConnection(model.taskOf(), model.project, model.task));

        Assertions.assertTrue(
                error.getMessage()
                        .contains(
                                "Found from EntityInstance types project but expected of type task"));
    }

    @Test
    void duplicateRelationshipsAreDetectedFromRowsAndStorageLookups() {

        TwoWayModel model = twoWayModel();
        List<RelationshipRow> rows = List.of(row(model.taskOf(), model.task, model.project));

        Assertions.assertTrue(
                rules.relationshipExistsBetween(rows, model.task, model.project, "task-of"));
        Assertions.assertTrue(
                rules.relationshipExistsBetween(rows, model.project, model.task, "tasks"));
        Assertions.assertTrue(
                rules.relationshipExistsBetween(
                        model.vectors(), model.project, model.task, "tasks", lookupFrom(rows)));
    }

    @Test
    void forwardCardinalityViolationsAreRejected() {

        TwoWayModel model = twoWayModel();
        EntityInstance anotherProject = instance(model.projectDefinition);
        List<RelationshipRow> rows = List.of(row(model.taskOf(), model.task, model.project));

        RuntimeException error =
                Assertions.assertThrows(
                        RuntimeException.class,
                        () ->
                                rules.assertCardinalityAllows(
                                        model.taskOf(),
                                        model.task,
                                        anotherProject,
                                        counterFrom(rows)));

        Assertions.assertTrue(
                error.getMessage()
                        .contains("Cannot add relationship type task-of, exceeds maximum 1"));
    }

    @Test
    void reverseCardinalityViolationsAreRejected() {

        TwoWayModel model = twoWayModel();
        EntityInstance anotherProject = instance(model.projectDefinition);
        List<RelationshipRow> rows = List.of(row(model.tasks(), model.project, model.task));

        RuntimeException error =
                Assertions.assertThrows(
                        RuntimeException.class,
                        () ->
                                rules.assertCardinalityAllows(
                                        model.tasks(),
                                        anotherProject,
                                        model.task,
                                        counterFrom(rows)));

        Assertions.assertTrue(
                error.getMessage()
                        .contains("Cannot add relationship type task-of, exceeds maximum 1"));
    }

    @Test
    void mandatoryRelationshipValidationUsesSharedCountSemantics() {

        MandatoryModel model = mandatoryModel(true);

        ValidationReport missing =
                rules.validateRelationships(
                        model.child,
                        List.of(),
                        resolverFor(model.child, model.parent),
                        counterFrom(List.of()));

        Assertions.assertFalse(missing.isValid());
        Assertions.assertTrue(
                missing.getCombinedErrorMessages()
                        .contains("Mandatory Relationship not found parent"));

        List<RelationshipRow> rows = List.of(row(model.parent(), model.child, model.parent));
        ValidationReport valid =
                rules.validateRelationships(
                        model.child,
                        rows,
                        resolverFor(model.child, model.parent),
                        counterFrom(rows));

        Assertions.assertTrue(valid.isValid());
    }

    @Test
    void relationshipRowValidationReportsMissingEndpoints() {

        TwoWayModel model = twoWayModel();

        ValidationReport report =
                rules.validateRelationshipRow(
                        new RelationshipRow(model.taskOf(), "missing-from", "missing-to"),
                        (entity, internalId) -> null);

        Assertions.assertFalse(report.isValid());
        Assertions.assertTrue(report.getCombinedErrorMessages().contains("No From Instance found"));
        Assertions.assertTrue(report.getCombinedErrorMessages().contains("No To Instance found"));
    }

    @Test
    void mandatoryCascadeDecisionReturnsMandatoryFromSideForOneWayRelationships() {

        MandatoryModel model = mandatoryModel(false);

        List<EntityInstance> alsoDelete =
                rules.instancesSubjectToMandatoryRelationship(
                        row(model.parent(), model.child, model.parent),
                        resolverFor(model.child, model.parent));

        Assertions.assertEquals(List.of(model.child), alsoDelete);
    }

    @Test
    void mandatoryCascadeDecisionReturnsMandatoryReverseSideForTwoWayRelationships() {

        TwoWayModel model = twoWayModel();
        model.tasks().setOptionality(Optionality.MANDATORY_RELATIONSHIP);

        List<EntityInstance> alsoDelete =
                rules.instancesSubjectToMandatoryRelationship(
                        row(model.taskOf(), model.task, model.project),
                        resolverFor(model.task, model.project));

        Assertions.assertEquals(List.of(model.project), alsoDelete);
    }

    private RelationshipRow row(
            final RelationshipVectorDefinition vector,
            final EntityInstance from,
            final EntityInstance to) {
        return new RelationshipRow(vector, from.getInternalId(), to.getInternalId());
    }

    private RelationshipRowLookup lookupFrom(final Collection<RelationshipRow> rows) {
        return (vector, fromInternalId, toInternalId) ->
                rows.stream()
                        .anyMatch(
                                row ->
                                        row.getVector() == vector
                                                && row.getFromInternalId().equals(fromInternalId)
                                                && row.getToInternalId().equals(toInternalId));
    }

    private RelationshipEndpointCounter counterFrom(final Collection<RelationshipRow> rows) {
        return (vector, endpoint, internalId) ->
                (int)
                        rows.stream()
                                .filter(row -> row.matchesEndpoint(vector, endpoint, internalId))
                                .count();
    }

    private RelationshipInstanceResolver resolverFor(final EntityInstance... instances) {
        return (entity, internalId) ->
                Arrays.stream(instances)
                        .filter(
                                instance ->
                                        instance.getEntity() == entity
                                                && instance.getInternalId().equals(internalId))
                        .findFirst()
                        .orElse(null);
    }

    private EntityInstance instance(final EntityDefinition entity) {
        return MutableEntityInstance.snapshotFromDraft(EntityInstanceDraft.forEntity(entity));
    }

    private TwoWayModel twoWayModel() {
        EntityDefinition taskDefinition = new EntityDefinition("task", "tasks");
        EntityDefinition projectDefinition = new EntityDefinition("project", "projects");
        RelationshipDefinition.create(
                        new RelationshipVectorDefinition(
                                taskDefinition,
                                "task-of",
                                projectDefinition,
                                Cardinality.ONE_TO_ONE()))
                .whenReversed(Cardinality.ONE_TO_MANY(), "tasks");
        return new TwoWayModel(
                taskDefinition,
                projectDefinition,
                instance(taskDefinition),
                instance(projectDefinition));
    }

    private MandatoryModel mandatoryModel(final boolean twoWay) {
        EntityDefinition childDefinition = new EntityDefinition("child", "children");
        EntityDefinition parentDefinition = new EntityDefinition("parent", "parents");
        RelationshipDefinition relationship =
                RelationshipDefinition.create(
                        new RelationshipVectorDefinition(
                                childDefinition,
                                "parent",
                                parentDefinition,
                                Cardinality.ONE_TO_ONE()));
        if (twoWay) {
            relationship.whenReversed(Cardinality.ONE_TO_MANY(), "children");
        }
        relationship.getFromRelationship().setOptionality(Optionality.MANDATORY_RELATIONSHIP);
        return new MandatoryModel(
                instance(childDefinition), instance(parentDefinition), relationship);
    }

    private static final class TwoWayModel {
        private final EntityDefinition taskDefinition;
        private final EntityDefinition projectDefinition;
        private final EntityInstance task;
        private final EntityInstance project;

        private TwoWayModel(
                final EntityDefinition taskDefinition,
                final EntityDefinition projectDefinition,
                final EntityInstance task,
                final EntityInstance project) {
            this.taskDefinition = taskDefinition;
            this.projectDefinition = projectDefinition;
            this.task = task;
            this.project = project;
        }

        private RelationshipVectorDefinition taskOf() {
            return taskDefinition.getNamedRelationshipTo("task-of", projectDefinition);
        }

        private RelationshipVectorDefinition tasks() {
            return projectDefinition.getNamedRelationshipTo("tasks", taskDefinition);
        }

        private List<RelationshipVectorDefinition> vectors() {
            return List.of(taskOf(), tasks());
        }
    }

    private static final class MandatoryModel {
        private final EntityInstance child;
        private final EntityInstance parent;
        private final RelationshipDefinition relationship;

        private MandatoryModel(
                final EntityInstance child,
                final EntityInstance parent,
                final RelationshipDefinition relationship) {
            this.child = child;
            this.parent = parent;
            this.relationship = relationship;
        }

        private RelationshipVectorDefinition parent() {
            return relationship.getFromRelationship();
        }
    }
}
