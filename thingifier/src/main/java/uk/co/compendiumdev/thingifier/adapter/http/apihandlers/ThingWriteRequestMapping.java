package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import uk.co.compendiumdev.thingifier.application.command.ThingWriteCommand;

public final class ThingWriteRequestMapping {

    private final ThingWriteCommand command;
    private final ApiMappingError error;
    private final ApiRouteDisplay routeDisplay;

    private ThingWriteRequestMapping(
            final ThingWriteCommand command,
            final ApiMappingError error,
            final ApiRouteDisplay routeDisplay) {
        this.command = command;
        this.error = error;
        this.routeDisplay = routeDisplay == null ? ApiRouteDisplay.empty() : routeDisplay;
    }

    public static ThingWriteRequestMapping command(final ThingWriteCommand command) {
        return command(command, ApiRouteDisplay.empty());
    }

    public static ThingWriteRequestMapping command(
            final ThingWriteCommand command, final ApiRouteDisplay routeDisplay) {
        return new ThingWriteRequestMapping(command, null, routeDisplay);
    }

    public static ThingWriteRequestMapping error(final ApiMappingError error) {
        return new ThingWriteRequestMapping(null, error, ApiRouteDisplay.empty());
    }

    public ThingWriteRequestMapping withRouteDisplay(final ApiRouteDisplay newRouteDisplay) {
        return new ThingWriteRequestMapping(command, error, newRouteDisplay);
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

    public ApiRouteDisplay getRouteDisplay() {
        return routeDisplay;
    }
}
