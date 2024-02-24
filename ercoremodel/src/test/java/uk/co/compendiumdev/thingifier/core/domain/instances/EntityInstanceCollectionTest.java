package uk.co.compendiumdev.thingifier.core.domain.instances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

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
        instance1.addAutoGUIDstoInstance();
        instance1.addAutoIncrementIdsToInstance();

        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
            collection.addInstance(instance1);
        });

        Assertions.assertTrue(exception.getMessage().contains("Cannot add instance, primary key field pk not set"));
    }

    @Test
    public void cannotCreateInstanceWithDuplicatePrimaryKey() {

        EntityInstanceCollection collection = new EntityInstanceCollection(entityDefn);

        EntityInstance instance1 = new EntityInstance(entityDefn);
        instance1.addAutoGUIDstoInstance();
        instance1.addAutoIncrementIdsToInstance();
        instance1.setValue("pk", "instance1");

        collection.addInstance(instance1);

        EntityInstance instance2 = new EntityInstance(entityDefn);
        instance2.addAutoIncrementIdsToInstance();
        instance2.setValue("pk", instance1.getPrimaryKeyValue());

        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
            collection.addInstance(instance2);
        });

        Assertions.assertTrue(exception.getMessage().contains("another instance with primary key value exists"));
    }

    // TODO: consider adding the AutoGUIDs and AutoIncrement values to an instance when it is added to the collection if they do not exist
}
