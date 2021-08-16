package uk.co.compendiumdev.thingifier.core.domain.instances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

public class ThingInstanceIntegerFieldTest {

    @Test
    public void byDefaultAnIntegerFieldIsZero(){

        EntityDefinition enumFieldEntity = new EntityDefinition("thing", "things");
        enumFieldEntity.addFields(Field.is("integer", FieldType.INTEGER));

        // TODO: allow nullable optional integers
        EntityInstance instance = new EntityInstance(enumFieldEntity);
        Assertions.assertEquals("0", instance.getFieldValue("integer").asString());
    }


    @Test
    public void cannotSetAValueOutwithMaxMinValueForIntegerFields(){

        EntityDefinition stringFieldEntity = new EntityDefinition("Test Session", "Test Sessions");
        stringFieldEntity.addFields(Field.is("integer", FieldType.INTEGER).
                withMaximumValue(100).
                withMinimumValue(50)
        );

        EntityInstance instance = new EntityInstance(stringFieldEntity);

        Assertions.assertThrows(IllegalArgumentException.class, ()->{
                instance.setValue("integer", "101");
        });

        Assertions.assertThrows(IllegalArgumentException.class, ()->{
            instance.setValue("integer", "49");
        });
    }


}
