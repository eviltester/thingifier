package uk.co.compendiumdev.thingifier.application;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import uk.co.compendiumdev.thingifier.application.command.AmendThingCommand;
import uk.co.compendiumdev.thingifier.application.command.ConnectExistingRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateAndConnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DeleteThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DisconnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.RelationshipReference;
import uk.co.compendiumdev.thingifier.application.command.ThingWriteCommand;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public final class ThingCommandService {

    private final ThingStore store;

    public ThingCommandService(final ThingStore store) {
        this.store = store;
    }

    public ThingCommandResult execute(final ThingWriteCommand command) {
        if (command instanceof CreateThingCommand) {
            CreateThingCommand create = (CreateThingCommand) command;
            return create(
                    create.getDraft(),
                    create.getRelationships(),
                    create.shouldValidateFinalRelationships());
        }

        if (command instanceof AmendThingCommand) {
            AmendThingCommand amend = (AmendThingCommand) command;
            return amend(
                    amend.getInstance(),
                    amend.getDraft(),
                    amend.shouldReplaceExistingFieldsAndRelationships(),
                    amend.getRelationships());
        }

        if (command instanceof DeleteThingCommand) {
            return delete(((DeleteThingCommand) command).getInstance());
        }

        if (command instanceof ConnectExistingRelationshipCommand) {
            ConnectExistingRelationshipCommand connect =
                    (ConnectExistingRelationshipCommand) command;
            return connectRelationship(
                    connect.getParent(), connect.getRelationshipName(), connect.getChild(), false);
        }

        if (command instanceof CreateAndConnectRelationshipCommand) {
            return createAndConnect((CreateAndConnectRelationshipCommand) command);
        }

        if (command instanceof DisconnectRelationshipCommand) {
            DisconnectRelationshipCommand disconnect = (DisconnectRelationshipCommand) command;
            return disconnectRelationship(
                    disconnect.getParent(),
                    disconnect.getChild(),
                    disconnect.getRelationshipName());
        }

        return ThingCommandResult.error(
                String.format("Unsupported command %s", command.getClass().getSimpleName()));
    }

    public ThingCommandResult create(
            final EntityInstanceDraft draft, final List<RelationshipReference> relationships) {
        return create(draft, relationships, true);
    }

    public ThingCommandResult create(
            final EntityInstanceDraft draft,
            final List<RelationshipReference> relationships,
            final boolean validateFinalRelationships) {
        EntityInstance created = null;
        try {
            created = store.entities().create(draft);
            ThingCommandResult relationshipResult =
                    connectRelationshipReferences(
                            created, relationships, validateFinalRelationships, true);
            if (relationshipResult.isError()) {
                rollbackDelete(created);
                return relationshipResult;
            }
            return ThingCommandResult.success(created);
        } catch (Exception e) {
            rollbackDelete(created);
            return ThingCommandResult.error(messageFrom(e));
        }
    }

    public ThingCommandResult amend(
            final EntityInstance instance,
            final EntityInstanceDraft draft,
            final boolean replaceExistingFieldsAndRelationships,
            final List<RelationshipReference> relationships) {
        EntityInstance updated = null;
        RelationshipSnapshot originalRelationships = RelationshipSnapshot.capture(store, instance);
        try {
            if (replaceExistingFieldsAndRelationships) {
                updated = store.entities().replace(instance, draft);
                originalRelationships.disconnectFrom(store, updated);
            } else {
                updated = store.entities().patch(instance, draft);
            }

            ThingCommandResult relationshipResult =
                    connectRelationshipReferences(updated, relationships, true, false);
            if (relationshipResult.isError()) {
                rollbackAmendment(updated, instance, originalRelationships);
                return relationshipResult;
            }

            if (replaceExistingFieldsAndRelationships) {
                originalRelationships.deleteFormerDependentsMadeInvalidBy(store, updated);
            }

            return ThingCommandResult.success(updated);
        } catch (Exception e) {
            if (updated != null) {
                rollbackAmendment(updated, instance, originalRelationships);
            }
            return ThingCommandResult.error(messageFrom(e));
        }
    }

    public ThingCommandResult delete(final EntityInstance instance) {
        try {
            store.entities().delete(instance);
            return ThingCommandResult.success();
        } catch (Exception e) {
            return ThingCommandResult.error(messageFrom(e));
        }
    }

    public ThingCommandResult connectRelationship(
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
                    rollbackDelete(child);
                }
                return ThingCommandResult.error(validNow.getErrorMessages());
            }

            return ThingCommandResult.success(child);
        } catch (Exception e) {
            if (!alreadyConnected) {
                store.relationships().disconnectBetween(parent, child, relationshipName);
            }
            if (deleteChildOnRollback) {
                rollbackDelete(child);
            }
            return ThingCommandResult.error(messageFrom(e));
        }
    }

    public ThingCommandResult disconnectRelationship(
            final EntityInstance parent,
            final EntityInstance child,
            final String relationshipName) {
        try {
            store.relationships().removeBetween(parent, child, relationshipName);
            return ThingCommandResult.success();
        } catch (Exception e) {
            return ThingCommandResult.error(messageFrom(e));
        }
    }

    public ThingCommandResult connectRelationships(
            final EntityInstance instance, final List<RelationshipReference> relationships) {
        return connectRelationshipReferences(instance, relationships, true, false);
    }

    private ThingCommandResult createAndConnect(final CreateAndConnectRelationshipCommand command) {
        ThingCommandResult createResult =
                create(command.getChildDraft(), command.getChildRelationships(), false);
        if (createResult.isError()) {
            return createResult;
        }

        ThingCommandResult connectResult =
                connectRelationship(
                        command.getParent(),
                        command.getRelationshipName(),
                        createResult.getInstance(),
                        true);
        if (connectResult.isError()) {
            return connectResult.withRolledBackCreatedInstance();
        }

        return ThingCommandResult.success(createResult.getInstance());
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
            return ThingCommandResult.error("Error creating relationships " + messageFrom(e));
        }
    }

    private ThingCommandResult connectRelationshipReferences(
            final EntityInstance instance,
            final List<RelationshipReference> references,
            final boolean validateFinalRelationships,
            final boolean prefixRelationshipErrors) {
        RelationshipResolution resolution = resolveRelationshipReferences(instance, references);
        if (resolution.hasErrors()) {
            if (prefixRelationshipErrors) {
                return ThingCommandResult.error(
                        "Invalid relationships: " + String.join(", ", resolution.errors));
            }
            return ThingCommandResult.error(resolution.errors);
        }

        return connectRelationships(instance, resolution.relationships, validateFinalRelationships);
    }

    private RelationshipResolution resolveRelationshipReferences(
            final EntityInstance instance, final List<RelationshipReference> references) {
        List<RelationshipConnection> relationships = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (RelationshipReference reference : references) {
            EntityInstance related = resolveRelationshipReference(instance, reference);
            if (related == null) {
                errors.add(missingRelationshipReferenceMessage(reference));
            } else {
                relationships.add(
                        new RelationshipConnection(reference.relationshipName(), related));
            }
        }

        return new RelationshipResolution(relationships, errors);
    }

    private EntityInstance resolveRelationshipReference(
            final EntityInstance instance, final RelationshipReference reference) {
        if (reference.hasExplicitTargetEntity()) {
            return resolveExplicitRelationshipReference(reference);
        }

        for (RelationshipVectorDefinition vector :
                instance.getEntity().related().getRelationships(reference.relationshipName())) {
            EntityDefinition relatedEntity = vector.getTo();
            EntityInstance related =
                    store.entityQueries()
                            .findByField(
                                    relatedEntity,
                                    reference.referenceFieldName(),
                                    reference.referenceValue());
            if (related == null) {
                related =
                        store.entityQueries()
                                .findByQueryIdentifier(relatedEntity, reference.referenceValue());
            }
            if (related != null) {
                return related;
            }
        }

        return null;
    }

    private EntityInstance resolveExplicitRelationshipReference(
            final RelationshipReference reference) {
        EntityInstance related =
                store.entityQueries()
                        .findByQueryIdentifier(
                                reference.targetEntity(), reference.referenceValue());
        if (related == null) {
            related =
                    store.entityQueries()
                            .findByField(
                                    reference.targetEntity(),
                                    reference.referenceFieldName(),
                                    reference.referenceValue());
        }
        return related;
    }

    private String missingRelationshipReferenceMessage(final RelationshipReference reference) {
        if (reference.hasExplicitTargetEntity()) {
            return String.format(
                    "cannot find %s of %s to relate to with %s %s",
                    reference.referenceFieldName(),
                    reference.targetTerm(),
                    reference.referenceFieldName(),
                    reference.referenceValue());
        }

        return String.format(
                "cannot find %s to relate to with %s %s",
                reference.relationshipName(),
                reference.referenceFieldName(),
                reference.referenceValue());
    }

    private void rollbackAmendment(
            final EntityInstance current,
            final EntityInstance original,
            final RelationshipSnapshot originalRelationships) {
        RelationshipSnapshot.capture(store, current).disconnectFrom(store, current);
        EntityInstance restored = store.entities().replace(current, draftFrom(original));
        originalRelationships.restoreTo(store, restored);
    }

    private void rollbackDelete(final EntityInstance instance) {
        if (instance != null) {
            store.entities().delete(instance);
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

    private String messageFrom(final Exception exception) {
        String message = exception.getMessage();
        return message == null ? "" : message;
    }

    private static final class RelationshipResolution {

        private final List<RelationshipConnection> relationships;
        private final List<String> errors;

        private RelationshipResolution(
                final List<RelationshipConnection> relationships, final List<String> errors) {
            this.relationships = relationships;
            this.errors = errors;
        }

        private boolean hasErrors() {
            return !errors.isEmpty();
        }
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
