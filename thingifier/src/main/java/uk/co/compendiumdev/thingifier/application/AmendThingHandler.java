package uk.co.compendiumdev.thingifier.application;

import java.util.ArrayList;
import java.util.List;
import uk.co.compendiumdev.thingifier.application.command.AmendThingCommand;
import uk.co.compendiumdev.thingifier.application.command.RelationshipReference;
import uk.co.compendiumdev.thingifier.application.command.ReplaceThingCommand;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreWriteException;

/**
 * Validates and applies commands that amend or replace entity instances.
 *
 * <p>The handler supports partial field amendment and full replacement while keeping validation
 * separate from mutation for lifecycle hook processing.
 */
final class AmendThingHandler {

    private final ThingStore store;
    private final ThingDefinitionResolver definitions;
    private final WriteValidationPolicy validation;
    private final ThingDraftFactory drafts;
    private final CreateThingHandler createHandler;
    private final RelationshipConnectionService relationships;

    /**
     * Creates the amend/replacement handler.
     *
     * @param store store to mutate
     * @param definitions resolver for model definitions and instances
     * @param validation write validation policy
     * @param drafts factory for create-on-replace drafts
     * @param createHandler handler used when replacement creates a missing instance
     * @param relationships service used to reconnect relationship references
     */
    AmendThingHandler(
            final ThingStore store,
            final ThingDefinitionResolver definitions,
            final WriteValidationPolicy validation,
            final ThingDraftFactory drafts,
            final CreateThingHandler createHandler,
            final RelationshipConnectionService relationships) {
        this.store = store;
        this.definitions = definitions;
        this.validation = validation;
        this.drafts = drafts;
        this.createHandler = createHandler;
        this.relationships = relationships;
    }

    /**
     * Validates and applies an amend command in one call.
     *
     * @param command amend command to handle
     * @return validation error or successful amend result
     */
    ThingCommandResult handle(final AmendThingCommand command) {
        ThingCommandResult validationResult = validate(command);
        if (validationResult != null) {
            return validationResult;
        }
        return apply(command);
    }

    /**
     * Validates an amend command without mutating the store.
     *
     * @param command amend command to validate
     * @return validation error, or null when validation succeeds
     */
    ThingCommandResult validate(final AmendThingCommand command) {
        EntityDefinition entity = definitions.entityNamed(command.getEntityName());
        ThingCommandResult typeValidation =
                validation.validateDeclaredFieldTypesIgnoringProtected(
                        entity, command.getBodyFields());
        if (typeValidation != null) {
            return typeValidation;
        }

        EntityInstance instance = definitions.resolveInstance(entity, command.getIdentifier());
        if (instance == null) {
            return ThingCommandResult.error(
                    ApplicationError.instanceNotFound(
                            command.getEntityName(), command.getIdentifier()));
        }
        ThingCommandResult relationshipValidation =
                relationships.validateRelationshipReferences(
                        entity, command.getRelationships(), false);
        if (relationshipValidation != null) {
            return relationshipValidation;
        }
        return null;
    }

    /**
     * Applies an amend command after validation.
     *
     * @param command validated amend command
     * @return command result containing the updated instance or an error
     */
    ThingCommandResult apply(final AmendThingCommand command) {
        EntityDefinition entity = definitions.entityNamed(command.getEntityName());
        EntityInstance instance = definitions.resolveInstance(entity, command.getIdentifier());
        if (instance == null) {
            return ThingCommandResult.error(
                    ApplicationError.instanceNotFound(
                            command.getEntityName(), command.getIdentifier()));
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
                    command.shouldReplaceExistingFields(),
                    command.shouldReplaceExistingRelationships(),
                    command.getRelationships());
        } catch (ThingStoreWriteException e) {
            throw e;
        } catch (Exception e) {
            return ThingCommandResult.error(ApplicationExceptionMessages.messageFrom(e));
        }
    }

    /**
     * Validates and applies a replace command in one call.
     *
     * @param command replace command to handle
     * @return validation error or successful replace/create result
     */
    ThingCommandResult handle(final ReplaceThingCommand command) {
        ThingCommandResult validationResult = validate(command);
        if (validationResult != null) {
            return validationResult;
        }
        return apply(command);
    }

    /**
     * Validates a replace command without mutating the store.
     *
     * <p>When the target does not exist, validation checks whether the replacement may create it.
     *
     * @param command replace command to validate
     * @return validation error, or null when validation succeeds
     */
    ThingCommandResult validate(final ReplaceThingCommand command) {
        EntityDefinition entity = definitions.entityNamed(command.getEntityName());
        if (entity == null) {
            return ThingCommandResult.error(
                    ApplicationError.notFound(
                            String.format("Could not find entity %s", command.getEntityName())));
        }
        ThingCommandResult typeValidation =
                validation.validateDeclaredFieldTypesIgnoringProtected(
                        entity, command.getBodyFields());
        if (typeValidation != null) {
            return typeValidation;
        }

        ThingCommandResult relationshipValidation =
                relationships.validateRelationshipReferences(
                        entity, command.getRelationships(), false);
        if (relationshipValidation != null) {
            return relationshipValidation;
        }

        List<NamedValue> fieldValues =
                validation.normalizedFieldValues(
                        entity, command.getFieldValues(), command.getBodyFields());
        EntityInstance instance = definitions.resolveInstance(entity, command.getIdentifier());
        if (instance != null) {
            try {
                List<NamedValue> replacementValues =
                        fieldValuesWithIdentifierIfMissing(
                                entity, command.getIdentifier(), fieldValues);
                new EntityInstanceDraftBuilder(instance).setFieldValuesFrom(replacementValues);
                return null;
            } catch (ThingStoreWriteException e) {
                throw e;
            } catch (Exception e) {
                return ThingCommandResult.error(ApplicationExceptionMessages.messageFrom(e));
            }
        }

        ThingCommandResult creationAllowed =
                validation.validateReplaceCreate(entity, command.getIdentifier(), fieldValues);
        if (creationAllowed != null) {
            return creationAllowed;
        }
        return null;
    }

    /**
     * Applies a replace command after validation.
     *
     * <p>Existing instances are fully replaced; missing instances are created when replacement
     * create policy allows it.
     *
     * @param command validated replace command
     * @return command result containing the updated or created instance
     */
    ThingCommandResult apply(final ReplaceThingCommand command) {
        EntityDefinition entity = definitions.entityNamed(command.getEntityName());
        if (entity == null) {
            return ThingCommandResult.error(
                    ApplicationError.notFound(
                            String.format("Could not find entity %s", command.getEntityName())));
        }
        List<NamedValue> fieldValues =
                validation.normalizedFieldValues(
                        entity, command.getFieldValues(), command.getBodyFields());
        EntityInstance instance = definitions.resolveInstance(entity, command.getIdentifier());
        if (instance != null) {
            try {
                List<NamedValue> replacementValues =
                        fieldValuesWithIdentifierIfMissing(
                                entity, command.getIdentifier(), fieldValues);
                EntityInstanceDraft draft =
                        new EntityInstanceDraftBuilder(instance)
                                .setFieldValuesFrom(replacementValues);
                return amend(instance, draft, true, true, command.getRelationships());
            } catch (ThingStoreWriteException e) {
                throw e;
            } catch (Exception e) {
                return ThingCommandResult.error(ApplicationExceptionMessages.messageFrom(e));
            }
        }
        try {
            EntityInstanceDraft draft =
                    drafts.createDraft(entity, command.getIdentifier(), fieldValues);
            ThingCommandResult created =
                    createHandler.create(draft, command.getRelationships(), true);
            if (created.isError()) {
                return created;
            }
            return ThingCommandResult.created(created.getInstance());
        } catch (ThingStoreWriteException e) {
            throw e;
        } catch (Exception e) {
            return ThingCommandResult.error(ApplicationExceptionMessages.messageFrom(e));
        }
    }

    /**
     * Applies field and relationship changes to an existing instance.
     *
     * @param instance existing instance to update
     * @param draft draft values to write
     * @param replaceExistingFields true to replace fields, false to patch them
     * @param replaceExistingRelationships true to disconnect relationships before reconnecting
     * @param relationshipReferences relationships to connect after the field update
     * @return command result containing the updated instance or an error
     */
    private ThingCommandResult amend(
            final EntityInstance instance,
            final EntityInstanceDraft draft,
            final boolean replaceExistingFields,
            final boolean replaceExistingRelationships,
            final List<RelationshipReference> relationshipReferences) {
        RelationshipSnapshot originalRelationships = RelationshipSnapshot.capture(store, instance);
        try {
            EntityInstance updated;
            if (replaceExistingFields) {
                updated = store.entities().replace(instance, draft);
            } else {
                updated = store.entities().patch(instance, draft);
            }

            if (replaceExistingRelationships) {
                originalRelationships.disconnectFrom(store, updated);
            }

            ThingCommandResult relationshipResult =
                    relationships.connectRelationshipReferences(
                            updated, relationshipReferences, true, false);
            if (relationshipResult.isError()) {
                return relationshipResult;
            }

            if (replaceExistingRelationships) {
                originalRelationships.deleteFormerDependentsMadeInvalidBy(store, updated);
            }

            return ThingCommandResult.success(updated);
        } catch (ThingStoreWriteException e) {
            throw e;
        } catch (Exception e) {
            return ThingCommandResult.error(ApplicationExceptionMessages.messageFrom(e));
        }
    }

    /**
     * Ensures replacement values include the primary key when the identifier came from the URI.
     *
     * @param entity entity definition being replaced
     * @param identifier URI identifier
     * @param fieldValues normalized field values from the request body
     * @return field values with primary key added when needed
     */
    private List<NamedValue> fieldValuesWithIdentifierIfMissing(
            final EntityDefinition entity,
            final String identifier,
            final List<NamedValue> fieldValues) {
        if (entity == null || !entity.hasPrimaryKeyField()) {
            return fieldValues;
        }

        String primaryKeyFieldName = entity.getPrimaryKeyField().getName();
        for (NamedValue fieldValue : fieldValues) {
            if (fieldValue.getName().equals(primaryKeyFieldName)) {
                return fieldValues;
            }
        }

        List<NamedValue> replacementValues = new ArrayList<>(fieldValues);
        replacementValues.add(new NamedValue(primaryKeyFieldName, identifier));
        return replacementValues;
    }
}
