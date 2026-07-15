package uk.co.compendiumdev.thingifier.crudui;

public final class CrudUiException extends RuntimeException {

    private final int statusCode;

    public CrudUiException(final int statusCode, final String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
