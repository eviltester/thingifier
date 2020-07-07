package uk.co.compendiumdev.thingifier.thingInstance.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

public class IntegerFieldInstanceTest {

    @Test
    public void byDefaultAnIntegerFieldIsZero(){

        ThingDefinition enumFieldEntity = ThingDefinition.create("thing", "things");
        enumFieldEntity.addFields(Field.is("integer", FieldType.INTEGER));

        // TODO: allow nullable optional integers
        ThingInstance instance = new ThingInstance(enumFieldEntity);
        Assertions.assertEquals("0", instance.getValue("integer"));
    }

    @Test
    public void canSetAMaxMinValueForIntegerFields(){

        ThingDefinition stringFieldEntity = ThingDefinition.create("Test Session", "Test Sessions");
        stringFieldEntity.addFields(Field.is("integer", FieldType.INTEGER).
                                        withMaximumValue(100).
                                        withMinimumValue(50)
        );

        ThingInstance instance = new ThingInstance(stringFieldEntity);

        instance.setValue("integer", "100");
        Assertions.assertEquals("100", instance.getValue("integer"));

        instance.setValue("integer", "99");
        Assertions.assertEquals("99", instance.getValue("integer"));

        instance.setValue("integer", "50");
        Assertions.assertEquals("50", instance.getValue("integer"));
    }

    @Test
    public void cannotSetAValueOutwithMaxMinValueForIntegerFields(){

        ThingDefinition stringFieldEntity = ThingDefinition.create("Test Session", "Test Sessions");
        stringFieldEntity.addFields(Field.is("integer", FieldType.INTEGER).
                withMaximumValue(100).
                withMinimumValue(50)
        );

        ThingInstance instance = new ThingInstance(stringFieldEntity);

        Assertions.assertThrows(IllegalArgumentException.class, ()->{
                instance.setValue("integer", "101");
        });

        Assertions.assertThrows(IllegalArgumentException.class, ()->{
            instance.setValue("integer", "49");
        });
    }


}
