package uk.co.compendiumdev.thingifier.thingdefinition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.domain.definitions.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;

public class ThingDefinitionTest {

    @Test
    public void entityDefinitionCreation(){
        ThingDefinition eDefn;
        eDefn = ThingDefinition.create("Requirement", "Requirements");

        Assertions.assertEquals("Requirement", eDefn.getName());
        Assertions.assertEquals("Requirements", eDefn.getPlural());


        Assertions.assertEquals(1, eDefn.getFieldNames().size()); // guid exists
        Assertions.assertTrue(eDefn.getFieldNames().contains("guid"));

        eDefn.addField(Field.is("Title"));

        Assertions.assertEquals(2, eDefn.getFieldNames().size());

        Assertions.assertTrue(eDefn.getFieldNames().contains("Title"));

        Assertions.assertTrue(eDefn.hasFieldNameDefined("Title"));
        Assertions.assertFalse(eDefn.hasFieldNameDefined("Description"));
    }



}
