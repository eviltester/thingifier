package uk.co.compendiumdev.thingifier.core.domain.instances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

public class ThingInstanceIdFieldTest {

    @Test
    public void byDefaultAnIDFieldIsOneWhenInstantiated(){

        EntityDefinition entity = new EntityDefinition("thing", "things");
        entity.addFields(Field.is("id", FieldType.ID));

        EntityInstance instance = new EntityInstance(entity);
        instance.addIdsToInstance();
        instance.addGUIDtoInstance();

        Assertions.assertEquals("1", instance.getFieldValue("id").asString());
    }



    @Test
    public void idsShouldAutoIncrementWhenInstancesCreated(){

        EntityDefinition entity = new EntityDefinition("thing", "things");
        entity.addFields(Field.is("id", FieldType.ID));

        EntityInstance instance = new EntityInstance(entity);
        instance.addIdsToInstance();
        Assertions.assertEquals("1", instance.getFieldValue("id").asString());

        EntityInstance instance2 = new EntityInstance(entity);
        instance2.addIdsToInstance();
        Assertions.assertEquals("2", instance2.getFieldValue("id").asString());
    }

    @Test
    public void notAllowedToAmendIdOfInstance(){

        EntityDefinition entity = new EntityDefinition("thing", "things");
        entity.addFields(Field.is("id", FieldType.ID));

        EntityInstance instance = new EntityInstance(entity);
        instance.addIdsToInstance();
        Assertions.assertEquals("1", instance.getFieldValue("id").asString());
        Assertions.assertThrows(IllegalArgumentException.class, ()-> {
            instance.setValue("id", "2");
        });
    }

}
