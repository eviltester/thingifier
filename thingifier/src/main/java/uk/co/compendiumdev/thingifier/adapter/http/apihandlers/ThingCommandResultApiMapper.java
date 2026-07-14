package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.List;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.ApplicationError;
import uk.co.compendiumdev.thingifier.application.ThingCommandResult;
import uk.co.compendiumdev.thingifier.application.command.AmendThingCommand;
import uk.co.compendiumdev.thingifier.application.command.ConnectExistingRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateAndConnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.CreateThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DeleteThingCommand;
import uk.co.compendiumdev.thingifier.application.command.DisconnectRelationshipCommand;
import uk.co.compendiumdev.thingifier.application.command.RelateThingCommand;
import uk.co.compendiumdev.thingifier.application.command.ReplaceThingCommand;
import uk.co.compendiumdev.thingifier.application.command.ThingWriteCommand;

public final class ThingCommandResultApiMapper {

    private static final String CREATED_ITEM_ROLLED_BACK_MESSAGE =
            " the newly created item was deleted. No new items have been created.";
    private static final String VALIDATION_PREFIX = "Failed Validation: ";

    private final ThingifierApiConfig apiConfig;

    public ThingCommandResultApiMapper(final ThingifierApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    public ApiResponse map(final ApiMappingError error) {
        return ApiResponse.error(error.statusCode(), error.messages());
    }

    public ApiResponse map(final ThingWriteCommand command, final ThingCommandResult result) {
        if (result.isError()) {
            return errorResponseFor(command, result);
        }

        if (command instanceof RelateThingCommand) {
            return ApiResponse.created(
                    result.createdInstance() ? result.getInstance() : null, apiConfig);
        }

        if (command instanceof CreateThingCommand
                || command instanceof CreateAndConnectRelationshipCommand) {
            return ApiResponse.created(result.getInstance(), apiConfig);
        }

        if (command instanceof ConnectExistingRelationshipCommand) {
            return ApiResponse.created(null, apiConfig);
        }

        if (command instanceof ReplaceThingCommand) {
            if (result.createdInstance()) {
                return ApiResponse.created(result.getInstance(), apiConfig);
            }
            return ApiResponse.success().returnSingleInstance(result.getInstance());
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
        List<String> messages = errorMessagesFor(result);
        int statusCode = statusFor(result.getError());
        ApiResponse response;
        if (command instanceof CreateThingCommand && messages.size() == 1) {
            response =
                    ApiResponse.error(
                            statusCode, creationErrorMessage(String.join(", ", messages)));
        } else {
            response = ApiResponse.error(statusCode, messages);
        }

        if (result.rolledBackCreatedInstance()) {
            response.addToErrorMessages(CREATED_ITEM_ROLLED_BACK_MESSAGE);
        }
        return response;
    }

    public static int statusFor(final ApplicationError error) {
        if (error == null) {
            return 400;
        }
        if (error.category() == ApplicationError.Category.NOT_FOUND) {
            return 404;
        }
        if (error.category() == ApplicationError.Category.CONFLICT) {
            return 409;
        }
        return 400;
    }

    private List<String> errorMessagesFor(final ThingCommandResult result) {
        ApplicationError error = result.getError();
        if (error == null) {
            return result.getErrorMessages();
        }

        if (error.code() == ApplicationError.Code.REPLACE_CREATE_AUTO_FIELDS_NOT_ALLOWED) {
            return List.of(
                    String.format(
                            "Cannot create %s with PUT due to Auto fields %s",
                            error.detail("entityName"), error.detail("fieldNames")));
        }

        if (error.code() == ApplicationError.Code.REPLACE_CREATE_KEY_MISMATCH) {
            return List.of(
                    String.format(
                            "Cannot create %s with PUT as key does not match body value %s != %s",
                            error.detail("entityName"),
                            error.detail("routeIdentifier"),
                            error.detail("bodyIdentifier")));
        }

        return result.getErrorMessages();
    }

    private String creationErrorMessage(final String rawMessage) {
        String message = rawMessage;
        if (message == null) {
            return "";
        }

        if (message.startsWith(VALIDATION_PREFIX)
                && message.substring(VALIDATION_PREFIX.length()).endsWith(" : field is mandatory")
                && !message.substring(VALIDATION_PREFIX.length()).contains(", ")) {
            return message.substring(VALIDATION_PREFIX.length());
        }
        if (isPlainValidationMessage(message)) {
            return VALIDATION_PREFIX + message;
        }
        return message;
    }

    private boolean isPlainValidationMessage(final String message) {
        return message != null
                && !message.startsWith(VALIDATION_PREFIX)
                && !message.startsWith("ERROR:")
                && !message.startsWith("Cannot Create");
    }
}
