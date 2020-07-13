package uk.co.compendiumdev.thingifier.thingInstance.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.domain.FieldType;
import uk.co.compendiumdev.thingifier.domain.definitions.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

public class ThingInstanceBooleanFieldTest {

    ThingDefinition entityTestSession;

    @BeforeEach
    public void createEntity(){

        entityTestSession = ThingDefinition.create("Test Session", "Test Sessions");
        entityTestSession.addFields(Field.is("review", FieldType.BOOLEAN).withDefaultValue("true"));
        entityTestSession.addFields(Field.is("falsey", FieldType.BOOLEAN));

        // TODO: allow 'optional' on boolean so could be nullable not set
    }


    @Test
    public void booleanFieldsCanOnlyBeSetAsTrueOrFalse() {

        ThingInstance session = new ThingInstance(entityTestSession);

        // false by default
        Assertions.assertEquals("true", session.getValue("review"));

        session.setValue("review", "false");
        Assertions.assertEquals("false", session.getValue("review"));

        session.setValue("review", "true");
        Assertions.assertEquals("true", session.getValue("review"));
    }

    @Test
    public void booleanFieldsRaiseExceptionForInvalidValue(){

        ThingInstance session = new ThingInstance(entityTestSession);

        Assertions.assertThrows(IllegalArgumentException.class, ()->{
            session.setValue("review", "BOB");
        });

        // unchanged from default
        Assertions.assertEquals("true", session.getValue("review"));
    }


    @Test
    public void booleanFieldsByDefaultAreFalse(){

        ThingInstance session = new ThingInstance(entityTestSession);

        // false by default
        Assertions.assertEquals("FALSE", session.getValue("falsey"));
    }

}
