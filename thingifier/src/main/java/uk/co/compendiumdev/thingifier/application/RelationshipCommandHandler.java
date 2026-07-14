package uk.co.compendiumdev.thingifier.application;

import java.util.List;
import uk.co.compendiumdev.thingifier.application.command.ConnectExistingRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateAndConnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.DisconnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.RelateThingCommand;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

final class RelationshipCommandHandler {

    private final ThingStore store;
    private final ThingDefinitionResolver definitions;
    private final WriteValidationPolicy validation;
    private final ThingDraftFactory drafts;
    private final CreateThingHandler createHandler;
    private final RelationshipConnectionService relationships;
    private final RelationshipTargetResolver targets;

    RelationshipCommandHandler(
            final ThingStore store,
            final ThingDefinitionResolver definitions,
            final WriteValidationPolicy validation,
            final ThingDraftFactory drafts,
            final CreateThingHandler createHandler,
            final RelationshipConnectionService relationships,
            final RelationshipTargetResolver targets) {
        this.store = store;
        this.definitions = definitions;
        this.validation = validation;
        this.drafts = drafts;
        this.createHandler = createHandler;
        this.relationships = relationships;
        this.targets = targets;
    }

    ThingCommandResult handle(final ConnectExistingRelationshipCommand command) {
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

        return relationships.connectRelationship(
                parent, relationshipToUse.getName(), related.instance(), false);
    }

    ThingCommandResult handle(final CreateAndConnectRelationshipCommand command) {
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
        } catch (Exception e) {
            return ThingCommandResult.error(ApplicationExceptionMessages.messageFrom(e));
        }
    }

    ThingCommandResult handle(final RelateThingCommand command) {
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
            return handle(connect);
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
        ThingCommandResult result = handle(create);
        if (result.isSuccessful()) {
            return ThingCommandResult.created(result.getInstance());
        }
        return result;
    }

    ThingCommandResult handle(final DisconnectRelationshipCommand command) {
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
            return ThingCommandResult.success();
        } catch (Exception e) {
            return ThingCommandResult.error(ApplicationExceptionMessages.messageFrom(e));
        }
    }

    private ThingCommandResult parentNotFound(final ConnectExistingRelationshipCommand command) {
        return parentNotFound(
                command.getParentEntityName(),
                command.getParentIdentifier(),
                command.getRelationshipName());
    }

    private ThingCommandResult parentNotFound(final CreateAndConnectRelationshipCommand command) {
        return parentNotFound(
                command.getParentEntityName(),
                command.getParentIdentifier(),
                command.getRelationshipName());
    }

    private ThingCommandResult parentNotFound(final RelateThingCommand command) {
        return parentNotFound(
                command.getParentEntityName(),
                command.getParentIdentifier(),
                command.getRelationshipName());
    }

    private ThingCommandResult parentNotFound(
            final String entityName, final String identifier, final String relationshipName) {
        return ThingCommandResult.error(
                ApplicationError.parentInstanceNotFound(entityName, identifier, relationshipName));
    }

    private ThingCommandResult relationshipSourceNotFound(
            final DisconnectRelationshipCommand command) {
        return ThingCommandResult.error(
                ApplicationError.relationshipSourceNotFound(
                        command.getParentEntityName(),
                        command.getParentIdentifier(),
                        command.getRelationshipName()));
    }

    private ThingCommandResult relationshipTargetNotFound(
            final DisconnectRelationshipCommand command) {
        return ThingCommandResult.error(
                ApplicationError.relationshipTargetNotFound(
                        command.getParentEntityName(),
                        command.getParentIdentifier(),
                        command.getRelationshipName(),
                        command.getChildIdentifier()));
    }
}
