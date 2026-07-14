package uk.co.compendiumdev.thingifier.application;

import uk.co.compendiumdev.thingifier.application.command.AmendThingCommand;
import uk.co.compendiumdev.thingifier.application.command.PutThingCommand;

final class AmendThingHandler {

    private final ThingCommandService service;

    AmendThingHandler(final ThingCommandService service) {
        this.service = service;
    }

    ThingCommandResult handle(final AmendThingCommand command) {
        return service.amend(command);
    }

    ThingCommandResult handle(final PutThingCommand command) {
        return service.put(command);
    }
}
