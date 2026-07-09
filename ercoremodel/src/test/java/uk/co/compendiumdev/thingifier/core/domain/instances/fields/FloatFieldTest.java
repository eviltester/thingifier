package uk.co.compendiumdev.thingifier.core.domain.instances.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

public class FloatFieldTest {

    @Test
    public void byDefaultAFloatFieldIsZero(){

        EntityDefinition enumFieldEntity = new EntityDefinition("thing", "things");
        enumFieldEntity.addFields(Field.is("float", FieldType.FLOAT));

        // TODO: allow nullable optional floats
        EntityInstance instance = EntityInstance.fromDraft(EntityInstanceDraft.forEntity(enumFieldEntity));
        Assertions.assertEquals("0.0", instance.getFieldValue("float").asString());
    }

    @Test
    public void floatShouldValidate(){

        EntityDefinition enumFieldEntity = new EntityDefinition("thing", "things");
        enumFieldEntity.addFields(Field.is("float", FieldType.FLOAT));

        final IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class,
                () -> {
                    EntityInstanceDraft.forEntity(enumFieldEntity).withField("float", "bob");
                });

        Assertions.assertTrue(e.getMessage().contains("float : bob does not match type FLOAT"),
                e.getMessage());
    }

    @Test
    public void canSetGetFloat(){

        EntityDefinition enumFieldEntity = new EntityDefinition("thing", "things");
        enumFieldEntity.addFields(Field.is("float", FieldType.FLOAT));

        EntityInstance instance = EntityInstance.fromDraft(EntityInstanceDraft.forEntity(enumFieldEntity).
                withField("float", "4.3"));

        Assertions.assertEquals("4.3", instance.getFieldValue("float").asString());
    }

}
