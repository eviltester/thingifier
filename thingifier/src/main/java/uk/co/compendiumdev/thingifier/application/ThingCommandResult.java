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

    private ThingCommandResult(
            final boolean successful,
            final EntityInstance instance,
            final List<String> errorMessages,
            final boolean rolledBackCreatedInstance) {
        this.successful = successful;
        this.instance = instance;
        this.errorMessages = Collections.unmodifiableList(new ArrayList<>(errorMessages));
        this.rolledBackCreatedInstance = rolledBackCreatedInstance;
    }

    public static ThingCommandResult success() {
        return new ThingCommandResult(true, null, List.of(), false);
    }

    public static ThingCommandResult success(final EntityInstance instance) {
        return new ThingCommandResult(true, instance, List.of(), false);
    }

    public static ThingCommandResult error(final String message) {
        if (message == null || message.isEmpty()) {
            return new ThingCommandResult(false, null, List.of(""), false);
        }
        return new ThingCommandResult(false, null, List.of(message), false);
    }

    public static ThingCommandResult error(final Collection<String> messages) {
        return new ThingCommandResult(false, null, new ArrayList<>(messages), false);
    }

    public ThingCommandResult withRolledBackCreatedInstance() {
        return new ThingCommandResult(successful, instance, errorMessages, true);
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
}
