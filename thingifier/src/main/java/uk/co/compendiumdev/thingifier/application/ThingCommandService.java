package uk.co.compendiumdev.thingifier.application;

import java.util.function.Supplier;
import uk.co.compendiumdev.thingifier.apiconfig.JsonOutputConfig;
import uk.co.compendiumdev.thingifier.application.command.AmendThingCommand;
import uk.co.compendiumdev.thingifier.application.command.ConnectExistingRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateAndConnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DeleteThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DisconnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.PatchThingDocumentCommand;
import uk.co.compendiumdev.thingifier.application.command.RelateThingCommand;
import uk.co.compendiumdev.thingifier.application.command.ReplaceThingCommand;
import uk.co.compendiumdev.thingifier.application.command.ThingWriteCommand;
import uk.co.compendiumdev.thingifier.application.command.UpdateConnectedRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.schema.SchemaDefinitionResolver;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

/**
 * Coordinates validation and execution of Thingifier write commands.
 *
 * <p>The service exposes both the original one-step {@link #execute(ThingWriteCommand)} operation
 * and the split {@link #validate(ThingWriteCommand)} / {@link #applyValidated(ThingWriteCommand)}
 * operations needed by lifecycle hooks. All normal writes still run inside the transaction runner.
 */
public final class ThingCommandService {

    private final WriteTransactionRunner transactionRunner;
    private final CreateThingHandler createHandler;
    private final AmendThingHandler amendHandler;
    private final PatchThingDocumentHandler patchDocumentHandler;
    private final DeleteThingHandler deleteHandler;
    private final RelationshipCommandHandler relationshipHandler;

    /**
     * Creates a command service with default type handling and JSON output configuration.
     *
     * @param store store to mutate
     * @param schema schema resolver for entity and relationship definitions
     */
    public ThingCommandService(final ThingStore store, final SchemaDefinitionResolver schema) {
        this(store, schema, false);
    }

    /**
     * Creates a command service with explicit declared-type enforcement.
     *
     * @param store store to mutate
     * @param schema schema resolver for entity and relationship definitions
     * @param enforceDeclaredTypes true when request values must match declared field types
     */
    public ThingCommandService(
            final ThingStore store,
            final SchemaDefinitionResolver schema,
            final boolean enforceDeclaredTypes) {
        this(store, schema, enforceDeclaredTypes, new JsonOutputConfig());
    }

    /**
     * Creates a command service with explicit validation and JSON rendering configuration.
     *
     * @param store store to mutate
     * @param schema schema resolver for entity and relationship definitions
     * @param enforceDeclaredTypes true when request values must match declared field types
     * @param jsonOutput JSON rendering configuration used by patch document handling
     */
    public ThingCommandService(
            final ThingStore store,
            final SchemaDefinitionResolver schema,
            final boolean enforceDeclaredTypes,
            final JsonOutputConfig jsonOutput) {
        ThingDefinitionResolver definitions = new ThingDefinitionResolver(store, schema);
        WriteValidationPolicy validation = new WriteValidationPolicy(store, enforceDeclaredTypes);
        RelationshipReferenceResolver relationshipResolver =
                new RelationshipReferenceResolver(store, schema);
        RelationshipConnectionService relationshipConnections =
                new RelationshipConnectionService(store, relationshipResolver);
        RelationshipCascadeDeleteService cascadeDeletes =
                new RelationshipCascadeDeleteService(store);
        ThingDraftFactory drafts = new ThingDraftFactory(store);

        this.transactionRunner = new WriteTransactionRunner(store);
        this.createHandler =
                new CreateThingHandler(
                        store, definitions, validation, drafts, relationshipConnections);
        this.amendHandler =
                new AmendThingHandler(
                        store,
                        definitions,
                        validation,
                        drafts,
                        createHandler,
                        relationshipConnections);
        this.patchDocumentHandler =
                new PatchThingDocumentHandler(definitions, amendHandler, jsonOutput);
        this.deleteHandler = new DeleteThingHandler(store, definitions, cascadeDeletes);
        this.relationshipHandler =
                new RelationshipCommandHandler(
                        store,
                        definitions,
                        validation,
                        drafts,
                        createHandler,
                        relationshipConnections,
                        new RelationshipTargetResolver(store),
                        cascadeDeletes);
    }

    /**
     * Validates and applies a write command inside one transaction.
     *
     * @param command write command to execute
     * @return command result from validation or application
     */
    public ThingCommandResult execute(final ThingWriteCommand command) {
        return transactionRunner.run(() -> executeInsideTransaction(command));
    }

    /**
     * Runs a caller-supplied write operation inside the same transaction mechanism.
     *
     * <p>Lifecycle write support uses this so hooks, validation, and action execution share the
     * original write transaction boundary.
     *
     * @param operation operation to run inside the transaction
     * @return command result from the operation
     */
    public ThingCommandResult runInTransaction(final Supplier<ThingCommandResult> operation) {
        return transactionRunner.run(operation);
    }

    /**
     * Validates a write command without mutating the store.
     *
     * @param command write command to validate
     * @return validation error result, or null when validation succeeds
     */
    public ThingCommandResult validate(final ThingWriteCommand command) {
        if (command instanceof CreateThingCommand) {
            return createHandler.validate((CreateThingCommand) command);
        }

        if (command instanceof AmendThingCommand) {
            return amendHandler.validate((AmendThingCommand) command);
        }

        if (command instanceof DeleteThingCommand) {
            return deleteHandler.validate((DeleteThingCommand) command);
        }

        if (command instanceof PatchThingDocumentCommand) {
            return patchDocumentHandler.validate((PatchThingDocumentCommand) command);
        }

        if (command instanceof ReplaceThingCommand) {
            return amendHandler.validate((ReplaceThingCommand) command);
        }

        if (command instanceof ConnectExistingRelationshipCommand) {
            return relationshipHandler.validate((ConnectExistingRelationshipCommand) command);
        }

        if (command instanceof CreateAndConnectRelationshipCommand) {
            return relationshipHandler.validate((CreateAndConnectRelationshipCommand) command);
        }

        if (command instanceof RelateThingCommand) {
            return relationshipHandler.validate((RelateThingCommand) command);
        }

        if (command instanceof UpdateConnectedRelationshipCommand) {
            return relationshipHandler.validate((UpdateConnectedRelationshipCommand) command);
        }

        if (command instanceof DisconnectRelationshipCommand) {
            return relationshipHandler.validate((DisconnectRelationshipCommand) command);
        }

        return ThingCommandResult.error(
                ApplicationError.unsupported(
                        String.format(
                                "Unsupported command %s", command.getClass().getSimpleName())));
    }

    /**
     * Applies a command that has already passed validation.
     *
     * <p>Callers are responsible for only passing validated commands; the method exists so
     * lifecycle hooks can run between validation and mutation.
     *
     * @param command validated write command to apply
     * @return command result from applying the mutation
     */
    public ThingCommandResult applyValidated(final ThingWriteCommand command) {
        return applyInsideTransaction(command);
    }

    /**
     * Runs the original validate-then-apply command flow inside a transaction.
     *
     * @param command write command to execute
     * @return validation or apply result
     */
    private ThingCommandResult executeInsideTransaction(final ThingWriteCommand command) {
        ThingCommandResult validationResult = validate(command);
        if (validationResult != null) {
            return validationResult;
        }
        return applyInsideTransaction(command);
    }

    /**
     * Dispatches a validated write command to its concrete mutation handler.
     *
     * @param command validated write command
     * @return command result from the matching handler
     */
    private ThingCommandResult applyInsideTransaction(final ThingWriteCommand command) {
        if (command instanceof CreateThingCommand) {
            return createHandler.apply((CreateThingCommand) command);
        }

        if (command instanceof AmendThingCommand) {
            return amendHandler.apply((AmendThingCommand) command);
        }

        if (command instanceof DeleteThingCommand) {
            return deleteHandler.apply((DeleteThingCommand) command);
        }

        if (command instanceof PatchThingDocumentCommand) {
            return patchDocumentHandler.apply((PatchThingDocumentCommand) command);
        }

        if (command instanceof ReplaceThingCommand) {
            return amendHandler.apply((ReplaceThingCommand) command);
        }

        if (command instanceof ConnectExistingRelationshipCommand) {
            return relationshipHandler.apply((ConnectExistingRelationshipCommand) command);
        }

        if (command instanceof CreateAndConnectRelationshipCommand) {
            return relationshipHandler.apply((CreateAndConnectRelationshipCommand) command);
        }

        if (command instanceof RelateThingCommand) {
            return relationshipHandler.apply((RelateThingCommand) command);
        }

        if (command instanceof UpdateConnectedRelationshipCommand) {
            return relationshipHandler.apply((UpdateConnectedRelationshipCommand) command);
        }

        if (command instanceof DisconnectRelationshipCommand) {
            return relationshipHandler.apply((DisconnectRelationshipCommand) command);
        }

        return ThingCommandResult.error(
                ApplicationError.unsupported(
                        String.format(
                                "Unsupported command %s", command.getClass().getSimpleName())));
    }
}
