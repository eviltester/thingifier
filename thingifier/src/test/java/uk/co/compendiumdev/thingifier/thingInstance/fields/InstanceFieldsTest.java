package uk.co.compendiumdev.thingifier.thingInstance.fields;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.generic.instances.InstanceFields;

import java.util.List;

public class InstanceFieldsTest {

    @Test
    public void canCreateAGenericInstance(){
        InstanceFields instance = new InstanceFields();

        instance.addValue("Title", "This Item title");
        instance.addValue("Ref", "Reference");

        List<String> fields = instance.getFields();

        Assertions.assertEquals(2, fields.size());
        Assertions.assertTrue(fields.contains("title"), "fields did not contain 'title'");
        Assertions.assertTrue(fields.contains("ref"), "fields did not contain 'ref'");

        Assertions.assertEquals("Reference", instance.getValue("Ref"));
    }
}
