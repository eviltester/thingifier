package uk.co.compendiumdev.thingifier.core.domain.definitions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class DefinedRelationshipsTest {

    @Test
    void canAddRelationship(){

        final DefinedRelationships rels = new DefinedRelationships();
        Assertions.assertFalse(
                rels.hasRelationship("bob"));

        rels.addRelationship(new RelationshipVectorDefinition(
                new EntityDefinition("thing1", "thing1"),
                "bob",
                new EntityDefinition("thing2", "thing2"),
                            Cardinality.ONE_TO_MANY()));

        Assertions.assertTrue(
                rels.hasRelationship("bob"));
    }

    @Test
    void canAddMultipleRelationshipsNamedSame(){

        final DefinedRelationships rels = new DefinedRelationships();

        rels.addRelationship(new RelationshipVectorDefinition(
                new EntityDefinition("thing1", "thing1"),
                "bob",
                new EntityDefinition("thing2", "thing2"),
                Cardinality.ONE_TO_ONE()));

        rels.addRelationship(new RelationshipVectorDefinition(
                new EntityDefinition("thing2", "thing2"),
                "bob",
                new EntityDefinition("thing3", "thing3"),
                Cardinality.ONE_TO_MANY()));

        final List<RelationshipVectorDefinition> vectors = rels.getRelationships("bob");
        Assertions.assertEquals(2, vectors.size());

        List<String> toCardinalities = new ArrayList<>();
        toCardinalities.add(vectors.get(0).getCardinality().right());
        toCardinalities.add(vectors.get(1).getCardinality().right());

        Assertions.assertTrue(toCardinalities.contains("1"));
        Assertions.assertTrue(toCardinalities.contains("*"));

        final Set<RelationshipVectorDefinition> unnamedvectors = rels.getRelationships();
        Assertions.assertEquals(2, unnamedvectors.size());
    }

    @Test
    void canAddAndRetrieveMultipleRelationships(){

        final DefinedRelationships rels = new DefinedRelationships();

        final RelationshipVectorDefinition bob1 = new RelationshipVectorDefinition(
                new EntityDefinition("thing1", "thing1"),
                "bob",
                new EntityDefinition("thing2", "thing2"),
                Cardinality.ONE_TO_ONE());
        rels.addRelationship(bob1);

        final RelationshipVectorDefinition bob2 = new RelationshipVectorDefinition(
                new EntityDefinition("thing2", "thing2"),
                "bob",
                new EntityDefinition("thing3", "thing3"),
                Cardinality.ONE_TO_MANY());
        rels.addRelationship(bob2);

        final RelationshipVectorDefinition connie1 = new RelationshipVectorDefinition(
                new EntityDefinition("thing1", "thing1"),
                "connie",
                new EntityDefinition("thing2", "thing2"),
                Cardinality.ONE_TO_MANY());
        rels.addRelationship(connie1);

        final RelationshipVectorDefinition dobbs1 = new RelationshipVectorDefinition(
                new EntityDefinition("thing1", "thing1"),
                "dobbs",
                new EntityDefinition("thing2", "thing2"),
                Cardinality.ONE_TO_MANY());
        rels.addRelationship(dobbs1);

        final List<RelationshipVectorDefinition> bobs = rels.getRelationships("bob");
        Assertions.assertEquals(2, bobs.size());
        Assertions.assertTrue(bobs.contains(bob1));
        Assertions.assertTrue(bobs.contains(bob2));

        final List<RelationshipVectorDefinition> connies = rels.getRelationships("connie");
        Assertions.assertEquals(1, connies.size());
        Assertions.assertTrue(connies.contains(connie1));

        final List<RelationshipVectorDefinition> dobbs = rels.getRelationships("dobbs");
        Assertions.assertEquals(1, dobbs.size());
        dobbs.contains(dobbs1);

        final Set<RelationshipVectorDefinition> all = rels.getRelationships();
        Assertions.assertEquals(4, all.size());
        Assertions.assertTrue(all.contains(bob1));
        Assertions.assertTrue(all.contains(bob2));
        Assertions.assertTrue(all.contains(connie1));
        Assertions.assertTrue(all.contains(dobbs1));
    }



}
