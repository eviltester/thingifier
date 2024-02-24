package uk.co.compendiumdev.thingifier.core.domain.instances;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.Random;

public class EntityInstanceFieldsAccessTest {

    EntityDefinition entityTestSession;

    @BeforeEach
    public void createDefinition(){
        entityTestSession =
                new EntityDefinition("Test Session", "Test Sessions");
        entityTestSession.addField(Field.is("CompletedStatus", FieldType.STRING).withDefaultValue("Not Completed"));
    }

    @Test
    public void defaultValuesAreReturnedByGetValue(){

        EntityInstance session = new EntityInstance(entityTestSession);
        Assertions.assertEquals("Not Completed",
                session.getFieldValue("CompletedStatus").asString());
    }


    @Test
    public void fieldValueAccessIsCaseInsensitive(){

        EntityInstance session = new EntityInstance(entityTestSession);

        Assertions.assertEquals("Not Completed",
                session.getFieldValue("CompletedStatus").asString());
        Assertions.assertEquals("Not Completed",
                session.getFieldValue("CoMpletedStatus").asString());
        Assertions.assertEquals("Not Completed",
                session.getFieldValue("CompletedSTATUS").asString());
        Assertions.assertEquals("Not Completed",
                session.getFieldValue("completedstatus").asString());
    }

    @Test
    public void fieldNameSettingIsCaseInsensitive(){

        EntityInstance session = new EntityInstance(entityTestSession);

        session.setValue("CompletedStatus", "in progress");
        Assertions.assertEquals("in progress",
                session.getFieldValue("CompletedStatus").asString());

        session.setValue("completedStatus", "blame George");
        Assertions.assertEquals("blame George",
                session.getFieldValue("CompletedStatus").asString());

        session.setValue("compLetedstatus", "done");
        Assertions.assertEquals("done",
                session.getFieldValue("CompletedSTATUS").asString());

        session.setValue("completedstatus", "done done");
        Assertions.assertEquals("done done",
                session.getFieldValue("completedstatus").asString());
    }

    @Test
    public void fieldNameSettingIsReallyCaseInsensitive() {

        EntityInstance session = new EntityInstance(entityTestSession);

        for(int x=0; x<100; x++) {
            String setAs = randomCaseSwitcher("CompletedStatus");
            String getAs = randomCaseSwitcher("CompletedStatus");
            String value = "in progress " + x;
            //System.out.println(setAs + " " + getAs + " " + value);

            session.setValue(setAs, value);
            Assertions.assertEquals(value,
                    session.getFieldValue(getAs).asString(),
                    setAs + " " + getAs);
        }
    }

    private String randomCaseSwitcher(String string){

        final Random rnd = new Random();
        StringBuilder ret = new StringBuilder();

        for(int ch=0; ch<string.length(); ch++){
            String nextChar = String.valueOf(string.charAt(ch));
            if(rnd.nextBoolean()){
                nextChar = nextChar.toUpperCase();
            }
            ret.append(nextChar);
        }

        return ret.toString();
    }
}
