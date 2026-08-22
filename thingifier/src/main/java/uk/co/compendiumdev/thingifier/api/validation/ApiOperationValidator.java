package uk.co.compendiumdev.thingifier.api.validation;

/**
 * Validates one matched API operation after request parsing and route policy checks.
 *
 * <p>Use this for request-aware API rules that do not belong in entity model validation, such as
 * "this route requires a principal-selected challenger" or "this public operation requires a field
 * that the entity itself does not always require". Validators are code-only and run after
 * authentication, data-scope selection, fixed-route resolution, content parsing, request view
 * checks, write-method policy, and command/query mapping have all succeeded.
 */
@FunctionalInterface
public interface ApiOperationValidator {

    /**
     * Validates the current API operation.
     *
     * @param context immutable request, route, auth, data-scope, and payload details
     * @return accepted or rejected validation result
     */
    ApiOperationValidationResult validate(ApiOperationValidationContext context);
}
