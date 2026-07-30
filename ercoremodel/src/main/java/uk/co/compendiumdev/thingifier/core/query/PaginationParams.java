package uk.co.compendiumdev.thingifier.core.query;

public final class PaginationParams {
    public static final String LIMIT_PARAMETER_NAME = "_limit";
    public static final String OFFSET_PARAMETER_NAME = "_offset";

    private Integer limit;
    private Integer offset;
    private String validationError;

    public PaginationParams(final QueryFilterParams queryParams) {
        QueryFilterParams params = queryParams == null ? new QueryFilterParams() : queryParams;

        for (FilterBy param : params.toList()) {
            if (isLimitParam(param.fieldName)) {
                limit = parseNonNegativeInteger(param, LIMIT_PARAMETER_NAME);
            }
            if (isOffsetParam(param.fieldName)) {
                offset = parseNonNegativeInteger(param, OFFSET_PARAMETER_NAME);
            }
        }
    }

    public static boolean isPaginationParam(final String key) {
        return isLimitParam(key) || isOffsetParam(key);
    }

    public static boolean isLimitParam(final String key) {
        return LIMIT_PARAMETER_NAME.equals(key);
    }

    public static boolean isOffsetParam(final String key) {
        return OFFSET_PARAMETER_NAME.equals(key);
    }

    public boolean hasLimit() {
        return limit != null;
    }

    public boolean hasOffset() {
        return offset != null;
    }

    public int limitOr(final int defaultLimit) {
        return hasLimit() ? limit : defaultLimit;
    }

    public int offsetOr(final int defaultOffset) {
        return hasOffset() ? offset : defaultOffset;
    }

    public boolean hasValidationError() {
        return validationError != null;
    }

    public String validationError() {
        return validationError;
    }

    private Integer parseNonNegativeInteger(final FilterBy param, final String parameterName) {
        if (hasValidationError()) {
            return null;
        }

        if (!"=".equals(param.filterOperation)) {
            validationError = String.format("%s must be a non-negative integer", parameterName);
            return null;
        }

        try {
            int value = Integer.parseInt(param.fieldValue.trim());
            if (value < 0) {
                validationError = String.format("%s must be a non-negative integer", parameterName);
                return null;
            }
            return value;
        } catch (NumberFormatException e) {
            validationError = String.format("%s must be a non-negative integer", parameterName);
            return null;
        }
    }
}
