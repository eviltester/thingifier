package uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FieldValueTest {

    @Test
    void canCreateAndAccessName(){
        final FieldValue value = FieldValue.is("generic", "value");
        Assertions.assertEquals("generic", value.getName());
    }
    @Test
    void canCreateAndGetValue(){
        final FieldValue value = FieldValue.is("generic", "value");
        Assertions.assertEquals("value", value.asString());
    }

    @Test
    void canSetGetFloatValue(){

        final FieldValue value = FieldValue.is("afloat", "4.3");
        Assertions.assertEquals("4.3", value.asString());
        Assertions.assertEquals(4.3F, value.asFloat());
    }

    @Test
    void canCloneSimpleValue(){

        final FieldValue value = FieldValue.is("simple", "value");
        final FieldValue clonedValue = value.cloned();

        Assertions.assertEquals(clonedValue.getName(), value.getName());
        Assertions.assertEquals(clonedValue.asString(), value.asString());
        Assertions.assertNotSame(clonedValue, value);
    }

    @Test
    void floatValueShouldBeSetAsValid_NoConversionPerformed(){

        final FieldValue value = FieldValue.is("afloat", "");

        Assertions.assertThrows(NumberFormatException.class,
                ()-> {value.asFloat();});
    }
}
