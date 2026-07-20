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
        INSTANCE_NOT_FOUND,
        PARENT_INSTANCE_NOT_FOUND,
        RELATIONSHIP_SOURCE_NOT_FOUND,
        RELATIONSHIP_TARGET_NOT_FOUND,
        CONFLICT,
        UNSUPPORTED_COMMAND,
        REPLACE_CREATE_AUTO_FIELDS_NOT_ALLOWED,
        REPLACE_CREATE_KEY_MISMATCH,
        MAX_INSTANCE_LIMIT_REACHED,
        MAX_INSTANCE_LIMIT_WOULD_BE_EXCEEDED,
        DUPLICATE_PRIMARY_KEY,
        MISSING_PRIMARY_KEY,
        STORE_WRITE_VALIDATION_FAILED
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

    public static ApplicationError instanceNotFound(
            final String entityName, final String identifier) {
        return new ApplicationError(
                Category.NOT_FOUND,
                Code.INSTANCE_NOT_FOUND,
                List.of("Could not find instance"),
                Map.of("entityName", entityName, "identifier", identifier));
    }

    public static ApplicationError parentInstanceNotFound(
            final String entityName, final String identifier, final String relationshipName) {
        return new ApplicationError(
                Category.NOT_FOUND,
                Code.PARENT_INSTANCE_NOT_FOUND,
                List.of("Could not find relationship parent instance"),
                Map.of(
                        "entityName",
                        entityName,
                        "identifier",
                        identifier,
                        "relationshipName",
                        relationshipName));
    }

    public static ApplicationError relationshipSourceNotFound(
            final String entityName, final String identifier, final String relationshipName) {
        return new ApplicationError(
                Category.NOT_FOUND,
                Code.RELATIONSHIP_SOURCE_NOT_FOUND,
                List.of("Could not find relationship source instance"),
                Map.of(
                        "entityName",
                        entityName,
                        "identifier",
                        identifier,
                        "relationshipName",
                        relationshipName));
    }

    public static ApplicationError relationshipTargetNotFound(
            final String entityName,
            final String identifier,
            final String relationshipName,
            final String childIdentifier) {
        return new ApplicationError(
                Category.NOT_FOUND,
                Code.RELATIONSHIP_TARGET_NOT_FOUND,
                List.of("Could not find related target instance"),
                Map.of(
                        "entityName",
                        entityName,
                        "identifier",
                        identifier,
                        "relationshipName",
                        relationshipName,
                        "childIdentifier",
                        childIdentifier));
    }

    public static ApplicationError conflict(final String message) {
        return new ApplicationError(Category.CONFLICT, Code.CONFLICT, List.of(message), Map.of());
    }

    public static ApplicationError maxInstanceLimitReached(
            final String message, final Map<String, String> details) {
        return new ApplicationError(
                Category.CONFLICT, Code.MAX_INSTANCE_LIMIT_REACHED, List.of(message), details);
    }

    public static ApplicationError maxInstanceLimitWouldBeExceeded(
            final String message, final Map<String, String> details) {
        return new ApplicationError(
                Category.CONFLICT,
                Code.MAX_INSTANCE_LIMIT_WOULD_BE_EXCEEDED,
                List.of(message),
                details);
    }

    public static ApplicationError duplicatePrimaryKey(
            final String message, final Map<String, String> details) {
        return new ApplicationError(
                Category.CONFLICT, Code.DUPLICATE_PRIMARY_KEY, List.of(message), details);
    }

    public static ApplicationError missingPrimaryKey(
            final String message, final Map<String, String> details) {
        return new ApplicationError(
                Category.VALIDATION, Code.MISSING_PRIMARY_KEY, List.of(message), details);
    }

    public static ApplicationError storeWriteValidationFailed(
            final String message, final Map<String, String> details) {
        return new ApplicationError(
                Category.VALIDATION, Code.STORE_WRITE_VALIDATION_FAILED, List.of(message), details);
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
