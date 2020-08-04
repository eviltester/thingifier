package uk.co.compendiumdev.thingifier.thingInstance.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.domain.definitions.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.domain.definitions.validation.VRule;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

public class StringFieldInstanceTest {

    // TODO: allow nullable string fields to have fully optional fields
    // TODO: add specific tests around the validation rule classes - currently these are 'used' not 'tested'

    @Test
    public void byDefaultAStringFieldIsEmpty(){

        ThingDefinition stringFieldEntity = ThingDefinition.create("Test Session", "Test Sessions");
        stringFieldEntity.addFields(Field.is("defaultString"));

        ThingInstance instance = new ThingInstance(stringFieldEntity);

        Assertions.assertEquals("", instance.getValue("defaultString"));
    }

    @Test
    public void canSetDefaultValuesOfStringFields(){

        ThingDefinition stringFieldEntity = ThingDefinition.create("Test Session", "Test Sessions");
        stringFieldEntity.addFields(Field.is("defaultString").withDefaultValue("bob"));

        ThingInstance instance = new ThingInstance(stringFieldEntity);

        Assertions.assertEquals("bob", instance.getValue("defaultString"));
    }

    @Test
    public void canConfigureStringsToValidateOnNotEmpty(){

        ThingDefinition stringFieldEntity = ThingDefinition.create("entity", "entities");
        stringFieldEntity.addFields(Field.is("defaultString").
                                        mandatory().
                                        withDefaultValue("").
                                        withValidation(VRule.notEmpty()));

        ThingInstance instance = new ThingInstance(stringFieldEntity);

        // defaultString is not valid because it has an empty string
        Assertions.assertFalse(instance.validate().isValid());

        instance.setValue("defaultString", "Eris");
        Assertions.assertTrue(instance.validate().isValid());
        Assertions.assertEquals("Eris", instance.getValue("defaultString"));
    }

    @Test
    public void canConfigureStringsToTruncateIfTooLarge(){

        ThingDefinition stringFieldEntity = ThingDefinition.create("entity", "entities");
        stringFieldEntity.addFields(Field.is("field").
                mandatory().
                withDefaultValue("").
                withValidation(VRule.notEmpty()).truncateStringTo(10));

        ThingInstance instance = new ThingInstance(stringFieldEntity);

        instance.setValue("field", "This is too long");
        String fieldValue = instance.getValue("field");

        Assertions.assertEquals(10, fieldValue.length());
        Assertions.assertEquals("This is to",fieldValue );
    }

    @Test
    public void canConfigureStringsToThrowErrorValidationErrorIfTooLarge(){

        ThingDefinition stringFieldEntity = ThingDefinition.create("entity", "entities");
        stringFieldEntity.addFields(Field.is("field").
                mandatory().
                withDefaultValue("").
                withValidation(VRule.maximumLength(10)));

        ThingInstance instance = new ThingInstance(stringFieldEntity);
        instance.overrideValue("field","12345678901");

        ValidationReport report = instance.validate();

        Assertions.assertFalse(report.isValid());
        report.getCombinedErrorMessages().contains("Maximum allowable length exceeded");
    }

    @Test
    public void canConfigureStringsToThrowErrorValidationErrorIfNotMatchRegex(){

        ThingDefinition stringFieldEntity = ThingDefinition.create("entity", "entities");
        stringFieldEntity.addFields(Field.is("field").
                mandatory().
                withDefaultValue("").
                withValidation(VRule.matchesRegex("^Bug:.*")));

        ThingInstance instance = new ThingInstance(stringFieldEntity);
        IllegalArgumentException e =
                Assertions.assertThrows(IllegalArgumentException.class, () ->{
            instance.setValue("field", "ISSUE: reporting a bug - this is a bug");
        });

        System.out.println(e.getMessage());
        Assertions.assertTrue(e.getMessage().contains("not match"));
    }

    @Test
    public void canConfigureStringToMatchMultipleRules(){

        ThingDefinition stringFieldEntity = ThingDefinition.create("entity", "entities");
        stringFieldEntity.addFields(Field.is("field").
                mandatory().
                withDefaultValue("").
                withValidation(VRule.maximumLength(10),
                                VRule.satisfiesRegex("^Bug:"),
                                VRule.matchesRegex("^Bug:.*")
                ));

        ThingInstance instance = new ThingInstance(stringFieldEntity);
        instance.setValue("field", "Bug: short");

        ValidationReport report = instance.validate();
        Assertions.assertTrue(report.isValid(), report.getCombinedErrorMessages());
    }



}
