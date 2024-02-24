package uk.co.compendiumdev.thingifier.core.domain.instances.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.AutoIncrement;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.HashMap;
import java.util.Map;

public class AutoIncrementIdFieldTest {

    @Test
    public void byDefaultAnIDFieldIsOneWhenInstantiated(){

        EntityDefinition entity = new EntityDefinition("thing", "things");
        entity.addFields(Field.is("id", FieldType.AUTO_INCREMENT));

        Map<String, AutoIncrement> autos = new HashMap<>();
        autos.put("id", new AutoIncrement("id", entity.getField("id").getDefaultValue().asInteger()));

        EntityInstance instance = new EntityInstance(entity);
        instance.addAutoIncrementIdsToInstance(autos);

        Assertions.assertEquals("1", instance.getFieldValue("id").asString());
    }



    @Test
    public void idsShouldAutoIncrementWhenInstancesCreated(){

        EntityDefinition entity = new EntityDefinition("thing", "things");
        entity.addFields(Field.is("id", FieldType.AUTO_INCREMENT));

        Map<String, AutoIncrement> autos = new HashMap<>();
        autos.put("id", new AutoIncrement("id", 1));

        EntityInstance instance = new EntityInstance(entity);
        instance.addAutoIncrementIdsToInstance(autos);
        Assertions.assertEquals("1", instance.getFieldValue("id").asString());

        EntityInstance instance2 = new EntityInstance(entity);
        instance2.addAutoIncrementIdsToInstance(autos);
        Assertions.assertEquals("2", instance2.getFieldValue("id").asString());
    }

    @Test
    public void notAllowedToAmendIdOfInstance(){

        EntityDefinition entity = new EntityDefinition("thing", "things");
        entity.addFields(Field.is("id", FieldType.AUTO_INCREMENT));

        EntityInstance instance = new EntityInstance(entity);
        Map<String, AutoIncrement> autos = new HashMap<>();
        autos.put("id", new AutoIncrement("id", 1));

        instance.addAutoIncrementIdsToInstance(autos);
        Assertions.assertEquals("1", instance.getFieldValue("id").asString());
        Assertions.assertThrows(IllegalArgumentException.class, ()-> {
            instance.setValue("id", "2");
        });
    }

}
