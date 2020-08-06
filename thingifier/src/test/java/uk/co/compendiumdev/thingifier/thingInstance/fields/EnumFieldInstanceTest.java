package uk.co.compendiumdev.thingifier.thingInstance.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.domain.FieldType;
import uk.co.compendiumdev.thingifier.domain.definitions.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

public class EnumFieldInstanceTest {

    @Test
    public void byDefaultAnEnumFieldIsEmpty(){

        ThingDefinition enumFieldEntity = ThingDefinition.create("thing", "things");
        enumFieldEntity.addFields(Field.is("enum", FieldType.ENUM));

        // TODO: have a validation process for the definition and make it a syntax error for Enum fields to have no values
        ThingInstance instance = new ThingInstance(enumFieldEntity);
        Assertions.assertEquals("", instance.getFieldValue("enum").asString());
    }

    @Test
    public void canSetAllowableValuesOfEnumFields(){

        ThingDefinition stringFieldEntity = ThingDefinition.create("Test Session", "Test Sessions");
        stringFieldEntity.addFields(Field.is("enum", FieldType.ENUM).
                                        withDefaultValue("bob").
                                        withExample("dukes").
                                        withExample("dobbs")
        );

        ThingInstance instance = new ThingInstance(stringFieldEntity);

        // use example
        instance.setValue("enum", "dukes");
        Assertions.assertEquals("dukes", instance.getFieldValue("enum").asString());

        // use default
        instance.setValue("enum", "bob");
        Assertions.assertEquals("bob", instance.getFieldValue("enum").asString());
    }

    @Test
    public void canNotSetUndefinedValueOfEnumFields(){

        ThingDefinition stringFieldEntity = ThingDefinition.create("Test Session", "Test Sessions");
        stringFieldEntity.addFields(Field.is("enum", FieldType.ENUM).
                withDefaultValue("bob").
                withExample("dukes").
                withExample("dobbs")
        );

        ThingInstance instance = new ThingInstance(stringFieldEntity);

        Assertions.assertThrows(IllegalArgumentException.class, ()-> {
            instance.setValue("enum", "connie");
        });

    }

}
