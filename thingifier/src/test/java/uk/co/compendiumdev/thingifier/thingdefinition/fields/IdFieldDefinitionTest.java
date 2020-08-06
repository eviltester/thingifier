package uk.co.compendiumdev.thingifier.thingdefinition.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.domain.FieldType;
import uk.co.compendiumdev.thingifier.domain.definitions.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;

public class IdFieldDefinitionTest {

    @Test
    public void byDefaultAnIdIsNull(){

        ThingDefinition entity = ThingDefinition.create("thing", "things");
        entity.addFields(Field.is("id", FieldType.ID));

        Assertions.assertEquals(null,
                entity.getField("id").
                        getDefaultValue().
                        getValueAsString());
    }
}
