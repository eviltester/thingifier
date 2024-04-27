package uk.co.compendiumdev.thingifier.core.domain.instances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

import java.util.UUID;

public class EntityInstanceCollectionTest {

    EntityDefinition entityDefn;

    @BeforeEach
    public void createEntity(){

        entityDefn = new EntityDefinition("Entity", "Entities");

        entityDefn.addAsPrimaryKeyField(Field.is("pk", FieldType.STRING));
        entityDefn.addField(Field.is("value", FieldType.AUTO_INCREMENT));
    }

    @Test
    public void cannotCreateInstanceWithoutPrimaryKeySet() {

        EntityInstanceCollection collection = new EntityInstanceCollection(entityDefn);

        EntityInstance instance1 = new EntityInstance(entityDefn);

        Exception exception = Assertions.assertThrows(RuntimeException.class,
                () -> collection.addInstance(instance1)
        );

        Assertions.assertTrue(exception.getMessage().contains("Cannot add instance, primary key field pk not set"));
    }

    @Test
    public void cannotCreateInstanceWithDuplicatePrimaryKey() {

        EntityInstanceCollection collection = new EntityInstanceCollection(entityDefn);

        EntityInstance instance1 = new EntityInstance(entityDefn);
        instance1.setValue("pk", "instance1");

        collection.addInstance(instance1);

        EntityInstance instance2 = new EntityInstance(entityDefn);
        instance2.setValue("pk", instance1.getPrimaryKeyValue());

        Exception exception = Assertions.assertThrows(RuntimeException.class,
                () -> collection.addInstance(instance2)
        );

        Assertions.assertTrue(exception.getMessage().contains("another instance with primary key value exists"));
    }

    @Test
    public void canAutoIncrementOnAdd(){
        EntityDefinition defn = new EntityDefinition("entity", "entities").
                addFields(Field.is("id", FieldType.AUTO_INCREMENT),
                        Field.is("name", FieldType.STRING));

        EntityInstanceCollection col = new EntityInstanceCollection(defn);

        EntityInstance instance1 = new EntityInstance(defn).setValue("name", "bob");
        EntityInstance instance2 = new EntityInstance(defn).setValue("name", "connie");

        col.addInstance(instance1);
        col.addInstance(instance2);

        Assertions.assertEquals(1, instance1.getFieldValue("id").asInteger());
        Assertions.assertEquals(2, instance2.getFieldValue("id").asInteger());
    }

    @Test
    public void canAutoGuidOnAdd(){
        EntityDefinition defn = new EntityDefinition("entity", "entities").
                addFields(Field.is("id", FieldType.AUTO_GUID),
                        Field.is("name", FieldType.STRING));

        EntityInstanceCollection col = new EntityInstanceCollection(defn);

        EntityInstance instance1 = new EntityInstance(defn).setValue("name", "bob");
        EntityInstance instance2 = new EntityInstance(defn).setValue("name", "connie");

        col.addInstance(instance1);
        col.addInstance(instance2);

        Assertions.assertEquals(instance1.getFieldValue("id").asString(), UUID.fromString(instance1.getFieldValue("id").asString()).toString() );
        Assertions.assertEquals(instance2.getFieldValue("id").asString(), UUID.fromString(instance2.getFieldValue("id").asString()).toString());
    }

    @Test
    public void canAutoGuidAndIdOnAdd(){
        EntityDefinition defn = new EntityDefinition("entity", "entities").
                addFields(
                        Field.is("guid", FieldType.AUTO_GUID),
                        Field.is("id", FieldType.AUTO_INCREMENT),
                        Field.is("name", FieldType.STRING));

        EntityInstanceCollection col = new EntityInstanceCollection(defn);

        EntityInstance instance1 = new EntityInstance(defn).setValue("name", "bob");
        EntityInstance instance2 = new EntityInstance(defn).setValue("name", "connie");

        col.addInstance(instance1);
        col.addInstance(instance2);

        Assertions.assertNotEquals(instance1.getFieldValue("guid").asString(), instance2.getFieldValue("guid").asString());
        Assertions.assertEquals(instance1.getFieldValue("guid").asString(), UUID.fromString(instance1.getFieldValue("guid").asString()).toString());
        Assertions.assertEquals(instance2.getFieldValue("guid").asString(), UUID.fromString(instance2.getFieldValue("guid").asString()).toString());
        Assertions.assertNotEquals(instance1.getFieldValue("id").asString(), instance2.getFieldValue("id").asString());
        Assertions.assertEquals(1, instance1.getFieldValue("id").asInteger());
        Assertions.assertEquals(2, instance2.getFieldValue("id").asInteger());
    }
}
