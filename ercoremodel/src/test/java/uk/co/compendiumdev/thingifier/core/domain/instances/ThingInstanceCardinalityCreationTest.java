package uk.co.compendiumdev.thingifier.core.domain.instances;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

import java.util.ArrayList;
import java.util.List;

public class ThingInstanceCardinalityCreationTest {

    @Test
    public void canCreateAndAddInstancesWithinCardinalityLimit() {

        EntityDefinition entityDefn = new EntityDefinition("Test", "Tests", 3);
        entityDefn.addField(Field.is("Title", FieldType.STRING));
        EntityInstanceCollection instances = new EntityInstanceCollection(entityDefn, new ArrayList<>());

        instances.addInstance(new EntityInstance(entityDefn).setValue("Title", "test1"));

        instances.addInstance(new EntityInstance(entityDefn).setValue("Title", "test2"));


        instances.addInstance(new EntityInstance(entityDefn).setValue("Title", "test3"));

        Assertions.assertEquals(3, instances.countInstances());
    }

    @Test
    public void cannotAddInstancesBeyondCardinalityLimit() {

        EntityDefinition entityDefn = new EntityDefinition("Test", "Tests", 3);
        entityDefn.addField(Field.is("Title", FieldType.STRING));
        EntityInstanceCollection instances = new EntityInstanceCollection(entityDefn, new ArrayList<>());

        instances.addInstance(new EntityInstance(entityDefn).setValue("Title", "test1"));

        instances.addInstance(new EntityInstance(entityDefn).setValue("Title", "test2"));


        instances.addInstance(new EntityInstance(entityDefn).setValue("Title", "test3"));

        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
                    instances.addInstance(new EntityInstance(entityDefn).setValue("Title", "test4"));
                });

        Assertions.assertEquals("ERROR: Cannot add instance, maximum limit of 3 reached", exception.getMessage());
        Assertions.assertEquals(3, instances.countInstances());
    }

    @Test
    public void cannotAddInstancesBeyondCardinalityLimitOfOne() {

        EntityDefinition entityDefn = new EntityDefinition("Test", "Tests", 1);
        entityDefn.addField(Field.is("Title", FieldType.STRING));
        EntityInstanceCollection instances = new EntityInstanceCollection(entityDefn, new ArrayList<>());

        instances.addInstance(new EntityInstance(entityDefn).setValue("Title", "test1"));

        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
            instances.addInstance(new EntityInstance(entityDefn).setValue("Title", "test2"));
        });

        Assertions.assertEquals("ERROR: Cannot add instance, maximum limit of 1 reached", exception.getMessage());
        Assertions.assertEquals(1, instances.countInstances());
    }

    @Test
    public void cannotAddMultiplesInstancesBeyondCardinality() {

        EntityDefinition entityDefn = new EntityDefinition("Test", "Tests", 3);
        entityDefn.addField(Field.is("Title", FieldType.STRING));
        EntityInstanceCollection instances = new EntityInstanceCollection(entityDefn, new ArrayList<>());

        instances.addInstance(new EntityInstance(entityDefn).setValue("Title", "test1"));

        List<EntityInstance> toAdd = new ArrayList<>();

        toAdd.add(new EntityInstance(entityDefn).setValue("Title", "test2"));
        toAdd.add(new EntityInstance(entityDefn).setValue("Title", "test3"));
        toAdd.add(new EntityInstance(entityDefn).setValue("Title", "test4"));

        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
            instances.addInstances(toAdd);
        });

        Assertions.assertEquals("ERROR: Cannot add instances, would exceed maximum limit of 3", exception.getMessage());
        Assertions.assertEquals(1, instances.countInstances());
    }

    @Test
    public void canAddMultiplesInstancesWithinCardinality() {

        EntityDefinition entityDefn = new EntityDefinition("Test", "Tests", 3);
        entityDefn.addField(Field.is("Title", FieldType.STRING));
        EntityInstanceCollection instances = new EntityInstanceCollection(entityDefn, new ArrayList<>());

        List<EntityInstance> toAdd = new ArrayList<>();

        toAdd.add(new EntityInstance(entityDefn).setValue("Title", "test1"));
        toAdd.add(new EntityInstance(entityDefn).setValue("Title", "test2"));
        toAdd.add(new EntityInstance(entityDefn).setValue("Title", "test3"));

        instances.addInstances(toAdd);

        Assertions.assertEquals(3, instances.countInstances());
    }


    @Test
    public void canAddManyWhenNoCardinalityLimit() {

        EntityDefinition entityDefn = new EntityDefinition("Test", "Tests", -1);
        entityDefn.addField(Field.is("Title", FieldType.STRING));
        EntityInstanceCollection instances = new EntityInstanceCollection(entityDefn, new ArrayList<>());

        for( int instanceNum = 1; instanceNum <= 100; instanceNum++){
            instances.addInstance(new EntityInstance(entityDefn).setValue("Title", "test" + instanceNum));
        }

        Assertions.assertEquals(100, instances.countInstances());
    }

    @Test
    public void canAddManyWhenDefaultNoCardinalityLimit() {

        EntityDefinition entityDefn = new EntityDefinition("Test", "Tests");
        entityDefn.addField(Field.is("Title", FieldType.STRING));
        EntityInstanceCollection instances = new EntityInstanceCollection(entityDefn, new ArrayList<>());

        for( int instanceNum = 1; instanceNum <= 100; instanceNum++){
            instances.addInstance(new EntityInstance(entityDefn).setValue("Title", "test" + instanceNum));
        }

        Assertions.assertEquals(100, instances.countInstances());
    }
}
