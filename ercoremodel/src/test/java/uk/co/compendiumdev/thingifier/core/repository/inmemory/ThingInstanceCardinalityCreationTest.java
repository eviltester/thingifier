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
import uk.co.compendiumdev.thingifier.core.repository.MutableEntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreWriteException;

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

        ThingStoreWriteException exception =
                Assertions.assertThrows(
                        ThingStoreWriteException.class,
                        () -> {
                            instances.addInstance(instance(entityDefn, "test4"));
                        });

        Assertions.assertEquals(
                ThingStoreWriteException.Reason.MAX_INSTANCE_LIMIT_REACHED, exception.reason());
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

        ThingStoreWriteException exception =
                Assertions.assertThrows(
                        ThingStoreWriteException.class,
                        () -> {
                            instances.addInstance(instance(entityDefn, "test2"));
                        });

        Assertions.assertEquals(
                ThingStoreWriteException.Reason.MAX_INSTANCE_LIMIT_REACHED, exception.reason());
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

        ThingStoreWriteException exception =
                Assertions.assertThrows(
                        ThingStoreWriteException.class,
                        () -> {
                            instances.addInstances(toAdd);
                        });

        Assertions.assertEquals(
                ThingStoreWriteException.Reason.MAX_INSTANCE_LIMIT_WOULD_BE_EXCEEDED,
                exception.reason());
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
        return MutableEntityInstance.snapshotFromDraft(
                EntityInstanceDraft.forEntity(entityDefn).withField("Title", title));
    }
}
