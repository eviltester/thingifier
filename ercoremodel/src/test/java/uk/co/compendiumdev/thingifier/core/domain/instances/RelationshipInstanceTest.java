package uk.co.compendiumdev.thingifier.core.domain.instances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVector;

class RelationshipInstanceTest {

    @Test
    void canCreateARelationshipInstance(){

        Thing thingfrom = Thing.create("from", "from");
        Thing thingto = Thing.create("to", "to");
        RelationshipVector vector = new RelationshipVector(
                thingfrom, "fromto", thingto, Cardinality.ONE_TO_ONE
        );
        RelationshipDefinition defn = RelationshipDefinition.create(vector);

        final ThingInstance fromInstance = thingfrom.createManagedInstance();
        final ThingInstance toInstance = thingfrom.createManagedInstance();
        RelationshipInstance rel = new RelationshipInstance(
                                    defn, fromInstance, toInstance);

        System.out.println(rel);

        Assertions.assertEquals(defn, rel.getRelationship());
        Assertions.assertEquals(toInstance, rel.getTo());
        Assertions.assertEquals(fromInstance, rel.getFrom());

        Assertions.assertTrue(rel.involves(fromInstance));
        Assertions.assertTrue(rel.involves(toInstance));
        Assertions.assertFalse(rel.involves(thingfrom.createManagedInstance()));

        Assertions.assertEquals(toInstance, rel.getOtherThingInstance(fromInstance));
        Assertions.assertEquals(fromInstance, rel.getOtherThingInstance(toInstance));

    }
}
