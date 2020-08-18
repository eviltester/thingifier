package uk.co.compendiumdev.thingifier.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.DataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

public class EntityRelModelTest {

    // Core needs a central class which 'manages' the Entities (Things)
    // and the Relationships, and the data gen
    // - this should be separate from the
    // Thingifier (which also has the API and the app)
    // the main class will be built by TDD, and refactoring in
    // code from the Thingifier

    @Test
    public void canCreateAnEntityRelModel(){

        EntityRelModel erm = new EntityRelModel();

        Assertions.assertEquals(0, erm.getThings().size());
        Assertions.assertFalse(erm.hasThingNamed("bob"));
        Assertions.assertNull(erm.findThingInstanceByGuid("bob"));
        Assertions.assertNull(erm.getThingNamed("bob"));
        Assertions.assertFalse(erm.hasThingWithPluralNamed("bob"));
        Assertions.assertNull(erm.getThingWithPluralNamed("bob"));
        Assertions.assertNull(erm.getThingNamedSingularOrPlural("bob"));
        Assertions.assertEquals(0, erm.getThingNames().size());
    }

    @Test
    public void nothingHappensWhenTryToDeleteThingThatDoesNotExist() {

        EntityRelModel erm = new EntityRelModel();

        erm.deleteThing(
                ThingInstance.create(
                        ThingDefinition.create("no", "nos")));
    }

    @Test
    public void canCreateAThingInAModel() {

        EntityRelModel erm = new EntityRelModel();
        Thing thing = erm.createThing("thing", "things");

        Assertions.assertEquals(1, erm.getThings().size());
        Assertions.assertTrue(erm.getThings().contains(thing));

        Assertions.assertTrue(erm.hasThingNamed("thing"));

        Assertions.assertNotNull(erm.getThingNamed("thing"));
        Assertions.assertEquals(thing, erm.getThingNamed("thing"));
        Assertions.assertTrue(erm.hasThingWithPluralNamed("things"));
        Assertions.assertNotNull(erm.getThingWithPluralNamed("things"));
        Assertions.assertEquals(thing, erm.getThingWithPluralNamed("things"));
        Assertions.assertEquals(thing, erm.getThingNamedSingularOrPlural("thing"));
        Assertions.assertEquals(thing, erm.getThingNamedSingularOrPlural("things"));
        Assertions.assertEquals(1, erm.getThingNames().size());

        Assertions.assertTrue(erm.getThingNames().contains("thing"));
    }

    @Test
    public void canFindAThingInAModel() {

        EntityRelModel erm = new EntityRelModel();
        Thing thing = erm.createThing("thing", "things");

        final ThingInstance instance = thing.createManagedInstance();

        Assertions.assertNotNull(
                erm.findThingInstanceByGuid(instance.getGUID()));
        Assertions.assertEquals(instance,
                    erm.findThingInstanceByGuid(instance.getGUID()));
    }

    @Test
    public void canDeleteAThingInAModel() {

        EntityRelModel erm = new EntityRelModel();
        Thing thing = erm.createThing("thing", "things");

        final ThingInstance instance = thing.createManagedInstance();
        erm.deleteThing(instance);

        Assertions.assertNull(
                erm.findThingInstanceByGuid(instance.getGUID()));
        Assertions.assertEquals(0,
                erm.getThingNamed(
                        instance.getEntity().getName()).countInstances());
    }

    @Test
    public void canClearAllDataInAModel() {

        EntityRelModel erm = new EntityRelModel();
        Thing thing = erm.createThing("thing", "things");
        Thing thing2 = erm.createThing("thing2", "thing2");

        thing.createManagedInstance();
        thing.createManagedInstance();
        thing2.createManagedInstance();
        thing2.createManagedInstance();
        thing2.createManagedInstance();

        Assertions.assertEquals(2, erm.getThings().size());
        Assertions.assertEquals(2,
                erm.getThingNamed("thing").getInstances().size());
        Assertions.assertEquals(3,
                erm.getThingNamed("thing2").getInstances().size());

        erm.clearAllData();

        Assertions.assertEquals(2, erm.getThings().size());
        Assertions.assertEquals(0,
                erm.getThingNamed("thing").getInstances().size());
        Assertions.assertEquals(0,
                erm.getThingNamed("thing2").getInstances().size());
    }

    @Test
    public void canCreateWithNoRelationships() {

        EntityRelModel erm = new EntityRelModel();

        Assertions.assertNotNull(erm.getRelationshipDefinitions());
        Assertions.assertEquals(0, erm.getRelationshipDefinitions().size());
        Assertions.assertFalse(erm.hasRelationshipNamed("bob"));
    }

    @Test
    public void canCreateRelationships() {

        EntityRelModel erm = new EntityRelModel();
        final Thing thing1 = erm.createThing("thing1", "thing1");
        final Thing thing2 = erm.createThing("thing2", "thing2");
        erm.defineRelationship(thing1, thing2, "things", Cardinality.ONE_TO_MANY);

        Assertions.assertNotNull(erm.getRelationshipDefinitions());
        Assertions.assertEquals(1, erm.getRelationshipDefinitions().size());
        Assertions.assertTrue(erm.hasRelationshipNamed("things"));
    }

    @Test
    public void canFindAReversedRelationship() {

        EntityRelModel erm = new EntityRelModel();
        Thing thing = erm.createThing("thing", "things");
        Thing dependent = erm.createThing("dependantthing", "dthings");
        erm.defineRelationship(thing, dependent, "things", Cardinality.ONE_TO_MANY)
                .whenReversed(Cardinality.ONE_TO_ONE, "idiewithoutyou").
                getReversedRelationship().
                setOptionality(Optionality.MANDATORY_RELATIONSHIP);

        Assertions.assertTrue(erm.hasRelationshipNamed("idiewithoutyou"));
    }

    @Test
    public void canDeleteAThingWithRelationships() {

        EntityRelModel erm = new EntityRelModel();
        Thing thing = erm.createThing("thing", "things");
        Thing dependent = erm.createThing("dependantthing", "dthings");
        erm.defineRelationship(thing, dependent, "things", Cardinality.ONE_TO_MANY)
            .whenReversed(Cardinality.ONE_TO_ONE,"idiewithoutyou").
            getReversedRelationship().
            setOptionality(Optionality.MANDATORY_RELATIONSHIP);

        final ThingInstance mainThing = thing.createManagedInstance();
        mainThing.getRelationships().connect("things",
                dependent.createManagedInstance());
        mainThing.getRelationships().connect("things",
                dependent.createManagedInstance());
        mainThing.getRelationships().connect("things",
                dependent.createManagedInstance());

        Assertions.assertEquals(2, erm.getThings().size());
        Assertions.assertEquals(3, mainThing.getRelationships().
                                            getConnectedItems("things").size());
        Assertions.assertEquals(3, erm.getThingNamed("dependantthing").
                                            getInstances().size());

        erm.deleteThing(mainThing);

        Assertions.assertNull(
                erm.findThingInstanceByGuid(mainThing.getGUID()));
        Assertions.assertEquals(0,
                erm.getThingNamed("thing").countInstances());
        Assertions.assertEquals(0,
                erm.getThingNamed("dependantthing").countInstances());

    }

    @Test
    public void canFunctionWithoutADataGenerator(){
        EntityRelModel erm = new EntityRelModel();
        erm.generateData();
        Assertions.assertEquals(0, erm.getThings().size());
    }

    @Test
    public void canSetAndUseADataGenerator(){
        EntityRelModel erm = new EntityRelModel();

        erm.setDataGenerator(new DataPopulator() {
            @Override
            public void populate(final EntityRelModel model) {
                model.createThing("thing", "things");
            }
        });

        erm.generateData();

        Assertions.assertEquals(1, erm.getThings().size());
    }
}
