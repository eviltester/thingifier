package uk.co.compendiumdev.thingifier.core.domain.definitions.relationship;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;

class RelationshipVectorTest {
    private Thing task;
    private Thing estimate;
    private RelationshipVector vector;

    // vector is pretty much a data class

    @BeforeEach
    void createDefaultTestVector(){
        task = Thing.create("task", "tasks");
        estimate = Thing.create("estimate", "estimates");

        vector =
                new RelationshipVector(
                        estimate,
                        "estimate-of",
                        task,
                        Cardinality.ONE_TO_MANY);
    }

    @Test
    void canCreateADefaultRelationshipVector(){

        Assertions.assertEquals(Cardinality.ONE_TO_MANY, vector.getCardinality());
        Assertions.assertEquals("estimate-of", vector.getName());
        Assertions.assertEquals(Optionality.OPTIONAL_RELATIONSHIP, vector.getOptionality());
        Assertions.assertEquals(estimate, vector.getFrom());
        Assertions.assertEquals(task, vector.getTo());

        Assertions.assertTrue(
                estimate.definition().related().hasRelationship("estimate-of"),
                "Expected the estimate to know about the relationship");

        // at this point the vector hasn't been associated with a relationship definition yet
        Assertions.assertNull(vector.getRelationshipDefinition());

    }

    @Test
    void canDefineAVectorAsMandatory() {

        vector.setOptionality(Optionality.MANDATORY_RELATIONSHIP);
        Assertions.assertEquals(Optionality.MANDATORY_RELATIONSHIP, vector.getOptionality());

    }

    @Test
    void canCreateAFullRelationshipVector(){

        final RelationshipDefinition rel = RelationshipDefinition.create(vector);
        Assertions.assertEquals(rel, vector.getRelationshipDefinition());
    }
}
