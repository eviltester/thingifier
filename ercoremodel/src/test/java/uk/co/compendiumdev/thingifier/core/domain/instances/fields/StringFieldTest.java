package uk.co.compendiumdev.thingifier.core.domain.instances.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.VRule;

public class StringFieldTest {

    // TODO: allow nullable string fields to have fully optional fields
    // TODO: add specific tests around the validation rule classes - currently these are 'used' not 'tested'

    @Test
    public void byDefaultAStringFieldIsEmpty(){

        EntityDefinition stringFieldEntity = new EntityDefinition("Test Session", "Test Sessions");
        stringFieldEntity.addFields(Field.is("defaultString", FieldType.STRING));

        EntityInstance instance = new EntityInstance(stringFieldEntity);

        Assertions.assertEquals("", instance.getFieldValue("defaultString").asString());
    }

    @Test
    public void canSetDefaultValuesOfStringFields(){

        EntityDefinition stringFieldEntity = new EntityDefinition("Test Session", "Test Sessions");
        stringFieldEntity.addFields(Field.is("defaultString", FieldType.STRING).withDefaultValue("bob"));

        EntityInstance instance = new EntityInstance(stringFieldEntity);

        Assertions.assertEquals("bob", instance.getFieldValue("defaultString").asString());
    }

    @Test
    public void canConfigureStringsToValidateOnNotEmpty(){

        EntityDefinition stringFieldEntity = new EntityDefinition("entity", "entities");
        stringFieldEntity.addFields(Field.is("defaultString", FieldType.STRING).
                makeMandatory().
                                        withDefaultValue("").
                                        withValidation(VRule.notEmpty()));

        EntityInstance instance = new EntityInstance(stringFieldEntity);

        // defaultString is not valid because it has an empty string
        Assertions.assertFalse(instance.validate().isValid());

        instance.setValue("defaultString", "Eris");
        Assertions.assertTrue(instance.validate().isValid());
        Assertions.assertEquals("Eris", instance.getFieldValue("defaultString").asString());
    }

    @Test
    public void canConfigureStringsToTruncateIfTooLargeWhenSetting(){

        EntityDefinition stringFieldEntity = new EntityDefinition("entity", "entities");
        stringFieldEntity.addFields(Field.is("field", FieldType.STRING).
                makeMandatory().
                withDefaultValue("").
                withValidation(VRule.notEmpty()).truncateStringTo(10));

        EntityInstance instance = new EntityInstance(stringFieldEntity);

        instance.setValue("field", "This is too long");
        String fieldValue = instance.getFieldValue("field").asString();

        Assertions.assertEquals(10, fieldValue.length());
        Assertions.assertEquals("This is to",fieldValue );
    }

    @Test
    public void canConfigureStringsToThrowErrorValidationErrorIfTooLarge(){

        EntityDefinition stringFieldEntity = new EntityDefinition("entity", "entities");
        stringFieldEntity.addFields(Field.is("field", FieldType.STRING).
                makeMandatory().
                withDefaultValue("").
                withValidation(VRule.maximumLength(10)));

        EntityInstance instance = new EntityInstance(stringFieldEntity);
        instance.overrideValue("field","12345678901");

        ValidationReport report = instance.validate();

        Assertions.assertFalse(report.isValid());
        report.getCombinedErrorMessages().contains("Maximum allowable length exceeded");
    }

    @Test
    public void canConfigureStringsToValidateonSetting(){

        EntityDefinition stringFieldEntity = new EntityDefinition("entity", "entities");
        stringFieldEntity.addFields(Field.is("field", FieldType.STRING).
                makeMandatory().
                withDefaultValue("").
                withValidation(VRule.matchesRegex("^Bug:.*")));

        EntityInstance instance = new EntityInstance(stringFieldEntity);
        IllegalArgumentException e =
                Assertions.assertThrows(IllegalArgumentException.class, () ->{
            instance.setValue("field", "ISSUE: reporting a bug - this is a bug");
        });

        System.out.println(e.getMessage());
        Assertions.assertTrue(e.getMessage().contains("not match"));
    }

}
