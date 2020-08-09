package uk.co.compendiumdev.thingifier.domain.definitions.field.definition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.domain.definitions.validation.VRule;

class StringFieldTest {

    @Test
    void byDefaultAFieldIsAnOptionalString(){

        final Field field = Field.is("defaultString");
        Assertions.assertEquals(FieldType.STRING, field.getType());
        Assertions.assertFalse(field.isMandatory());
    }

    @Test
    void canHaveStringFieldWithExamples(){

        final Field field = Field.is("example", FieldType.STRING)
                .withExample("Eris").withExample("Dukes");

        Assertions.assertEquals(
                2,field.getExamples().size());

        String randomExample = field.getRandomExampleValue();
        Assertions.assertTrue("|Eris|Dukes|".contains(String.format("|%s|", randomExample)),
                "Did not expect " + randomExample);
    }

    @Test
    void canConfigureStringsToThrowErrorValidationErrorIfTooLarge(){

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
    void canConfigureStringsToThrowErrorValidationErrorIfNotMatchRegex(){

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
    void canConfigureStringToMatchMultipleRules(){

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
