package uk.co.compendiumdev.thingifier.core.domain.instances.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingRepository;
import uk.co.compendiumdev.thingifier.core.repository.inmemory.InMemoryThingRepository;

public class AutoGuidTest {

    EntityDefinition entityTestSession;
    ThingRepository repository;

    @BeforeEach
    public void createEntity() {

        ERSchema schema = new ERSchema();
        entityTestSession = schema.defineEntity("Test Session", "Test Sessions", -1);
        entityTestSession.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        entityTestSession.addField(Field.is("Title", FieldType.STRING));
        repository = new InMemoryThingRepository("test");
        repository.initializeFrom(schema);
    }

    @Test
    public void anInstanceHasAGuid() {

        EntityInstance session;
        session = repository.createInstance(EntityInstanceDraft.forEntity(entityTestSession));

        Assertions.assertNotNull(session.getPrimaryKeyValue());
        Assertions.assertTrue(
                session.getPrimaryKeyValue().length() > 8,
                "Guid should be longish " + session.getPrimaryKeyValue());
        Assertions.assertTrue(
                session.getPrimaryKeyValue().contains("-"),
                "Guids should contain -" + session.getPrimaryKeyValue());
    }

    @Test
    public void anInstanceCanAccessGuidAsFieldOrMethod() {

        EntityInstance session;
        session = repository.createInstance(EntityInstanceDraft.forEntity(entityTestSession));

        Assertions.assertEquals(
                session.getPrimaryKeyValue(), session.getFieldValue("guid").asString());
    }
}
