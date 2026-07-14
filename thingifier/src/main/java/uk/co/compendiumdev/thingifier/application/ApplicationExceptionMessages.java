package uk.co.compendiumdev.thingifier.application;

final class ApplicationExceptionMessages {

    private ApplicationExceptionMessages() {}

    static String messageFrom(final Exception exception) {
        String message = exception.getMessage();
        return message == null ? "" : message;
    }
}
