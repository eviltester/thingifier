package uk.co.compendiumdev.thingifier.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;

public class MultipleEntityInstancesDatabasesTest {


    @Test
    public void byDefaultASingleDatabaseModelIsCreatedAndUsed(){

        EntityRelModel erm = new EntityRelModel();
        EntityDefinition thingDefn = erm.createEntityDefinition("thing", "things");
        thingDefn.addField(Field.is("Title", FieldType.STRING));
        thingDefn.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));

        EntityInstance thing1 = new EntityInstance(thingDefn);
        thing1.addAutoGUIDstoInstance();
        thing1.setValue("Title", "Thing 1");

        EntityInstanceCollection thing = erm.getInstanceData().getInstanceCollectionForEntityNamed("thing");
        thing.addInstance(thing1);

        final String thingGUID = thing1.getPrimaryKeyValue();
        EntityInstance foundThing = erm.getInstanceData().findEntityInstanceByGUID(thingGUID);

        Assertions.assertEquals("Thing 1", foundThing.getFieldValue("Title").asString());
        Assertions.assertEquals(foundThing, thing1);
    }

    @Test
    public void weCanAddANewNamedDatabaseToTheErm(){

        EntityRelModel erm = new EntityRelModel();
        EntityDefinition thingDefn = erm.createEntityDefinition("thing", "things");
        thingDefn.addField(Field.is("Title", FieldType.STRING));
        thingDefn.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));

        erm.createInstanceDatabase("other_things");

        EntityInstance thing1 = new EntityInstance(thingDefn);
        thing1.addAutoGUIDstoInstance();
        thing1.setValue("Title", "Thing 1");

        EntityInstanceCollection thing = erm.getInstanceData("other_things").getInstanceCollectionForEntityNamed("thing");
        thing.addInstance(thing1);

        String guid = thing1.getPrimaryKeyValue();
        EntityInstance foundThing = erm.getInstanceData("other_things").findEntityInstanceByGUID(guid);

        Assertions.assertEquals("Thing 1", foundThing.getFieldValue("Title").asString());
        Assertions.assertEquals(foundThing, thing1);

        Assertions.assertEquals(0,  erm.getInstanceData().getInstanceCollectionForEntityNamed("thing").countInstances());
    }

    @Test
    public void cannotCreateDuplicateNamedDatabases(){

        EntityRelModel erm = new EntityRelModel();
        EntityDefinition thingDefn = erm.createEntityDefinition("thing", "things");
        thingDefn.addField(Field.is("Title", FieldType.STRING));

        erm.createInstanceDatabase("other_things");

        Exception e = Assertions.assertThrows(IllegalStateException.class, ()->{
            erm.createInstanceDatabase("other_things");
        });

        Assertions.assertEquals("ERM Database Already Exists with name other_things",  e.getMessage());
    }

    @Test
    public void canDeleteNamedDatabaseFromTheErm(){

        // Given an ERM with a Model
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition thingDefn = erm.createEntityDefinition("thing", "things");
        thingDefn.addField(Field.is("Title", FieldType.STRING));
        thingDefn.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));

        erm.createInstanceDatabase("other_things");

        EntityInstance thing1 = new EntityInstance(thingDefn);
        thing1.addAutoGUIDstoInstance();
        thing1.setValue("Title", "Thing 1");

        EntityInstanceCollection thing = erm.getInstanceData("other_things").getInstanceCollectionForEntityNamed("thing");
        thing.addInstance(thing1);

        String guid = thing1.getPrimaryKeyValue();
        EntityInstance foundThing = erm.getInstanceData("other_things").findEntityInstanceByGUID(guid);

        Assertions.assertEquals("Thing 1", foundThing.getFieldValue("Title").asString());

        // WHEN we delete the database

        erm.deleteInstanceDatabase("other_things");

        // There is no such database
        ERInstanceData noDatabase = erm.getInstanceData("other_things");
        Assertions.assertNull(noDatabase);
    }

    @Test
    public void cannotDeletDefaultDatabaseFromTheErm(){

        // Given an ERM with a Model
        EntityRelModel erm = new EntityRelModel();
        EntityDefinition thingDefn = erm.createEntityDefinition("thing", "things");
        thingDefn.addField(Field.is("Title", FieldType.STRING));


        // WE cannot delete default database
        Exception e = Assertions.assertThrows(IllegalStateException.class, ()->{
            erm.deleteInstanceDatabase(EntityRelModel.DEFAULT_DATABASE_NAME);
        });

        Assertions.assertEquals("Cannot delete default database", e.getMessage());
        // There is no such database
        ERInstanceData isDatabase = erm.getInstanceData(EntityRelModel.DEFAULT_DATABASE_NAME);
        Assertions.assertNotNull(isDatabase);
    }
}
