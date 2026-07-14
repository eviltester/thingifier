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
import uk.co.compendiumdev.thingifier.application.command.RelateThingCommand;
import uk.co.compendiumdev.thingifier.application.command.RelationshipReference;
import uk.co.compendiumdev.thingifier.application.command.ReplaceThingCommand;
import uk.co.compendiumdev.thingifier.application.command.ThingWriteCommand;
import uk.co.compendiumdev.thingifier.application.schema.SchemaDefinitionResolver;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public final class ThingCommandService {

    private final ThingStore store;
    private final SchemaDefinitionResolver schema;
    private final WriteValidationPolicy validation;
    private final RelationshipReferenceResolver relationshipResolver;
    private final WriteTransactionRunner transactionRunner;
    private final CreateThingHandler createHandler;
    private final AmendThingHandler amendHandler;
    private final RelationshipCommandHandler relationshipHandler;

    public ThingCommandService(final ThingStore store, final SchemaDefinitionResolver schema) {
        this(store, schema, false);
    }

    public ThingCommandService(
            final ThingStore store,
            final SchemaDefinitionResolver schema,
            final boolean enforceDeclaredTypes) {
        this.store = store;
        this.schema = schema;
        this.validation = new WriteValidationPolicy(store, enforceDeclaredTypes);
        this.relationshipResolver = new RelationshipReferenceResolver(store, schema);
        this.transactionRunner = new WriteTransactionRunner(store);
        this.createHandler = new CreateThingHandler(this);
        this.amendHandler = new AmendThingHandler(this);
        this.relationshipHandler = new RelationshipCommandHandler(this);
    }

    public ThingCommandResult execute(final ThingWriteCommand command) {
        return transactionRunner.run(() -> executeInsideTransaction(command));
    }

    private ThingCommandResult executeInsideTransaction(final ThingWriteCommand command) {
        if (command instanceof CreateThingCommand) {
            return createHandler.handle((CreateThingCommand) command);
        }

        if (command instanceof AmendThingCommand) {
            return amendHandler.handle((AmendThingCommand) command);
        }

        if (command instanceof DeleteThingCommand) {
            return delete((DeleteThingCommand) command);
        }

        if (command instanceof ReplaceThingCommand) {
            return amendHandler.handle((ReplaceThingCommand) command);
        }

        if (command instanceof ConnectExistingRelationshipCommand) {
            return relationshipHandler.handle((ConnectExistingRelationshipCommand) command);
        }

        if (command instanceof CreateAndConnectRelationshipCommand) {
            return relationshipHandler.handle((CreateAndConnectRelationshipCommand) command);
        }

        if (command instanceof RelateThingCommand) {
            return relationshipHandler.handle((RelateThingCommand) command);
        }

        if (command instanceof DisconnectRelationshipCommand) {
            return relationshipHandler.handle((DisconnectRelationshipCommand) command);
        }

        return ThingCommandResult.error(
                ApplicationError.unsupported(
                        String.format(
                                "Unsupported command %s", command.getClass().getSimpleName())));
    }

    ThingCommandResult create(final CreateThingCommand command) {
        EntityDefinition entity = entityNamed(command.getEntityName());
        if (entity == null) {
            return ThingCommandResult.error(
                    ApplicationError.notFound(
                            String.format("Could not find entity %s", command.getEntityName())));
        }

        ThingCommandResult typeValidation =
                validation.validateDeclaredFieldTypes(entity, command.getBodyFields());
        if (typeValidation != null) {
            return typeValidation;
        }

        List<NamedValue> fieldValues =
                validation.normalizedFieldValues(
                        entity, command.getFieldValues(), command.getBodyFields());
        ThingCommandResult validationResult =
                validation.validateCreate(
                        entity,
                        fieldValues,
                        command.hasRequestedPrimaryKey(),
                        command.getRequestedPrimaryKey());
        if (validationResult != null) {
            return validationResult;
        }

        try {
            EntityInstanceDraft draft =
                    createDraft(entity, command.getRequestedPrimaryKey(), fieldValues);
            return create(
                    draft, command.getRelationships(), command.shouldValidateFinalRelationships());
        } catch (Exception e) {
            return ThingCommandResult.error(messageFrom(e));
        }
    }

    ThingCommandResult create(
            final EntityInstanceDraft draft,
            final List<RelationshipReference> relationships,
            final boolean validateFinalRelationships) {
        try {
            EntityInstance created = store.entities().create(draft);
            ThingCommandResult relationshipResult =
                    connectRelationshipReferences(
                            created, relationships, validateFinalRelationships, true);
            if (relationshipResult.isError()) {
                return relationshipResult;
            }
            return ThingCommandResult.success(created);
        } catch (Exception e) {
            return ThingCommandResult.error(messageFrom(e));
        }
    }

    ThingCommandResult amend(final AmendThingCommand command) {
        EntityDefinition entity = entityNamed(command.getEntityName());
        ThingCommandResult typeValidation =
                validation.validateDeclaredFieldTypesIgnoringProtected(
                        entity, command.getBodyFields());
        if (typeValidation != null) {
            return typeValidation;
        }

        EntityInstance instance = resolveInstance(entity, command.getIdentifier());
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
            List<NamedValue> fieldValues =
                    validation.normalizedFieldValues(
                            entity, command.getFieldValues(), command.getBodyFields());
            EntityInstanceDraft draft =
                    new EntityInstanceDraftBuilder(instance).setFieldValuesFrom(fieldValues);
            return amend(
                    instance,
                    draft,
                    command.shouldReplaceExistingFieldsAndRelationships(),
                    command.getRelationships());
        } catch (Exception e) {
            return ThingCommandResult.error(messageFrom(e));
        }
    }

    ThingCommandResult amend(
            final EntityInstance instance,
            final EntityInstanceDraft draft,
            final boolean replaceExistingFieldsAndRelationships,
            final List<RelationshipReference> relationships) {
        RelationshipSnapshot originalRelationships = RelationshipSnapshot.capture(store, instance);
        try {
            EntityInstance updated;
            if (replaceExistingFieldsAndRelationships) {
                updated = store.entities().replace(instance, draft);
                originalRelationships.disconnectFrom(store, updated);
            } else {
                updated = store.entities().patch(instance, draft);
            }

            ThingCommandResult relationshipResult =
                    connectRelationshipReferences(updated, relationships, true, false);
            if (relationshipResult.isError()) {
                return relationshipResult;
            }

            if (replaceExistingFieldsAndRelationships) {
                originalRelationships.deleteFormerDependentsMadeInvalidBy(store, updated);
            }

            return ThingCommandResult.success(updated);
        } catch (Exception e) {
            return ThingCommandResult.error(messageFrom(e));
        }
    }

    ThingCommandResult replace(final ReplaceThingCommand command) {
        EntityDefinition entity = entityNamed(command.getEntityName());
        ThingCommandResult typeValidation =
                validation.validateDeclaredFieldTypesIgnoringProtected(
                        entity, command.getBodyFields());
        if (typeValidation != null) {
            return typeValidation;
        }

        List<NamedValue> fieldValues =
                validation.normalizedFieldValues(
                        entity, command.getFieldValues(), command.getBodyFields());
        EntityInstance instance = resolveInstance(entity, command.getIdentifier());
        if (instance != null) {
            try {
                EntityInstanceDraft draft =
                        new EntityInstanceDraftBuilder(instance).setFieldValuesFrom(fieldValues);
                return amend(instance, draft, true, command.getRelationships());
            } catch (Exception e) {
                return ThingCommandResult.error(messageFrom(e));
            }
        }

        ThingCommandResult creationAllowed =
                validation.validateReplaceCreate(entity, command.getIdentifier(), fieldValues);
        if (creationAllowed != null) {
            return creationAllowed;
        }

        try {
            EntityInstanceDraft draft = createDraft(entity, command.getIdentifier(), fieldValues);
            ThingCommandResult created = create(draft, command.getRelationships(), true);
            if (created.isError()) {
                return created;
            }
            return ThingCommandResult.created(created.getInstance());
        } catch (Exception e) {
            return ThingCommandResult.error(messageFrom(e));
        }
    }

    private ThingCommandResult delete(final DeleteThingCommand command) {
        EntityDefinition entity = entityNamed(command.getEntityName());
        EntityInstance instance = resolveInstance(entity, command.getIdentifier());
        if (instance == null) {
            return ThingCommandResult.error(
                    ApplicationError.notFound(
                            String.format(
                                    "Could not find any instances with %s",
                                    command.getRouteDisplay())));
        }

        try {
            store.entities().delete(instance);
            return ThingCommandResult.success();
        } catch (Exception e) {
            return ThingCommandResult.error(messageFrom(e));
        }
    }

    ThingCommandResult connectExistingRelationship(
            final ConnectExistingRelationshipCommand command) {
        EntityDefinition parentEntity = entityNamed(command.getParentEntityName());
        EntityInstance parent = resolveInstance(parentEntity, command.getParentIdentifier());
        if (parent == null) {
            return parentNotFound(command.getRouteDisplay());
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

    ThingCommandResult createAndConnect(final CreateAndConnectRelationshipCommand command) {
        EntityDefinition parentEntity = entityNamed(command.getParentEntityName());
        EntityInstance parent = resolveInstance(parentEntity, command.getParentIdentifier());
        if (parent == null) {
            return parentNotFound(command.getRouteDisplay());
        }

        EntityDefinition childEntity = entityNamed(command.getChildEntityName());
        ThingCommandResult typeValidation =
                validation.validateDeclaredFieldTypes(childEntity, command.getChildBodyFields());
        if (typeValidation != null) {
            return typeValidation;
        }

        List<NamedValue> childFieldValues =
                validation.normalizedFieldValues(
                        childEntity, command.getChildFieldValues(), command.getChildBodyFields());
        ThingCommandResult validationResult =
                validation.validateCreate(childEntity, childFieldValues, false, "");
        if (validationResult != null) {
            return validationResult;
        }

        try {
            EntityInstanceDraft childDraft = createDraft(childEntity, "", childFieldValues);
            ThingCommandResult createResult =
                    create(childDraft, command.getChildRelationships(), false);
            if (createResult.isError()) {
                return createResult;
            }

            ThingCommandResult connectResult =
                    connectRelationship(
                            parent,
                            command.getRelationshipName(),
                            createResult.getInstance(),
                            true);
            if (connectResult.isError()) {
                return connectResult.withRolledBackCreatedInstance();
            }

            return ThingCommandResult.success(createResult.getInstance());
        } catch (Exception e) {
            return ThingCommandResult.error(messageFrom(e));
        }
    }

    ThingCommandResult relate(final RelateThingCommand command) {
        EntityDefinition parentEntity = entityNamed(command.getParentEntityName());
        if (parentEntity == null) {
            return parentNotFound(command.getRouteDisplay());
        }

        RelationshipVectorDefinition vector =
                firstRelationshipVector(parentEntity, command.getRelationshipName());
        if (vector == null) {
            return ThingCommandResult.error(
                    String.format(
                            "Could not find a relationship named %s for %s",
                            command.getRelationshipName(), parentEntity.getName()));
        }

        List<NamedValue> bodyFieldValues =
                validation.normalizedFieldValues(
                        vector.getTo(), command.getBodyFieldValues(), command.getBodyFields());
        boolean referencesExistingRelatedItem =
                bodyReferencesExistingRelatedItem(vector.getTo(), bodyFieldValues);

        ThingCommandResult typeValidation =
                referencesExistingRelatedItem
                        ? validation.validateDeclaredFieldTypesIgnoringProtected(
                                vector.getTo(), command.getBodyFields())
                        : validation.validateDeclaredFieldTypes(
                                vector.getTo(), command.getBodyFields());
        if (typeValidation != null) {
            return typeValidation;
        }

        EntityInstance parent = resolveInstance(parentEntity, command.getParentIdentifier());
        if (parent == null) {
            return parentNotFound(command.getRouteDisplay());
        }

        if (referencesExistingRelatedItem) {
            ConnectExistingRelationshipCommand connect =
                    new ConnectExistingRelationshipCommand(
                            command.getParentEntityName(),
                            command.getParentIdentifier(),
                            command.getRelationshipName(),
                            bodyFieldValues,
                            command.getRouteDisplay());
            return connectExistingRelationship(connect);
        }

        CreateAndConnectRelationshipCommand create =
                new CreateAndConnectRelationshipCommand(
                        command.getParentEntityName(),
                        command.getParentIdentifier(),
                        command.getRelationshipName(),
                        vector.getTo().getName(),
                        bodyFieldValues,
                        command.getBodyFields(),
                        command.getBodyRelationships(),
                        command.getRouteDisplay());
        ThingCommandResult result = createAndConnect(create);
        if (result.isSuccessful()) {
            return ThingCommandResult.created(result.getInstance());
        }
        return result;
    }

    ThingCommandResult disconnectRelationship(final DisconnectRelationshipCommand command) {
        EntityDefinition parentEntity = entityNamed(command.getParentEntityName());
        EntityInstance parent = resolveInstance(parentEntity, command.getParentIdentifier());
        if (parent == null) {
            return relationshipRouteNotFound(command.getRouteDisplay());
        }

        EntityInstance child =
                relatedInstanceMatchingIdentifier(
                        parent, command.getRelationshipName(), command.getChildIdentifier());
        if (child == null) {
            return relationshipRouteNotFound(command.getRouteDisplay());
        }

        try {
            store.relationships().removeBetween(parent, child, command.getRelationshipName());
            return ThingCommandResult.success();
        } catch (Exception e) {
            return ThingCommandResult.error(messageFrom(e));
        }
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
            return ThingCommandResult.error(messageFrom(e));
        }
    }

    private ThingCommandResult connectRelationshipReferences(
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

    private EntityInstanceDraft createDraft(
            final EntityDefinition entity,
            final String requestedPrimaryKey,
            final List<NamedValue> fieldValues) {
        EntityInstanceDraft baseDraft = EntityInstanceDraft.forEntity(entity);
        if (requestedPrimaryKey != null
                && !requestedPrimaryKey.isEmpty()
                && entity.hasPrimaryKeyField()) {
            Field primaryKeyField = entity.getPrimaryKeyField();
            if (primaryKeyField.getType() == FieldType.AUTO_INCREMENT
                    || primaryKeyField.getType() == FieldType.AUTO_GUID) {
                baseDraft.withProtectedField(primaryKeyField.getName(), requestedPrimaryKey);
            } else {
                baseDraft.withField(primaryKeyField.getName(), requestedPrimaryKey);
            }
        }

        List<NamedValue> values = new ArrayList<>(fieldValues);
        if (requestedPrimaryKey != null && !requestedPrimaryKey.isEmpty()) {
            store.administration().accommodateProtectedIds(entity, values);
            EntityInstanceDraft draft =
                    new EntityInstanceDraftBuilder(entity)
                            .overrideFieldValuesFromArgsIgnoring(
                                    values, entity.getFieldNamesOfType(FieldType.AUTO_GUID));
            copyBaseDraftValues(baseDraft, draft);
            return draft;
        }

        return new EntityInstanceDraftBuilder(entity).setFieldValuesFrom(values);
    }

    private EntityDefinition entityNamed(final String entityName) {
        if (entityName == null || entityName.isEmpty()) {
            return null;
        }
        EntityDefinition entity = schema.entityNamed(entityName);
        if (entity != null) {
            return entity;
        }
        return schema.definitionWithSingularOrPluralName(entityName);
    }

    private EntityInstance resolveInstance(
            final EntityDefinition entity, final String queryIdentifier) {
        if (entity == null) {
            return null;
        }
        return store.entityQueries().findByQueryIdentifier(entity, queryIdentifier);
    }

    private ThingCommandResult parentNotFound(final String routeDisplay) {
        return ThingCommandResult.error(
                ApplicationError.notFound(
                        String.format(
                                "Could not find parent thing for relationship %s", routeDisplay)));
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

    private RelationshipVectorDefinition firstRelationshipVector(
            final EntityDefinition entity, final String relationshipName) {
        List<RelationshipVectorDefinition> vectors =
                entity.related().getRelationships(relationshipName);
        if (vectors.isEmpty()) {
            return null;
        }
        return vectors.get(0);
    }

    private boolean bodyReferencesExistingRelatedItem(
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

    private void copyBaseDraftValues(
            final EntityInstanceDraft baseDraft, final EntityInstanceDraft draft) {
        for (NamedValue value : baseDraft.getFieldValues()) {
            draft.withField(value.getName(), value.asString());
        }
        for (NamedValue protectedValue : baseDraft.getProtectedFieldValues()) {
            draft.withProtectedField(protectedValue.getName(), protectedValue.asString());
        }
    }

    private static String messageFrom(final Exception exception) {
        String message = exception.getMessage();
        return message == null ? "" : message;
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
