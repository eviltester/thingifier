package uk.co.compendiumdev.thingifier.core.repository.inmemory;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

public class ThingInstanceCardinalityCreationTest {

    @Test
    public void canCreateAndAddInstancesWithinCardinalityLimit() {

        EntityDefinition entityDefn = new EntityDefinition("Test", "Tests", 3);
        entityDefn.addField(Field.is("Title", FieldType.STRING));
        InMemoryEntityInstanceCollection instances =
                new InMemoryEntityInstanceCollection(entityDefn, new ArrayList<>());

        instances.addInstance(instance(entityDefn, "test1"));

        instances.addInstance(instance(entityDefn, "test2"));

        instances.addInstance(instance(entityDefn, "test3"));

        Assertions.assertEquals(3, instances.countInstances());
    }

    @Test
    public void cannotAddInstancesBeyondCardinalityLimit() {

        EntityDefinition entityDefn = new EntityDefinition("Test", "Tests", 3);
        entityDefn.addField(Field.is("Title", FieldType.STRING));
        InMemoryEntityInstanceCollection instances =
                new InMemoryEntityInstanceCollection(entityDefn, new ArrayList<>());

        instances.addInstance(instance(entityDefn, "test1"));

        instances.addInstance(instance(entityDefn, "test2"));

        instances.addInstance(instance(entityDefn, "test3"));

        Exception exception =
                Assertions.assertThrows(
                        RuntimeException.class,
                        () -> {
                            instances.addInstance(instance(entityDefn, "test4"));
                        });

        Assertions.assertEquals(
                "ERROR: Cannot add instance, maximum limit of 3 reached", exception.getMessage());
        Assertions.assertEquals(3, instances.countInstances());
    }

    @Test
    public void cannotAddInstancesBeyondCardinalityLimitOfOne() {

        EntityDefinition entityDefn = new EntityDefinition("Test", "Tests", 1);
        entityDefn.addField(Field.is("Title", FieldType.STRING));
        InMemoryEntityInstanceCollection instances =
                new InMemoryEntityInstanceCollection(entityDefn, new ArrayList<>());

        instances.addInstance(instance(entityDefn, "test1"));

        Exception exception =
                Assertions.assertThrows(
                        RuntimeException.class,
                        () -> {
                            instances.addInstance(instance(entityDefn, "test2"));
                        });

        Assertions.assertEquals(
                "ERROR: Cannot add instance, maximum limit of 1 reached", exception.getMessage());
        Assertions.assertEquals(1, instances.countInstances());
    }

    @Test
    public void cannotAddMultiplesInstancesBeyondCardinality() {

        EntityDefinition entityDefn = new EntityDefinition("Test", "Tests", 3);
        entityDefn.addField(Field.is("Title", FieldType.STRING));
        InMemoryEntityInstanceCollection instances =
                new InMemoryEntityInstanceCollection(entityDefn, new ArrayList<>());

        instances.addInstance(instance(entityDefn, "test1"));

        List<EntityInstance> toAdd = new ArrayList<>();

        toAdd.add(instance(entityDefn, "test2"));
        toAdd.add(instance(entityDefn, "test3"));
        toAdd.add(instance(entityDefn, "test4"));

        Exception exception =
                Assertions.assertThrows(
                        RuntimeException.class,
                        () -> {
                            instances.addInstances(toAdd);
                        });

        Assertions.assertEquals(
                "ERROR: Cannot add instances, would exceed maximum limit of 3",
                exception.getMessage());
        Assertions.assertEquals(1, instances.countInstances());
    }

    @Test
    public void canAddMultiplesInstancesWithinCardinality() {

        EntityDefinition entityDefn = new EntityDefinition("Test", "Tests", 3);
        entityDefn.addField(Field.is("Title", FieldType.STRING));
        InMemoryEntityInstanceCollection instances =
                new InMemoryEntityInstanceCollection(entityDefn, new ArrayList<>());

        List<EntityInstance> toAdd = new ArrayList<>();

        toAdd.add(instance(entityDefn, "test1"));
        toAdd.add(instance(entityDefn, "test2"));
        toAdd.add(instance(entityDefn, "test3"));

        instances.addInstances(toAdd);

        Assertions.assertEquals(3, instances.countInstances());
    }

    @Test
    public void canAddManyWhenNoCardinalityLimit() {

        EntityDefinition entityDefn = new EntityDefinition("Test", "Tests", -1);
        entityDefn.addField(Field.is("Title", FieldType.STRING));
        InMemoryEntityInstanceCollection instances =
                new InMemoryEntityInstanceCollection(entityDefn, new ArrayList<>());

        for (int instanceNum = 1; instanceNum <= 100; instanceNum++) {
            instances.addInstance(instance(entityDefn, "test" + instanceNum));
        }

        Assertions.assertEquals(100, instances.countInstances());
    }

    @Test
    public void canAddManyWhenDefaultNoCardinalityLimit() {

        EntityDefinition entityDefn = new EntityDefinition("Test", "Tests");
        entityDefn.addField(Field.is("Title", FieldType.STRING));
        InMemoryEntityInstanceCollection instances =
                new InMemoryEntityInstanceCollection(entityDefn, new ArrayList<>());

        for (int instanceNum = 1; instanceNum <= 100; instanceNum++) {
            instances.addInstance(instance(entityDefn, "test" + instanceNum));
        }

        Assertions.assertEquals(100, instances.countInstances());
    }

    private EntityInstance instance(final EntityDefinition entityDefn, final String title) {
        return EntityInstance.fromDraft(
                EntityInstanceDraft.forEntity(entityDefn).withField("Title", title));
    }
}
