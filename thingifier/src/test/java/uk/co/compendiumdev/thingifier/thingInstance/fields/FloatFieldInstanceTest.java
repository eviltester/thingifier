package uk.co.compendiumdev.thingifier.thingInstance.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.domain.FieldType;
import uk.co.compendiumdev.thingifier.domain.definitions.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

public class FloatFieldInstanceTest {

    @Test
    public void byDefaultAFloatFieldIsZero(){

        ThingDefinition enumFieldEntity = ThingDefinition.create("thing", "things");
        enumFieldEntity.addFields(Field.is("float", FieldType.FLOAT));

        // TODO: allow nullable optional floats
        ThingInstance instance = new ThingInstance(enumFieldEntity);
        Assertions.assertEquals("0.0", instance.getValue("float"));
    }
}
