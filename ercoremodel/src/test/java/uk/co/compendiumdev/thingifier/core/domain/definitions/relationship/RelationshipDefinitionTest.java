package uk.co.compendiumdev.thingifier.core.domain.definitions.relationship;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

class RelationshipDefinitionTest {

    private EntityDefinition task;
    private EntityDefinition estimate;
    private RelationshipVectorDefinition estimateToTask;

    @BeforeEach
    void defaultVector(){

        task = new EntityDefinition("task", "tasks");
        estimate = new EntityDefinition("estimate", "estimates");

        estimateToTask =
                new RelationshipVectorDefinition(
                        estimate,
                        "estimate-of",
                        task,
                        Cardinality.ONE_TO_MANY());

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

        rel.whenReversed(Cardinality.ONE_TO_MANY(), "estimates");

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
