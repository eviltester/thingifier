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
        entityTestSession.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        entityTestSession.addField(Field.is("Title", FieldType.STRING));
    }

    @Test
    public void anInstanceHasAGuid() {

        EntityInstance session;
        session = new EntityInstance(entityTestSession);
        session.addGUIDtoInstance();

        Assertions.assertNotNull(session.getPrimaryKeyValue());
        Assertions.assertTrue(
                session.getPrimaryKeyValue().length()>8,
                "Guid should be longish " + session.getPrimaryKeyValue());
        Assertions.assertTrue(
                session.getPrimaryKeyValue().contains("-"),
                "Guids should contain -" + session.getPrimaryKeyValue());
    }

    @Test
    public void anInstanceCanAccessGuidAsFieldOrMethod() {

        EntityInstance session;
        session = new EntityInstance(entityTestSession);
        session.addGUIDtoInstance();

        Assertions.assertEquals(session.getPrimaryKeyValue(), session.getFieldValue("guid").asString());

    }
}
