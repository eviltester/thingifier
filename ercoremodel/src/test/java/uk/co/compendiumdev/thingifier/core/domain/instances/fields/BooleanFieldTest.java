package uk.co.compendiumdev.thingifier.core.domain.instances.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public class BooleanFieldTest {

    EntityDefinition entityTestSession;

    @BeforeEach
    public void createEntity(){

        entityTestSession = new EntityDefinition("Test Session", "Test Sessions");
        entityTestSession.addFields(Field.is("review", FieldType.BOOLEAN).withDefaultValue("true"));
        entityTestSession.addFields(Field.is("falsey", FieldType.BOOLEAN));

        // TODO: allow 'optional' on boolean so could be nullable not set
    }


    @Test
    public void booleanFieldsCanOnlyBeSetAsTrueOrFalse() {

        EntityInstance session = new EntityInstance(entityTestSession);

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

        EntityInstance session = new EntityInstance(entityTestSession);

        Assertions.assertThrows(IllegalArgumentException.class, ()->{
            session.setValue("review", "BOB");
        });

        // unchanged from default
        Assertions.assertEquals("true", session.getFieldValue("review").asString());
    }


    @Test
    public void booleanFieldsByDefaultAreFalse(){

        EntityInstance session = new EntityInstance(entityTestSession);

        // false by default
        Assertions.assertEquals("false", session.getFieldValue("falsey").asString());
    }

    @Test
    public void booleanFieldsDefaultCanBeConfigured(){

        EntityInstance session = new EntityInstance(entityTestSession);

        // false by default
        Assertions.assertEquals("true", session.getFieldValue("review").asString());
    }

}
