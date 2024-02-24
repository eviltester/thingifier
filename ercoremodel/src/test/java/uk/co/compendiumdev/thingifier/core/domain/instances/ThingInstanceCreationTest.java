package uk.co.compendiumdev.thingifier.core.domain.instances;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

public class ThingInstanceCreationTest {

    EntityDefinition entityTestSession;

    @BeforeEach
    public void createEntity(){

        entityTestSession = new EntityDefinition("Test Session", "Test Sessions");

        entityTestSession.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        entityTestSession.addField(Field.is("Title", FieldType.STRING));
        entityTestSession.addFields(Field.is("CompletedStatus", FieldType.STRING).withDefaultValue("Not Completed"));
        entityTestSession.addFields(Field.is("review", FieldType.BOOLEAN).withDefaultValue("TRUE"));
        entityTestSession.addFields(Field.is("falsey", FieldType.BOOLEAN));
    }

    @Test
    public void canCreateNewEntityInstance() {

        EntityInstance session;
        session = new EntityInstance(entityTestSession);

        Assertions.assertEquals(4+1, session.getFieldNames().size()); // +1 for guid

        session.setValue("Title", "My Test Session");
        Assertions.assertEquals("My Test Session", session.getFieldValue("Title").asString());

        Assertions.assertEquals("Test Session", session.getEntity().getName());

        System.out.println(session.toString());
    }


    @Test
    public void canCreateUniqueGUIDs(){
        EntityInstance session = new EntityInstance(entityTestSession);
        session.addAutoGUIDstoInstance();

        Assertions.assertNotNull(session.getPrimaryKeyValue());

        EntityInstance session2 = new EntityInstance(entityTestSession);
        session2.addAutoGUIDstoInstance();

        Assertions.assertNotNull(session2.getPrimaryKeyValue());
        Assertions.assertTrue(session2.getPrimaryKeyValue().length()>10);

        Assertions.assertNotEquals(session.getPrimaryKeyValue(), session2.getPrimaryKeyValue());
    }


    @Test
    public void canCreateAThingWithAGUID(){

        // note potential bug this is risky if the GUID is later created
        EntityInstance instance = new EntityInstance(entityTestSession);
        instance.overrideValue("guid", "1234-1234-1324-1234");

        Assertions.assertEquals("1234-1234-1324-1234", instance.getPrimaryKeyValue());
    }


}
