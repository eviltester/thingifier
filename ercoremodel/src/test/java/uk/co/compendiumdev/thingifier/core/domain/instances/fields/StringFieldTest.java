package uk.co.compendiumdev.thingifier.core.domain.instances.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.VRule;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

public class StringFieldTest {

    // TODO: allow nullable string fields to have fully optional fields
    // TODO: add specific tests around the validation rule classes - currently these are 'used' not
    // 'tested'

    @Test
    public void byDefaultAStringFieldIsEmpty() {

        EntityDefinition stringFieldEntity = new EntityDefinition("Test Session", "Test Sessions");
        stringFieldEntity.addFields(Field.is("defaultString", FieldType.STRING));

        EntityInstance instance =
                EntityInstance.fromDraft(EntityInstanceDraft.forEntity(stringFieldEntity));

        Assertions.assertEquals("", instance.getFieldValue("defaultString").asString());
    }

    @Test
    public void canSetDefaultValuesOfStringFields() {

        EntityDefinition stringFieldEntity = new EntityDefinition("Test Session", "Test Sessions");
        stringFieldEntity.addFields(
                Field.is("defaultString", FieldType.STRING).withDefaultValue("bob"));

        EntityInstance instance =
                EntityInstance.fromDraft(EntityInstanceDraft.forEntity(stringFieldEntity));

        Assertions.assertEquals("bob", instance.getFieldValue("defaultString").asString());
    }

    @Test
    public void canConfigureStringsToValidateOnNotEmpty() {

        EntityDefinition stringFieldEntity = new EntityDefinition("entity", "entities");
        stringFieldEntity.addFields(
                Field.is("defaultString", FieldType.STRING)
                        .makeMandatory()
                        .withDefaultValue("")
                        .withValidation(VRule.notEmpty()));

        EntityInstance instance =
                EntityInstance.fromDraft(EntityInstanceDraft.forEntity(stringFieldEntity));

        // defaultString is not valid because it has an empty string
        Assertions.assertFalse(instance.validate().isValid());

        instance =
                EntityInstance.fromDraft(
                        EntityInstanceDraft.forEntity(stringFieldEntity)
                                .withField("defaultString", "Eris"));
        Assertions.assertTrue(instance.validate().isValid());
        Assertions.assertEquals("Eris", instance.getFieldValue("defaultString").asString());
    }

    @Test
    public void canConfigureStringsToTruncateIfTooLargeWhenSetting() {

        EntityDefinition stringFieldEntity = new EntityDefinition("entity", "entities");
        stringFieldEntity.addFields(
                Field.is("field", FieldType.STRING)
                        .makeMandatory()
                        .withDefaultValue("")
                        .withValidation(VRule.notEmpty())
                        .truncateStringTo(10));

        EntityInstance instance =
                EntityInstance.fromDraft(
                        EntityInstanceDraft.forEntity(stringFieldEntity)
                                .withField("field", "This is too long"));
        String fieldValue = instance.getFieldValue("field").asString();

        Assertions.assertEquals(10, fieldValue.length());
        Assertions.assertEquals("This is to", fieldValue);
    }

    @Test
    public void canConfigureStringsToThrowErrorValidationErrorIfTooLarge() {

        EntityDefinition stringFieldEntity = new EntityDefinition("entity", "entities");
        stringFieldEntity.addFields(
                Field.is("field", FieldType.STRING)
                        .makeMandatory()
                        .withDefaultValue("")
                        .withValidation(VRule.maximumLength(10)));

        IllegalArgumentException e =
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                EntityInstanceDraft.forEntity(stringFieldEntity)
                                        .withField("field", "12345678901"));

        Assertions.assertTrue(e.getMessage().contains("Maximum allowable length exceeded"));
    }

    @Test
    public void canConfigureStringsToValidateonSetting() {

        EntityDefinition stringFieldEntity = new EntityDefinition("entity", "entities");
        stringFieldEntity.addFields(
                Field.is("field", FieldType.STRING)
                        .makeMandatory()
                        .withDefaultValue("")
                        .withValidation(VRule.matchesRegex("^Bug:.*")));

        IllegalArgumentException e =
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            EntityInstanceDraft.forEntity(stringFieldEntity)
                                    .withField("field", "ISSUE: reporting a bug - this is a bug");
                        });

        System.out.println(e.getMessage());
        Assertions.assertTrue(e.getMessage().contains("not match"));
    }
}
