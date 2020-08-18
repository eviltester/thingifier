package uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

import java.util.HashSet;
import java.util.Set;

class BooleanFieldTest {

    @Test
    void byDefaultABooleanFieldIsFalse(){

        final Field field = Field.is("false", FieldType.BOOLEAN);

        Assertions.assertEquals("false", field.getDefaultValue().asString());
    }

    @Test
    void canConfigureBooleanDefaultAsTrue(){

        final Field field = Field.is("false", FieldType.BOOLEAN).withDefaultValue("true");

        Assertions.assertEquals("true", field.getDefaultValue().asString());
    }

    @Test
    void booleanCanOnlyBeFalseOrTrue(){

        final Field field = Field.is("boolean", FieldType.BOOLEAN);

        Assertions.assertTrue(
                field.validate(
                        FieldValue.is("boolean","false")).
                        isValid());

        Assertions.assertTrue(
                field.validate(
                        FieldValue.is("boolean","true")).
                        isValid());

        Assertions.assertTrue(
                field.validate(
                        FieldValue.is("boolean","TRUE")).
                        isValid());

        Assertions.assertFalse(
                field.validate(
                        FieldValue.is("boolean","bob")).
                        isValid());
    }


    @Test
    void booleanExamplesAreTrueOrFalse(){

        final Field field = Field.is("boolean", FieldType.BOOLEAN);

        Set<String> bools = new HashSet<>();

        for(int x=0; x<100; x++){
            bools.add(field.getRandomExampleValue());
        }

        Assertions.assertEquals(2, bools.size());
        Assertions.assertTrue(bools.contains("true"));
        Assertions.assertTrue(bools.contains("false"));

    }

    @Test
    void canConvertFromString(){
        final Field field = Field.is("boolean", FieldType.BOOLEAN);

        Assertions.assertEquals("true",
                field.getActualValueToAdd(
                        FieldValue.is("boolean", "true")));

        Assertions.assertEquals("true",
                field.getActualValueToAdd(
                        FieldValue.is("boolean", "True")));

        Assertions.assertEquals("false",
                field.getActualValueToAdd(
                        FieldValue.is("boolean", "FALSE")));

    }
}
