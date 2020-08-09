package uk.co.compendiumdev.thingifier.domain.instances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;

public class IdFieldInstanceTest {

    @Test
    public void byDefaultAnIDFieldIsOneWhenInstantiated(){

        ThingDefinition entity = ThingDefinition.create("thing", "things");
        entity.addFields(Field.is("id", FieldType.ID));

        ThingInstance instance = new ThingInstance(entity);
        Assertions.assertEquals("1", instance.getFieldValue("id").asString());
    }



    @Test
    public void idsShouldAutoIncrementWhenInstancesCreated(){

        ThingDefinition entity = ThingDefinition.create("thing", "things");
        entity.addFields(Field.is("id", FieldType.ID));

        ThingInstance instance = new ThingInstance(entity);
        Assertions.assertEquals("1", instance.getFieldValue("id").asString());

        ThingInstance instance2 = new ThingInstance(entity);
        Assertions.assertEquals("2", instance2.getFieldValue("id").asString());
    }

    @Test
    public void notAllowedToAmendIdOfInstance(){

        ThingDefinition entity = ThingDefinition.create("thing", "things");
        entity.addFields(Field.is("id", FieldType.ID));

        ThingInstance instance = new ThingInstance(entity);
        Assertions.assertEquals("1", instance.getFieldValue("id").asString());
        Assertions.assertThrows(IllegalArgumentException.class, ()-> {
            instance.setValue("id", "2");
        });
    }

}
