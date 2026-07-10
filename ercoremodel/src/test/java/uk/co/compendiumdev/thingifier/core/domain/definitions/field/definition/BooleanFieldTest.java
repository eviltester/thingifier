package uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

class BooleanFieldTest {

    @Test
    void byDefaultABooleanFieldIsFalse() {

        final Field field = Field.is("false", FieldType.BOOLEAN);

        Assertions.assertEquals("false", field.getDefaultValue().asString());
    }

    @Test
    void canConfigureBooleanDefaultAsTrue() {

        final Field field = Field.is("false", FieldType.BOOLEAN).withDefaultValue("true");

        Assertions.assertEquals("true", field.getDefaultValue().asString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "true", "TRUE", "faLSE"})
    void booleanAcceptsTrueOrFalseValues(final String value) {

        final Field field = Field.is("boolean", FieldType.BOOLEAN);

        Assertions.assertTrue(field.validate(FieldValue.is(field, value)).isValid());
    }

    @ParameterizedTest
    @ValueSource(strings = {"bob", "", "yes", "1"})
    void booleanRejectsNonBooleanValues(final String value) {

        final Field field = Field.is("boolean", FieldType.BOOLEAN);

        Assertions.assertFalse(field.validate(FieldValue.is(field, value)).isValid());
    }

    @Test
    void booleanExamplesAreTrueOrFalse() {

        final Field field = Field.is("boolean", FieldType.BOOLEAN);

        Set<String> bools = new HashSet<>();

        for (int x = 0; x < 100; x++) {
            bools.add(field.getRandomExampleValue());
        }

        Assertions.assertEquals(Set.of("true", "false"), bools);
    }

    @ParameterizedTest
    @CsvSource({"true,true", "True,true", "FALSE,false", "faLSE,false"})
    void canConvertFromString(final String value, final String expected) {
        final Field field = Field.is("boolean", FieldType.BOOLEAN);

        Assertions.assertEquals(expected, field.getActualValueToAdd(FieldValue.is(field, value)));
    }
}
