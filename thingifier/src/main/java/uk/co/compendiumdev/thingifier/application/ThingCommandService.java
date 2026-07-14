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
import uk.co.compendiumdev.thingifier.application.command.PutThingCommand;
import uk.co.compendiumdev.thingifier.application.command.RelationshipReference;
import uk.co.compendiumdev.thingifier.application.command.ThingWriteCommand;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
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
            return amend(amend);
        }

        if (command instanceof DeleteThingCommand) {
            return delete((DeleteThingCommand) command);
        }

        if (command instanceof PutThingCommand) {
            return put((PutThingCommand) command);
        }

        if (command instanceof ConnectExistingRelationshipCommand) {
            ConnectExistingRelationshipCommand connect =
                    (ConnectExistingRelationshipCommand) command;
            return connectExistingRelationship(connect);
        }

        if (command instanceof CreateAndConnectRelationshipCommand) {
            return createAndConnect((CreateAndConnectRelationshipCommand) command);
        }

        if (command instanceof DisconnectRelationshipCommand) {
            DisconnectRelationshipCommand disconnect = (DisconnectRelationshipCommand) command;
            return disconnectRelationship(disconnect);
        }

        return ThingCommandResult.error(
                ApplicationError.unsupported(
                        String.format(
                                "Unsupported command %s", command.getClass().getSimpleName())));
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

    private ThingCommandResult amend(final AmendThingCommand command) {
        if (command.hasResolvedInstance()) {
            return amend(
                    command.getInstance(),
                    command.getDraft(),
                    command.shouldReplaceExistingFieldsAndRelationships(),
                    command.getRelationships());
        }

        EntityInstance instance = resolveInstance(command.getEntity(), command.getIdentifier());
        if (instance == null) {
            String message = command.getMissingInstanceMessage();
            if (message == null || message.isEmpty()) {
                message =
                        String.format(
                                "Could not find any instances with %s", command.getIdentifier());
            }
            return ThingCommandResult.error(ApplicationError.notFound(message));
        }

        try {
            EntityInstanceDraft draft =
                    new EntityInstanceDraftBuilder(instance)
                            .setFieldValuesFrom(command.getFieldValues());
            return amend(
                    instance,
                    draft,
                    command.shouldReplaceExistingFieldsAndRelationships(),
                    command.getRelationships());
        } catch (Exception e) {
            return ThingCommandResult.error(messageFrom(e));
        }
    }

    private ThingCommandResult put(final PutThingCommand command) {
        EntityInstance instance = resolveInstance(command.getEntity(), command.getIdentifier());
        if (instance != null) {
            try {
                EntityInstanceDraft draft =
                        new EntityInstanceDraftBuilder(instance)
                                .setFieldValuesFrom(command.getFieldValues());
                return amend(instance, draft, true, command.getRelationships());
            } catch (Exception e) {
                return ThingCommandResult.error(messageFrom(e));
            }
        }

        try {
            ThingCommandResult creationAllowed = validatePutCreate(command);
            if (creationAllowed != null) {
                return creationAllowed;
            }
            EntityInstanceDraft draft = createDraftWithPrimaryKey(command);
            ThingCommandResult created = create(draft, command.getRelationships(), true);
            if (created.isError()) {
                return created;
            }
            return ThingCommandResult.created(created.getInstance());
        } catch (Exception e) {
            return ThingCommandResult.error(messageFrom(e));
        }
    }

    private ThingCommandResult validatePutCreate(final PutThingCommand command) {
        List<Field> forbiddenPutCreationFields =
                command.getEntity().getFieldsOfType(FieldType.AUTO_INCREMENT, FieldType.AUTO_GUID);
        if (!forbiddenPutCreationFields.isEmpty()) {
            return ThingCommandResult.error(
                    String.format(
                            "Cannot create %s with PUT due to Auto fields %s",
                            command.getEntity().getName(), fieldNames(forbiddenPutCreationFields)));
        }

        Field primaryKey = command.getEntity().getPrimaryKeyField();
        for (NamedValue namedValue : command.getFieldValues()) {
            if (namedValue.name.equals(primaryKey.getName())
                    && !namedValue.value.equals(command.getIdentifier())) {
                return ThingCommandResult.error(
                        String.format(
                                "Cannot create %s with PUT as key does not match body value %s != %s",
                                command.getEntity().getName(),
                                command.getIdentifier(),
                                namedValue.value));
            }
        }

        return null;
    }

    private EntityInstanceDraft createDraftWithPrimaryKey(final PutThingCommand command) {
        EntityInstanceDraft baseDraft = EntityInstanceDraft.forEntity(command.getEntity());
        Field primaryKeyField = command.getEntity().getPrimaryKeyField();
        if (primaryKeyField.getType() == FieldType.AUTO_INCREMENT
                || primaryKeyField.getType() == FieldType.AUTO_GUID) {
            baseDraft.withProtectedField(primaryKeyField.getName(), command.getIdentifier());
        } else {
            baseDraft.withField(primaryKeyField.getName(), command.getIdentifier());
        }

        List<NamedValue> fieldValues = new ArrayList<>(command.getFieldValues());
        store.administration().accommodateProtectedIds(command.getEntity(), fieldValues);
        EntityInstanceDraft draft =
                new EntityInstanceDraftBuilder(command.getEntity())
                        .overrideFieldValuesFromArgsIgnoring(
                                fieldValues,
                                command.getEntity().getFieldNamesOfType(FieldType.AUTO_GUID));
        copyBaseDraftValues(baseDraft, draft);
        return draft;
    }

    public ThingCommandResult delete(final EntityInstance instance) {
        try {
            store.entities().delete(instance);
            return ThingCommandResult.success();
        } catch (Exception e) {
            return ThingCommandResult.error(messageFrom(e));
        }
    }

    private ThingCommandResult delete(final DeleteThingCommand command) {
        if (command.hasResolvedInstance()) {
            return delete(command.getInstance());
        }

        EntityInstance instance = resolveInstance(command.getEntity(), command.getIdentifier());
        if (instance == null) {
            return ThingCommandResult.error(
                    ApplicationError.notFound(
                            String.format(
                                    "Could not find any instances with %s",
                                    command.getRouteDisplay())));
        }
        return delete(instance);
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

    private ThingCommandResult disconnectRelationship(final DisconnectRelationshipCommand command) {
        if (command.hasResolvedRelationship()) {
            return disconnectRelationship(
                    command.getParent(), command.getChild(), command.getRelationshipName());
        }

        EntityInstance parent =
                resolveInstance(command.getParentEntity(), command.getParentIdentifier());
        if (parent == null) {
            return relationshipRouteNotFound(command.getRouteDisplay());
        }

        EntityInstance child =
                relatedInstanceMatchingIdentifier(
                        parent, command.getRelationshipName(), command.getChildIdentifier());
        if (child == null) {
            return relationshipRouteNotFound(command.getRouteDisplay());
        }

        return disconnectRelationship(parent, child, command.getRelationshipName());
    }

    public ThingCommandResult connectRelationships(
            final EntityInstance instance, final List<RelationshipReference> relationships) {
        return connectRelationshipReferences(instance, relationships, true, false);
    }

    private ThingCommandResult createAndConnect(final CreateAndConnectRelationshipCommand command) {
        EntityInstance parent = command.getParent();
        if (!command.hasResolvedParent()) {
            parent = resolveInstance(command.getParentEntity(), command.getParentIdentifier());
            if (parent == null) {
                return ThingCommandResult.error(
                        ApplicationError.notFound(
                                String.format(
                                        "Could not find parent thing for relationship %s",
                                        command.getRouteDisplay())));
            }
        }

        ThingCommandResult createResult =
                create(command.getChildDraft(), command.getChildRelationships(), false);
        if (createResult.isError()) {
            return createResult;
        }

        ThingCommandResult connectResult =
                connectRelationship(
                        parent, command.getRelationshipName(), createResult.getInstance(), true);
        if (connectResult.isError()) {
            return connectResult.withRolledBackCreatedInstance();
        }

        return ThingCommandResult.success(createResult.getInstance());
    }

    private ThingCommandResult connectExistingRelationship(
            final ConnectExistingRelationshipCommand command) {
        if (command.hasResolvedRelationship()) {
            return connectRelationship(
                    command.getParent(), command.getRelationshipName(), command.getChild(), false);
        }

        EntityInstance parent =
                resolveInstance(command.getParentEntity(), command.getParentIdentifier());
        if (parent == null) {
            return ThingCommandResult.error(
                    ApplicationError.notFound(
                            String.format(
                                    "Could not find parent thing for relationship %s",
                                    command.getRouteDisplay())));
        }

        RelatedItemResolution related =
                resolveRelatedItemFromReferenceFields(
                        parent, command.getRelationshipName(), command.getChildReferenceFields());
        if (related.error != null) {
            return ThingCommandResult.error(related.error);
        }

        RelationshipVectorDefinition relationshipToUse =
                parent.getEntity()
                        .getNamedRelationshipTo(
                                command.getRelationshipName(), related.instance.getEntity());
        ThingCommandResult relationshipError =
                relationshipErrorIfInvalid(
                        parent, related.instance, relationshipToUse, command.getRelationshipName());
        if (relationshipError != null) {
            return relationshipError;
        }

        return connectRelationship(parent, relationshipToUse.getName(), related.instance, false);
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

    private EntityInstance resolveInstance(
            final EntityDefinition entity, final String queryIdentifier) {
        return store.entityQueries().findByQueryIdentifier(entity, queryIdentifier);
    }

    private ThingCommandResult relationshipRouteNotFound(final String routeDisplay) {
        return ThingCommandResult.error(
                ApplicationError.notFound(
                        String.format("Could not find any instances with %s", routeDisplay)));
    }

    private RelatedItemResolution resolveRelatedItemFromReferenceFields(
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

    private ThingCommandResult relationshipErrorIfInvalid(
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

    private EntityInstance relatedInstanceMatchingIdentifier(
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

    private void copyBaseDraftValues(
            final EntityInstanceDraft baseDraft, final EntityInstanceDraft draft) {
        for (NamedValue value : baseDraft.getFieldValues()) {
            draft.withField(value.getName(), value.asString());
        }
        for (NamedValue protectedValue : baseDraft.getProtectedFieldValues()) {
            draft.withProtectedField(protectedValue.getName(), protectedValue.asString());
        }
    }

    private String fieldNames(final List<Field> fields) {
        String names = "";
        for (Field field : fields) {
            if (!names.isEmpty()) {
                names = names + ", ";
            }
            names = names + field.getName();
        }
        return names;
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

    private static final class RelatedItemResolution {

        private final EntityInstance instance;
        private final ApplicationError error;

        private RelatedItemResolution(final EntityInstance instance, final ApplicationError error) {
            this.instance = instance;
            this.error = error;
        }

        private static RelatedItemResolution success(final EntityInstance instance) {
            return new RelatedItemResolution(instance, null);
        }

        private static RelatedItemResolution error(final ApplicationError error) {
            return new RelatedItemResolution(null, error);
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
