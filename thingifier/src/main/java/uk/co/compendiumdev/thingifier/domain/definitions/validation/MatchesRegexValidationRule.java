package uk.co.compendiumdev.thingifier.domain.definitions.validation;

import uk.co.compendiumdev.thingifier.domain.definitions.fielddefinition.FieldValue;

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
    public boolean validates(final FieldValue value) {
        final String stringValue = value.asString();
        return pattern.matcher(stringValue).matches();
    }

    @Override
    public String getErrorMessage(final FieldValue value) {
        return String.format("%s does not match the regex %s",value.getName(), this.regexToMatch);
    }
}
