package uk.co.compendiumdev.thingifier.domain.instances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.domain.definitions.fielddefinition.FieldType;
import uk.co.compendiumdev.thingifier.domain.definitions.fielddefinition.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

public class FloatFieldInstanceTest {

    @Test
    public void byDefaultAFloatFieldIsZero(){

        ThingDefinition enumFieldEntity = ThingDefinition.create("thing", "things");
        enumFieldEntity.addFields(Field.is("float", FieldType.FLOAT));

        // TODO: allow nullable optional floats
        ThingInstance instance = new ThingInstance(enumFieldEntity);
        Assertions.assertEquals("0.0", instance.getFieldValue("float").asString());
    }

    @Test
    public void floatShouldValidate(){

        ThingDefinition enumFieldEntity = ThingDefinition.create("thing", "things");
        enumFieldEntity.addFields(Field.is("float", FieldType.FLOAT));

        ThingInstance instance = new ThingInstance(enumFieldEntity);

        final IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class,
                () -> {
                    instance.setValue("float", "bob");
                });

        Assertions.assertTrue(e.getMessage().contains("float : bob does not match type FLOAT"),
                e.getMessage());
    }

    @Test
    public void canSetGetFloat(){

        ThingDefinition enumFieldEntity = ThingDefinition.create("thing", "things");
        enumFieldEntity.addFields(Field.is("float", FieldType.FLOAT));

        ThingInstance instance = new ThingInstance(enumFieldEntity);

        instance.setValue("float", "4.3");

        Assertions.assertEquals("4.3", instance.getFieldValue("float").asString());
    }

}
