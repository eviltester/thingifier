package uk.co.compendiumdev.thingifier.core.domain.definitions.relationship;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;

class RelationshipDefinitionTest {

    private Thing task;
    private Thing estimate;
    private RelationshipVector estimateToTask;

    @BeforeEach
    void defaultVector(){

        task = Thing.create("task", "tasks");
        estimate = Thing.create("estimate", "estimates");

        estimateToTask =
                new RelationshipVector(
                        estimate,
                        "estimate-of",
                        task,
                        Cardinality.ONE_TO_MANY);

    }

    @Test
    void aRelationshipIsCreatedFromVector(){

        final RelationshipDefinition rel = RelationshipDefinition.
                                            create(estimateToTask);

        Assertions.assertEquals(estimate,rel.getFromRelationship().getFrom());
        Assertions.assertEquals(task,rel.getFromRelationship().getTo());
        Assertions.assertEquals(estimateToTask,rel.getFromRelationship());
        Assertions.assertNull(rel.getReversedRelationship());
        Assertions.assertFalse(rel.isTwoWay());

        Assertions.assertTrue(rel.isKnownAs("estimate-of"));
        Assertions.assertFalse(rel.isKnownAs("estimates"));

        System.out.println(rel.toString());

    }

    @Test
    void aReverseRelationshipVectorIsCreatedFromRelationship(){

        final RelationshipDefinition rel = RelationshipDefinition.
                create(estimateToTask);

        rel.whenReversed(Cardinality.ONE_TO_MANY, "estimates");

        Assertions.assertTrue(rel.isTwoWay());

        Assertions.assertEquals(estimate,rel.getFromRelationship().getFrom());
        Assertions.assertEquals(task,rel.getFromRelationship().getTo());
        Assertions.assertEquals(estimateToTask,rel.getFromRelationship());

        Assertions.assertNotNull(rel.getReversedRelationship());
        Assertions.assertEquals(task,rel.getReversedRelationship().getFrom());
        Assertions.assertEquals(estimate,rel.getReversedRelationship().getTo());
        Assertions.assertEquals("estimates",rel.getReversedRelationship().getName());

        Assertions.assertTrue(rel.isKnownAs("estimate-of"));
        Assertions.assertTrue(rel.isKnownAs("estimates"));


        System.out.println(rel.toString());

    }
}
