package uk.co.compendiumdev.thingifier.core.domain.instances.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.inmemory.InMemoryThingStore;

public class AutoIncrementIdFieldTest {

    @Test
    public void byDefaultAnIDFieldIsOneWhenInstantiated() {

        ERSchema schema = new ERSchema();
        EntityDefinition entity = schema.defineEntity("thing", "things", -1);
        entity.addFields(Field.is("id", FieldType.AUTO_INCREMENT));
        ThingStore repository = new InMemoryThingStore("test");
        repository.administration().initializeFrom(schema);
        EntityInstance instance =
                repository.entities().create(EntityInstanceDraft.forEntity(entity));

        Assertions.assertEquals("1", instance.getFieldValue("id").asString());
    }

    @Test
    public void idsShouldAutoIncrementWhenInstancesCreated() {

        ERSchema schema = new ERSchema();
        EntityDefinition entity = schema.defineEntity("thing", "things", -1);
        entity.addFields(Field.is("id", FieldType.AUTO_INCREMENT));
        ThingStore repository = new InMemoryThingStore("test");
        repository.administration().initializeFrom(schema);

        EntityInstance instance =
                repository.entities().create(EntityInstanceDraft.forEntity(entity));
        Assertions.assertEquals("1", instance.getFieldValue("id").asString());

        EntityInstance instance2 =
                repository.entities().create(EntityInstanceDraft.forEntity(entity));
        Assertions.assertEquals("2", instance2.getFieldValue("id").asString());
    }

    @Test
    public void notAllowedToAmendIdOfInstance() {

        ERSchema schema = new ERSchema();
        EntityDefinition entity = schema.defineEntity("thing", "things", -1);
        entity.addFields(Field.is("id", FieldType.AUTO_INCREMENT));
        ThingStore repository = new InMemoryThingStore("test");
        repository.administration().initializeFrom(schema);

        EntityInstance instance =
                repository.entities().create(EntityInstanceDraft.forEntity(entity));
        Assertions.assertEquals("1", instance.getFieldValue("id").asString());
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    repository
                            .entities()
                            .patch(
                                    instance,
                                    EntityInstanceDraft.forEntity(entity).withField("id", "2"));
                });
    }
}
