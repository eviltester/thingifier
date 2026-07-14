package uk.co.compendiumdev.thingifier.application;

import uk.co.compendiumdev.thingifier.application.command.CreateThingCommand;

final class CreateThingHandler {

    private final ThingCommandService service;

    CreateThingHandler(final ThingCommandService service) {
        this.service = service;
    }

    ThingCommandResult handle(final CreateThingCommand command) {
        return service.create(command);
    }
}
