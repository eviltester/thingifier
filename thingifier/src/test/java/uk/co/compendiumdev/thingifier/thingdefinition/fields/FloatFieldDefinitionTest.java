package uk.co.compendiumdev.thingifier.thingdefinition.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.domain.FieldType;
import uk.co.compendiumdev.thingifier.domain.definitions.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;

public class FloatFieldDefinitionTest {

    @Test
    public void byDefaultAFloatFieldIsZero(){

        ThingDefinition enumFieldEntity = ThingDefinition.create("thing", "things");
        enumFieldEntity.addFields(Field.is("float", FieldType.FLOAT));

        Assertions.assertEquals("0.0",  enumFieldEntity.getField("float").getDefaultValue());
    }


    @Test
    public void examplesForFloatFieldsBasedOnMinAndMax() {

        ThingDefinition stringFieldEntity = ThingDefinition.create("Test Session", "Test Sessions");
        stringFieldEntity.addFields(Field.is("float", FieldType.FLOAT).
                withMaximumValue(10.0F).
                withMinimumValue(5.5F)
        );

        final Field field = stringFieldEntity.getField("float");

        for(int x=0;x<100;x++){
            final float fieldValue = Float.parseFloat(field.getRandomExampleValue());
            Assertions.assertTrue(field.withinAllowedFloatRange(fieldValue), "not in range " + fieldValue);
        }
    }



}
