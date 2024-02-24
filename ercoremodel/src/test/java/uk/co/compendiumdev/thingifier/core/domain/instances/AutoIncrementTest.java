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

        auto.update();
        Assertions.assertEquals(2, auto.getCurrentValue());
    }

    @Test
    public void canUpdateAutoIncrementMultipleTimes(){
        AutoIncrement auto = new AutoIncrement("afield", 1);

        auto.update();
        auto.update();
        auto.update();
        Assertions.assertEquals(4, auto.getCurrentValue());
    }

    @Test
    public void canUpdateAutoIncrementInJumps(){
        AutoIncrement auto = new AutoIncrement("afield", 1);
        auto.by(10);

        auto.update();
        Assertions.assertEquals(11, auto.getCurrentValue());
    }

    @Test
    public void canUpdateAutoIncrementInMultipleJumps(){
        AutoIncrement auto = new AutoIncrement("afield", 1);
        auto.by(10);

        auto.update();
        auto.update();
        auto.update();
        Assertions.assertEquals(31, auto.getCurrentValue());
    }
}
