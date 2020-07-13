package uk.co.compendiumdev.thingifier.domain.definitions.validation;

import java.util.regex.Pattern;

public class MatchesRegexValidationRule implements ValidationRule {
    private final String regexToMatch;
    private final Pattern pattern;

    public MatchesRegexValidationRule(final String regexToMatch) {
        this.regexToMatch = regexToMatch;
        // test if regex is valid - throws exception if not
        pattern = Pattern.compile(regexToMatch);
    }

    @Override
    public boolean validates(final String value) {
        return pattern.matcher(value).matches();
    }

    @Override
    public String getErrorMessage(final String fieldName) {
        return String.format("%s does not match the regex %s",fieldName, this.regexToMatch);
    }
}
