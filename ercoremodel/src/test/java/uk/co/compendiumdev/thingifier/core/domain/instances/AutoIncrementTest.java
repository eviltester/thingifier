package uk.co.compendiumdev.thingifier.core.domain.instances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

public class AutoIncrementTest {

    @Test
    public void canAutoIncrement(){
        AutoIncrement auto = new AutoIncrement("afield", 1);

        Assertions.assertEquals("afield", auto.getName());
        Assertions.assertEquals(1, auto.getCurrentValue());
    }

    @Test
    public void canUpdateAutoIncrement(){
        AutoIncrement auto = new AutoIncrement("afield", 1);
        Assertions.assertEquals(1, auto.getNextValueAndUpdate());
        Assertions.assertEquals(2, auto.getNextValueAndUpdate());
    }

    @Test
    public void canUpdateAutoIncrementMultipleTimes(){
        AutoIncrement auto = new AutoIncrement("afield", 1);

        Assertions.assertEquals(1, auto.getNextValueAndUpdate());
        Assertions.assertEquals(2, auto.getNextValueAndUpdate());
        Assertions.assertEquals(3, auto.getNextValueAndUpdate());
        Assertions.assertEquals(4, auto.getNextValueAndUpdate());
    }

    @Test
    public void canUpdateAutoIncrementInJumps(){
        AutoIncrement auto = new AutoIncrement("afield", 1);
        auto.by(10);

        Assertions.assertEquals(1, auto.getNextValueAndUpdate());
        Assertions.assertEquals(11, auto.getNextValueAndUpdate());
    }

    @Test
    public void canUpdateAutoIncrementInMultipleJumps(){
        AutoIncrement auto = new AutoIncrement("afield", 1);
        auto.by(5);

        Assertions.assertEquals(1, auto.getNextValueAndUpdate());
        Assertions.assertEquals(6, auto.getNextValueAndUpdate());
        Assertions.assertEquals(11, auto.getNextValueAndUpdate());
        Assertions.assertEquals(16, auto.getNextValueAndUpdate());
    }
}
