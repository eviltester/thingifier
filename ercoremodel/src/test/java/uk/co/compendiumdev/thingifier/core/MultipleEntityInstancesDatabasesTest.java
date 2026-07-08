package uk.co.compendiumdev.thingifier.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;

public class MultipleEntityInstancesDatabasesTest {

    @Test
    public void byDefaultASingleDatabaseModelIsCreatedAndUsed() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition thingDefn = defineThing(erm);

        ThingRepository repository = erm.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME);
        EntityInstance thing = repository.addInstance(
                new EntityInstance(thingDefn).setValue("Title", "Thing 1"));

        EntityInstance foundThing = repository.findEntityInstanceByGUID(thing.getPrimaryKeyValue());

        Assertions.assertEquals("Thing 1", foundThing.getFieldValue("Title").asString());
        Assertions.assertEquals(foundThing, thing);
    }

    @Test
    public void weCanAddANewNamedDatabaseToTheErm() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition thingDefn = defineThing(erm);

        erm.createInstanceDatabase("other_things");

        ThingRepository otherRepository = erm.getRepository("other_things");
        EntityInstance thing = otherRepository.addInstance(
                new EntityInstance(thingDefn).setValue("Title", "Thing 1"));

        EntityInstance foundThing = otherRepository.findEntityInstanceByGUID(thing.getPrimaryKeyValue());

        Assertions.assertEquals("Thing 1", foundThing.getFieldValue("Title").asString());
        Assertions.assertEquals(foundThing, thing);
        Assertions.assertEquals(0,
                erm.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME).countInstances(thingDefn));
    }

    @Test
    public void cannotCreateDuplicateNamedDatabases() {
        EntityRelModel erm = new EntityRelModel();
        defineThing(erm);

        erm.createInstanceDatabase("other_things");

        Exception e = Assertions.assertThrows(
                IllegalStateException.class,
                () -> erm.createInstanceDatabase("other_things"));

        Assertions.assertEquals("ERM Database Already Exists with name other_things", e.getMessage());
    }

    @Test
    public void canDeleteNamedDatabaseFromTheErm() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition thingDefn = defineThing(erm);

        erm.createInstanceDatabase("other_things");

        ThingRepository otherRepository = erm.getRepository("other_things");
        EntityInstance thing = otherRepository.addInstance(
                new EntityInstance(thingDefn).setValue("Title", "Thing 1"));

        Assertions.assertNotNull(
                otherRepository.findEntityInstanceByGUID(thing.getPrimaryKeyValue()));

        erm.deleteInstanceDatabase("other_things");

        Assertions.assertNull(erm.getRepository("other_things"));
    }

    @Test
    public void cannotDeleteDefaultDatabaseFromTheErm() {
        EntityRelModel erm = new EntityRelModel();
        defineThing(erm);

        Exception e = Assertions.assertThrows(
                IllegalStateException.class,
                () -> erm.deleteInstanceDatabase(EntityRelModel.DEFAULT_DATABASE_NAME));

        Assertions.assertEquals("Cannot delete default database", e.getMessage());
        Assertions.assertNotNull(erm.getRepository(EntityRelModel.DEFAULT_DATABASE_NAME));
    }

    private EntityDefinition defineThing(final EntityRelModel erm) {
        EntityDefinition thingDefn = erm.createEntityDefinition("thing", "things");
        thingDefn.addField(Field.is("Title", FieldType.STRING));
        thingDefn.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        return thingDefn;
    }
}
