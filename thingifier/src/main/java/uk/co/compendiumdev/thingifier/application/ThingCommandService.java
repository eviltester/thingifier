package uk.co.compendiumdev.thingifier.application;

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
import uk.co.compendiumdev.thingifier.application.schema.SchemaDefinitionResolver;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public final class ThingCommandService {

    private final WriteTransactionRunner transactionRunner;
    private final CreateThingHandler createHandler;
    private final AmendThingHandler amendHandler;
    private final PatchThingDocumentHandler patchDocumentHandler;
    private final DeleteThingHandler deleteHandler;
    private final RelationshipCommandHandler relationshipHandler;

    public ThingCommandService(final ThingStore store, final SchemaDefinitionResolver schema) {
        this(store, schema, false);
    }

    public ThingCommandService(
            final ThingStore store,
            final SchemaDefinitionResolver schema,
            final boolean enforceDeclaredTypes) {
        this(store, schema, enforceDeclaredTypes, new JsonOutputConfig());
    }

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
        this.deleteHandler = new DeleteThingHandler(store, definitions);
        this.relationshipHandler =
                new RelationshipCommandHandler(
                        store,
                        definitions,
                        validation,
                        drafts,
                        createHandler,
                        relationshipConnections,
                        new RelationshipTargetResolver(store));
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
            return deleteHandler.handle((DeleteThingCommand) command);
        }

        if (command instanceof PatchThingDocumentCommand) {
            return patchDocumentHandler.handle((PatchThingDocumentCommand) command);
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
}
