package uk.co.compendiumdev.thingifier.core.domain.instances;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;

import java.util.Random;

public class ThingInstanceFieldsAccessTest {

    ThingDefinition entityTestSession;

    @BeforeEach
    public void createDefinition(){
        entityTestSession =
                ThingDefinition.create("Test Session", "Test Sessions");
        entityTestSession.addField(Field.is("CompletedStatus").withDefaultValue("Not Completed"));
    }

    @Test
    public void defaultValuesAreReturnedByGetValue(){

        ThingInstance session = ThingInstance.create(entityTestSession);
        Assertions.assertEquals("Not Completed",
                session.getFieldValue("CompletedStatus").asString());
    }


    @Test
    public void fieldValueAccessIsCaseInsensitive(){

        ThingInstance session = ThingInstance.create(entityTestSession);

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

        ThingInstance session = ThingInstance.create(entityTestSession);

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

        ThingInstance session = ThingInstance.create(entityTestSession);

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
