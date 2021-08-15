package uk.co.compendiumdev.thingifier.core.domain.instances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;

public class ThingInstanceGuidTest {

    ThingDefinition entityTestSession;

    @BeforeEach
    public void createEntity(){

        entityTestSession = new ThingDefinition("Test Session", "Test Sessions");

        entityTestSession.addField(Field.is("Title"));
    }

    @Test
    public void anInstanceHasAGuid() {

        ThingInstance session;
        session = new ThingInstance(entityTestSession);
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

        ThingInstance session;
        session = new ThingInstance(entityTestSession);
        session.addGUIDtoInstance();

        Assertions.assertEquals(session.getGUID(), session.getFieldValue("guid").asString());

    }
}
