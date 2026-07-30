package uk.co.compendiumdev.thingifier.core.query;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

/** Repository-backed URL query coverage for API-style entity reads. */
public class SortingViaQueryFiltersTest {

    EntityDefinition thing;
    EntityRelModel erModel;

    @BeforeEach
    public void setupThingifier() {

        erModel = new EntityRelModel();
        thing =
                erModel.createEntityDefinition("thing", "things")
                        .addFields(
                                Field.is("truefalse", FieldType.BOOLEAN),
                                Field.is("int", FieldType.INTEGER));
    }

    @Test
    public void canSortIntViaAQuery() {

        final EntityInstance thing1 =
                erModel.getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(EntityInstanceDraft.forEntity(thing).withField("int", "1"));

        final EntityInstance thing2 =
                erModel.getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(EntityInstanceDraft.forEntity(thing).withField("int", "2"));

        final EntityInstance thing3 =
                erModel.getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(EntityInstanceDraft.forEntity(thing).withField("int", "3"));

        QueryFilterParams params = new QueryFilterParams();
        params.put("_sortBy", "-int");

        RepositoryQuery ascSortedResults = queryCollection(erModel, thing, params);

        Assertions.assertTrue(
                ascSortedResults.isResultACollection(), "result should be a collection");
        final List<EntityInstance> instances = ascSortedResults.getListEntityInstances();
        Assertions.assertEquals(3, instances.size(), "expected 3 values");
        Assertions.assertEquals(thing3, instances.get(0));
        Assertions.assertEquals(thing2, instances.get(1));

        // then repeat sort and get different results

        params = new QueryFilterParams();
        params.put("_sortBy", "+int");

        RepositoryQuery descSortedResults = queryCollection(erModel, thing, params);

        final List<EntityInstance> descInstances = descSortedResults.getListEntityInstances();
        Assertions.assertEquals(3, descInstances.size(), "expected 3 values");
        Assertions.assertEquals(thing1, descInstances.get(0));
        Assertions.assertEquals(thing2, descInstances.get(1));
        Assertions.assertEquals(thing3, descInstances.get(2));

        // check that default sort is ascending
        params = new QueryFilterParams();
        params.put("_sortBy", "int");

        RepositoryQuery defaultSortedResults = queryCollection(erModel, thing, params);

        final List<EntityInstance> defaultSortedInstances =
                defaultSortedResults.getListEntityInstances();
        Assertions.assertEquals(3, defaultSortedInstances.size(), "expected 3 values");
        Assertions.assertEquals(thing1, defaultSortedInstances.get(0));
        Assertions.assertEquals(thing2, defaultSortedInstances.get(1));
        Assertions.assertEquals(thing3, defaultSortedInstances.get(2));
    }

    @Test
    public void sortByParameterNameIsExactAndCaseSensitive() {
        Assertions.assertTrue(SortByFieldName.isSortByParam("_sortBy"));
        Assertions.assertFalse(SortByFieldName.isSortByParam("_SortBy"));
        Assertions.assertFalse(SortByFieldName.isSortByParam("sortBy"));
        Assertions.assertFalse(SortByFieldName.isSortByParam("sortby"));
        Assertions.assertFalse(SortByFieldName.isSortByParam("sort_by"));
    }

    @Test
    public void canSortByMultipleFieldsViaAQuery() {
        final EntityInstance falseLow =
                erModel.getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(thing)
                                        .withField("truefalse", "false")
                                        .withField("int", "1"));
        final EntityInstance falseHigh =
                erModel.getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(thing)
                                        .withField("truefalse", "false")
                                        .withField("int", "3"));
        final EntityInstance trueLow =
                erModel.getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(thing)
                                        .withField("truefalse", "true")
                                        .withField("int", "2"));
        final EntityInstance trueHigh =
                erModel.getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(thing)
                                        .withField("truefalse", "true")
                                        .withField("int", "4"));

        QueryFilterParams params = new QueryFilterParams();
        params.put("_sortBy", "+truefalse,-int");

        RepositoryQuery sortedResults = queryCollection(erModel, thing, params);

        Assertions.assertEquals(
                List.of(falseHigh, falseLow, trueHigh, trueLow),
                sortedResults.getListEntityInstances());
    }

    @Test
    public void canSortViaAQuery() {

        EntityRelModel aThingifier = new EntityRelModel();
        EntityDefinition thing = aThingifier.createEntityDefinition("thing", "things");
        thing.addField(Field.is("truefalse", FieldType.BOOLEAN));

        final EntityInstance trueThing =
                aThingifier
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(thing)
                                        .withField("truefalse", "true"));

        final EntityInstance falseThing =
                aThingifier
                        .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entities()
                        .create(
                                EntityInstanceDraft.forEntity(thing)
                                        .withField("truefalse", "false"));

        QueryFilterParams params = new QueryFilterParams();
        params.put("_sortBy", "-truefalse");

        RepositoryQuery ascSortedResults = queryCollection(aThingifier, thing, params);

        Assertions.assertTrue(
                ascSortedResults.isResultACollection(), "result should be a collection");
        final List<EntityInstance> instances = ascSortedResults.getListEntityInstances();
        Assertions.assertEquals(2, instances.size(), "expected 2 values");
        Assertions.assertEquals(trueThing, instances.get(0));
        Assertions.assertEquals(falseThing, instances.get(1));

        // then repeat sort and get different results

        params = new QueryFilterParams();
        params.put("_sortBy", "+truefalse");

        RepositoryQuery descSortedResults = queryCollection(aThingifier, thing, params);

        final List<EntityInstance> descInstances = descSortedResults.getListEntityInstances();
        Assertions.assertEquals(2, descInstances.size(), "expected 2 values");
        Assertions.assertEquals(falseThing, descInstances.get(0));
        Assertions.assertEquals(trueThing, descInstances.get(1));

        // check that default sort is ascending
        params = new QueryFilterParams();
        params.put("_sortBy", "truefalse");

        RepositoryQuery defaultSortedResults = queryCollection(aThingifier, thing, params);

        final List<EntityInstance> defaultSortedInstances =
                defaultSortedResults.getListEntityInstances();
        Assertions.assertEquals(2, defaultSortedInstances.size(), "expected 2 values");
        Assertions.assertEquals(falseThing, defaultSortedInstances.get(0));
        Assertions.assertEquals(trueThing, defaultSortedInstances.get(1));
    }

    private RepositoryQuery queryCollection(
            final EntityRelModel model,
            final EntityDefinition entity,
            final QueryFilterParams params) {
        return new RepositoryQuery(
                        model.getStore(EntityRelModel.DEFAULT_DATABASE_NAME),
                        RepositoryQuerySpec.collection(entity))
                .performQuery(params);
    }
}
