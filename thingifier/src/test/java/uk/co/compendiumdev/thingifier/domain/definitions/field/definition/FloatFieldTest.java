package uk.co.compendiumdev.thingifier.domain.definitions.field.definition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.field.definition.FieldType;

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



}
