package uk.co.compendiumdev.thingifier.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ApplicationError {

    public enum Category {
        VALIDATION,
        NOT_FOUND,
        CONFLICT,
        UNSUPPORTED
    }

    public enum Code {
        VALIDATION_FAILED,
        NOT_FOUND,
        CONFLICT,
        UNSUPPORTED_COMMAND,
        REPLACE_CREATE_AUTO_FIELDS_NOT_ALLOWED,
        REPLACE_CREATE_KEY_MISMATCH
    }

    private final Category category;
    private final Code code;
    private final List<String> messages;
    private final Map<String, String> details;

    private ApplicationError(
            final Category category,
            final Code code,
            final Collection<String> messages,
            final Map<String, String> details) {
        this.category = category;
        this.code = code;
        this.messages = Collections.unmodifiableList(new ArrayList<>(messages));
        this.details = Collections.unmodifiableMap(new HashMap<>(details));
    }

    public static ApplicationError validation(final String message) {
        return validation(List.of(message));
    }

    public static ApplicationError validation(final Collection<String> messages) {
        return new ApplicationError(
                Category.VALIDATION, Code.VALIDATION_FAILED, messages, Map.of());
    }

    public static ApplicationError notFound(final String message) {
        return new ApplicationError(Category.NOT_FOUND, Code.NOT_FOUND, List.of(message), Map.of());
    }

    public static ApplicationError conflict(final String message) {
        return new ApplicationError(Category.CONFLICT, Code.CONFLICT, List.of(message), Map.of());
    }

    public static ApplicationError unsupported(final String message) {
        return new ApplicationError(
                Category.UNSUPPORTED, Code.UNSUPPORTED_COMMAND, List.of(message), Map.of());
    }

    public static ApplicationError replaceCreateAutoFieldsNotAllowed(
            final String entityName, final String fieldNames) {
        return new ApplicationError(
                Category.VALIDATION,
                Code.REPLACE_CREATE_AUTO_FIELDS_NOT_ALLOWED,
                List.of("Cannot create " + entityName + " because generated fields are present"),
                Map.of("entityName", entityName, "fieldNames", fieldNames));
    }

    public static ApplicationError replaceCreateKeyMismatch(
            final String entityName, final String routeIdentifier, final String bodyIdentifier) {
        return new ApplicationError(
                Category.VALIDATION,
                Code.REPLACE_CREATE_KEY_MISMATCH,
                List.of("Cannot create " + entityName + " because identity values do not match"),
                Map.of(
                        "entityName",
                        entityName,
                        "routeIdentifier",
                        routeIdentifier,
                        "bodyIdentifier",
                        bodyIdentifier));
    }

    public Category category() {
        return category;
    }

    public Code code() {
        return code;
    }

    public List<String> messages() {
        return messages;
    }

    public Map<String, String> details() {
        return details;
    }

    public String detail(final String name) {
        return details.get(name);
    }
}
