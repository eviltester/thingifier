package uk.co.compendiumdev.thingifier.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public final class ThingCommandResult {

    private final boolean successful;
    private final EntityInstance instance;
    private final List<String> errorMessages;
    private final boolean rolledBackCreatedInstance;
    private final ApplicationError error;
    private final boolean createdInstance;

    private ThingCommandResult(
            final boolean successful,
            final EntityInstance instance,
            final List<String> errorMessages,
            final boolean rolledBackCreatedInstance,
            final ApplicationError error,
            final boolean createdInstance) {
        this.successful = successful;
        this.instance = instance;
        this.errorMessages = Collections.unmodifiableList(new ArrayList<>(errorMessages));
        this.rolledBackCreatedInstance = rolledBackCreatedInstance;
        this.error = error;
        this.createdInstance = createdInstance;
    }

    public static ThingCommandResult success() {
        return new ThingCommandResult(true, null, List.of(), false, null, false);
    }

    public static ThingCommandResult success(final EntityInstance instance) {
        return new ThingCommandResult(true, instance, List.of(), false, null, false);
    }

    public static ThingCommandResult created(final EntityInstance instance) {
        return new ThingCommandResult(true, instance, List.of(), false, null, true);
    }

    public static ThingCommandResult error(final String message) {
        if (message == null || message.isEmpty()) {
            return error(ApplicationError.validation(""));
        }
        return error(ApplicationError.validation(message));
    }

    public static ThingCommandResult error(final Collection<String> messages) {
        return error(ApplicationError.validation(messages));
    }

    public static ThingCommandResult error(final ApplicationError error) {
        return new ThingCommandResult(false, null, error.messages(), false, error, false);
    }

    public ThingCommandResult withRolledBackCreatedInstance() {
        return new ThingCommandResult(
                successful, instance, errorMessages, true, error, createdInstance);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public boolean isError() {
        return !successful;
    }

    public EntityInstance getInstance() {
        return instance;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public String getCombinedErrorMessage() {
        return String.join(", ", errorMessages);
    }

    public boolean rolledBackCreatedInstance() {
        return rolledBackCreatedInstance;
    }

    public ApplicationError getError() {
        return error;
    }

    public int statusCode() {
        if (error == null) {
            return 200;
        }
        return error.statusCode();
    }

    public boolean createdInstance() {
        return createdInstance;
    }
}
