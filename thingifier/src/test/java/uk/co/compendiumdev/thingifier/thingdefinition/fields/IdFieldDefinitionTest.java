package uk.co.compendiumdev.thingifier.thingdefinition.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;

public class IdFieldDefinitionTest {

    @Test
    public void byDefaultAnIdIsNull(){

        ThingDefinition entity = ThingDefinition.create("thing", "things");
        entity.addFields(Field.is("id", FieldType.ID));

        // todo: add some validation so that an entity only has one ID type

        // ids are managed at the entity level, not the field level
        Assertions.assertEquals(null,
                entity.getField("id").getDefaultValue());
    }
}
