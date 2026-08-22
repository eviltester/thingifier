package uk.co.compendiumdev.thingifier.api.validation;

/**
 * Named route-level operation validator registration.
 *
 * <p>The name is for diagnostics and future tooling. Validators themselves are code-only in v1 so
 * they are deliberately not exported to YAML, model import/export, or OpenAPI.
 */
public final class ApiOperationValidatorDefinition {

    private final String name;
    private final ApiOperationValidator validator;

    /**
     * Creates a named validator registration.
     *
     * @param name stable validator name for readers and diagnostics
     * @param validator callback to run for the route
     * @throws IllegalArgumentException when the name is blank or the validator is null
     */
    public ApiOperationValidatorDefinition(
            final String name, final ApiOperationValidator validator) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("validator name is required");
        }
        if (validator == null) {
            throw new IllegalArgumentException("validator is required");
        }
        this.name = name.trim();
        this.validator = validator;
    }

    /**
     * Returns the registration name.
     *
     * @return validator name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the validator callback.
     *
     * @return operation validator
     */
    public ApiOperationValidator validator() {
        return validator;
    }
}
