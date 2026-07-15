package uk.co.compendiumdev.thingifier.application.schema.definition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SchemaDefinitionValidationReport {

    private final List<SchemaDefinitionValidationError> errors;

    public SchemaDefinitionValidationReport() {
        errors = new ArrayList<>();
    }

    public void addError(final String path, final String message) {
        errors.add(new SchemaDefinitionValidationError(path, message));
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<SchemaDefinitionValidationError> errors() {
        return Collections.unmodifiableList(errors);
    }

    public List<String> messages() {
        final List<String> messages = new ArrayList<>();
        for (SchemaDefinitionValidationError error : errors) {
            messages.add(error.toString());
        }
        return messages;
    }

    public String combinedMessages() {
        return String.join("; ", messages());
    }

    public static final class SchemaDefinitionValidationError {

        private final String path;
        private final String message;

        private SchemaDefinitionValidationError(final String path, final String message) {
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
