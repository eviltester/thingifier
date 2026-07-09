package uk.co.compendiumdev.thingifier.core.repository.sqlite;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

final class SqliteRegexToLikeConverter {

    private SqliteRegexToLikeConverter() {}

    static Conversion convert(final String regex) {
        validateRegex(regex);

        String pattern = regex;
        if (pattern.startsWith("^")) {
            pattern = pattern.substring(1);
        }
        if (hasTrailingUnescapedDollar(pattern)) {
            pattern = pattern.substring(0, pattern.length() - 1);
        }

        StringBuilder exactValue = new StringBuilder();
        StringBuilder likeValue = new StringBuilder();
        boolean usesLikeWildcard = false;

        for (int index = 0; index < pattern.length(); index++) {
            char current = pattern.charAt(index);

            if (current == '\\') {
                if (index == pattern.length() - 1) {
                    throw new RegexToLikeConversionException("Trailing regex escape");
                }
                char escaped = pattern.charAt(++index);
                if (isUnsupportedEscape(escaped)) {
                    throw new RegexToLikeConversionException("Unsupported regex escape");
                }
                appendLiteral(exactValue, likeValue, escaped);
                continue;
            }

            if (current == '.') {
                if (index < pattern.length() - 1 && pattern.charAt(index + 1) == '*') {
                    likeValue.append('%');
                    usesLikeWildcard = true;
                    index++;
                } else {
                    likeValue.append('_');
                    usesLikeWildcard = true;
                }
                continue;
            }

            if (isUnsupportedRegexMeta(current)) {
                throw new RegexToLikeConversionException("Unsupported regex operator");
            }

            appendLiteral(exactValue, likeValue, current);
        }

        if (usesLikeWildcard) {
            return Conversion.like(likeValue.toString());
        }
        return Conversion.equalTo(exactValue.toString());
    }

    private static void validateRegex(final String regex) {
        if (regex == null) {
            throw new RegexToLikeConversionException("Regex value is null");
        }
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new RegexToLikeConversionException("Invalid regex", e);
        }
    }

    private static void appendLiteral(
            final StringBuilder exactValue, final StringBuilder likeValue, final char literal) {
        exactValue.append(literal);
        if (literal == '\\' || literal == '%' || literal == '_') {
            likeValue.append('\\');
        }
        likeValue.append(literal);
    }

    private static boolean isUnsupportedEscape(final char escaped) {
        return Character.isLetterOrDigit(escaped);
    }

    private static boolean isUnsupportedRegexMeta(final char current) {
        return current == '['
                || current == ']'
                || current == '('
                || current == ')'
                || current == '{'
                || current == '}'
                || current == '+'
                || current == '?'
                || current == '*'
                || current == '|'
                || current == '^'
                || current == '$';
    }

    private static boolean hasTrailingUnescapedDollar(final String pattern) {
        if (pattern.isEmpty() || pattern.charAt(pattern.length() - 1) != '$') {
            return false;
        }

        int backslashes = 0;
        for (int index = pattern.length() - 2;
                index >= 0 && pattern.charAt(index) == '\\';
                index--) {
            backslashes++;
        }
        return backslashes % 2 == 0;
    }

    static final class Conversion {
        private final boolean equality;
        private final String sqlValue;

        private Conversion(final boolean equality, final String sqlValue) {
            this.equality = equality;
            this.sqlValue = sqlValue;
        }

        static Conversion equalTo(final String sqlValue) {
            return new Conversion(true, sqlValue);
        }

        static Conversion like(final String sqlValue) {
            return new Conversion(false, sqlValue);
        }

        boolean isEquality() {
            return equality;
        }

        String sqlValue() {
            return sqlValue;
        }
    }

    static final class RegexToLikeConversionException extends RuntimeException {
        RegexToLikeConversionException(final String message) {
            super(message);
        }

        RegexToLikeConversionException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
