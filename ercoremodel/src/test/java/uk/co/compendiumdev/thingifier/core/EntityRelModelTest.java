package uk.co.compendiumdev.thingifier.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.datapopulator.DataPopulator;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.Optionality;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;
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

        Assertions.assertEquals(0, erm.getInstanceData().getAllInstanceCollections().size());
        Assertions.assertFalse(erm.hasEntityNamed("bob"));
        Assertions.assertNull(erm.getInstanceData().findEntityInstanceByGUID("bob"));
        Assertions.assertNull(erm.getInstanceData().getInstanceCollectionForEntityNamed("bob"));
        Assertions.assertFalse(erm.hasEntityWithPluralNamed("bob"));
        Assertions.assertNull(erm.getSchema().getEntityDefinitionWithPluralNamed("bob"));
        Assertions.assertNull(erm.getSchema().getDefinitionWithSingularOrPluralNamed("bob"));
        Assertions.assertEquals(0, erm.getEntityNames().size());
    }

    @Test
    public void nothingHappensWhenTryToDeleteThingThatDoesNotExist() {

        EntityRelModel erm = new EntityRelModel();

        final EntityInstance anEntityInstance = new EntityInstance(
                new EntityDefinition("no", "nos"));
        erm.getInstanceData().deleteEntityInstance(anEntityInstance);
    }

    @Test
    public void canCreateAThingInAModel() {

        EntityRelModel erm = new EntityRelModel();
        erm.createEntityDefinition("thing", "things");

        EntityInstanceCollection thing = erm.getInstanceData().getInstanceCollectionForEntityNamed("thing");

        Assertions.assertEquals(1, erm.getInstanceData().getAllInstanceCollections().size());
        Assertions.assertTrue(erm.getInstanceData().getAllInstanceCollections().contains(thing));

        Assertions.assertTrue(erm.hasEntityNamed("thing"));

        Assertions.assertNotNull(erm.getInstanceData().getInstanceCollectionForEntityNamed("thing"));
        Assertions.assertEquals(thing, erm.getInstanceData().getInstanceCollectionForEntityNamed("thing"));
        Assertions.assertTrue(erm.hasEntityWithPluralNamed("things"));
        Assertions.assertNotNull(erm.getSchema().getEntityDefinitionWithPluralNamed("things"));
        Assertions.assertEquals(thing.definition(), erm.getSchema().getEntityDefinitionWithPluralNamed("things"));
        Assertions.assertEquals(thing.definition(), erm.getSchema().getDefinitionWithSingularOrPluralNamed("thing"));
        Assertions.assertEquals(thing.definition(), erm.getSchema().getDefinitionWithSingularOrPluralNamed("things"));
        Assertions.assertEquals(1, erm.getEntityNames().size());

        Assertions.assertTrue(erm.getEntityNames().contains("thing"));
    }

    @Test
    public void canFindAThingInAModel() {

        EntityRelModel erm = new EntityRelModel();
        EntityDefinition defn = erm.createEntityDefinition("thing", "things");
        defn.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));

        EntityInstanceCollection thing = erm.getInstanceData().getInstanceCollectionForEntityNamed("thing");

        final EntityInstance instance = thing.addInstance(new EntityInstance(thing.definition()));

        final String thingGUID1 = instance.getPrimaryKeyValue();
        Assertions.assertNotNull(
                erm.getInstanceData().findEntityInstanceByGUID(thingGUID1));
        final String thingGUID = instance.getPrimaryKeyValue();
        Assertions.assertEquals(instance,
                erm.getInstanceData().findEntityInstanceByGUID(thingGUID));
    }

    @Test
    public void canDeleteAThingInAModel() {

        EntityRelModel erm = new EntityRelModel();
        erm.createEntityDefinition("thing", "things");
        EntityInstanceCollection thing = erm.getInstanceData().getInstanceCollectionForEntityNamed("thing");

        final EntityInstance instance = thing.addInstance(new EntityInstance(thing.definition()));
        erm.getInstanceData().deleteEntityInstance(instance);

        final String thingGUID = instance.getPrimaryKeyValue();
        Assertions.assertNull(
                erm.getInstanceData().findEntityInstanceByGUID(thingGUID));
        final String entityName = instance.getEntity().getName();
        Assertions.assertEquals(0,
                erm.getInstanceData().getInstanceCollectionForEntityNamed(entityName).countInstances());
    }

    @Test
    public void canClearAllDataInAModel() {

        EntityRelModel erm = new EntityRelModel();
        erm.createEntityDefinition("thing", "things");
        EntityInstanceCollection thing = erm.getInstanceData().getInstanceCollectionForEntityNamed("thing");
        erm.createEntityDefinition("thing2", "thing2");
        EntityInstanceCollection thing2 = erm.getInstanceData().getInstanceCollectionForEntityNamed("thing2");

        thing.addInstance(new EntityInstance(thing.definition()));
        thing.addInstance(new EntityInstance(thing.definition()));
        thing2.addInstance(new EntityInstance(thing2.definition()));
        thing2.addInstance(new EntityInstance(thing2.definition()));
        thing2.addInstance(new EntityInstance(thing2.definition()));

        Assertions.assertEquals(2, erm.getInstanceData().getAllInstanceCollections().size());
        Assertions.assertEquals(2,
                erm.getInstanceData().getInstanceCollectionForEntityNamed("thing").getInstances().size());
        Assertions.assertEquals(3,
                erm.getInstanceData().getInstanceCollectionForEntityNamed("thing2").getInstances().size());

        erm.getInstanceData().clearAllData();

        Assertions.assertEquals(2, erm.getInstanceData().getAllInstanceCollections().size());
        Assertions.assertEquals(0,
                erm.getInstanceData().getInstanceCollectionForEntityNamed("thing").getInstances().size());
        Assertions.assertEquals(0,
                erm.getInstanceData().getInstanceCollectionForEntityNamed("thing2").getInstances().size());
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
        final EntityInstanceCollection thing1 = erm.getInstanceData().getInstanceCollectionForEntityNamed("thing1");
        EntityDefinition t2d = erm.createEntityDefinition("thing2", "thing2");
        final EntityInstanceCollection thing2 = erm.getInstanceData().getInstanceCollectionForEntityNamed("thing1");
        erm.createRelationshipDefinition(t1d, t2d, "things", Cardinality.ONE_TO_MANY());

        Assertions.assertNotNull(erm.getRelationshipDefinitions());
        Assertions.assertEquals(1, erm.getRelationshipDefinitions().size());
        Assertions.assertTrue(erm.hasRelationshipNamed("things"));
    }

    @Test
    public void canFindAReversedRelationship() {

        EntityRelModel erm = new EntityRelModel();
        final EntityDefinition td = erm.createEntityDefinition("thing", "things");
        EntityInstanceCollection thing = erm.getInstanceData().getInstanceCollectionForEntityNamed("thing");
        final EntityDefinition dpd = erm.createEntityDefinition("dependantthing", "dthings");
        EntityInstanceCollection dependent = erm.getInstanceData().getInstanceCollectionForEntityNamed("dependantthing");

        erm.createRelationshipDefinition(td, dpd, "things", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_ONE(), "idiewithoutyou").
                getReversedRelationship().
                setOptionality(Optionality.MANDATORY_RELATIONSHIP);

        Assertions.assertTrue(erm.hasRelationshipNamed("idiewithoutyou"));
    }

    @Test
    public void canDeleteAThingWithRelationships() {

        EntityRelModel erm = new EntityRelModel();
        final EntityDefinition thingdefn = erm.createEntityDefinition("thing", "things");
        EntityInstanceCollection thing = erm.getInstanceData().getInstanceCollectionForEntityNamed("thing");
        final EntityDefinition depdefn = erm.createEntityDefinition("dependantthing", "dthings");
        EntityInstanceCollection dependent = erm.getInstanceData().getInstanceCollectionForEntityNamed("dependantthing");

        erm.createRelationshipDefinition(thingdefn, depdefn, "things", Cardinality.ONE_TO_MANY())
            .whenReversed(Cardinality.ONE_TO_ONE(),"idiewithoutyou").
            getReversedRelationship().
            setOptionality(Optionality.MANDATORY_RELATIONSHIP);

        final EntityInstance mainThing = thing.addInstance(new EntityInstance(thing.definition()));
        mainThing.getRelationships().connect("things",
                dependent.addInstance(new EntityInstance(dependent.definition())));
        mainThing.getRelationships().connect("things",
                dependent.addInstance(new EntityInstance(dependent.definition())));
        mainThing.getRelationships().connect("things",
                dependent.addInstance(new EntityInstance(dependent.definition())));

        Assertions.assertEquals(2, erm.getInstanceData().getAllInstanceCollections().size());
        Assertions.assertEquals(3, mainThing.getRelationships().
                                            getConnectedItems("things").size());
        Assertions.assertEquals(3, erm.getInstanceData().getInstanceCollectionForEntityNamed("dependantthing").
                                            getInstances().size());

        erm.getInstanceData().deleteEntityInstance(mainThing);

        final String thingGUID = mainThing.getPrimaryKeyValue();
        Assertions.assertNull(
                erm.getInstanceData().findEntityInstanceByGUID(thingGUID));
        Assertions.assertEquals(0,
                erm.getInstanceData().getInstanceCollectionForEntityNamed("thing").countInstances());
        Assertions.assertEquals(0,
                erm.getInstanceData().getInstanceCollectionForEntityNamed("dependantthing").countInstances());

    }

    @Test
    public void canFunctionWithoutADataGenerator(){
        EntityRelModel erm = new EntityRelModel();
        Assertions.assertEquals(0, erm.getInstanceData().getAllInstanceCollections().size());
    }

    @Test
    public void canSetAndUseADataGenerator(){
        EntityRelModel erm = new EntityRelModel();

        DataPopulator dataPopulator = new DataPopulator() {
            @Override
            public void populate(final ERSchema schema, final ERInstanceData database) {

                // Normally a populate would deal with the database instance, not the entitiesd
                schema.defineEntity("thing", "things", -1);
                database.createInstanceCollectionFrom(schema);
            }
        };

        dataPopulator.populate(erm.getSchema(), erm.getInstanceData());

        Assertions.assertEquals(1, erm.getInstanceData().getAllInstanceCollections().size());
    }
}
