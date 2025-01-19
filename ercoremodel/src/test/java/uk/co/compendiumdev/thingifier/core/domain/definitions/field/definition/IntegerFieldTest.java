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
                withMinMaxValues(5, 10);

        Set<Integer> setValues = new HashSet<>();

        for(int x=0;x<100;x++){
            final int fieldValue = Integer.parseInt(field.getRandomExampleValue());

            setValues.add(fieldValue);
            Assertions.assertTrue(
                    field.validate(
                                    FieldValue.is(field, String.format("%d",fieldValue))).
                            isValid());
        }

        // check we generated all 6 - 5, 6, 7, 8, 9, 10
        Assertions.assertEquals(6, setValues.size());
    }

    @Test
    void setIntegerMinAndMax() {

        final Field field = Field.is("integer", FieldType.INTEGER).
                withMinMaxValues(5, 10);

        Assertions.assertFalse(
                field.validate(
                                FieldValue.is(field, "4")).
                        isValid());

        Assertions.assertTrue(
                field.validate(
                                FieldValue.is(field, "5")).
                        isValid());


        Assertions.assertTrue(
                field.validate(
                                FieldValue.is(field, "7")).
                        isValid());

        Assertions.assertTrue(
                field.validate(
                                FieldValue.is(field, "10")).
                        isValid());


        Assertions.assertFalse(
                field.validate(
                                FieldValue.is(field, "11")).
                        isValid());

    }

    @Test
    void getDefaultIntegerMinAndMax() {

        final Field field = Field.is("integer", FieldType.INTEGER);

        Assertions.assertTrue(
                field.validate(
                                FieldValue.is(field, String.format("%d",Integer.MIN_VALUE))
                        ).
                        isValid());

        Assertions.assertTrue(
                field.validate(
                                FieldValue.is(field, String.format("%d",Integer.MAX_VALUE))
                        ).
                        isValid());
    }

    @Test
    void validateInteger(){

        final Field field = Field.is("integer", FieldType.INTEGER);

        Assertions.assertTrue(
                field.validate(
                        FieldValue.is(field, "1")).
                        isValid());

        Assertions.assertTrue(
                field.validate(
                        FieldValue.is(field, "01")).
                        isValid());

        Assertions.assertTrue(
                field.validate(
                        FieldValue.is(field, "-1")).
                        isValid());

        Assertions.assertFalse(
                field.validate(
                        FieldValue.is(field, "bob")).
                        isValid());

        Assertions.assertFalse(
                field.validate(
                        FieldValue.is(field, "")).
                        isValid());
    }

    @Test
    void canConvertFromString(){
        final Field field = Field.is("integer", FieldType.INTEGER);

        Assertions.assertEquals("1",
                field.getActualValueToAdd(
                FieldValue.is(field, "1.0")));

        Assertions.assertEquals("1",
                field.getActualValueToAdd(
                        FieldValue.is(field, "1")));

    }
}
