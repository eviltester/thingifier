package uk.co.compendiumdev.thingifier.core.domain.instances.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

public class EnumFieldTest {

    @Test
    public void byDefaultAnEnumFieldIsEmpty() {

        EntityDefinition enumFieldEntity = new EntityDefinition("thing", "things");
        enumFieldEntity.addFields(Field.is("enum", FieldType.ENUM));

        // TODO: have a validation process for the definition and make it a syntax error for Enum
        // fields to have no values
        EntityInstance instance =
                uk.co.compendiumdev.thingifier.core.repository.MutableEntityInstance
                        .snapshotFromDraft(EntityInstanceDraft.forEntity(enumFieldEntity));
        Assertions.assertEquals("", instance.getFieldValue("enum").asString());
    }

    @Test
    public void canSetAllowableValuesOfEnumFields() {

        EntityDefinition stringFieldEntity = new EntityDefinition("Test Session", "Test Sessions");
        stringFieldEntity.addFields(
                Field.is("enum", FieldType.ENUM)
                        .withDefaultValue("bob")
                        .withExample("dukes")
                        .withExample("dobbs"));

        // use example
        EntityInstance instance =
                uk.co.compendiumdev.thingifier.core.repository.MutableEntityInstance
                        .snapshotFromDraft(
                                EntityInstanceDraft.forEntity(stringFieldEntity)
                                        .withField("enum", "dukes"));
        Assertions.assertEquals("dukes", instance.getFieldValue("enum").asString());

        // use default
        instance =
                uk.co.compendiumdev.thingifier.core.repository.MutableEntityInstance
                        .snapshotFromDraft(
                                EntityInstanceDraft.forEntity(stringFieldEntity)
                                        .withField("enum", "bob"));
        Assertions.assertEquals("bob", instance.getFieldValue("enum").asString());
    }

    @Test
    public void canNotSetUndefinedValueOfEnumFields() {

        EntityDefinition stringFieldEntity = new EntityDefinition("Test Session", "Test Sessions");
        stringFieldEntity.addFields(
                Field.is("enum", FieldType.ENUM)
                        .withDefaultValue("bob")
                        .withExample("dukes")
                        .withExample("dobbs"));

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    EntityInstanceDraft.forEntity(stringFieldEntity).withField("enum", "connie");
                });
    }
}
