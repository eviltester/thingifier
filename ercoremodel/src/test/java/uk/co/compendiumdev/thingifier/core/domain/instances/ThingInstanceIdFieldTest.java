package uk.co.compendiumdev.thingifier.core.domain.instances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;

public class ThingInstanceIdFieldTest {

    @Test
    public void byDefaultAnIDFieldIsOneWhenInstantiated(){

        ThingDefinition entity = new ThingDefinition("thing", "things");
        entity.addFields(Field.is("id", FieldType.ID));

        ThingInstance instance = new ThingInstance(entity);
        instance.addIdsToInstance();
        instance.addGUIDtoInstance();

        Assertions.assertEquals("1", instance.getFieldValue("id").asString());
    }



    @Test
    public void idsShouldAutoIncrementWhenInstancesCreated(){

        ThingDefinition entity = new ThingDefinition("thing", "things");
        entity.addFields(Field.is("id", FieldType.ID));

        ThingInstance instance = new ThingInstance(entity);
        instance.addIdsToInstance();
        Assertions.assertEquals("1", instance.getFieldValue("id").asString());

        ThingInstance instance2 = new ThingInstance(entity);
        instance2.addIdsToInstance();
        Assertions.assertEquals("2", instance2.getFieldValue("id").asString());
    }

    @Test
    public void notAllowedToAmendIdOfInstance(){

        ThingDefinition entity = new ThingDefinition("thing", "things");
        entity.addFields(Field.is("id", FieldType.ID));

        ThingInstance instance = new ThingInstance(entity);
        instance.addIdsToInstance();
        Assertions.assertEquals("1", instance.getFieldValue("id").asString());
        Assertions.assertThrows(IllegalArgumentException.class, ()-> {
            instance.setValue("id", "2");
        });
    }

}
