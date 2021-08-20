package uk.co.compendiumdev.thingifier.core.query;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SortingViaQueryFiltersTest {

    // todo: lower level testing at the EntityInstanceListSorter level

    @Test
    public void canSortViaAQuery(){

        EntityRelModel aThingifier = new EntityRelModel();
        aThingifier.createEntityDefinition("thing", "things")
                .addField(Field.is("truefalse", FieldType.BOOLEAN));

        EntityInstanceCollection thing = aThingifier.getInstanceCollectionForEntityNamed("thing");

        final EntityInstance trueThing = thing.createManagedInstance();
        trueThing.setValue("truefalse", "true");

        final EntityInstance falseThing = thing.createManagedInstance();
        falseThing.setValue("truefalse", "false");

        Map<String, String> params = new HashMap<>();
        params.put("sortBy", "+truefalse");

        SimpleQuery ascSortedResults = new SimpleQuery(aThingifier, "things").
                                        performQuery(params);

        Assertions.assertTrue(ascSortedResults.isResultACollection(), "result should be a collection");
        final List<EntityInstance> instances = ascSortedResults.getListEntityInstances();
        Assertions.assertEquals(2, instances.size(), "expected 2 values");
        Assertions.assertEquals(trueThing,instances.get(0));
        Assertions.assertEquals(falseThing,instances.get(1));

        // then repeat sort and get different results

        params = new HashMap<>();
        params.put("sortBy", "-truefalse");

        SimpleQuery descSortedResults = new SimpleQuery(aThingifier, "things").
                performQuery(params);

        final List<EntityInstance> descInstances = descSortedResults.getListEntityInstances();
        Assertions.assertEquals(2, descInstances.size(), "expected 2 values");
        Assertions.assertEquals(falseThing,descInstances.get(0));
        Assertions.assertEquals(trueThing,descInstances.get(1));

        // check that default sort is descending
        params = new HashMap<>();
        params.put("sortBy", "-truefalse");

        SimpleQuery defaultSortedResults = new SimpleQuery(aThingifier, "things").
                performQuery(params);

        final List<EntityInstance> defaultSortedInstances = defaultSortedResults.getListEntityInstances();
        Assertions.assertEquals(2, defaultSortedInstances.size(), "expected 2 values");
        Assertions.assertEquals(falseThing,defaultSortedInstances.get(0));
        Assertions.assertEquals(trueThing,defaultSortedInstances.get(1));
        Assertions.assertEquals(falseThing,defaultSortedInstances.get(0));
        Assertions.assertEquals(trueThing,defaultSortedInstances.get(1));
    }
}
