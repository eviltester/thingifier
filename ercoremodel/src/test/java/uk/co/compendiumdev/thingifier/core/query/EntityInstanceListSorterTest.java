package uk.co.compendiumdev.thingifier.core.query;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.MutableEntityInstance;

public class EntityInstanceListSorterTest {

    private EntityDefinition thing;

    @BeforeEach
    public void setupEntityDefinition() {
        thing =
                new EntityDefinition("thing", "things", -1)
                        .addFields(
                                Field.is("category", FieldType.STRING),
                                Field.is("priority", FieldType.INTEGER));
    }

    @Test
    public void sortsBySingleFieldAscendingAndDescending() {
        EntityInstance low = instance("beta", "1");
        EntityInstance high = instance("alpha", "3");
        List<EntityInstance> unsorted = List.of(low, high);

        QueryFilterParams ascending = new QueryFilterParams();
        ascending.put("_sortBy", "+priority");
        Assertions.assertEquals(
                List.of(low, high), new EntityInstanceListSorter(ascending).sort(unsorted));

        QueryFilterParams descending = new QueryFilterParams();
        descending.put("_sortBy", "-priority");
        Assertions.assertEquals(
                List.of(high, low), new EntityInstanceListSorter(descending).sort(unsorted));
    }

    @Test
    public void sortsByMultipleFieldsUsingLaterFieldsAsTieBreakers() {
        EntityInstance alphaLow = instance("alpha", "1");
        EntityInstance alphaHigh = instance("alpha", "3");
        EntityInstance betaLow = instance("beta", "2");
        EntityInstance betaHigh = instance("beta", "4");
        List<EntityInstance> unsorted = List.of(betaLow, alphaLow, betaHigh, alphaHigh);

        QueryFilterParams params = new QueryFilterParams();
        params.put("_sortBy", "+category,-priority");

        List<EntityInstance> sorted = new EntityInstanceListSorter(params).sort(unsorted);

        Assertions.assertEquals(List.of(alphaHigh, alphaLow, betaHigh, betaLow), sorted);
    }

    @Test
    public void trimsAndIgnoresBlankMultiFieldSortTokens() {
        EntityInstance alphaLow = instance("alpha", "1");
        EntityInstance alphaHigh = instance("alpha", "3");
        EntityInstance betaLow = instance("beta", "2");
        EntityInstance betaHigh = instance("beta", "4");
        List<EntityInstance> unsorted = List.of(betaLow, alphaLow, betaHigh, alphaHigh);

        QueryFilterParams params = new QueryFilterParams();
        params.put("_sortBy", " +category, , -priority ");

        List<EntityInstance> sorted = new EntityInstanceListSorter(params).sort(unsorted);

        Assertions.assertEquals(List.of(alphaHigh, alphaLow, betaHigh, betaLow), sorted);
    }

    @Test
    public void ignoresUnknownSortFieldsAndDoesNotMutateInputList() {
        EntityInstance low = instance("beta", "1");
        EntityInstance high = instance("alpha", "3");
        List<EntityInstance> unsorted = List.of(low, high);

        QueryFilterParams params = new QueryFilterParams();
        params.put("_sortBy", "+missing");

        List<EntityInstance> sorted = new EntityInstanceListSorter(params).sort(unsorted);

        Assertions.assertEquals(List.of(low, high), sorted);
        Assertions.assertNotSame(unsorted, sorted);
    }

    private EntityInstance instance(final String category, final String priority) {
        return MutableEntityInstance.forEntity(thing)
                .setValue("category", category)
                .setValue("priority", priority)
                .toEntityInstance();
    }
}
