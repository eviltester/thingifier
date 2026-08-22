package uk.co.compendiumdev.thingifier.api.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Factory methods for common API operation validators.
 *
 * <p>These helpers cover route-level API rules which are more about a public operation contract
 * than entity state. They are intentionally code-only so applications can compose them with custom
 * validators without adding serialization requirements to the model.
 */
public final class ApiOperationValidators {

    private ApiOperationValidators() {}

    /**
     * Starts a validator that rejects when parsed request body fields are absent.
     *
     * <p>This checks field presence only. Use entity field validation for type, mandatory, and
     * non-empty value rules.
     *
     * @param fieldNames body field names that must be present
     * @return builder used to choose the rejection response
     * @throws IllegalArgumentException when no field names are supplied or any field name is blank
     */
    public static RequiredBodyFieldsValidatorBuilder requireBodyFields(final String... fieldNames) {
        return new RequiredBodyFieldsValidatorBuilder(fieldNames);
    }

    /** Builder for {@link #requireBodyFields(String...)} rejection details. */
    public static final class RequiredBodyFieldsValidatorBuilder {

        private final List<String> fieldNames;

        private RequiredBodyFieldsValidatorBuilder(final String... fieldNames) {
            if (fieldNames == null || fieldNames.length == 0) {
                throw new IllegalArgumentException("at least one field name is required");
            }
            List<String> names = new ArrayList<>();
            for (String fieldName : fieldNames) {
                if (fieldName == null || fieldName.trim().isEmpty()) {
                    throw new IllegalArgumentException("field name is required");
                }
                names.add(fieldName.trim());
            }
            this.fieldNames = Collections.unmodifiableList(names);
        }

        /**
         * Creates the validator with the rejection response to use when any field is missing.
         *
         * @param statusCode HTTP-style status code to return
         * @param message error message to include in Thingifier's normal error response
         * @return operation validator
         */
        public ApiOperationValidator onMissing(final int statusCode, final String message) {
            ApiOperationValidationResult rejection =
                    ApiOperationValidationResult.reject(statusCode, message);
            return context -> {
                for (String fieldName : fieldNames) {
                    if (!context.requestBody().asMap().containsKey(fieldName)) {
                        return rejection;
                    }
                }
                return ApiOperationValidationResult.accept();
            };
        }
    }
}
