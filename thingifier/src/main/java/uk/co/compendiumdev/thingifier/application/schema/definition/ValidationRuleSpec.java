package uk.co.compendiumdev.thingifier.application.schema.definition;

public final class ValidationRuleSpec {

    public static final String NOT_EMPTY = "notEmpty";
    public static final String MAXIMUM_LENGTH = "maximumLength";
    public static final String MATCHES_REGEX = "matchesRegex";
    public static final String SATISFIES_REGEX = "satisfiesRegex";

    private final String name;
    private final String value;

    public ValidationRuleSpec(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    public static ValidationRuleSpec notEmpty() {
        return new ValidationRuleSpec(NOT_EMPTY, null);
    }

    public static ValidationRuleSpec maximumLength(final int length) {
        return new ValidationRuleSpec(MAXIMUM_LENGTH, String.valueOf(length));
    }

    public static ValidationRuleSpec matchesRegex(final String regex) {
        return new ValidationRuleSpec(MATCHES_REGEX, regex);
    }

    public static ValidationRuleSpec satisfiesRegex(final String regex) {
        return new ValidationRuleSpec(SATISFIES_REGEX, regex);
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }
}
