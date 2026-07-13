package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.ThingCommandResult;
import uk.co.compendiumdev.thingifier.application.command.AmendThingCommand;
import uk.co.compendiumdev.thingifier.application.command.ConnectExistingRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateAndConnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DeleteThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DisconnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.ThingWriteCommand;

public final class ThingCommandResultApiMapper {

    private static final String CREATED_ITEM_ROLLED_BACK_MESSAGE =
            " the newly created item was deleted. No new items have been created.";

    private final ThingifierApiConfig apiConfig;

    public ThingCommandResultApiMapper(final ThingifierApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    public ApiResponse map(final ThingWriteCommand command, final ThingCommandResult result) {
        if (result.isError()) {
            return errorResponseFor(command, result);
        }

        if (command instanceof CreateThingCommand
                || command instanceof CreateAndConnectRelationshipCommand) {
            return ApiResponse.created(result.getInstance(), apiConfig);
        }

        if (command instanceof ConnectExistingRelationshipCommand) {
            return ApiResponse.created(null, apiConfig);
        }

        if (command instanceof AmendThingCommand) {
            return ApiResponse.success().returnSingleInstance(result.getInstance());
        }

        if (command instanceof DeleteThingCommand
                || command instanceof DisconnectRelationshipCommand) {
            return ApiResponse.success();
        }

        return ApiResponse.error(400, result.getErrorMessages());
    }

    private ApiResponse errorResponseFor(
            final ThingWriteCommand command, final ThingCommandResult result) {
        ApiResponse response;
        if (command instanceof CreateThingCommand && result.getErrorMessages().size() == 1) {
            response =
                    ApiResponse.error(400, creationErrorMessage(result.getCombinedErrorMessage()));
        } else {
            response = ApiResponse.error(400, result.getErrorMessages());
        }

        if (result.rolledBackCreatedInstance()) {
            response.addToErrorMessages(CREATED_ITEM_ROLLED_BACK_MESSAGE);
        }
        return response;
    }

    private String creationErrorMessage(final String rawMessage) {
        String message = rawMessage;
        if (message == null) {
            return "";
        }

        String validationPrefix = "Failed Validation: ";
        if (message.startsWith(validationPrefix)
                && message.substring(validationPrefix.length()).endsWith(" : field is mandatory")
                && !message.substring(validationPrefix.length()).contains(", ")) {
            return message.substring(validationPrefix.length());
        }
        return message;
    }
}
