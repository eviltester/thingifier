package uk.co.compendiumdev.thingifier.core.domain.instances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVector;

import java.util.Collection;
import java.util.List;

class RelationshipsTest {

    private ThingInstance fromInstance;
    private ThingInstance toInstance;
    private RelationshipDefinition defn;
    private Thing thingfrom;
    private Thing thingto;

    @BeforeEach
    void baseData(){
        thingfrom = Thing.create("from", "from");
        thingto = Thing.create("to", "to");
        RelationshipVector vector = new RelationshipVector(
                thingfrom, "fromto", thingto, Cardinality.ONE_TO_ONE
        );

        defn = RelationshipDefinition.create(vector);

        fromInstance = thingfrom.createManagedInstance();
        toInstance = thingto.createManagedInstance();
    }

    @Test
    void canCreateAThingInstanceRelationships(){

        final ThingInstanceRelationships relationships = new ThingInstanceRelationships(fromInstance);

        System.out.println(relationships.toString());

        Assertions.assertNull(
                relationships.getTypeOfConnectableItems("bob"));
        Assertions.assertTrue(
                relationships.getConnectedItems("bob").isEmpty());
        Assertions.assertTrue(
                relationships.getConnectedItemsOfType("bob").isEmpty());

        relationships.removeAllRelationshipsInvolving(toInstance);
        Assertions.assertFalse(relationships.hasAnyRelationshipInstances());

        relationships.removeAllRelationships();
        Assertions.assertFalse(relationships.hasAnyRelationshipInstances());
    }

    @Test
    void canConnectToThingWhenRelationshipIsDefined(){

        final ThingInstanceRelationships relationships = new ThingInstanceRelationships(fromInstance);

        relationships.connect("fromto", toInstance);
        Assertions.assertTrue(relationships.hasAnyRelationshipInstances());

        Assertions.assertFalse(
            toInstance.getRelationships().
                hasAnyRelationshipInstances());

        final Collection<ThingInstance> instances =
                relationships.getConnectedItems("fromto");

        Assertions.assertTrue(instances.contains(toInstance));

        final Collection<ThingInstance> typeinstances =
                relationships.getConnectedItemsOfType("to");

        Assertions.assertTrue(typeinstances.contains(toInstance));
    }

    @Test
    void cannotConnectToThingWhenNoRelationshipIsDefined(){

        final ThingInstanceRelationships relationships = new ThingInstanceRelationships(fromInstance);

        final IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class,
                () -> relationships.connect(
                        "unknownrelationship", toInstance));

        Assertions.assertTrue(e.getMessage().contains("Unknown Relationship"),
                            e.getMessage());
    }

    @Test
    void cannotConnectToThingWhenRelationshipIsNotForTypePassedIn(){

        final ThingInstanceRelationships relationships = new ThingInstanceRelationships(fromInstance);

        final ThingInstance instance = thingfrom.createManagedInstance();

        final IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class,
                () -> relationships.connect(
                        "fromto",
                        instance));

        Assertions.assertTrue(
            e.getMessage().contains("Unknown Relationship"),
                e.getMessage());



    }

    @Test
    void canCreateATwoWayConnection(){

        final ThingInstanceRelationships relationships = new ThingInstanceRelationships(fromInstance);

        defn.whenReversed(Cardinality.ONE_TO_ONE, "tofrom");

        relationships.connect("fromto", toInstance);

        Assertions.assertTrue(relationships.hasAnyRelationshipInstances());

        Assertions.assertTrue(
            toInstance.getRelationships().
                hasAnyRelationshipInstances());
    }


    @Test
    void canCheckForTypesWithoutAnyInstances(){

        defn.whenReversed(Cardinality.ONE_TO_ONE, "bob");

        Assertions.assertEquals(
            thingto.definition(),
                fromInstance.getRelationships().getTypeOfConnectableItems("fromto"));

        Assertions.assertEquals(
                thingfrom.definition(),
                toInstance.getRelationships().getTypeOfConnectableItems("fromto"));

    }


    @Test
    void nullWhenNoRelationshipsForTypes(){

        final ThingInstanceRelationships relationships = new ThingInstanceRelationships(fromInstance);

        Assertions.assertNull(
            relationships.
                getTypeOfConnectableItems("bob"));
    }

    @Test
    void removeRelationshipsBasedOnName(){

        final ThingInstanceRelationships relationships = new ThingInstanceRelationships(fromInstance);

        relationships.connect("fromto", toInstance);
        Assertions.assertTrue(relationships.hasAnyRelationshipInstances());

        relationships.removeRelationshipsInvolving(
                toInstance, "fromto");

        Assertions.assertFalse(relationships.hasAnyRelationshipInstances());
    }

    @Test
    void removeRelationshipsBasedOnThingInstance(){

        final ThingInstanceRelationships relationships = new ThingInstanceRelationships(fromInstance);

        relationships.connect("fromto", toInstance);
        Assertions.assertTrue(relationships.hasAnyRelationshipInstances());

        relationships.removeAllRelationshipsInvolving(toInstance);

        Assertions.assertFalse(relationships.hasAnyRelationshipInstances());
    }

    @Test
    void removeAllRelationshipsNonTwoWay(){

        final ThingInstanceRelationships relationships = new ThingInstanceRelationships(fromInstance);

        relationships.connect("fromto", toInstance);
        Assertions.assertTrue(relationships.hasAnyRelationshipInstances());

        relationships.removeAllRelationships();

        Assertions.assertFalse(relationships.hasAnyRelationshipInstances());
    }

    @Test
    void removeAllRelationshipsTwoWay(){

        final ThingInstanceRelationships relationships = new ThingInstanceRelationships(fromInstance);

        defn.whenReversed(Cardinality.ONE_TO_ONE, "tofrom");
        defn.getReversedRelationship().setOptionality(Optionality.MANDATORY_RELATIONSHIP);

        relationships.connect("fromto", toInstance);

        Assertions.assertTrue(relationships.hasAnyRelationshipInstances());

        final List<ThingInstance> thingsToDelete = relationships.removeAllRelationships();

        Assertions.assertFalse(relationships.hasAnyRelationshipInstances());

        //toInstance needs to be deleted because mandatory relationship removed
        // so it is returned in the list
        Assertions.assertFalse(thingsToDelete.isEmpty());
        Assertions.assertTrue(thingsToDelete.contains(toInstance));
    }

    @Test
    void removeAllRelationshipsTwoWayReversed(){


        defn.whenReversed(Cardinality.ONE_TO_ONE, "tofrom");
        defn.getReversedRelationship().
                setOptionality(Optionality.MANDATORY_RELATIONSHIP);

        fromInstance.getRelationships().
                connect("fromto", toInstance);

        Assertions.assertTrue(fromInstance.getRelationships().
                hasAnyRelationshipInstances());

        final List<ThingInstance> thingsToDelete =
                toInstance.getRelationships().
                        removeAllRelationships();

        Assertions.assertFalse(fromInstance.
                getRelationships().
                hasAnyRelationshipInstances());

        //toInstance should have been marked as delete when mandatory relationship removed
        Assertions.assertFalse(thingsToDelete.isEmpty());
        Assertions.assertTrue(thingsToDelete.contains(toInstance));
    }

    @Test
    void validationFailsBecauseRelationshipIsMandatory(){

        // set the relationship to mandatory
        // but do not create a relationship instance
        defn.getFromRelationship().setOptionality(Optionality.MANDATORY_RELATIONSHIP);

        final ValidationReport valid = fromInstance.getRelationships().validateRelationships();
        Assertions.assertFalse(valid.isValid());
    }

    @Test
    void validationPassesBecauseRelationshipIsOptionalAndNoInstance(){

        final ValidationReport valid = fromInstance.getRelationships().validateRelationships();
        Assertions.assertTrue(valid.isValid());
    }

    @Test
    void validationPassesWhenInstanceAndRelationshipIsMandatory(){

        defn.getFromRelationship().setOptionality(Optionality.MANDATORY_RELATIONSHIP);

        fromInstance.getRelationships().
                connect("fromto", toInstance);

        final ValidationReport valid = fromInstance.getRelationships().validateRelationships();
        Assertions.assertTrue(valid.isValid());
    }
}
