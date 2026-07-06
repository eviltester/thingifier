package uk.co.compendiumdev.thingifier.core.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SqliteRegexToLikeConverterTest {

    @Test
    public void convertsExactLiteralRegexToEquality() {
        SqliteRegexToLikeConverter.Conversion conversion =
                SqliteRegexToLikeConverter.convert("^abc$");

        Assertions.assertTrue(conversion.isEquality());
        Assertions.assertEquals("abc", conversion.sqlValue());
    }

    @Test
    public void convertsPrefixRegexToLike() {
        SqliteRegexToLikeConverter.Conversion conversion =
                SqliteRegexToLikeConverter.convert("^abc.*");

        Assertions.assertFalse(conversion.isEquality());
        Assertions.assertEquals("abc%", conversion.sqlValue());
    }

    @Test
    public void convertsContainsRegexToLike() {
        SqliteRegexToLikeConverter.Conversion conversion =
                SqliteRegexToLikeConverter.convert(".*abc.*");

        Assertions.assertFalse(conversion.isEquality());
        Assertions.assertEquals("%abc%", conversion.sqlValue());
    }

    @Test
    public void convertsSuffixRegexToLike() {
        SqliteRegexToLikeConverter.Conversion conversion =
                SqliteRegexToLikeConverter.convert(".*abc$");

        Assertions.assertFalse(conversion.isEquality());
        Assertions.assertEquals("%abc", conversion.sqlValue());
    }

    @Test
    public void convertsSingleCharacterWildcardRegexToLike() {
        SqliteRegexToLikeConverter.Conversion conversion =
                SqliteRegexToLikeConverter.convert("a.b");

        Assertions.assertFalse(conversion.isEquality());
        Assertions.assertEquals("a_b", conversion.sqlValue());
    }

    @Test
    public void convertsMultiCharacterWildcardRegexToLike() {
        SqliteRegexToLikeConverter.Conversion conversion =
                SqliteRegexToLikeConverter.convert("a.*b");

        Assertions.assertFalse(conversion.isEquality());
        Assertions.assertEquals("a%b", conversion.sqlValue());
    }

    @Test
    public void escapesLikeWildcardsInRegexLiterals() {
        SqliteRegexToLikeConverter.Conversion conversion =
                SqliteRegexToLikeConverter.convert("a.*%_");

        Assertions.assertFalse(conversion.isEquality());
        Assertions.assertEquals("a%\\%\\_", conversion.sqlValue());
    }

    @Test
    public void rejectsRegexCharacterClasses() {
        Assertions.assertThrows(
                SqliteRegexToLikeConverter.RegexToLikeConversionException.class,
                () -> SqliteRegexToLikeConverter.convert("Repository [Pp]roject"));
    }

    @Test
    public void rejectsRegexAlternation() {
        Assertions.assertThrows(
                SqliteRegexToLikeConverter.RegexToLikeConversionException.class,
                () -> SqliteRegexToLikeConverter.convert("cat|dog"));
    }

    @Test
    public void rejectsRegexGroups() {
        Assertions.assertThrows(
                SqliteRegexToLikeConverter.RegexToLikeConversionException.class,
                () -> SqliteRegexToLikeConverter.convert("(cat)"));
    }

    @Test
    public void rejectsRegexShorthandCharacterClasses() {
        Assertions.assertThrows(
                SqliteRegexToLikeConverter.RegexToLikeConversionException.class,
                () -> SqliteRegexToLikeConverter.convert("\\d+"));
    }

    @Test
    public void rejectsMalformedRegex() {
        Assertions.assertThrows(
                SqliteRegexToLikeConverter.RegexToLikeConversionException.class,
                () -> SqliteRegexToLikeConverter.convert("["));
    }
}
