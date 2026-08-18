package uk.co.compendiumdev.thingifier.application;

import java.util.List;
import uk.co.compendiumdev.thingifier.application.command.ConnectExistingRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateAndConnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.DisconnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.RelateThingCommand;
import uk.co.compendiumdev.thingifier.application.command.UpdateConnectedRelationshipCommand;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreWriteException;

/**
 * Validates and applies relationship write commands.
 *
 * <p>Relationship writes can connect an existing child, create and connect a new child, infer the
 * correct operation from a request body, or disconnect an existing relationship. Each command has a
 * validation path separated from the mutation path for lifecycle hook processing.
 */
final class RelationshipCommandHandler {

    private final ThingStore store;
    private final ThingDefinitionResolver definitions;
    private final WriteValidationPolicy validation;
    private final ThingDraftFactory drafts;
    private final CreateThingHandler createHandler;
    private final RelationshipConnectionService relationships;
    private final RelationshipTargetResolver targets;
    private final RelationshipCascadeDeleteService cascadeDeletes;

    /**
     * Creates the relationship command handler.
     *
     * @param store store to mutate
     * @param definitions resolver for model definitions and instances
     * @param validation write validation policy
     * @param drafts factory for child drafts
     * @param createHandler handler used to persist newly related children
     * @param relationships service used to connect relationship references
     * @param targets resolver for relationship targets and references
     */
    RelationshipCommandHandler(
            final ThingStore store,
            final ThingDefinitionResolver definitions,
            final WriteValidationPolicy validation,
            final ThingDraftFactory drafts,
            final CreateThingHandler createHandler,
            final RelationshipConnectionService relationships,
            final RelationshipTargetResolver targets,
            final RelationshipCascadeDeleteService cascadeDeletes) {
        this.store = store;
        this.definitions = definitions;
        this.validation = validation;
        this.drafts = drafts;
        this.createHandler = createHandler;
        this.relationships = relationships;
        this.targets = targets;
        this.cascadeDeletes = cascadeDeletes;
    }

    /**
     * Validates and applies a connect-existing relationship command in one call.
     *
     * @param command connect-existing command to handle
     * @return validation error or successful relationship result
     */
    ThingCommandResult handle(final ConnectExistingRelationshipCommand command) {
        ThingCommandResult validationResult = validate(command);
        if (validationResult != null) {
            return validationResult;
        }
        return apply(command);
    }

    /**
     * Validates that the parent and referenced related instance can be connected.
     *
     * @param command connect-existing command to validate
     * @return validation error, or null when validation succeeds
     */
    ThingCommandResult validate(final ConnectExistingRelationshipCommand command) {
        EntityDefinition parentEntity = definitions.entityNamed(command.getParentEntityName());
        EntityInstance parent =
                definitions.resolveInstance(parentEntity, command.getParentIdentifier());
        if (parent == null) {
            return parentNotFound(command);
        }

        RelationshipTargetResolver.RelatedItemResolution related =
                targets.resolveRelatedItemFromReferenceFields(
                        parent, command.getRelationshipName(), command.getChildReferenceFields());
        if (related.error() != null) {
            return ThingCommandResult.error(related.error());
        }

        RelationshipVectorDefinition relationshipToUse =
                parent.getEntity()
                        .getNamedRelationshipTo(
                                command.getRelationshipName(), related.instance().getEntity());
        ThingCommandResult relationshipError =
                relationships.relationshipErrorIfInvalid(
                        parent,
                        related.instance(),
                        relationshipToUse,
                        command.getRelationshipName());
        if (relationshipError != null) {
            return relationshipError;
        }
        return null;
    }

    /**
     * Applies a connect-existing relationship command after validation.
     *
     * @param command validated connect-existing command
     * @return command result from connecting the relationship
     */
    ThingCommandResult apply(final ConnectExistingRelationshipCommand command) {
        EntityDefinition parentEntity = definitions.entityNamed(command.getParentEntityName());
        EntityInstance parent =
                definitions.resolveInstance(parentEntity, command.getParentIdentifier());
        if (parent == null) {
            return parentNotFound(command);
        }

        RelationshipTargetResolver.RelatedItemResolution related =
                targets.resolveRelatedItemFromReferenceFields(
                        parent, command.getRelationshipName(), command.getChildReferenceFields());
        if (related.error() != null) {
            return ThingCommandResult.error(related.error());
        }

        RelationshipVectorDefinition relationshipToUse =
                parent.getEntity()
                        .getNamedRelationshipTo(
                                command.getRelationshipName(), related.instance().getEntity());
        return relationships.connectRelationship(
                parent, relationshipToUse.getName(), related.instance(), false);
    }

    /**
     * Validates and applies a create-and-connect relationship command in one call.
     *
     * @param command create-and-connect command to handle
     * @return validation error or successful creation/relationship result
     */
    ThingCommandResult handle(final CreateAndConnectRelationshipCommand command) {
        ThingCommandResult validationResult = validate(command);
        if (validationResult != null) {
            return validationResult;
        }
        return apply(command);
    }

    /**
     * Validates that a new child can be created and connected to the parent.
     *
     * @param command create-and-connect command to validate
     * @return validation error, or null when validation succeeds
     */
    ThingCommandResult validate(final CreateAndConnectRelationshipCommand command) {
        EntityDefinition parentEntity = definitions.entityNamed(command.getParentEntityName());
        EntityInstance parent =
                definitions.resolveInstance(parentEntity, command.getParentIdentifier());
        if (parent == null) {
            return parentNotFound(command);
        }

        EntityDefinition childEntity = definitions.entityNamed(command.getChildEntityName());
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
        ThingCommandResult relationshipValidation =
                relationships.validateRelationshipReferences(
                        childEntity, command.getChildRelationships(), true);
        if (relationshipValidation != null) {
            return relationshipValidation;
        }
        return null;
    }

    /**
     * Applies a create-and-connect command after validation.
     *
     * @param command validated create-and-connect command
     * @return command result containing the created child or an error
     */
    ThingCommandResult apply(final CreateAndConnectRelationshipCommand command) {
        EntityDefinition parentEntity = definitions.entityNamed(command.getParentEntityName());
        EntityInstance parent =
                definitions.resolveInstance(parentEntity, command.getParentIdentifier());
        if (parent == null) {
            return parentNotFound(command);
        }

        EntityDefinition childEntity = definitions.entityNamed(command.getChildEntityName());
        List<NamedValue> childFieldValues =
                validation.normalizedFieldValues(
                        childEntity, command.getChildFieldValues(), command.getChildBodyFields());
        try {
            EntityInstanceDraft childDraft = drafts.createDraft(childEntity, "", childFieldValues);
            ThingCommandResult createResult =
                    createHandler.create(childDraft, command.getChildRelationships(), false);
            if (createResult.isError()) {
                return createResult;
            }

            ThingCommandResult connectResult =
                    relationships.connectRelationship(
                            parent,
                            command.getRelationshipName(),
                            createResult.getInstance(),
                            true);
            if (connectResult.isError()) {
                return connectResult.withRolledBackCreatedInstance();
            }

            return ThingCommandResult.success(createResult.getInstance());
        } catch (ThingStoreWriteException e) {
            throw e;
        } catch (Exception e) {
            return ThingCommandResult.error(ApplicationExceptionMessages.messageFrom(e));
        }
    }

    /**
     * Validates and applies a relate command in one call.
     *
     * @param command relate command to handle
     * @return validation error or successful relationship result
     */
    ThingCommandResult handle(final RelateThingCommand command) {
        ThingCommandResult validationResult = validate(command);
        if (validationResult != null) {
            return validationResult;
        }
        return apply(command);
    }

    /**
     * Validates a body-driven relationship command.
     *
     * <p>The command is resolved as connect-existing when the body references an existing child,
     * otherwise it is validated as create-and-connect.
     *
     * @param command relate command to validate
     * @return validation error, or null when validation succeeds
     */
    ThingCommandResult validate(final RelateThingCommand command) {
        EntityDefinition parentEntity = definitions.entityNamed(command.getParentEntityName());
        if (parentEntity == null) {
            return parentNotFound(command);
        }

        RelationshipVectorDefinition vector =
                targets.firstRelationshipVector(parentEntity, command.getRelationshipName());
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
                targets.bodyReferencesExistingRelatedItem(vector.getTo(), bodyFieldValues);

        ThingCommandResult typeValidation =
                referencesExistingRelatedItem
                        ? validation.validateDeclaredFieldTypesIgnoringProtected(
                                vector.getTo(), command.getBodyFields())
                        : validation.validateDeclaredFieldTypes(
                                vector.getTo(), command.getBodyFields());
        if (typeValidation != null) {
            return typeValidation;
        }

        ThingCommandResult relationshipValidation =
                relationships.validateRelationshipReferences(
                        vector.getTo(), command.getBodyRelationships(), false);
        if (relationshipValidation != null) {
            return relationshipValidation;
        }

        if (definitions.resolveInstance(parentEntity, command.getParentIdentifier()) == null) {
            return parentNotFound(command);
        }

        if (referencesExistingRelatedItem) {
            ConnectExistingRelationshipCommand connect =
                    new ConnectExistingRelationshipCommand(
                            command.getParentEntityName(),
                            command.getParentIdentifier(),
                            command.getRelationshipName(),
                            bodyFieldValues);
            return validate(connect);
        }

        CreateAndConnectRelationshipCommand create =
                new CreateAndConnectRelationshipCommand(
                        command.getParentEntityName(),
                        command.getParentIdentifier(),
                        command.getRelationshipName(),
                        vector.getTo().getName(),
                        bodyFieldValues,
                        command.getBodyFields(),
                        command.getBodyRelationships());
        return validate(create);
    }

    /**
     * Applies a body-driven relationship command after validation.
     *
     * @param command validated relate command
     * @return command result from connect-existing or create-and-connect
     */
    ThingCommandResult apply(final RelateThingCommand command) {
        EntityDefinition parentEntity = definitions.entityNamed(command.getParentEntityName());
        if (parentEntity == null) {
            return parentNotFound(command);
        }

        RelationshipVectorDefinition vector =
                targets.firstRelationshipVector(parentEntity, command.getRelationshipName());
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
                targets.bodyReferencesExistingRelatedItem(vector.getTo(), bodyFieldValues);

        EntityInstance parent =
                definitions.resolveInstance(parentEntity, command.getParentIdentifier());
        if (parent == null) {
            return parentNotFound(command);
        }

        if (referencesExistingRelatedItem) {
            ConnectExistingRelationshipCommand connect =
                    new ConnectExistingRelationshipCommand(
                            command.getParentEntityName(),
                            command.getParentIdentifier(),
                            command.getRelationshipName(),
                            bodyFieldValues);
            return apply(connect);
        }

        CreateAndConnectRelationshipCommand create =
                new CreateAndConnectRelationshipCommand(
                        command.getParentEntityName(),
                        command.getParentIdentifier(),
                        command.getRelationshipName(),
                        vector.getTo().getName(),
                        bodyFieldValues,
                        command.getBodyFields(),
                        command.getBodyRelationships());
        ThingCommandResult result = apply(create);
        if (result.isSuccessful()) {
            return ThingCommandResult.created(result.getInstance());
        }
        return result;
    }

    ThingCommandResult handle(final UpdateConnectedRelationshipCommand command) {
        ThingCommandResult validationResult = validate(command);
        if (validationResult != null) {
            return validationResult;
        }
        return apply(command);
    }

    ThingCommandResult validate(final UpdateConnectedRelationshipCommand command) {
        EntityDefinition parentEntity = definitions.entityNamed(command.getParentEntityName());
        if (parentEntity == null) {
            return parentNotFound(
                    command.getParentEntityName(),
                    command.getParentIdentifier(),
                    command.getRelationshipName());
        }

        RelationshipVectorDefinition vector =
                targets.firstRelationshipVector(parentEntity, command.getRelationshipName());
        if (vector == null) {
            return ThingCommandResult.error(
                    String.format(
                            "Could not find a relationship named %s for %s",
                            command.getRelationshipName(), parentEntity.getName()));
        }

        ThingCommandResult typeValidation =
                validation.validateDeclaredFieldTypesIgnoringProtected(
                        vector.getTo(), command.getChildBodyFields());
        if (typeValidation != null) {
            return typeValidation;
        }

        List<NamedValue> childFieldValues =
                validation.normalizedFieldValues(
                        vector.getTo(),
                        command.getChildFieldValues(),
                        command.getChildBodyFields());

        EntityInstance parent =
                definitions.resolveInstance(parentEntity, command.getParentIdentifier());
        if (parent == null) {
            return parentNotFound(command);
        }

        RelationshipTargetResolver.RelatedItemResolution related =
                targets.resolveRelatedItemFromReferenceFields(
                        parent, command.getRelationshipName(), childFieldValues);
        if (related.error() != null) {
            return ThingCommandResult.error(related.error());
        }

        ThingCommandResult relationshipError =
                relationships.relationshipErrorIfInvalid(
                        parent, related.instance(), vector, command.getRelationshipName());
        if (relationshipError != null) {
            return relationshipError;
        }

        if (!targets.isRelated(parent, command.getRelationshipName(), related.instance())) {
            return relationshipTargetNotFound(command, related.instance());
        }

        ThingCommandResult relationshipValidation =
                relationships.validateRelationshipReferences(
                        vector.getTo(), command.getChildRelationships(), false);
        if (relationshipValidation != null) {
            return relationshipValidation;
        }

        try {
            new EntityInstanceDraftBuilder(related.instance()).setFieldValuesFrom(childFieldValues);
            return null;
        } catch (ThingStoreWriteException e) {
            throw e;
        } catch (Exception e) {
            return ThingCommandResult.error(ApplicationExceptionMessages.messageFrom(e));
        }
    }

    ThingCommandResult apply(final UpdateConnectedRelationshipCommand command) {
        EntityDefinition parentEntity = definitions.entityNamed(command.getParentEntityName());
        EntityInstance parent =
                definitions.resolveInstance(parentEntity, command.getParentIdentifier());
        if (parent == null) {
            return parentNotFound(command);
        }

        RelationshipVectorDefinition vector =
                targets.firstRelationshipVector(parentEntity, command.getRelationshipName());
        if (vector == null) {
            return ThingCommandResult.error(
                    String.format(
                            "Could not find a relationship named %s for %s",
                            command.getRelationshipName(), parentEntity.getName()));
        }

        List<NamedValue> childFieldValues =
                validation.normalizedFieldValues(
                        vector.getTo(),
                        command.getChildFieldValues(),
                        command.getChildBodyFields());
        RelationshipTargetResolver.RelatedItemResolution related =
                targets.resolveRelatedItemFromReferenceFields(
                        parent, command.getRelationshipName(), childFieldValues);
        if (related.error() != null) {
            return ThingCommandResult.error(related.error());
        }
        if (!targets.isRelated(parent, command.getRelationshipName(), related.instance())) {
            return relationshipTargetNotFound(command, related.instance());
        }

        try {
            EntityInstanceDraft draft =
                    new EntityInstanceDraftBuilder(related.instance())
                            .setFieldValuesFrom(childFieldValues);
            EntityInstance updated = store.entities().patch(related.instance(), draft);
            ThingCommandResult relationshipResult =
                    relationships.connectRelationshipReferences(
                            updated, command.getChildRelationships(), true, false);
            if (relationshipResult.isError()) {
                return relationshipResult;
            }
            return ThingCommandResult.success(updated);
        } catch (ThingStoreWriteException e) {
            throw e;
        } catch (Exception e) {
            return ThingCommandResult.error(ApplicationExceptionMessages.messageFrom(e));
        }
    }

    /**
     * Validates and applies a disconnect relationship command in one call.
     *
     * @param command disconnect command to handle
     * @return validation error or successful disconnect result
     */
    ThingCommandResult handle(final DisconnectRelationshipCommand command) {
        ThingCommandResult validationResult = validate(command);
        if (validationResult != null) {
            return validationResult;
        }
        return apply(command);
    }

    /**
     * Validates that the source and target instances are currently related.
     *
     * @param command disconnect command to validate
     * @return validation error, or null when validation succeeds
     */
    ThingCommandResult validate(final DisconnectRelationshipCommand command) {
        EntityDefinition parentEntity = definitions.entityNamed(command.getParentEntityName());
        EntityInstance parent =
                definitions.resolveInstance(parentEntity, command.getParentIdentifier());
        if (parent == null) {
            return relationshipSourceNotFound(command);
        }

        EntityInstance child =
                targets.relatedInstanceMatchingIdentifier(
                        parent, command.getRelationshipName(), command.getChildIdentifier());
        if (child == null) {
            return relationshipTargetNotFound(command);
        }
        return null;
    }

    /**
     * Applies a disconnect command after validation.
     *
     * @param command validated disconnect command
     * @return successful command result or store error
     */
    ThingCommandResult apply(final DisconnectRelationshipCommand command) {
        EntityDefinition parentEntity = definitions.entityNamed(command.getParentEntityName());
        EntityInstance parent =
                definitions.resolveInstance(parentEntity, command.getParentIdentifier());
        if (parent == null) {
            return relationshipSourceNotFound(command);
        }

        EntityInstance child =
                targets.relatedInstanceMatchingIdentifier(
                        parent, command.getRelationshipName(), command.getChildIdentifier());
        if (child == null) {
            return relationshipTargetNotFound(command);
        }
        try {
            store.relationships().removeBetween(parent, child, command.getRelationshipName());
            cascadeDeletes.deleteTargetAfterDisconnect(
                    parent, command.getRelationshipName(), child);
            return ThingCommandResult.success();
        } catch (ThingStoreWriteException e) {
            throw e;
        } catch (Exception e) {
            return ThingCommandResult.error(ApplicationExceptionMessages.messageFrom(e));
        }
    }

    /**
     * Builds a parent-not-found error for connect-existing commands.
     *
     * @param command command whose parent was missing
     * @return command result containing the parent error
     */
    private ThingCommandResult parentNotFound(final ConnectExistingRelationshipCommand command) {
        return parentNotFound(
                command.getParentEntityName(),
                command.getParentIdentifier(),
                command.getRelationshipName());
    }

    /**
     * Builds a parent-not-found error for create-and-connect commands.
     *
     * @param command command whose parent was missing
     * @return command result containing the parent error
     */
    private ThingCommandResult parentNotFound(final CreateAndConnectRelationshipCommand command) {
        return parentNotFound(
                command.getParentEntityName(),
                command.getParentIdentifier(),
                command.getRelationshipName());
    }

    /**
     * Builds a parent-not-found error for relate commands.
     *
     * @param command command whose parent was missing
     * @return command result containing the parent error
     */
    private ThingCommandResult parentNotFound(final RelateThingCommand command) {
        return parentNotFound(
                command.getParentEntityName(),
                command.getParentIdentifier(),
                command.getRelationshipName());
    }

    private ThingCommandResult parentNotFound(final UpdateConnectedRelationshipCommand command) {
        return parentNotFound(
                command.getParentEntityName(),
                command.getParentIdentifier(),
                command.getRelationshipName());
    }

    /**
     * Builds the standard parent-not-found error.
     *
     * @param entityName parent entity name
     * @param identifier parent identifier
     * @param relationshipName relationship name
     * @return command result containing the parent error
     */
    private ThingCommandResult parentNotFound(
            final String entityName, final String identifier, final String relationshipName) {
        return ThingCommandResult.error(
                ApplicationError.parentInstanceNotFound(entityName, identifier, relationshipName));
    }

    /**
     * Builds the standard missing relationship source error for disconnects.
     *
     * @param command disconnect command
     * @return command result containing the source error
     */
    private ThingCommandResult relationshipSourceNotFound(
            final DisconnectRelationshipCommand command) {
        return ThingCommandResult.error(
                ApplicationError.relationshipSourceNotFound(
                        command.getParentEntityName(),
                        command.getParentIdentifier(),
                        command.getRelationshipName()));
    }

    /**
     * Builds the standard missing relationship target error for disconnects.
     *
     * @param command disconnect command
     * @return command result containing the target error
     */
    private ThingCommandResult relationshipTargetNotFound(
            final DisconnectRelationshipCommand command) {
        return ThingCommandResult.error(
                ApplicationError.relationshipTargetNotFound(
                        command.getParentEntityName(),
                        command.getParentIdentifier(),
                        command.getRelationshipName(),
                        command.getChildIdentifier()));
    }

    private ThingCommandResult relationshipTargetNotFound(
            final UpdateConnectedRelationshipCommand command, final EntityInstance child) {
        return ThingCommandResult.error(
                ApplicationError.relationshipTargetNotFound(
                        command.getParentEntityName(),
                        command.getParentIdentifier(),
                        command.getRelationshipName(),
                        child.getPrimaryKeyValue()));
    }
}
