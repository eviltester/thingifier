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

public class QueryFiltersIdTest {

    EntityRelModel erModel;

    @BeforeEach
    public void setupCollectionTestData(){
        erModel = new EntityRelModel();
        erModel.createEntityDefinition("thing", "things")
                .addFields(Field.is("id", FieldType.AUTO_INCREMENT))
                .addFields( Field.is("fakeid", FieldType.INTEGER)
                );

        EntityInstanceCollection thing = erModel.getInstanceData().getInstanceCollectionForEntityNamed("thing");

        // fakeid is a proxy for the actual id which always starts at 1 and auto increments
        thing.addInstance(new EntityInstance(thing.definition())).setValue("fakeid", "1");
        thing.addInstance(new EntityInstance(thing.definition())).setValue("fakeid", "2");
        thing.addInstance(new EntityInstance(thing.definition())).setValue("fakeid", "3");
        thing.addInstance(new EntityInstance(thing.definition())).setValue("fakeid", "4");

    }

    @Test
    public void canFilterAndSortIdAsc() {

        QueryFilterParams params = new QueryFilterParams();
        params.put("id", ">=3");
        params.put("sortBy", "+id");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").
                performQuery(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(2, instances.size(), "expected 2 true values");
        Assertions.assertEquals(3, instances.get(0).getFieldValue("fakeid").asInteger());
        Assertions.assertEquals(4, instances.get(1).getFieldValue("fakeid").asInteger());
    }

    @Test
    public void canFilterAndSortIdDesc() {

        QueryFilterParams params = new QueryFilterParams();
        params.put("id", "<3");
        params.put("sortBy", "-id");

        SimpleQuery queryResults = new SimpleQuery(erModel.getSchema(), erModel.getInstanceData(), "things").
                performQuery(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(2, instances.size(), "expected 2 true values");
        Assertions.assertEquals(2, instances.get(0).getFieldValue("fakeid").asInteger());
        Assertions.assertEquals(1, instances.get(1).getFieldValue("fakeid").asInteger());
    }

}
