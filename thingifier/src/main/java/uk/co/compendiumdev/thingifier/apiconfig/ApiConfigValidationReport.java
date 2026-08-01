package uk.co.compendiumdev.thingifier.apiconfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ApiConfigValidationReport {

    private final List<ApiConfigValidationMessage> errors;
    private final List<ApiConfigValidationMessage> warnings;

    public ApiConfigValidationReport() {
        errors = new ArrayList<>();
        warnings = new ArrayList<>();
    }

    public void addError(final String path, final String message) {
        errors.add(new ApiConfigValidationMessage(path, message));
    }

    public void addWarning(final String path, final String message) {
        warnings.add(new ApiConfigValidationMessage(path, message));
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public List<ApiConfigValidationMessage> errors() {
        return Collections.unmodifiableList(errors);
    }

    public List<ApiConfigValidationMessage> warnings() {
        return Collections.unmodifiableList(warnings);
    }

    public List<String> errorMessages() {
        return messagesFor(errors);
    }

    public List<String> warningMessages() {
        return messagesFor(warnings);
    }

    public String combinedErrorMessages() {
        return String.join("; ", errorMessages());
    }

    public String combinedWarningMessages() {
        return String.join("; ", warningMessages());
    }

    private List<String> messagesFor(final List<ApiConfigValidationMessage> messages) {
        final List<String> output = new ArrayList<>();
        for (ApiConfigValidationMessage message : messages) {
            output.add(message.toString());
        }
        return output;
    }

    public static final class ApiConfigValidationMessage {

        private final String path;
        private final String message;

        private ApiConfigValidationMessage(final String path, final String message) {
            this.path = path;
            this.message = message;
        }

        public String path() {
            return path;
        }

        public String message() {
            return message;
        }

        @Override
        public String toString() {
            if (path == null || path.trim().isEmpty()) {
                return message;
            }
            return path + ": " + message;
        }
    }
}
