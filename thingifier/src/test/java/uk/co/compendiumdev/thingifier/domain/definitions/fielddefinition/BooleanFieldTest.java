package uk.co.compendiumdev.thingifier.domain.definitions.fielddefinition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BooleanFieldTest {

    @Test
    public void byDefaultABooleanFieldIsFalse(){

        final Field field = Field.is("false", FieldType.BOOLEAN);

        Assertions.assertEquals("false", field.getDefaultValue().asString());
    }

    @Test
    public void canConfigureBooleanDefaultAsTrue(){

        final Field field = Field.is("false", FieldType.BOOLEAN).withDefaultValue("true");

        Assertions.assertEquals("true", field.getDefaultValue().asString());
    }

    @Test
    public void booleanCanOnlyBeFalseOrTrue(){

        final Field field = Field.is("boolean", FieldType.BOOLEAN);

        Assertions.assertTrue(
                field.validate(FieldValue.is("boolean","false")).isValid());

        Assertions.assertTrue(
                field.validate(FieldValue.is("boolean","true")).isValid());

        Assertions.assertTrue(
                field.validate(FieldValue.is("boolean","TRUE")).isValid());

        Assertions.assertFalse(
                field.validate(FieldValue.is("boolean","bob")).isValid());


    }
}
