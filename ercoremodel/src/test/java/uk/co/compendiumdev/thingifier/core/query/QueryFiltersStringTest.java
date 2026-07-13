package uk.co.compendiumdev.thingifier.core.query;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

/** Repository-backed URL query coverage for API-style entity reads. */
public class QueryFiltersStringTest {

    EntityRelModel erModel;

    @BeforeEach
    public void setupCollectionTestData() {
        erModel = new EntityRelModel();
        erModel.createEntityDefinition("thing", "things")
                .addFields(Field.is("string", FieldType.STRING));

        erModel.getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(
                                        erModel.getSchema().getEntityDefinitionNamed("thing"))
                                .withField("string", "one"));
        erModel.getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(
                                        erModel.getSchema().getEntityDefinitionNamed("thing"))
                                .withField("string", "two"));
        erModel.getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(
                                        erModel.getSchema().getEntityDefinitionNamed("thing"))
                                .withField("string", "three"));
        erModel.getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(
                                        erModel.getSchema().getEntityDefinitionNamed("thing"))
                                .withField("string", "four"));
    }

    @Test
    public void canSortStringAsc() {

        QueryFilterParams params = new QueryFilterParams();
        params.put("sortBy", "+string");

        RepositoryQuery queryResults = queryThings(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(4, instances.size(), "expected 2 true values");
        Assertions.assertEquals("four", instances.get(0).getFieldValue("string").asString());
        Assertions.assertEquals("one", instances.get(1).getFieldValue("string").asString());
        Assertions.assertEquals("three", instances.get(2).getFieldValue("string").asString());
        Assertions.assertEquals("two", instances.get(3).getFieldValue("string").asString());
    }

    @Test
    public void canSortStringDesc() {

        QueryFilterParams params = new QueryFilterParams();
        params.put("sortBy", "-string");

        RepositoryQuery queryResults = queryThings(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(4, instances.size(), "expected 2 true values");

        Assertions.assertEquals("two", instances.get(0).getFieldValue("string").asString());
        Assertions.assertEquals("three", instances.get(1).getFieldValue("string").asString());
        Assertions.assertEquals("one", instances.get(2).getFieldValue("string").asString());
        Assertions.assertEquals("four", instances.get(3).getFieldValue("string").asString());
    }

    @Test
    public void canFilterStringEquals() {

        QueryFilterParams params = new QueryFilterParams();
        params.put("string", "=one");

        RepositoryQuery queryResults = queryThings(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(1, instances.size(), "expected 2 true values");
        Assertions.assertEquals("one", instances.get(0).getFieldValue("string").asString());
    }

    @Test
    public void canFilterAndSortStringUsingRegexAsc() {

        QueryFilterParams params = new QueryFilterParams();
        params.put("string", "~=.*o.*");
        params.put("sortBy", "+string");

        RepositoryQuery queryResults = queryThings(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(3, instances.size(), "expected 2 true values");

        Assertions.assertEquals("four", instances.get(0).getFieldValue("string").asString());
        Assertions.assertEquals("one", instances.get(1).getFieldValue("string").asString());
        Assertions.assertEquals("two", instances.get(2).getFieldValue("string").asString());
    }

    @Test
    public void canFilterAndSortStringUsingRegexDesc() {

        QueryFilterParams params = new QueryFilterParams();
        params.put("string", "~=.*o.*");
        params.put("sortBy", "-string");

        RepositoryQuery queryResults = queryThings(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(3, instances.size(), "expected 2 true values");

        Assertions.assertEquals("four", instances.get(2).getFieldValue("string").asString());
        Assertions.assertEquals("one", instances.get(1).getFieldValue("string").asString());
        Assertions.assertEquals("two", instances.get(0).getFieldValue("string").asString());
    }

    @Test
    public void canFilterAndSortStringUsingWildcardAsc() {

        QueryFilterParams params = new QueryFilterParams();
        params.put("string", "*=*e");
        params.put("sortBy", "+string");

        RepositoryQuery queryResults = queryThings(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(2, instances.size(), "expected 2 true values");

        Assertions.assertEquals("one", instances.get(0).getFieldValue("string").asString());
        Assertions.assertEquals("three", instances.get(1).getFieldValue("string").asString());
    }

    @Test
    public void canFilterAndSortStringUsingWildcardAscForT() {

        QueryFilterParams params = new QueryFilterParams();
        params.put("string", "*=t*");
        params.put("sortBy", "+string");

        RepositoryQuery queryResults = queryThings(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(2, instances.size(), "expected 2 true values");

        Assertions.assertEquals("three", instances.get(0).getFieldValue("string").asString());
        Assertions.assertEquals("two", instances.get(1).getFieldValue("string").asString());
    }

    @Test
    public void canFilterAndSortStringUsingWildcardAscForTBit() {

        QueryFilterParams params = new QueryFilterParams();
        params.put("string", "*=t?*");
        params.put("sortBy", "+string");

        RepositoryQuery queryResults = queryThings(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(2, instances.size(), "expected 2 true values");

        Assertions.assertEquals("three", instances.get(0).getFieldValue("string").asString());
        Assertions.assertEquals("two", instances.get(1).getFieldValue("string").asString());
    }

    @Test
    public void canFilterAndSortStringUsingWildcardAscForTBitO() {

        QueryFilterParams params = new QueryFilterParams();
        params.put("string", "*=t?o");
        params.put("sortBy", "+string");

        RepositoryQuery queryResults = queryThings(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(1, instances.size(), "expected 2 true values");

        Assertions.assertEquals("two", instances.get(0).getFieldValue("string").asString());
    }

    private RepositoryQuery queryThings(final QueryFilterParams params) {
        return new RepositoryQuery(
                        erModel.getStore(EntityRelModel.DEFAULT_DATABASE_NAME),
                        RepositoryQuerySpec.collection(
                                erModel.getSchema().getEntityDefinitionNamed("thing")))
                .performQuery(params);
    }
}
