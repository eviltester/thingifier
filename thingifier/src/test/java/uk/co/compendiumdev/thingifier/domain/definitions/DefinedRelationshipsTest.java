package uk.co.compendiumdev.thingifier.domain.definitions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.domain.definitions.relationship.RelationshipVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DefinedRelationshipsTest {

    @Test
    void canAddRelationship(){

        final DefinedRelationships rels = new DefinedRelationships();
        Assertions.assertFalse(
                rels.hasRelationship("bob"));

        rels.addRelationship(new RelationshipVector(
                "bob",
                            new Cardinality("1", "1")));

        Assertions.assertTrue(
                rels.hasRelationship("bob"));
    }

    @Test
    void canAddMultipleRelationshipsNamedSame(){

        final DefinedRelationships rels = new DefinedRelationships();

        rels.addRelationship(new RelationshipVector(
                "bob",
                new Cardinality("1", "1")));

        rels.addRelationship(new RelationshipVector(
                "bob",
                new Cardinality("1", "*")));

        final List<RelationshipVector> vectors = rels.getRelationships("bob");
        Assertions.assertEquals(2, vectors.size());

        List<String> toCardinalities = new ArrayList<>();
        toCardinalities.add(vectors.get(0).getCardinality().right());
        toCardinalities.add(vectors.get(1).getCardinality().right());

        Assertions.assertTrue(toCardinalities.contains("1"));
        Assertions.assertTrue(toCardinalities.contains("*"));

        final Set<RelationshipVector> unnamedvectors = rels.getRelationships();
        Assertions.assertEquals(2, unnamedvectors.size());
    }

    @Test
    void canAddAndRetrieveMultipleRelationships(){

        final DefinedRelationships rels = new DefinedRelationships();

        final RelationshipVector bob1 = new RelationshipVector(
                "bob",
                new Cardinality("1", "1"));
        rels.addRelationship(bob1);

        final RelationshipVector bob2 = new RelationshipVector(
                "bob",
                new Cardinality("1", "*"));
        rels.addRelationship(bob2);

        final RelationshipVector connie1 = new RelationshipVector(
                "connie",
                new Cardinality("0", "*"));
        rels.addRelationship(connie1);

        final RelationshipVector dobbs1 = new RelationshipVector(
                "dobbs",
                new Cardinality("0", "0"));
        rels.addRelationship(dobbs1);

        final List<RelationshipVector> bobs = rels.getRelationships("bob");
        Assertions.assertEquals(2, bobs.size());
        Assertions.assertTrue(bobs.contains(bob1));
        Assertions.assertTrue(bobs.contains(bob2));

        final List<RelationshipVector> connies = rels.getRelationships("connie");
        Assertions.assertEquals(1, connies.size());
        Assertions.assertTrue(connies.contains(connie1));

        final List<RelationshipVector> dobbs = rels.getRelationships("dobbs");
        Assertions.assertEquals(1, dobbs.size());
        dobbs.contains(dobbs1);

        final Set<RelationshipVector> all = rels.getRelationships();
        Assertions.assertEquals(4, all.size());
        Assertions.assertTrue(all.contains(bob1));
        Assertions.assertTrue(all.contains(bob2));
        Assertions.assertTrue(all.contains(connie1));
        Assertions.assertTrue(all.contains(dobbs1));
    }


    // todo: this seems too high level to be tested at DefinedRelationship
    // perhaps this method should be on the Thing
    @Test
    void canHaveNamedRelationshipBetweenThings(){

        final Thing stress = Thing.create("stress", "stress");
        final Thing slack = Thing.create("slack", "slack");

        final RelationshipVector vec =
                new RelationshipVector("withbob", new Cardinality("1", "0"));
        final RelationshipDefinition defn = RelationshipDefinition.create(stress, slack, vec);


        final DefinedRelationships rels = new DefinedRelationships();
        rels.addRelationship(vec);

        Assertions.assertNull(rels.getRelationship("pink", stress.definition()));

        Assertions.assertNull(rels.getRelationship("withbob", stress.definition()));
        Assertions.assertNotNull(rels.getRelationship("withbob", slack.definition()));
    }
}
