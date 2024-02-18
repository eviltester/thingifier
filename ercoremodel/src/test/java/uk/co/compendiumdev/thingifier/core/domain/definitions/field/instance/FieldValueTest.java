package uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

class FieldValueTest {

    @Test
    void canCreateAndAccessName(){
        final Field field = Field.is("generic", FieldType.STRING);

        final FieldValue value = FieldValue.is(field, "value");
        Assertions.assertEquals("generic", value.getName());
    }
    @Test
    void canCreateAndGetValue(){

        final Field field = Field.is("generic", FieldType.STRING);
        final FieldValue value = FieldValue.is(field, "value");
        Assertions.assertEquals("value", value.asString());
    }

    @Test
    void canSetGetFloatValue(){

        final Field field = Field.is("generic", FieldType.STRING);

        final FieldValue value = FieldValue.is(field, "4.3");
        Assertions.assertEquals("4.3", value.asString());
        Assertions.assertEquals(4.3F, value.asFloat());
    }

    @Test
    void canCloneSimpleValue(){

        final Field field = Field.is("simple", FieldType.STRING);

        final FieldValue value = FieldValue.is(field, "value");
        final FieldValue clonedValue = value.cloned();

        Assertions.assertEquals(clonedValue.getName(), value.getName());
        Assertions.assertEquals(clonedValue.asString(), value.asString());
        Assertions.assertNotSame(clonedValue, value);
    }

    @Test
    void floatValueShouldBeSetAsValid_NoConversionPerformed(){

        final Field field = Field.is("aFloat", FieldType.STRING);

        final FieldValue value = FieldValue.is(field, "");

        Assertions.assertThrows(NumberFormatException.class,
                ()-> {value.asFloat();});
    }
}
