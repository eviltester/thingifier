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
public class QueryFiltersBooleanTest {

    // todo: advanced filtering i.e. < > partial text match, etc.
    // e.g
    // https://www.moesif.com/blog/technical/api-design/REST-API-Design-Filtering-Sorting-and-Pagination/
    // https://softwareengineering.stackexchange.com/questions/233164/how-do-searches-fit-into-a-restful-interface

    // TODO: risk that the HTTP server does not pass in args in a way that flow through to
    // repository URL
    // query
    //       so test this at an HTTP level as well

    EntityRelModel erModel;

    @BeforeEach
    public void setupCollectionTestData() {
        erModel = new EntityRelModel();
        erModel.createEntityDefinition("thing", "things")
                .addFields(Field.is("truefalse", FieldType.BOOLEAN));

        erModel.getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(
                                        erModel.getSchema().getEntityDefinitionNamed("thing"))
                                .withField("truefalse", "true"));
        erModel.getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(
                                        erModel.getSchema().getEntityDefinitionNamed("thing"))
                                .withField("truefalse", "true"));
        erModel.getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(
                                        erModel.getSchema().getEntityDefinitionNamed("thing"))
                                .withField("truefalse", "true"));
        erModel.getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(
                                        erModel.getSchema().getEntityDefinitionNamed("thing"))
                                .withField("truefalse", "false"));
    }

    @Test
    public void canFilterBooleanMatchesTrue() {

        QueryFilterParams params = new QueryFilterParams();
        params.put("truefalse", "true");

        RepositoryQuery queryResults = queryThings(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(3, instances.size(), "expected 3 true values");
    }

    @Test
    public void canFilterBooleanMatchesFalse() {

        QueryFilterParams params = new QueryFilterParams();
        params.put("truefalse", "false");

        RepositoryQuery queryResults = queryThings(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(1, instances.size(), "expected 1 false value");
    }

    @Test
    public void canFilterBooleanMatchesNotFalse() {
        QueryFilterParams params = new QueryFilterParams();
        params.put("truefalse", "!false");

        RepositoryQuery queryResults = queryThings(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(3, instances.size(), "expected 3 true value");
    }

    @Test
    public void canFilterBooleanMatchesNotTrue() {
        QueryFilterParams params = new QueryFilterParams();
        params.put("truefalse", "!true");

        RepositoryQuery queryResults = queryThings(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(1, instances.size(), "expected 1 !true value");
    }

    @Test
    public void canSortBooleanMatchesAsc() {
        QueryFilterParams params = new QueryFilterParams();
        params.put("_sortBy", "+truefalse");

        RepositoryQuery queryResults = queryThings(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(4, instances.size(), "expected 4 value");

        Assertions.assertEquals(false, instances.get(0).getFieldValue("truefalse").asBoolean());
        Assertions.assertEquals(true, instances.get(1).getFieldValue("truefalse").asBoolean());
        Assertions.assertEquals(true, instances.get(2).getFieldValue("truefalse").asBoolean());
        Assertions.assertEquals(true, instances.get(3).getFieldValue("truefalse").asBoolean());
    }

    @Test
    public void canSortBooleanMatchesDesc() {
        QueryFilterParams params = new QueryFilterParams();
        params.put("_sortBy", "-truefalse");

        RepositoryQuery queryResults = queryThings(params);

        Assertions.assertTrue(queryResults.isResultACollection(), "result should be a collection");
        List<EntityInstance> instances = queryResults.getListEntityInstances();
        Assertions.assertEquals(4, instances.size(), "expected 4 value");

        Assertions.assertEquals(true, instances.get(0).getFieldValue("truefalse").asBoolean());
        Assertions.assertEquals(true, instances.get(1).getFieldValue("truefalse").asBoolean());
        Assertions.assertEquals(true, instances.get(2).getFieldValue("truefalse").asBoolean());
        Assertions.assertEquals(false, instances.get(3).getFieldValue("truefalse").asBoolean());
    }

    private RepositoryQuery queryThings(final QueryFilterParams params) {
        return new RepositoryQuery(
                        erModel.getStore(EntityRelModel.DEFAULT_DATABASE_NAME),
                        RepositoryQuerySpec.collection(
                                erModel.getSchema().getEntityDefinitionNamed("thing")))
                .performQuery(params);
    }
}
