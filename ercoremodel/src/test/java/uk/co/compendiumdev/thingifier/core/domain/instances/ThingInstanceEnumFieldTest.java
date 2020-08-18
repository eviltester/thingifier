package uk.co.compendiumdev.thingifier.core.domain.instances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;

public class ThingInstanceEnumFieldTest {

    @Test
    public void byDefaultAnEnumFieldIsEmpty(){

        ThingDefinition enumFieldEntity = ThingDefinition.create("thing", "things");
        enumFieldEntity.addFields(Field.is("enum", FieldType.ENUM));

        // TODO: have a validation process for the definition and make it a syntax error for Enum fields to have no values
        ThingInstance instance = ThingInstance.create(enumFieldEntity);
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

        ThingInstance instance = ThingInstance.create(stringFieldEntity);

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

        ThingInstance instance = ThingInstance.create(stringFieldEntity);

        Assertions.assertThrows(IllegalArgumentException.class, ()-> {
            instance.setValue("enum", "connie");
        });

    }

}
