package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

public class VRule {

    private VRule(){
        // don't be tempted, this is a factory class
    }

    public static ValidationRule notEmpty() {
        return new NotEmptyValidationRule();
    }

    public static ValidationRule maximumLength(final int maxLength) {
        return new MaximumLengthValidationRule(maxLength);
    }

    public static ValidationRule matchesRegex(final String regexToMatch) {
        return new MatchesRegexValidationRule(regexToMatch);
    }
    public static ValidationRule satisfiesRegex(final String regexToFind) {
        return new FindsRegexValidationRule(regexToFind);
    }
}
