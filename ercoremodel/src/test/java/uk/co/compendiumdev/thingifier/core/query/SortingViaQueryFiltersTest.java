package uk.co.compendiumdev.thingifier.core.query;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;

import java.util.List;

public class SortingViaQueryFiltersTest {

    // todo: lower level testing at the EntityInstanceListSorter level

    EntityInstanceCollection thing;
    EntityRelModel erModel;

    @BeforeEach
    public void setupThingifier(){

        erModel = new EntityRelModel();
        erModel.createEntityDefinition("thing", "things")
                .addFields(Field.is("truefalse", FieldType.BOOLEAN),
                        Field.is("int", FieldType.INTEGER));

        thing = erModel.getInstanceData().getInstanceCollectionForEntityNamed("thing");

    }

    @Test
    public void canSortIntViaAQuery(){

        final EntityInstance thing1 = thing.addInstance(new EntityInstance(thing.definition()));
        thing1.setValue("int", "1");

        final EntityInstance thing2 = thing.addInstance(new EntityInstance(thing.definition()));
        thing2.setValue("int", "2");

        final EntityInstance thing3 = thing.addInstance(new EntityInstance(thing.definition()));
        thing3.setValue("int", "3");

        QueryFilterParams params = new QueryFilterParams();
        params.put("sortBy", "-int");

        SimpleQuery ascSortedResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").
                performQuery(params);

        Assertions.assertTrue(ascSortedResults.isResultACollection(), "result should be a collection");
        final List<EntityInstance> instances = ascSortedResults.getListEntityInstances();
        Assertions.assertEquals(3, instances.size(), "expected 3 values");
        Assertions.assertEquals(thing3,instances.get(0));
        Assertions.assertEquals(thing2,instances.get(1));

        // then repeat sort and get different results

        params = new QueryFilterParams();
        params.put("sortBy", "+int");

        SimpleQuery descSortedResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").
                performQuery(params);

        final List<EntityInstance> descInstances = descSortedResults.getListEntityInstances();
        Assertions.assertEquals(3, descInstances.size(), "expected 3 values");
        Assertions.assertEquals(thing1,descInstances.get(0));
        Assertions.assertEquals(thing2,descInstances.get(1));
        Assertions.assertEquals(thing3,descInstances.get(2));

        // check that default sort is ascending
        params = new QueryFilterParams();
        params.put("sortBy", "int");

        SimpleQuery defaultSortedResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").
                performQuery(params);

        final List<EntityInstance> defaultSortedInstances = defaultSortedResults.getListEntityInstances();
        Assertions.assertEquals(3, defaultSortedInstances.size(), "expected 3 values");
        Assertions.assertEquals(thing1,defaultSortedInstances.get(0));
        Assertions.assertEquals(thing2,defaultSortedInstances.get(1));
        Assertions.assertEquals(thing3,defaultSortedInstances.get(2));
    }

    @Test
    public void canSortViaAQuery(){

        EntityRelModel aThingifier = new EntityRelModel();
        aThingifier.createEntityDefinition("thing", "things")
                .addField(Field.is("truefalse", FieldType.BOOLEAN));

        EntityInstanceCollection thing = aThingifier.getInstanceData().getInstanceCollectionForEntityNamed("thing");

        final EntityInstance trueThing = thing.addInstance(new EntityInstance(thing.definition()));
        trueThing.setValue("truefalse", "true");

        final EntityInstance falseThing = thing.addInstance(new EntityInstance(thing.definition()));
        falseThing.setValue("truefalse", "false");

        QueryFilterParams params = new QueryFilterParams();
        params.put("sortBy", "-truefalse");

        SimpleQuery ascSortedResults = new SimpleQuery(aThingifier.getSchema(), aThingifier.getInstanceData(), "things").
                                        performQuery(params);

        Assertions.assertTrue(ascSortedResults.isResultACollection(), "result should be a collection");
        final List<EntityInstance> instances = ascSortedResults.getListEntityInstances();
        Assertions.assertEquals(2, instances.size(), "expected 2 values");
        Assertions.assertEquals(trueThing,instances.get(0));
        Assertions.assertEquals(falseThing,instances.get(1));

        // then repeat sort and get different results

        params = new QueryFilterParams();
        params.put("sortBy", "+truefalse");

        SimpleQuery descSortedResults = new SimpleQuery(aThingifier.getSchema(), aThingifier.getInstanceData(), "things").
                performQuery(params);

        final List<EntityInstance> descInstances = descSortedResults.getListEntityInstances();
        Assertions.assertEquals(2, descInstances.size(), "expected 2 values");
        Assertions.assertEquals(falseThing,descInstances.get(0));
        Assertions.assertEquals(trueThing,descInstances.get(1));

        // check that default sort is ascending
        params = new QueryFilterParams();
        params.put("sortBy", "truefalse");

        SimpleQuery defaultSortedResults = new SimpleQuery(aThingifier.getSchema(), aThingifier.getInstanceData(), "things").
                performQuery(params);

        final List<EntityInstance> defaultSortedInstances = defaultSortedResults.getListEntityInstances();
        Assertions.assertEquals(2, defaultSortedInstances.size(), "expected 2 values");
        Assertions.assertEquals(falseThing,defaultSortedInstances.get(0));
        Assertions.assertEquals(trueThing,defaultSortedInstances.get(1));
    }
}
