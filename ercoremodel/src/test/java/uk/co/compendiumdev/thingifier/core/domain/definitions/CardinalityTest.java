package uk.co.compendiumdev.thingifier.core.domain.definitions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

class CardinalityTest {

    @Test
    void canGetLeftAndRightValues(){
        Assertions.assertEquals("1", Cardinality.ONE_TO_MANY().left());
        Assertions.assertEquals("*", Cardinality.ONE_TO_MANY().right());
    }

    @Test
    void cannotAddRelationshipExceedCardinality(){

        final DefinedRelationships rels = new DefinedRelationships();
        Assertions.assertFalse(
                rels.hasRelationship("bob"));

        final EntityDefinition thing1 = new EntityDefinition("thing1", "thing1");
        final EntityDefinition thing2 = new EntityDefinition("thing2", "thing2");

        final RelationshipVectorDefinition relationshipVector = new RelationshipVectorDefinition(
                thing1,
                "bob",
                thing2,
                new Cardinality(0, 2));

        final RelationshipDefinition parentRelationship =
                RelationshipDefinition.create(relationshipVector);

        rels.addRelationship(relationshipVector);

        final EntityInstance instance1 = new EntityInstance(thing1);
        final EntityInstance instance2 = new EntityInstance(thing2);
        final EntityInstance instance3 = new EntityInstance(thing2);
        final EntityInstance instance4 = new EntityInstance(thing2);

        instance1.getRelationships().connect("bob", instance2);
        instance1.getRelationships().connect("bob", instance3);

        Assertions.assertEquals(true,instance1.validate().isValid());

        // this should fail
        boolean failed = false;
        try {
            instance1.getRelationships().connect("bob", instance4);
        }catch(RuntimeException e){
            System.out.println(e.getMessage());
            failed = true;
        }

        Assertions.assertTrue(failed);

    }
}
