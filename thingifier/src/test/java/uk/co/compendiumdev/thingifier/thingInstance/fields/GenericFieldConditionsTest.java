package uk.co.compendiumdev.thingifier.thingInstance.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.domain.definitions.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

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
        Assertions.assertEquals("Not Completed", session.getValue("CompletedStatus"));
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
    public void fieldNameAccessIsCaseInsensitive(){
        ThingInstance session = new ThingInstance(entityTestSession);

        Assertions.assertEquals("Not Completed", session.getValue("CompletedStatus"));
        Assertions.assertEquals("Not Completed", session.getValue("CoMpletedStatus"));
        Assertions.assertEquals("Not Completed", session.getValue("CompletedSTATUS"));
        Assertions.assertEquals("Not Completed", session.getValue("completedstatus"));
    }

}
