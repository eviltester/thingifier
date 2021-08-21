package uk.co.compendiumdev.thingifier.core.domain.instances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

public class ThingInstanceGuidTest {

    EntityDefinition entityTestSession;

    @BeforeEach
    public void createEntity(){

        entityTestSession = new EntityDefinition("Test Session", "Test Sessions");

        entityTestSession.addField(Field.is("Title", FieldType.STRING));
    }

    @Test
    public void anInstanceHasAGuid() {

        EntityInstance session;
        session = new EntityInstance(entityTestSession);
        session.addGUIDtoInstance();

        Assertions.assertNotNull(session.getGUID());
        Assertions.assertTrue(
                session.getGUID().length()>8,
                "Guid should be longish " + session.getGUID());
        Assertions.assertTrue(
                session.getGUID().contains("-"),
                "Guids should contain -" + session.getGUID());
    }

    @Test
    public void anInstanceCanAccessGuidAsFieldOrMethod() {

        EntityInstance session;
        session = new EntityInstance(entityTestSession);
        session.addGUIDtoInstance();

        Assertions.assertEquals(session.getGUID(), session.getFieldValue("guid").asString());

    }
}
