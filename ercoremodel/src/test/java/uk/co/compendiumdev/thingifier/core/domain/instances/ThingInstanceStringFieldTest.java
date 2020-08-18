package uk.co.compendiumdev.thingifier.core.domain.instances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.VRule;

public class ThingInstanceStringFieldTest {

    // TODO: allow nullable string fields to have fully optional fields
    // TODO: add specific tests around the validation rule classes - currently these are 'used' not 'tested'

    @Test
    public void byDefaultAStringFieldIsEmpty(){

        ThingDefinition stringFieldEntity = ThingDefinition.create("Test Session", "Test Sessions");
        stringFieldEntity.addFields(Field.is("defaultString"));

        ThingInstance instance = ThingInstance.create(stringFieldEntity);

        Assertions.assertEquals("", instance.getFieldValue("defaultString").asString());
    }

    @Test
    public void canSetDefaultValuesOfStringFields(){

        ThingDefinition stringFieldEntity = ThingDefinition.create("Test Session", "Test Sessions");
        stringFieldEntity.addFields(Field.is("defaultString").withDefaultValue("bob"));

        ThingInstance instance = ThingInstance.create(stringFieldEntity);

        Assertions.assertEquals("bob", instance.getFieldValue("defaultString").asString());
    }

    @Test
    public void canConfigureStringsToValidateOnNotEmpty(){

        ThingDefinition stringFieldEntity = ThingDefinition.create("entity", "entities");
        stringFieldEntity.addFields(Field.is("defaultString").
                makeMandatory().
                                        withDefaultValue("").
                                        withValidation(VRule.notEmpty()));

        ThingInstance instance = ThingInstance.create(stringFieldEntity);

        // defaultString is not valid because it has an empty string
        Assertions.assertFalse(instance.validate().isValid());

        instance.setValue("defaultString", "Eris");
        Assertions.assertTrue(instance.validate().isValid());
        Assertions.assertEquals("Eris", instance.getFieldValue("defaultString").asString());
    }

    @Test
    public void canConfigureStringsToTruncateIfTooLargeWhenSetting(){

        ThingDefinition stringFieldEntity = ThingDefinition.create("entity", "entities");
        stringFieldEntity.addFields(Field.is("field").
                makeMandatory().
                withDefaultValue("").
                withValidation(VRule.notEmpty()).truncateStringTo(10));

        ThingInstance instance = ThingInstance.create(stringFieldEntity);

        instance.setValue("field", "This is too long");
        String fieldValue = instance.getFieldValue("field").asString();

        Assertions.assertEquals(10, fieldValue.length());
        Assertions.assertEquals("This is to",fieldValue );
    }

    @Test
    public void canConfigureStringsToThrowErrorValidationErrorIfTooLarge(){

        ThingDefinition stringFieldEntity = ThingDefinition.create("entity", "entities");
        stringFieldEntity.addFields(Field.is("field").
                makeMandatory().
                withDefaultValue("").
                withValidation(VRule.maximumLength(10)));

        ThingInstance instance = ThingInstance.create(stringFieldEntity);
        instance.overrideValue("field","12345678901");

        ValidationReport report = instance.validate();

        Assertions.assertFalse(report.isValid());
        report.getCombinedErrorMessages().contains("Maximum allowable length exceeded");
    }

    @Test
    public void canConfigureStringsToValidateonSetting(){

        ThingDefinition stringFieldEntity = ThingDefinition.create("entity", "entities");
        stringFieldEntity.addFields(Field.is("field").
                makeMandatory().
                withDefaultValue("").
                withValidation(VRule.matchesRegex("^Bug:.*")));

        ThingInstance instance = ThingInstance.create(stringFieldEntity);
        IllegalArgumentException e =
                Assertions.assertThrows(IllegalArgumentException.class, () ->{
            instance.setValue("field", "ISSUE: reporting a bug - this is a bug");
        });

        System.out.println(e.getMessage());
        Assertions.assertTrue(e.getMessage().contains("not match"));
    }

}
