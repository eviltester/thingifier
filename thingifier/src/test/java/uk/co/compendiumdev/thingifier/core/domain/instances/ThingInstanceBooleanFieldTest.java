package uk.co.compendiumdev.thingifier.core.domain.instances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;

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

        ThingInstance session = ThingInstance.create(entityTestSession);

        session.setValue("review", "false");
        Assertions.assertEquals("false", session.getFieldValue("review").asString());

        session.setValue("review", "faLSE");
        Assertions.assertEquals("false", session.getFieldValue("review").asString());

        session.setValue("review", "true");
        Assertions.assertEquals("true", session.getFieldValue("review").asString());

        session.setValue("review", "TRUE");
        Assertions.assertEquals("true", session.getFieldValue("review").asString());
    }

    @Test
    public void booleanFieldsRaiseExceptionForInvalidValue(){

        ThingInstance session = ThingInstance.create(entityTestSession);

        Assertions.assertThrows(IllegalArgumentException.class, ()->{
            session.setValue("review", "BOB");
        });

        // unchanged from default
        Assertions.assertEquals("true", session.getFieldValue("review").asString());
    }


    @Test
    public void booleanFieldsByDefaultAreFalse(){

        ThingInstance session = ThingInstance.create(entityTestSession);

        // false by default
        Assertions.assertEquals("false", session.getFieldValue("falsey").asString());
    }

    @Test
    public void booleanFieldsDefaultCanBeConfigured(){

        ThingInstance session = ThingInstance.create(entityTestSession);

        // false by default
        Assertions.assertEquals("true", session.getFieldValue("review").asString());
    }

}
