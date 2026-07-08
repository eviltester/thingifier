package uk.co.compendiumdev.thingifier.core.query;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.List;

/**
 * Repository-backed URL query coverage for API-style entity reads.
 */
public class QueryFiltersIdTest {

    EntityRelModel erModel;

    @BeforeEach
    public void setupCollectionTestData(){
        erModel = new EntityRelModel();
        erModel.createEntityDefinition("thing", "things")
                .addFields(Field.is("id", FieldType.AUTO_INCREMENT))
                .addFields( Field.is("fakeid", FieldType.INTEGER)
                );

        // fakeid is a proxy for the actual id which always starts at 1 and auto increments
        RepositoryUrlQueryTestSupport.add(erModel, "thing", "fakeid", "1");
        RepositoryUrlQueryTestSupport.add(erModel, "thing", "fakeid", "2");
        RepositoryUrlQueryTestSupport.add(erModel, "thing", "fakeid", "3");
        RepositoryUrlQueryTestSupport.add(erModel, "thing", "fakeid", "4");

    }

    @Test
    public void canFilterAndSortIdAsc() {

        QueryFilterParams params = new QueryFilterParams();
        params.put("id", ">=3");
        params.put("sortBy", "+id");

        RepositoryUrlQuery queryResults = RepositoryUrlQueryTestSupport.query(erModel, "things").
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

        RepositoryUrlQuery queryResults = RepositoryUrlQueryTestSupport.query(erModel, "things").
                performQuery(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(2, instances.size(), "expected 2 true values");
        Assertions.assertEquals(2, instances.get(0).getFieldValue("fakeid").asInteger());
        Assertions.assertEquals(1, instances.get(1).getFieldValue("fakeid").asInteger());
    }

}
