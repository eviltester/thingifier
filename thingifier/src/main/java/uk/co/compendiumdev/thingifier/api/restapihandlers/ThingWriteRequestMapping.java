package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.application.command.ThingWriteCommand;

public final class ThingWriteRequestMapping {

    private final ThingWriteCommand command;
    private final ApiMappingError error;

    private ThingWriteRequestMapping(final ThingWriteCommand command, final ApiMappingError error) {
        this.command = command;
        this.error = error;
    }

    public static ThingWriteRequestMapping command(final ThingWriteCommand command) {
        return new ThingWriteRequestMapping(command, null);
    }

    public static ThingWriteRequestMapping error(final ApiMappingError error) {
        return new ThingWriteRequestMapping(null, error);
    }

    public boolean isError() {
        return error != null;
    }

    public ThingWriteCommand getCommand() {
        return command;
    }

    public ApiMappingError getError() {
        return error;
    }
}
