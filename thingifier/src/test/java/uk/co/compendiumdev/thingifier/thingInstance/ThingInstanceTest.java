package uk.co.compendiumdev.thingifier.thingInstance;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

public class ThingInstanceTest {

    ThingDefinition entityTestSession;

    @BeforeEach
    public void createEntity(){

        entityTestSession = ThingDefinition.create("Test Session", "Test Sessions");

        entityTestSession.addField(Field.is("Title"));
        entityTestSession.addFields(Field.is("CompletedStatus").withDefaultValue("Not Completed"));
        entityTestSession.addFields(Field.is("review", FieldType.BOOLEAN).withDefaultValue("TRUE"));
        entityTestSession.addFields(Field.is("falsey", FieldType.BOOLEAN));
    }

    @Test
    public void defaultValuesAreReturnedByGetValue(){

        ThingInstance session = new ThingInstance(entityTestSession);
        Assertions.assertEquals("Not Completed", session.getValue("CompletedStatus"));
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
