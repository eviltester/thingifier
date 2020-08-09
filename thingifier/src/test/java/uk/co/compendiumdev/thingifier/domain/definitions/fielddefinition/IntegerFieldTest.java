package uk.co.compendiumdev.thingifier.domain.definitions.fielddefinition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;

import java.util.HashSet;
import java.util.Set;

public class IntegerFieldTest {

    @Test
    public void byDefaultAnIntegerFieldIsZero(){

        final Field field = Field.is("integer", FieldType.INTEGER);

        Assertions.assertEquals("0", field.
                                                getDefaultValue().asString());
    }

    @Test
    public void examplesForIntegerFieldsBasedOnMinAndMax() {

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
}
