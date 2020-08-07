package uk.co.compendiumdev.thingifier.domain.instances.fieldinstantiation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.domain.definitions.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GenericFieldConditionsTest {

    ThingDefinition entityTestSession;

    @BeforeEach
    public void createEntity(){

        entityTestSession = ThingDefinition.create("Test Session", "Test Sessions");
        entityTestSession.addFields(Field.is("CompletedStatus").withDefaultValue("Not Completed"));
    }

    @Test
    public void defaultValuesAreReturnedByGetValue(){

        ThingInstance session = new ThingInstance(entityTestSession);
        Assertions.assertEquals("Not Completed", session.getFieldValue("CompletedStatus").asString());
    }

    @Test
    public void canCreateFieldWithExamples(){

        entityTestSession.addField(Field.is("complicated").withExample("nuclear physics"));

        Assertions.assertEquals(1,
                entityTestSession.getField("complicated").getExamples().size());

        Assertions.assertEquals("nuclear physics",
                entityTestSession.getField("complicated").getRandomExampleValue());
    }

    @Test
    public void randomExamplesReturnsAll(){

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("nuclear physics");
        expectedValues.add("people");
        expectedValues.add("decisions");

        entityTestSession.addField(Field.is("complicated").
                            withExample("nuclear physics").
                            withExample("people").
                            withExample("decisions"));

        Assertions.assertEquals(3,
                entityTestSession.getField("complicated").getExamples().size());

        int tries = 0;
        // potential for intermittency,
        // but it really shouldn't take 100 tries to generate all out of 3
        while(expectedValues.size()!=0 && tries<100){

            String randomValue = entityTestSession.
                    getField("complicated").
                    getRandomExampleValue();
            System.out.println(randomValue);
            expectedValues.remove(randomValue);

            tries++;
        }

        System.out.println("all random examples generated in " + tries);
        Assertions.assertEquals(0, expectedValues.size());
    }



    @Test
    public void fieldNameAccessIsCaseInsensitive(){
        ThingInstance session = new ThingInstance(entityTestSession);

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
        ThingInstance session = new ThingInstance(entityTestSession);

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
        ThingInstance session = new ThingInstance(entityTestSession);

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
