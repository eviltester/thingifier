package uk.co.compendiumdev.thingifier.domain.instances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.domain.definitions.fielddefinition.FieldType;
import uk.co.compendiumdev.thingifier.domain.definitions.fielddefinition.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

public class IntegerFieldInstanceTest {

    @Test
    public void byDefaultAnIntegerFieldIsZero(){

        ThingDefinition enumFieldEntity = ThingDefinition.create("thing", "things");
        enumFieldEntity.addFields(Field.is("integer", FieldType.INTEGER));

        // TODO: allow nullable optional integers
        ThingInstance instance = new ThingInstance(enumFieldEntity);
        Assertions.assertEquals("0", instance.getFieldValue("integer").asString());
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
