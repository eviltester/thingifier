package uk.co.compendiumdev.thingifier.thingdefinition.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.domain.FieldType;
import uk.co.compendiumdev.thingifier.domain.definitions.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;

public class IntegerFieldDefinitionTest {

    @Test
    public void byDefaultAnIntegerFieldIsZero(){

        ThingDefinition enumFieldEntity = ThingDefinition.create("thing", "things");
        enumFieldEntity.addFields(Field.is("integer", FieldType.INTEGER));

        Assertions.assertEquals("0", enumFieldEntity.
                                            getField("integer").
                                                getDefaultValue().asString());
    }

    @Test
    public void examplesForIntegerFieldsBasedOnMinAndMax() {

        ThingDefinition stringFieldEntity = ThingDefinition.create("Test Session", "Test Sessions");
        stringFieldEntity.addFields(Field.is("integer", FieldType.INTEGER).
                withMaximumValue(10).
                withMinimumValue(5)
        );

        final Field field = stringFieldEntity.getField("integer");

        for(int x=0;x<100;x++){
            final int fieldValue = Integer.parseInt(field.getRandomExampleValue());
            Assertions.assertTrue(field.withinAllowedIntegerRange(fieldValue), "not in range " + fieldValue);
        }
    }
}
