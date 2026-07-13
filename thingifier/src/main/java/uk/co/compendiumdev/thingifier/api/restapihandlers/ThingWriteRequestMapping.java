package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.command.ThingWriteCommand;

public final class ThingWriteRequestMapping {

    private final ThingWriteCommand command;
    private final ApiResponse errorResponse;

    private ThingWriteRequestMapping(
            final ThingWriteCommand command, final ApiResponse errorResponse) {
        this.command = command;
        this.errorResponse = errorResponse;
    }

    public static ThingWriteRequestMapping command(final ThingWriteCommand command) {
        return new ThingWriteRequestMapping(command, null);
    }

    public static ThingWriteRequestMapping error(final ApiResponse errorResponse) {
        return new ThingWriteRequestMapping(null, errorResponse);
    }

    public boolean isError() {
        return errorResponse != null;
    }

    public ThingWriteCommand getCommand() {
        return command;
    }

    public ApiResponse getErrorResponse() {
        return errorResponse;
    }
}
