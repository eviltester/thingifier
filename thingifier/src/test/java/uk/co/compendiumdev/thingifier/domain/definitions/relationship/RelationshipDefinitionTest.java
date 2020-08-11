package uk.co.compendiumdev.thingifier.domain.definitions.relationship;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.domain.definitions.Cardinality;

public class RelationshipDefinitionTest {

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
                        Cardinality.one_to_many);

    }

    @Test
    void aRelationshipIsCreatedFromaVector(){

        final RelationshipDefinition rel = RelationshipDefinition.
                                            create(estimateToTask);

        // todo: in theory these have no meaning
        //  because the relationship is 'between' or 'involving'
        // the 'to', 'from' concept is at the vector level
        Assertions.assertEquals(estimate,rel.from());
        Assertions.assertEquals(task,rel.to());

        Assertions.assertFalse(rel.isTwoWay());
    }

}
