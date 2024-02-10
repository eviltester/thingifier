package uk.co.compendiumdev.thingifier.core.query;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryFiltersFloatTest {

    EntityRelModel erModel;

    @BeforeEach
    public void setupCollectionTestData(){
        erModel = new EntityRelModel();
        erModel.createEntityDefinition("thing", "things")
                .addFields(
                        Field.is("float", FieldType.FLOAT)
                );

        EntityInstanceCollection thing = erModel.getInstanceData().getInstanceCollectionForEntityNamed("thing");

        thing.createManagedInstance().setValue("float", "4.4");
        thing.createManagedInstance().setValue("float", "1.1");
        thing.createManagedInstance().setValue("float", "3.3");
        thing.createManagedInstance().setValue("float", "2.2");

    }

    @Test
    public void canFilterFloatEquals() {

        Map<String, String> params = new HashMap<>();
        params.put("float", "1.1");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").performQuery(params);
        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(1, instances.size(), "expected 1 value");
        Assertions.assertEquals(1.1F, instances.get(0).getFieldValue("float").asFloat());
    }

    @Test
    public void canFilterFloatNotEquals() {

        Map<String, String> params = new HashMap<>();
        params.put("float", "!1.1");
        params.put("sortby", "+float");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").performQuery(params);
        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(3, instances.size(), "expected 3 value");
        Assertions.assertEquals(2.2F, instances.get(0).getFieldValue("float").asFloat());
        Assertions.assertEquals(3.3F, instances.get(1).getFieldValue("float").asFloat());
        Assertions.assertEquals(4.4F, instances.get(2).getFieldValue("float").asFloat());
    }

    // Allow params to be supplied as a map for multiple filters
    @Disabled("todo: because params are supplied as a map so we don't receive multiple filters")
    @Test
    public void canFilterFloatCombinationOfConditions() {

        // TODO: handle multiple conditions
        Map<String, String> params = new HashMap<>();
        params.put("float", ">1.1");   // greater than 1
        params.put("float", "!3.3");    // but not equal to 3
        params.put("sortby", "+float");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").performQuery(params);
        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(2, instances.size(), "expected 2 value");
        Assertions.assertEquals(2.2F, instances.get(0).getFieldValue("float").asFloat());
        Assertions.assertEquals(4.4F, instances.get(1).getFieldValue("float").asFloat());
    }

    @Test
    public void canFilterFloatGreaterThan() {
        Map<String, String> params = new HashMap<>();
        params.put("float", ">1.1");
        params.put("sortby", "+float");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").performQuery(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(3, instances.size(), "expected 3 values");
        Assertions.assertEquals(2.2F, instances.get(0).getFieldValue("float").asFloat());
        Assertions.assertEquals(3.3F, instances.get(1).getFieldValue("float").asFloat());
        Assertions.assertEquals(4.4F, instances.get(2).getFieldValue("float").asFloat());
    }

    @Test
    public void canFilterFloatLessThan() {
        Map<String, String> params = new HashMap<>();
        params.put("float", "<2.0");
        params.put("sortby", "+float");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").performQuery(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(1, instances.size(), "expected 1 values");
        Assertions.assertEquals(1.1F, instances.get(0).getFieldValue("float").asFloat());
    }

    @Test
    public void canFilterFloatLessThanNotMatching() {
        Map<String, String> params = new HashMap<>();
        params.put("float", "<1.1");
        params.put("sortby", "+float");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").performQuery(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(0, instances.size(), "expected 0 values");
    }

    @Test
    public void canFilterFloatGreaterThanEquals(){
        Map<String, String> params = new HashMap<>();
        params.put("float", ">=3.3");
        params.put("sortby", "+float");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").
                performQuery(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(2, instances.size(), "expected 2 values");
        Assertions.assertEquals(3.3F, instances.get(0).getFieldValue("float").asFloat());
        Assertions.assertEquals(4.4F, instances.get(1).getFieldValue("float").asFloat());
    }

    @Test
    public void canFilterFloatLessThanEquals(){
        Map<String, String> params = new HashMap<>();
        params.put("float", "<=3.3");
        params.put("sortby", "+float");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").
                performQuery(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(3, instances.size(), "expected 3 values");
        Assertions.assertEquals(1.1F, instances.get(0).getFieldValue("float").asFloat());
        Assertions.assertEquals(2.2F, instances.get(1).getFieldValue("float").asFloat());
        Assertions.assertEquals(3.3F, instances.get(2).getFieldValue("float").asFloat());
    }

    @Test
    public void canFilterFloatLessThanEqualsSortDesc(){
        Map<String, String> params = new HashMap<>();
        params.put("float", "<=3.3");
        params.put("sortby", "-float");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").
                performQuery(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(3, instances.size(), "expected 3 values");
        Assertions.assertEquals(3.3F, instances.get(0).getFieldValue("float").asFloat());
        Assertions.assertEquals(2.2F, instances.get(1).getFieldValue("float").asFloat());
        Assertions.assertEquals(1.1F, instances.get(2).getFieldValue("float").asFloat());
    }

}
