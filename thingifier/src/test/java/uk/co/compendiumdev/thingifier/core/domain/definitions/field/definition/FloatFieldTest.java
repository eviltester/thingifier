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
                withMaximumValue(10.0F).
                withMinimumValue(5.5F);

        for(int x=0;x<100;x++){
            final float fieldValue = Float.parseFloat(field.getRandomExampleValue());
            Assertions.assertTrue(
                    field.withinAllowedFloatRange(fieldValue),
                    "not in range " + fieldValue);
            Assertions.assertTrue(
                    fieldValue>=5.5F,
                    "example too low " + fieldValue);
            Assertions.assertTrue(
                    fieldValue<=10.0F,
                    "example too high " + fieldValue);
        }
    }

    @Test
    void validateFloatOutwithMaxAndMin() {

        final Field field = Field.is("float", FieldType.FLOAT).
                withMaximumValue(10.0F).
                withMinimumValue(5.5F);

        Assertions.assertFalse(field.withinAllowedFloatRange(
                FieldValue.is("float", "11.0")
                        .asFloat()));

        Assertions.assertFalse(field.withinAllowedFloatRange(
                FieldValue.is("float", "5.4").
                        asFloat()));

        Assertions.assertFalse(field.validate(
                FieldValue.is("float", "5.4")).isValid());
    }

    @Test
    void validationReportsAsFailsForNonFloat() {

        final Field field = Field.is("float", FieldType.FLOAT);

        Assertions.assertFalse(
                field.validate(
                    FieldValue.is("float", "bob")).
                        isValid());
    }

    @Test
    void getFloatMinAndMax() {

        final Field field = Field.is("float", FieldType.FLOAT).
                withMaximumValue(10.0F).
                withMinimumValue(5.0F);

        Assertions.assertEquals(10.0F, field.getMaximumFloatValue());
        Assertions.assertEquals(5.0F, field.getMinimumFloatValue());
    }

    @Test
    void getDefaultIntegerMinAndMax() {

        final Field field = Field.is("float", FieldType.FLOAT);

        Assertions.assertEquals(Float.MAX_VALUE, field.getMaximumFloatValue());
        Assertions.assertEquals(Float.MIN_VALUE, field.getMinimumFloatValue());
    }

    @Test
    void canConvertFromString(){
        final Field field = Field.is("float", FieldType.FLOAT);

        Assertions.assertEquals("1.0",
                field.getActualValueToAdd(
                        FieldValue.is("integer", "1.0")));

        Assertions.assertEquals("1.0",
                field.getActualValueToAdd(
                        FieldValue.is("integer", "1")));

    }

}
