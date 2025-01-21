package uk.co.compendiumdev.thingifier.core.domain.instances.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public class IntegerFieldTest {

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
                withMinMaxValues(50,100)
        );

        EntityInstance instance = new EntityInstance(stringFieldEntity);

        Assertions.assertThrows(IllegalArgumentException.class, ()->{
                instance.setValue("integer", "101");
        });

        Assertions.assertThrows(IllegalArgumentException.class, ()->{
            instance.setValue("integer", "49");
        });
    }

    @Test
    public void canSetIntegerField(){

        EntityDefinition stringFieldEntity = new EntityDefinition("Test Session", "Test Sessions");
        stringFieldEntity.addFields(Field.is("integer", FieldType.INTEGER).
                withMinMaxValues(50,100)
        );

        EntityInstance instance = new EntityInstance(stringFieldEntity);

        instance.setValue("integer", "75");

        Assertions.assertEquals(instance.getFieldValue("integer").asString(),"75");
    }


}
