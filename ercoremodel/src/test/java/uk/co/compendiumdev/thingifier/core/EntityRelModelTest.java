package uk.co.compendiumdev.thingifier.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.DataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;

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

        Assertions.assertEquals(0, erm.getAllEntityInstanceCollections().size());
        Assertions.assertFalse(erm.hasEntityNamed("bob"));
        Assertions.assertNull(erm.findEntityInstanceByGuid("bob"));
        Assertions.assertNull(erm.getInstanceCollectionForEntityNamed("bob"));
        Assertions.assertFalse(erm.hasEntityWithPluralNamed("bob"));
        Assertions.assertNull(erm.getSchema().getDefinitionWithPluralNamed("bob"));
        Assertions.assertNull(erm.getSchema().getDefinitionWithSingularOrPluralNamed("bob"));
        Assertions.assertEquals(0, erm.getEntityNames().size());
    }

    @Test
    public void nothingHappensWhenTryToDeleteThingThatDoesNotExist() {

        EntityRelModel erm = new EntityRelModel();

        erm.deleteEntityInstance(
                new EntityInstance(
                        new EntityDefinition("no", "nos")));
    }

    @Test
    public void canCreateAThingInAModel() {

        EntityRelModel erm = new EntityRelModel();
        erm.createEntityDefinition("thing", "things");

        EntityInstanceCollection thing = erm.getInstanceCollectionForEntityNamed("thing");

        Assertions.assertEquals(1, erm.getAllEntityInstanceCollections().size());
        Assertions.assertTrue(erm.getAllEntityInstanceCollections().contains(thing));

        Assertions.assertTrue(erm.hasEntityNamed("thing"));

        Assertions.assertNotNull(erm.getInstanceCollectionForEntityNamed("thing"));
        Assertions.assertEquals(thing, erm.getInstanceCollectionForEntityNamed("thing"));
        Assertions.assertTrue(erm.hasEntityWithPluralNamed("things"));
        Assertions.assertNotNull(erm.getSchema().getDefinitionWithPluralNamed("things"));
        Assertions.assertEquals(thing.definition(), erm.getSchema().getDefinitionWithPluralNamed("things"));
        Assertions.assertEquals(thing.definition(), erm.getSchema().getDefinitionWithSingularOrPluralNamed("thing"));
        Assertions.assertEquals(thing.definition(), erm.getSchema().getDefinitionWithSingularOrPluralNamed("things"));
        Assertions.assertEquals(1, erm.getEntityNames().size());

        Assertions.assertTrue(erm.getEntityNames().contains("thing"));
    }

    @Test
    public void canFindAThingInAModel() {

        EntityRelModel erm = new EntityRelModel();
        erm.createEntityDefinition("thing", "things");
        EntityInstanceCollection thing = erm.getInstanceCollectionForEntityNamed("thing");

        final EntityInstance instance = thing.createManagedInstance();

        Assertions.assertNotNull(
                erm.findEntityInstanceByGuid(instance.getGUID()));
        Assertions.assertEquals(instance,
                    erm.findEntityInstanceByGuid(instance.getGUID()));
    }

    @Test
    public void canDeleteAThingInAModel() {

        EntityRelModel erm = new EntityRelModel();
        erm.createEntityDefinition("thing", "things");
        EntityInstanceCollection thing = erm.getInstanceCollectionForEntityNamed("thing");

        final EntityInstance instance = thing.createManagedInstance();
        erm.deleteEntityInstance(instance);

        Assertions.assertNull(
                erm.findEntityInstanceByGuid(instance.getGUID()));
        Assertions.assertEquals(0,
                erm.getInstanceCollectionForEntityNamed(
                        instance.getEntity().getName()).countInstances());
    }

    @Test
    public void canClearAllDataInAModel() {

        EntityRelModel erm = new EntityRelModel();
        erm.createEntityDefinition("thing", "things");
        EntityInstanceCollection thing = erm.getInstanceCollectionForEntityNamed("thing");
        erm.createEntityDefinition("thing2", "thing2");
        EntityInstanceCollection thing2 = erm.getInstanceCollectionForEntityNamed("thing2");

        thing.createManagedInstance();
        thing.createManagedInstance();
        thing2.createManagedInstance();
        thing2.createManagedInstance();
        thing2.createManagedInstance();

        Assertions.assertEquals(2, erm.getAllEntityInstanceCollections().size());
        Assertions.assertEquals(2,
                erm.getInstanceCollectionForEntityNamed("thing").getInstances().size());
        Assertions.assertEquals(3,
                erm.getInstanceCollectionForEntityNamed("thing2").getInstances().size());

        erm.clearAllData();

        Assertions.assertEquals(2, erm.getAllEntityInstanceCollections().size());
        Assertions.assertEquals(0,
                erm.getInstanceCollectionForEntityNamed("thing").getInstances().size());
        Assertions.assertEquals(0,
                erm.getInstanceCollectionForEntityNamed("thing2").getInstances().size());
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
        EntityDefinition t1d = erm.createEntityDefinition("thing1", "thing1");
        final EntityInstanceCollection thing1 = erm.getInstanceCollectionForEntityNamed("thing1");
        EntityDefinition t2d = erm.createEntityDefinition("thing2", "thing2");
        final EntityInstanceCollection thing2 = erm.getInstanceCollectionForEntityNamed("thing1");
        erm.createRelationshipDefinition(t1d, t2d, "things", Cardinality.ONE_TO_MANY);

        Assertions.assertNotNull(erm.getRelationshipDefinitions());
        Assertions.assertEquals(1, erm.getRelationshipDefinitions().size());
        Assertions.assertTrue(erm.hasRelationshipNamed("things"));
    }

    @Test
    public void canFindAReversedRelationship() {

        EntityRelModel erm = new EntityRelModel();
        final EntityDefinition td = erm.createEntityDefinition("thing", "things");
        EntityInstanceCollection thing = erm.getInstanceCollectionForEntityNamed("thing");
        final EntityDefinition dpd = erm.createEntityDefinition("dependantthing", "dthings");
        EntityInstanceCollection dependent = erm.getInstanceCollectionForEntityNamed("dependantthing");

        erm.createRelationshipDefinition(td, dpd, "things", Cardinality.ONE_TO_MANY)
                .whenReversed(Cardinality.ONE_TO_ONE, "idiewithoutyou").
                getReversedRelationship().
                setOptionality(Optionality.MANDATORY_RELATIONSHIP);

        Assertions.assertTrue(erm.hasRelationshipNamed("idiewithoutyou"));
    }

    @Test
    public void canDeleteAThingWithRelationships() {

        EntityRelModel erm = new EntityRelModel();
        final EntityDefinition thingdefn = erm.createEntityDefinition("thing", "things");
        EntityInstanceCollection thing = erm.getInstanceCollectionForEntityNamed("thing");
        final EntityDefinition depdefn = erm.createEntityDefinition("dependantthing", "dthings");
        EntityInstanceCollection dependent = erm.getInstanceCollectionForEntityNamed("dependantthing");

        erm.createRelationshipDefinition(thingdefn, depdefn, "things", Cardinality.ONE_TO_MANY)
            .whenReversed(Cardinality.ONE_TO_ONE,"idiewithoutyou").
            getReversedRelationship().
            setOptionality(Optionality.MANDATORY_RELATIONSHIP);

        final EntityInstance mainThing = thing.createManagedInstance();
        mainThing.getRelationships().connect("things",
                dependent.createManagedInstance());
        mainThing.getRelationships().connect("things",
                dependent.createManagedInstance());
        mainThing.getRelationships().connect("things",
                dependent.createManagedInstance());

        Assertions.assertEquals(2, erm.getAllEntityInstanceCollections().size());
        Assertions.assertEquals(3, mainThing.getRelationships().
                                            getConnectedItems("things").size());
        Assertions.assertEquals(3, erm.getInstanceCollectionForEntityNamed("dependantthing").
                                            getInstances().size());

        erm.deleteEntityInstance(mainThing);

        Assertions.assertNull(
                erm.findEntityInstanceByGuid(mainThing.getGUID()));
        Assertions.assertEquals(0,
                erm.getInstanceCollectionForEntityNamed("thing").countInstances());
        Assertions.assertEquals(0,
                erm.getInstanceCollectionForEntityNamed("dependantthing").countInstances());

    }

    @Test
    public void canFunctionWithoutADataGenerator(){
        EntityRelModel erm = new EntityRelModel();
        erm.generateData();
        Assertions.assertEquals(0, erm.getAllEntityInstanceCollections().size());
    }

    @Test
    public void canSetAndUseADataGenerator(){
        EntityRelModel erm = new EntityRelModel();

        erm.setDataGenerator(new DataPopulator() {
            @Override
            public void populate(final EntityRelModel model) {
                model.createEntityDefinition("thing", "things");
            }
        });

        erm.generateData();

        Assertions.assertEquals(1, erm.getAllEntityInstanceCollections().size());
    }
}
