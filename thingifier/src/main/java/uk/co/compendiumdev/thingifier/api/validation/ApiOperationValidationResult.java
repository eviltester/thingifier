package uk.co.compendiumdev.thingifier.api.validation;

/**
 * Result returned by an {@link ApiOperationValidator}.
 *
 * <p>The result intentionally models only accept or reject for v1. A rejection is converted into
 * Thingifier's normal API error response and stops later operation validators, entity validators,
 * lifecycle validation phases, and mutation.
 */
public final class ApiOperationValidationResult {

    private final boolean accepted;
    private final int statusCode;
    private final String message;

    private ApiOperationValidationResult(
            final boolean accepted, final int statusCode, final String message) {
        this.accepted = accepted;
        this.statusCode = statusCode;
        this.message = message == null ? "" : message;
    }

    /**
     * Creates an accepted result so the operation can continue.
     *
     * @return accepted validation result
     */
    public static ApiOperationValidationResult accept() {
        return new ApiOperationValidationResult(true, 200, "");
    }

    /**
     * Creates a rejected result with the response status and message to return to the caller.
     *
     * @param statusCode HTTP-style status code to return
     * @param message error message to include in Thingifier's normal error response
     * @return rejected validation result
     * @throws IllegalArgumentException when the status code is outside the HTTP range
     */
    public static ApiOperationValidationResult reject(final int statusCode, final String message) {
        if (statusCode < 100 || statusCode > 599) {
            throw new IllegalArgumentException("statusCode must be between 100 and 599");
        }
        return new ApiOperationValidationResult(false, statusCode, message);
    }

    /**
     * Reports whether validation accepted the operation.
     *
     * @return true when processing can continue
     */
    public boolean accepted() {
        return accepted;
    }

    /**
     * Reports whether validation rejected the operation.
     *
     * @return true when processing must stop
     */
    public boolean rejected() {
        return !accepted;
    }

    /**
     * Returns the status code for a rejected operation.
     *
     * @return rejection status code
     */
    public int statusCode() {
        return statusCode;
    }

    /**
     * Returns the error message for a rejected operation.
     *
     * @return rejection message, never null
     */
    public String message() {
        return message;
    }
}
