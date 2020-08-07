package uk.co.compendiumdev.thingifier.domain.instances.fieldinstantiation;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.domain.FieldType;
import uk.co.compendiumdev.thingifier.domain.definitions.DefinedFields;
import uk.co.compendiumdev.thingifier.domain.definitions.Field;
import uk.co.compendiumdev.thingifier.domain.instances.InstanceFields;

public class DefaultInstanceFieldsTest {

    @Test
    public void byDefaultFieldIsAString(){

        DefinedFields fieldsDefn = new DefinedFields();
        fieldsDefn.addField(Field.is("Title"));
        fieldsDefn.addField(Field.is("Ref"));

        Assertions.assertEquals(FieldType.STRING,
                fieldsDefn.getField("Title").getType());

        InstanceFields instance = new InstanceFields(fieldsDefn);

        instance.setValue("Title", "This Item title");
        instance.setValue("Ref", "Reference");

        Assertions.assertNotNull(instance.getAssignedValue("Title"), "fields did not contain 'title'");
        Assertions.assertNotNull(instance.getAssignedValue("Ref"), "fields did not contain 'ref'");

        Assertions.assertEquals("Reference", instance.getFieldValue("Ref").asString());
    }
}
