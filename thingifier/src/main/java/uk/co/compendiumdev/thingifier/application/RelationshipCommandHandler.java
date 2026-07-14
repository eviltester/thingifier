package uk.co.compendiumdev.thingifier.application;

import uk.co.compendiumdev.thingifier.application.command.ConnectExistingRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateAndConnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.DisconnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.RelateThingCommand;

final class RelationshipCommandHandler {

    private final ThingCommandService service;

    RelationshipCommandHandler(final ThingCommandService service) {
        this.service = service;
    }

    ThingCommandResult handle(final ConnectExistingRelationshipCommand command) {
        return service.connectExistingRelationship(command);
    }

    ThingCommandResult handle(final CreateAndConnectRelationshipCommand command) {
        return service.createAndConnect(command);
    }

    ThingCommandResult handle(final RelateThingCommand command) {
        return service.relate(command);
    }

    ThingCommandResult handle(final DisconnectRelationshipCommand command) {
        return service.disconnectRelationship(command);
    }
}
