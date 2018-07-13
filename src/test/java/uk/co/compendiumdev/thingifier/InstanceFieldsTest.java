package uk.co.compendiumdev.thingifier;


import uk.co.compendiumdev.thingifier.generic.instances.InstanceFields;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

public class InstanceFieldsTest {

    @Test
    public void canCreateAGenericInstance(){
        InstanceFields instance = new InstanceFields();

        instance.addValue("Title", "This Item title");
        instance.addValue("Ref", "Reference");

        List<String> fields = instance.getFields();

        assertThat(fields.size(), is(2));

        assertThat(fields, containsInAnyOrder("title", "ref"));
        assertThat(instance.getValue("Ref"), is("Reference"));
    }
}
