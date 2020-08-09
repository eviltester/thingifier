package uk.co.compendiumdev.thingifier.domain.definitions.fielddefinition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FieldValueTest {

    @Test
    public void canCreateAndAccessName(){
        final FieldValue value = FieldValue.is("generic", "value");
        Assertions.assertEquals("generic", value.getName());
    }
    @Test
    public void canCreateAndGetValue(){
        final FieldValue value = FieldValue.is("generic", "value");
        Assertions.assertEquals("value", value.asString());
    }

    @Test
    public void canSetGetFloatValue(){

        final FieldValue value = FieldValue.is("afloat", "4.3");
        Assertions.assertEquals("4.3", value.asString());
        Assertions.assertEquals(4.3F, value.asFloat());
    }

    @Test
    public void canCloneSimpleValue(){

        final FieldValue value = FieldValue.is("simple", "value");
        final FieldValue clonedValue = value.cloned();

        Assertions.assertEquals(clonedValue.getName(), value.getName());
        Assertions.assertEquals(clonedValue.asString(), value.asString());
        Assertions.assertNotSame(clonedValue, value);
    }

    @Test
    public void floatValueShouldBeSetAsValid_NoConversionPerformed(){

        final FieldValue value = FieldValue.is("afloat", "");

        Assertions.assertThrows(NumberFormatException.class,
                ()-> {value.asFloat();});
    }

    // todo field as object test
}
