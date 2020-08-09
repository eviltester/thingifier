package uk.co.compendiumdev.thingifier.domain.definitions.fielddefinition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.domain.definitions.validation.VRule;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

public class StringFieldTest {

    @Test
    public void byDefaultAFieldIsAnOptionalString(){

        final Field field = Field.is("defaultString");
        Assertions.assertEquals(FieldType.STRING, field.getType());
        Assertions.assertFalse(field.isMandatory());
    }

    @Test
    public void canHaveStringFieldWithExamples(){

        final Field field = Field.is("example", FieldType.STRING)
                .withExample("Eris").withExample("Dukes");

        Assertions.assertEquals(
                2,field.getExamples().size());

        String randomExample = field.getRandomExampleValue();
        Assertions.assertTrue("|Eris|Dukes|".contains(String.format("|%s|", randomExample)),
                "Did not expect " + randomExample);
    }

    @Test
    public void canConfigureStringsToThrowErrorValidationErrorIfTooLarge(){

        final Field field = Field.is("field").
                makeMandatory().
                withDefaultValue("").
                withValidation(VRule.maximumLength(10));

        final ValidationReport report =
                field.validate(
                        FieldValue.is("field", "12345678901"));

        Assertions.assertFalse(report.isValid());
        report.getCombinedErrorMessages().contains("Maximum allowable length exceeded");
    }

    @Test
    public void canConfigureStringsToThrowErrorValidationErrorIfNotMatchRegex(){

        final Field field = Field.is("field").
                makeMandatory().
                withDefaultValue("").
                withValidation(VRule.matchesRegex("^Bug:.*"));


        final ValidationReport report =
                field.validate(
                        FieldValue.is("field", "ISSUE: reporting a bug - this is a bug"));

        Assertions.assertFalse(report.isValid());
        report.getCombinedErrorMessages().contains("not match");
    }

    @Test
    public void canConfigureStringToMatchMultipleRules(){

        final Field field = Field.is("field").
                makeMandatory().
                withDefaultValue("").
                withValidation(VRule.maximumLength(10),
                        VRule.satisfiesRegex("^Bug:"),
                        VRule.matchesRegex(".*ort$")
                );

        final ValidationReport report =
                field.validate(
                        FieldValue.is("field", "Bug: short"));

        Assertions.assertTrue(report.isValid(), report.getCombinedErrorMessages());
    }


}
