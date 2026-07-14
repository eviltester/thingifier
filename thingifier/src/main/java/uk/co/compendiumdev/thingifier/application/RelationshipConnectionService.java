package uk.co.compendiumdev.thingifier.application;

import java.util.ArrayList;
import java.util.List;
import uk.co.compendiumdev.thingifier.application.command.RelationshipReference;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class RelationshipConnectionService {

    private final ThingStore store;
    private final RelationshipReferenceResolver relationshipResolver;

    RelationshipConnectionService(
            final ThingStore store, final RelationshipReferenceResolver relationshipResolver) {
        this.store = store;
        this.relationshipResolver = relationshipResolver;
    }

    ThingCommandResult connectRelationship(
            final EntityInstance parent,
            final String relationshipName,
            final EntityInstance child,
            final boolean deleteChildOnRollback) {
        boolean alreadyConnected =
                isRelated(parent, new RelationshipConnection(relationshipName, child));
        try {
            store.relationships().connect(parent, relationshipName, child);

            ValidationReport validNow = store.relationships().validate(child);
            if (!validNow.isValid()) {
                if (!alreadyConnected) {
                    store.relationships().disconnectBetween(parent, child, relationshipName);
                }
                if (deleteChildOnRollback) {
                    store.entities().delete(child);
                }
                return ThingCommandResult.error(validNow.getErrorMessages());
            }

            return ThingCommandResult.success(child);
        } catch (Exception e) {
            if (!alreadyConnected) {
                store.relationships().disconnectBetween(parent, child, relationshipName);
            }
            if (deleteChildOnRollback) {
                store.entities().delete(child);
            }
            return ThingCommandResult.error(ApplicationExceptionMessages.messageFrom(e));
        }
    }

    ThingCommandResult connectRelationshipReferences(
            final EntityInstance instance,
            final List<RelationshipReference> references,
            final boolean validateFinalRelationships,
            final boolean prefixRelationshipErrors) {
        RelationshipReferenceResolver.Resolution resolution =
                relationshipResolver.resolve(instance, references);
        if (resolution.hasErrors()) {
            if (prefixRelationshipErrors) {
                return ThingCommandResult.error(
                        "Invalid relationships: " + String.join(", ", resolution.errors()));
            }
            return ThingCommandResult.error(resolution.errors());
        }

        return connectRelationships(
                instance, resolution.relationships(), validateFinalRelationships);
    }

    ThingCommandResult relationshipErrorIfInvalid(
            final EntityInstance parent,
            final EntityInstance child,
            final RelationshipVectorDefinition relationshipToUse,
            final String relationshipName) {
        if (relationshipToUse == null) {
            return ThingCommandResult.error(
                    String.format(
                            "Could not find a relationship named %s between %s and a %s",
                            relationshipName,
                            parent.getEntity().getName(),
                            child.getEntity().getName()));
        }

        if (relationshipToUse.getTo() != child.getEntity()) {
            return ThingCommandResult.error(
                    String.format(
                            "Could not connect %s (%s) to %s (%s) via relationship %s because it is a %s instead of a %s",
                            parent.getPrimaryKeyValue(),
                            parent.getEntity().getName(),
                            child.getPrimaryKeyValue(),
                            child.getEntity().getName(),
                            relationshipToUse.getName(),
                            child.getEntity().getName(),
                            relationshipToUse.getTo().getName()));
        }
        return null;
    }

    private ThingCommandResult connectRelationships(
            final EntityInstance instance,
            final List<RelationshipConnection> relationships,
            final boolean validateFinalRelationships) {
        List<RelationshipConnection> connectedByCommand = new ArrayList<>();
        try {
            for (RelationshipConnection relationship : relationships) {
                boolean alreadyConnected = isRelated(instance, relationship);
                store.relationships()
                        .connect(
                                instance,
                                relationship.relationshipName(),
                                relationship.relatedInstance());
                if (!alreadyConnected) {
                    connectedByCommand.add(relationship);
                }
            }

            if (validateFinalRelationships) {
                ValidationReport finalRelationships = store.relationships().validate(instance);
                if (!finalRelationships.isValid()) {
                    disconnectConnections(instance, connectedByCommand);
                    return ThingCommandResult.error(finalRelationships.getErrorMessages());
                }
            }

            return ThingCommandResult.success(instance);
        } catch (Exception e) {
            disconnectConnections(instance, connectedByCommand);
            return ThingCommandResult.error(
                    "Error creating relationships " + ApplicationExceptionMessages.messageFrom(e));
        }
    }

    private void disconnectConnections(
            final EntityInstance instance, final List<RelationshipConnection> relationships) {
        for (RelationshipConnection relationship : relationships) {
            store.relationships()
                    .disconnectBetween(
                            instance,
                            relationship.relatedInstance(),
                            relationship.relationshipName());
        }
    }

    private boolean isRelated(
            final EntityInstance instance, final RelationshipConnection relationship) {
        for (EntityInstance related :
                store.relationships().listRelated(instance, relationship.relationshipName())) {
            if (related.getInternalId().equals(relationship.relatedInstance().getInternalId())) {
                return true;
            }
        }
        return false;
    }
}
