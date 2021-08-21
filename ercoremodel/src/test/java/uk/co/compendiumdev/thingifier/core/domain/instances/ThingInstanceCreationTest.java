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

        entityTestSession.addField(Field.is("Title", FieldType.STRING));
        entityTestSession.addFields(Field.is("CompletedStatus", FieldType.STRING).withDefaultValue("Not Completed"));
        entityTestSession.addFields(Field.is("review", FieldType.BOOLEAN).withDefaultValue("TRUE"));
        entityTestSession.addFields(Field.is("falsey", FieldType.BOOLEAN));
    }

    @Test
    public void canCreateNewEntityInstance() {

        EntityInstance session;
        session = new EntityInstance(entityTestSession);
        session.addGUIDtoInstance();
        session.addIdsToInstance();

        Assertions.assertEquals(4+1, session.getFieldNames().size()); // +1 for guid

        session.setValue("Title", "My Test Session");
        Assertions.assertEquals("My Test Session", session.getFieldValue("Title").asString());

        Assertions.assertEquals("Test Session", session.getEntity().getName());

        System.out.println(session.toString());
    }


    @Test
    public void canCreateUniqueGUIDs(){
        EntityInstance session = new EntityInstance(entityTestSession);
        session.addGUIDtoInstance();

        Assertions.assertNotNull(session.getGUID());

        EntityInstance session2 = new EntityInstance(entityTestSession);
        session2.addGUIDtoInstance();

        Assertions.assertNotNull(session2.getGUID());
        Assertions.assertTrue(session2.getGUID().length()>10);

        Assertions.assertNotEquals(session.getGUID(), session2.getGUID());
    }


    @Test
    public void canCreateAThingWithAGUID(){

        // note potential bug this is risky if the GUID is later created
        EntityInstance instance = new EntityInstance(entityTestSession);
        instance.overrideValue("guid", "1234-1234-1324-1234");
        instance.addIdsToInstance();

        Assertions.assertEquals("1234-1234-1324-1234", instance.getGUID());
    }
}
