package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class ApiMappingError {

    private final int statusCode;
    private final List<String> messages;

    private ApiMappingError(final int statusCode, final Collection<String> messages) {
        this.statusCode = statusCode;
        this.messages = Collections.unmodifiableList(new ArrayList<>(messages));
    }

    public static ApiMappingError withMessage(final int statusCode, final String message) {
        return new ApiMappingError(statusCode, Collections.singletonList(message));
    }

    public static ApiMappingError withMessages(
            final int statusCode, final Collection<String> messages) {
        return new ApiMappingError(statusCode, messages);
    }

    public int statusCode() {
        return statusCode;
    }

    public List<String> messages() {
        return messages;
    }

    public String firstMessage() {
        if (messages.isEmpty()) {
            return "";
        }
        return messages.get(0);
    }
}
