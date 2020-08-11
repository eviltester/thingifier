package uk.co.compendiumdev.thingifier.domain.definitions.relationship;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;

public class RelationshipVectorTest {

    // vector is pretty much a data class

    @Test
    void canCreateADefaultRelationshipVector(){

        final RelationshipVector vector =
                new RelationshipVector(
                        Thing.create("estimate", "estimates"),
                        "estimate-of",
                        Thing.create("task", "tasks"),
                        Cardinality.one_to_many);

        Assertions.assertEquals(Cardinality.one_to_many, vector.getCardinality());
        Assertions.assertEquals("estimate-of", vector.getName());
        Assertions.assertEquals(Optionality.OPTIONAL_RELATIONSHIP, vector.getOptionality());

        // at this point the vector hasn't been associated with a relationship definition yet
        // so it is just a notional construct
        Assertions.assertNull(vector.getRelationshipDefinition());

    }

    @Test
    void canCreateAFullRelationshipVector(){


        final Thing task = Thing.create("task", "tasks");
        final Thing estimate = Thing.create("estimate", "estimates");

        final RelationshipVector vector =
                new RelationshipVector(
                        estimate,
                        "estimate-of",
                        task,
                        Cardinality.one_to_many);


        final RelationshipDefinition rel = RelationshipDefinition.create(vector);


        Assertions.assertEquals(Cardinality.one_to_many, vector.getCardinality());
        Assertions.assertEquals("estimate-of", vector.getName());
        Assertions.assertEquals(Optionality.OPTIONAL_RELATIONSHIP, vector.getOptionality());

        Assertions.assertEquals(rel, vector.getRelationshipDefinition());
        Assertions.assertEquals(estimate, vector.getFrom());
        Assertions.assertEquals(task, vector.getTo());
    }
}
