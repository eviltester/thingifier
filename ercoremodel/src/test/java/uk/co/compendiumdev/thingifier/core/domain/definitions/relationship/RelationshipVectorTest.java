package uk.co.compendiumdev.thingifier.core.domain.definitions.relationship;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

class RelationshipVectorTest {
    private EntityDefinition task;
    private EntityDefinition estimate;
    private RelationshipVectorDefinition vector;

    // vector is pretty much a data class

    @BeforeEach
    void createDefaultTestVector(){
        task = new EntityDefinition("task", "tasks");
        estimate = new EntityDefinition("estimate", "estimates");

        vector =
                new RelationshipVectorDefinition(
                        estimate,
                        "estimate-of",
                        task,
                        Cardinality.ONE_TO_MANY());
    }

    @Test
    void canCreateADefaultRelationshipVector(){

        Assertions.assertEquals("*", vector.getCardinality().right());
        Assertions.assertEquals("estimate-of", vector.getName());
        Assertions.assertEquals(Optionality.OPTIONAL_RELATIONSHIP, vector.getOptionality());
        Assertions.assertEquals(estimate, vector.getFrom());
        Assertions.assertEquals(task, vector.getTo());

        Assertions.assertTrue(
                estimate.related().hasRelationship("estimate-of"),
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
