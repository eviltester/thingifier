package uk.co.compendiumdev.thingifier.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class ApplicationError {

    public enum Category {
        VALIDATION,
        NOT_FOUND,
        CONFLICT,
        UNSUPPORTED
    }

    private final Category category;
    private final int statusCode;
    private final List<String> messages;

    private ApplicationError(
            final Category category, final int statusCode, final Collection<String> messages) {
        this.category = category;
        this.statusCode = statusCode;
        this.messages = Collections.unmodifiableList(new ArrayList<>(messages));
    }

    public static ApplicationError validation(final String message) {
        return validation(List.of(message));
    }

    public static ApplicationError validation(final Collection<String> messages) {
        return new ApplicationError(Category.VALIDATION, 400, messages);
    }

    public static ApplicationError notFound(final String message) {
        return new ApplicationError(Category.NOT_FOUND, 404, List.of(message));
    }

    public static ApplicationError conflict(final String message) {
        return new ApplicationError(Category.CONFLICT, 409, List.of(message));
    }

    public static ApplicationError unsupported(final String message) {
        return new ApplicationError(Category.UNSUPPORTED, 400, List.of(message));
    }

    public Category category() {
        return category;
    }

    public int statusCode() {
        return statusCode;
    }

    public List<String> messages() {
        return messages;
    }
}
