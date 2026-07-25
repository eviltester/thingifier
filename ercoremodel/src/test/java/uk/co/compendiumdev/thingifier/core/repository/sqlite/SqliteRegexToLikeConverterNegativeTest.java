package uk.co.compendiumdev.thingifier.core.repository.sqlite;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class SqliteRegexToLikeConverterNegativeTest {

    @Test
    public void rejectsNullRegex() {
        Assertions.assertThrows(
                SqliteRegexToLikeConverter.RegexToLikeConversionException.class,
                () -> SqliteRegexToLikeConverter.convert(null));
    }

    @Test
    public void emptyRegexDoesNotBecomeMatchAllLikeQuery() {
        SqliteRegexToLikeConverter.Conversion conversion = SqliteRegexToLikeConverter.convert("");

        Assertions.assertTrue(conversion.isEquality());
        Assertions.assertEquals("", conversion.sqlValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"*", "**", "abc*", "abc**", "abc+", "abc?", "abc{2}"})
    public void rejectsUnsupportedQuantifiersAndRepeatedWildcards(final String regex) {
        Assertions.assertThrows(
                SqliteRegexToLikeConverter.RegexToLikeConversionException.class,
                () -> SqliteRegexToLikeConverter.convert(regex));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc\\", "\\d+", "\\w+", "\\s+", "\\1"})
    public void rejectsTrailingEscapesAndRegexShorthandEscapes(final String regex) {
        Assertions.assertThrows(
                SqliteRegexToLikeConverter.RegexToLikeConversionException.class,
                () -> SqliteRegexToLikeConverter.convert(regex));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab^c", "ab$c", "cat|dog", "(cat)", "(?i)cat", "cat(?=dog)", "[abc]"})
    public void rejectsUnsupportedRegexOperators(final String regex) {
        Assertions.assertThrows(
                SqliteRegexToLikeConverter.RegexToLikeConversionException.class,
                () -> SqliteRegexToLikeConverter.convert(regex));
    }

    @Test
    public void sqlInjectionTextIsTreatedAsAnEqualityParameterValue() {
        SqliteRegexToLikeConverter.Conversion conversion =
                SqliteRegexToLikeConverter.convert("' OR 1=1 --");

        Assertions.assertTrue(conversion.isEquality());
        Assertions.assertEquals("' OR 1=1 --", conversion.sqlValue());
    }

    @Test
    public void sqlInjectionTextWithRegexWildcardsIsStillOnlyAParameterValue() {
        SqliteRegexToLikeConverter.Conversion conversion =
                SqliteRegexToLikeConverter.convert(".*' OR 1=1 --.*");

        Assertions.assertFalse(conversion.isEquality());
        Assertions.assertEquals("%' OR 1=1 --%", conversion.sqlValue());
    }

    @Test
    public void sqlLikeWildcardsInSuspiciousTextAreEscaped() {
        SqliteRegexToLikeConverter.Conversion conversion =
                SqliteRegexToLikeConverter.convert(".*admin_%' OR 1=1 --.*");

        Assertions.assertFalse(conversion.isEquality());
        Assertions.assertEquals("%admin\\_\\%' OR 1=1 --%", conversion.sqlValue());
    }

    @Test
    public void escapedSqlLikeWildcardsRemainLiteralInEqualityConversion() {
        SqliteRegexToLikeConverter.Conversion conversion =
                SqliteRegexToLikeConverter.convert("admin\\_\\%");

        Assertions.assertTrue(conversion.isEquality());
        Assertions.assertEquals("admin_%", conversion.sqlValue());
    }
}
