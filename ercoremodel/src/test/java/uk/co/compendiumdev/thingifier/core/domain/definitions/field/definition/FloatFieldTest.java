package uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

class FloatFieldTest {

    @Test
    void byDefaultAFloatFieldIsZero(){

        final Field field = Field.is("float", FieldType.FLOAT);

        Assertions.assertEquals("0.0", field.
                                                getDefaultValue().asString());
    }


    @Test
    void examplesForFloatFieldsBasedOnMinAndMax() {

        final Field field = Field.is("float", FieldType.FLOAT).
                withMinMaxValues(5.5F,10.0F);

        for(int x=0;x<100;x++){
            final float fieldValue = Float.parseFloat(field.getRandomExampleValue());
            Assertions.assertTrue(
                    field.validate(
                                    FieldValue.is(field, String.format("%f",fieldValue))).
                            isValid());
        }
    }


    @Test
    void validationReportsAsFailsForNonFloat() {

        final Field field = Field.is("float", FieldType.FLOAT);

        Assertions.assertFalse(
                field.validate(
                    FieldValue.is(field, "bob")).
                        isValid());
    }

    @Test
    void checkFloatMinAndMax() {

        final Field field = Field.is("float", FieldType.FLOAT).
                withMinMaxValues(5.0F,10.0F);

        Assertions.assertFalse(
                field.validate(
                                FieldValue.is(field, "4.9")).
                        isValid());

        Assertions.assertFalse(
                field.validate(
                                FieldValue.is(field, "4.99")).
                        isValid());

        Assertions.assertTrue(
                field.validate(
                                FieldValue.is(field, "5.0")).
                        isValid());

        Assertions.assertTrue(
                field.validate(
                                FieldValue.is(field, "5.1")).
                        isValid());

        Assertions.assertTrue(
                field.validate(
                                FieldValue.is(field, "9.99")).
                        isValid());

        Assertions.assertTrue(
                field.validate(
                                FieldValue.is(field, "10.00")).
                        isValid());

        Assertions.assertTrue(
                field.validate(
                                FieldValue.is(field, "10")).
                        isValid());


        Assertions.assertFalse(
                field.validate(
                                FieldValue.is(field, "10.01")).
                        isValid());

        Assertions.assertFalse(
                field.validate(
                                FieldValue.is(field, "11.00")).
                        isValid());
    }

    @Test
    void noDefaultFloatMinAndMax() {

        final Field field = Field.is("float", FieldType.FLOAT);

        Assertions.assertTrue(
                field.validate(
                                FieldValue.is(field, "-99999.1")).
                        isValid());

        Assertions.assertTrue(
                field.validate(
                                FieldValue.is(field, "0.0")).
                        isValid());

        Assertions.assertTrue(
                field.validate(
                                FieldValue.is(field, "10000.00")).
                        isValid());

        Assertions.assertTrue(
                field.validate(
                                FieldValue.is(field, "10000.01")).
                        isValid());
    }

    @Test
    void canConvertFromString(){
        final Field field = Field.is("float", FieldType.FLOAT);

        Assertions.assertEquals("1.0",
                field.getActualValueToAdd(
                        FieldValue.is(field, "1.0")));

        Assertions.assertEquals("1.0",
                field.getActualValueToAdd(
                        FieldValue.is(field, "1")));

    }

}
