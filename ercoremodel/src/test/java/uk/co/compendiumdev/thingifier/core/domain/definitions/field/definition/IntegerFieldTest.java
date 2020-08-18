package uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

import java.util.HashSet;
import java.util.Set;

class IntegerFieldTest {

    @Test
    void byDefaultAnIntegerFieldIsZero(){

        final Field field = Field.is("integer", FieldType.INTEGER);

        Assertions.assertEquals("0", field.
                                                getDefaultValue().asString());
    }

    @Test
    void examplesForIntegerFieldsBasedOnMinAndMax() {

        final Field field = Field.is("integer", FieldType.INTEGER).
                withMaximumValue(10).
                withMinimumValue(5);

        Set<Integer> setValues = new HashSet<>();

        for(int x=0;x<100;x++){
            final int fieldValue = Integer.parseInt(field.getRandomExampleValue());

            setValues.add(fieldValue);
            Assertions.assertTrue(
                    field.withinAllowedIntegerRange(fieldValue),
                    "not in range " + fieldValue);

            Assertions.assertTrue(
                    fieldValue >= 5,
                    "too low range " + fieldValue);

            Assertions.assertTrue(
                    fieldValue <= 10,
                    "to high for range " + fieldValue);
        }

        // check we generated all 6 - 5, 6, 7, 8, 9, 10
        Assertions.assertEquals(6, setValues.size());
    }

    @Test
    void examplesForIntegerOutsideMinAndMax() {

        final Field field = Field.is("integer", FieldType.INTEGER).
                withMaximumValue(10).
                withMinimumValue(5);

        Set<Integer> setValues = new HashSet<>();

        Assertions.assertFalse(
                    field.withinAllowedIntegerRange(
                            FieldValue.is("integer", "11").
                                    asInteger()));

        Assertions.assertFalse(
                field.withinAllowedIntegerRange(
                        FieldValue.is("integer", "4").
                                asInteger()));

        Assertions.assertFalse(
                field.validate(
                        FieldValue.is("integer", "4")).
                        isValid());
    }

    @Test
    void getIntegerMinAndMax() {

        final Field field = Field.is("integer", FieldType.INTEGER).
                withMaximumValue(10).
                withMinimumValue(5);

        Assertions.assertEquals(10, field.getMaximumIntegerValue());
        Assertions.assertEquals(5, field.getMinimumIntegerValue());
    }

    @Test
    void getDefaultIntegerMinAndMax() {

        final Field field = Field.is("integer", FieldType.INTEGER);

        Assertions.assertEquals(Integer.MAX_VALUE, field.getMaximumIntegerValue());
        Assertions.assertEquals(Integer.MIN_VALUE, field.getMinimumIntegerValue());
    }

    @Test
    void validateInteger(){

        final Field field = Field.is("integer", FieldType.INTEGER);

        Assertions.assertTrue(
                field.validate(
                        FieldValue.is("integer", "1")).
                        isValid());

        Assertions.assertTrue(
                field.validate(
                        FieldValue.is("integer", "01")).
                        isValid());

        Assertions.assertTrue(
                field.validate(
                        FieldValue.is("integer", "-1")).
                        isValid());

        Assertions.assertFalse(
                field.validate(
                        FieldValue.is("integer", "bob")).
                        isValid());

        Assertions.assertFalse(
                field.validate(
                        FieldValue.is("integer", "")).
                        isValid());
    }

    @Test
    void canConvertFromString(){
        final Field field = Field.is("integer", FieldType.INTEGER);

        Assertions.assertEquals("1",
                field.getActualValueToAdd(
                FieldValue.is("integer", "1.0")));

        Assertions.assertEquals("1",
                field.getActualValueToAdd(
                        FieldValue.is("integer", "1")));

    }
}
