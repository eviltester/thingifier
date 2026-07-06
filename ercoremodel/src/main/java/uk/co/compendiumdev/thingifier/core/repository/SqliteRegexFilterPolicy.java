package uk.co.compendiumdev.thingifier.core.repository;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

final class SqliteRegexFilterPolicy {

    private static final int MAX_REGEX_LENGTH = 512;
    private static final Pattern NESTED_QUANTIFIER =
            Pattern.compile("\\((?:\\\\.|[^\\\\)])*[+*](?:\\\\.|[^\\\\)])*\\)\\s*[+*?{]");

    private SqliteRegexFilterPolicy() {
    }

    static Pattern compileSupported(final String regex) {
        if (regex == null) {
            throw new UnsupportedRegexFilterException("Regex value is null");
        }
        if (regex.length() > MAX_REGEX_LENGTH) {
            throw new UnsupportedRegexFilterException(
                    "Regex exceeds maximum supported length of " + MAX_REGEX_LENGTH);
        }
        if (NESTED_QUANTIFIER.matcher(regex).find()) {
            throw new UnsupportedRegexFilterException(
                    "Regex uses nested quantifiers that are not allowed for SQLite filtering");
        }

        try {
            return Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new UnsupportedRegexFilterException("Invalid regex", e);
        }
    }

    static final class UnsupportedRegexFilterException extends RuntimeException {
        UnsupportedRegexFilterException(final String message) {
            super(message);
        }

        UnsupportedRegexFilterException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
