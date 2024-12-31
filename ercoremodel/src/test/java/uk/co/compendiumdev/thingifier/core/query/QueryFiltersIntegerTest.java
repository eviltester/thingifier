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

public class QueryFiltersIntegerTest {

    // todo: advanced filtering i.e. < > partial text match, etc.
    // e.g https://www.moesif.com/blog/technical/api-design/REST-API-Design-Filtering-Sorting-and-Pagination/
    // https://softwareengineering.stackexchange.com/questions/233164/how-do-searches-fit-into-a-restful-interface

    EntityRelModel erModel;

    @BeforeEach
    public void setupCollectionTestData(){
        erModel = new EntityRelModel();
        erModel.createEntityDefinition("thing", "things")
                .addFields(
                            Field.is("int", FieldType.INTEGER)
                );

        EntityInstanceCollection thing = erModel.getInstanceData().getInstanceCollectionForEntityNamed("thing");

        thing.addInstance(new EntityInstance(thing.definition())).setValue("int", "3");
        thing.addInstance(new EntityInstance(thing.definition())).setValue("int", "1");
        thing.addInstance(new EntityInstance(thing.definition())).setValue("int", "4");
        thing.addInstance(new EntityInstance(thing.definition())).setValue("int", "2");

    }

    @Test
    public void canFilterIntegerEquals() {

        QueryFilterParams params = new QueryFilterParams();
        params.put("int", "1");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").performQuery(params);
        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(1, instances.size(), "expected 1 value");
        Assertions.assertEquals(1, instances.get(0).getFieldValue("int").asInteger());
    }

    @Test
    public void canFilterIntegerNotEquals() {

        QueryFilterParams params = new QueryFilterParams();
        params.put("int", "!1");
        params.put("sortby", "+int");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").performQuery(params);
        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(3, instances.size(), "expected 3 value");
        Assertions.assertEquals(2, instances.get(0).getFieldValue("int").asInteger());
        Assertions.assertEquals(3, instances.get(1).getFieldValue("int").asInteger());
        Assertions.assertEquals(4, instances.get(2).getFieldValue("int").asInteger());
    }


    @Test
    public void canFilterIntegerCombinationOfConditions() {

        QueryFilterParams params = new QueryFilterParams();
        params.put("int", ">1");   // greater than 1
        params.put("int", "!3");    // and not equal to 3
        params.put("sortby", "+int");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").performQuery(params);
        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(2, instances.size(), "expected 2 value");
        Assertions.assertEquals(2, instances.get(0).getFieldValue("int").asInteger());
        Assertions.assertEquals(4, instances.get(1).getFieldValue("int").asInteger());
    }

    @Test
    public void canFilterIntegerGreaterThan() {
        QueryFilterParams params = new QueryFilterParams();
        params.put("int", ">1");
        params.put("sortby", "+int");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").performQuery(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(3, instances.size(), "expected 3 values");
        Assertions.assertEquals(2, instances.get(0).getFieldValue("int").asInteger());
        Assertions.assertEquals(3, instances.get(1).getFieldValue("int").asInteger());
        Assertions.assertEquals(4, instances.get(2).getFieldValue("int").asInteger());
    }

    @Test
    public void canFilterIntegerLessThan() {
        QueryFilterParams params = new QueryFilterParams();
        params.put("int", "<2");
        params.put("sortby", "+int");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").performQuery(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(1, instances.size(), "expected 1 values");
        Assertions.assertEquals(1, instances.get(0).getFieldValue("int").asInteger());
    }

    @Test
    public void canFilterIntegerLessThanNotMatching() {
        QueryFilterParams params = new QueryFilterParams();
        params.put("int", "<1");
        params.put("sortby", "+int");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").performQuery(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(0, instances.size(), "expected 0 values");
    }

    @Test
    public void canFilterIntegerGreaterThanEquals(){
        QueryFilterParams params = new QueryFilterParams();
        params.put("int", ">=3");
        params.put("sortby", "+int");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").
                performQuery(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(2, instances.size(), "expected 2 values");
        Assertions.assertEquals(3, instances.get(0).getFieldValue("int").asInteger());
        Assertions.assertEquals(4, instances.get(1).getFieldValue("int").asInteger());
    }

    @Test
    public void canFilterIntegerLessThanEquals(){
        QueryFilterParams params = new QueryFilterParams();
        params.put("int", "<=3");
        params.put("sortby", "+int");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").
                performQuery(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(3, instances.size(), "expected 3 values");
        Assertions.assertEquals(1, instances.get(0).getFieldValue("int").asInteger());
        Assertions.assertEquals(2, instances.get(1).getFieldValue("int").asInteger());
        Assertions.assertEquals(3, instances.get(2).getFieldValue("int").asInteger());
    }

    @Test
    public void canFilterIntegerLessThanSortedDesc(){
        QueryFilterParams params = new QueryFilterParams();
        params.put("int", "<3");
        params.put("sortby", "-int");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").
                performQuery(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(2, instances.size(), "expected 3 values");
        Assertions.assertEquals(2, instances.get(0).getFieldValue("int").asInteger());
        Assertions.assertEquals(1, instances.get(1).getFieldValue("int").asInteger());
    }


    @Test
    public void canIntegerSortedDesc(){
        QueryFilterParams params = new QueryFilterParams();
        params.put("sortby", "-int");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").
                performQuery(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(4, instances.size(), "expected 3 values");
        Assertions.assertEquals(4, instances.get(0).getFieldValue("int").asInteger());
        Assertions.assertEquals(3, instances.get(1).getFieldValue("int").asInteger());
        Assertions.assertEquals(2, instances.get(2).getFieldValue("int").asInteger());
        Assertions.assertEquals(1, instances.get(3).getFieldValue("int").asInteger());
    }

    @Test
    public void canIntegerSortedAsc(){
        QueryFilterParams params = new QueryFilterParams();
        params.put("sortby", "+int");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").
                performQuery(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(4, instances.size(), "expected 3 values");
        Assertions.assertEquals(1, instances.get(0).getFieldValue("int").asInteger());
        Assertions.assertEquals(2, instances.get(1).getFieldValue("int").asInteger());
        Assertions.assertEquals(3, instances.get(2).getFieldValue("int").asInteger());
        Assertions.assertEquals(4, instances.get(3).getFieldValue("int").asInteger());
    }


    @Test
    public void canRegexFilterInteger(){
        QueryFilterParams params = new QueryFilterParams();
        params.put("int", "~=[1,2]");
        params.put("sortby", "+int");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").
                performQuery(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(2, instances.size(), "expected 3 values");
        Assertions.assertEquals(1, instances.get(0).getFieldValue("int").asInteger());
        Assertions.assertEquals(2, instances.get(1).getFieldValue("int").asInteger());
    }


}
