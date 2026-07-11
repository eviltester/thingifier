package uk.co.compendiumdev.thingifier.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public class MultipleEntityInstancesDatabasesTest {

    @Test
    public void byDefaultASingleDatabaseModelIsCreatedAndUsed() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition thingDefn = defineThing(erm);

        ThingStore repository = erm.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
        EntityInstance thing = create(repository, thingDefn);

        EntityInstance foundThing =
                repository.entityQueries().findByGuid(thing.getPrimaryKeyValue());

        Assertions.assertEquals("Thing 1", foundThing.getFieldValue("Title").asString());
        Assertions.assertEquals(foundThing, thing);
    }

    @Test
    public void weCanAddANewNamedDatabaseToTheErm() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition thingDefn = defineThing(erm);

        erm.createInstanceDatabase("other_things");

        ThingStore otherRepository = erm.getStore("other_things");
        EntityInstance thing = create(otherRepository, thingDefn);

        EntityInstance foundThing =
                otherRepository.entityQueries().findByGuid(thing.getPrimaryKeyValue());

        Assertions.assertEquals("Thing 1", foundThing.getFieldValue("Title").asString());
        Assertions.assertEquals(foundThing, thing);
        Assertions.assertEquals(
                0,
                erm.getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                        .entityQueries()
                        .count(thingDefn));
    }

    @Test
    public void cannotCreateDuplicateNamedDatabases() {
        EntityRelModel erm = new EntityRelModel();
        defineThing(erm);

        erm.createInstanceDatabase("other_things");

        Exception e =
                Assertions.assertThrows(
                        IllegalStateException.class,
                        () -> erm.createInstanceDatabase("other_things"));

        Assertions.assertEquals(
                "ERM Database Already Exists with name other_things", e.getMessage());
    }

    @Test
    public void canDeleteNamedDatabaseFromTheErm() {
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition thingDefn = defineThing(erm);

        erm.createInstanceDatabase("other_things");

        ThingStore otherRepository = erm.getStore("other_things");
        EntityInstance thing = create(otherRepository, thingDefn);

        Assertions.assertNotNull(
                otherRepository.entityQueries().findByGuid(thing.getPrimaryKeyValue()));

        erm.deleteInstanceDatabase("other_things");

        Assertions.assertNull(erm.getStore("other_things"));
    }

    @Test
    public void cannotDeleteDefaultDatabaseFromTheErm() {
        EntityRelModel erm = new EntityRelModel();
        defineThing(erm);

        Exception e =
                Assertions.assertThrows(
                        IllegalStateException.class,
                        () -> erm.deleteInstanceDatabase(EntityRelModel.DEFAULT_DATABASE_NAME));

        Assertions.assertEquals("Cannot delete default database", e.getMessage());
        Assertions.assertNotNull(erm.getStore(EntityRelModel.DEFAULT_DATABASE_NAME));
    }

    private EntityDefinition defineThing(final EntityRelModel erm) {
        EntityDefinition thingDefn = erm.createEntityDefinition("thing", "things");
        thingDefn.addField(Field.is("Title", FieldType.STRING));
        thingDefn.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        return thingDefn;
    }

    private EntityInstance create(final ThingStore repository, final EntityDefinition thingDefn) {
        return repository
                .entities()
                .create(EntityInstanceDraft.forEntity(thingDefn).withField("Title", "Thing 1"));
    }
}
