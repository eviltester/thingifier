package uk.co.compendiumdev.thingifier.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;
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
}
