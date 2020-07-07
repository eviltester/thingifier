package uk.co.compendiumdev.thingifier.thingInstance;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.generic.instances.ThingInstance;

public class ThingInstanceBooleanFieldTest {

    ThingDefinition entityTestSession;

    @BeforeEach
    public void createEntity(){

        entityTestSession = ThingDefinition.create("Test Session", "Test Sessions");
        entityTestSession.defineField("Title");
        entityTestSession.addFields(Field.is("CompletedStatus").withDefaultValue("Not Completed"));
        entityTestSession.addFields(Field.is("review", FieldType.BOOLEAN).withDefaultValue("TRUE"));
        entityTestSession.addFields(Field.is("falsey", FieldType.BOOLEAN));
    }


    @Test
    public void booleanFieldsCanOnlyBeSetAsTrueOrFalse(){

        ThingInstance session = new ThingInstance(entityTestSession);

        // false by default
        Assertions.assertEquals("TRUE", session.getValue("review"));

        session.setValue("review", "FALSE");
        Assertions.assertEquals("FALSE", session.getValue("review"));

        session.setValue("review", "TRUE");
        Assertions.assertEquals("TRUE", session.getValue("review"));


        try {
            session.setValue("review", "BOB");

            Assertions.fail("An Exception should have been thrown");
        }catch(IllegalArgumentException e){
            // unchanged from default
            Assertions.assertEquals("TRUE", session.getValue("review"));
        }

    }


    @Test
    public void booleanFieldsByDefaultAreFalse(){

        ThingInstance session = new ThingInstance(entityTestSession);

        // false by default
        Assertions.assertEquals("FALSE", session.getValue("falsey"));
    }

}
