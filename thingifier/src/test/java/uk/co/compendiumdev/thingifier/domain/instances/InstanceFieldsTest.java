package uk.co.compendiumdev.thingifier.domain.instances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.domain.definitions.DefinedFields;
import uk.co.compendiumdev.thingifier.domain.definitions.field.definition.Field;

public class InstanceFieldsTest {

    @Test
    public void canSetAndGetValuesForAField(){

        DefinedFields fieldsDefn = new DefinedFields();
        fieldsDefn.addField(Field.is("Ref"));

        InstanceFields instance = new InstanceFields(fieldsDefn);

        instance.setValue("Ref", "Reference");

        Assertions.assertNotNull(instance.getAssignedValue("Ref"), "fields did not contain 'ref'");

        Assertions.assertEquals("Reference", instance.getFieldValue("Ref").asString());
    }
}
