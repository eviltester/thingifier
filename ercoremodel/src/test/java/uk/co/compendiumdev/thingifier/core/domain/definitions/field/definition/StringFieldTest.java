package uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.VRule;

class StringFieldTest {

    @Test
    void byDefaultAFieldIsAnOptionalString(){

        final Field field = Field.is("defaultString", FieldType.STRING);
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
    void stringWithoutExamplesIsRandom(){

        final Field field = Field.is("example", FieldType.STRING);

        final String example = field.getRandomExampleValue();
        System.out.println(example);
        Assertions.assertNotNull(example);
        Assertions.assertFalse(example.trim().isEmpty());
        Assertions.assertTrue(example.length()<=20);
    }

    @Test
    void canConfigureStringsToThrowErrorValidationErrorIfTooLarge(){

        final Field field = Field.is("field", FieldType.STRING).
                makeMandatory().
                withDefaultValue("").
                withValidation(VRule.maximumLength(10));

        final ValidationReport report =
                field.validate(
                        FieldValue.is(field, "12345678901"));

        Assertions.assertFalse(report.isValid());
        Assertions.assertTrue(report.getCombinedErrorMessages().contains("Maximum allowable length exceeded"),
                "expected error message - Maximum allowable length exceeded but was " + report.getCombinedErrorMessages());
    }

    @Test
    void canConfigureStringsToThrowErrorValidationErrorIfNotMatchRegex(){

        final Field field = Field.is("field", FieldType.STRING).
                makeMandatory().
                withDefaultValue("").
                withValidation(VRule.matchesRegex("^Bug:.*"));

        final ValidationReport report =
                field.validate(
                        FieldValue.is(field, "ISSUE: reporting a bug - this is a bug"));

        Assertions.assertFalse(report.isValid());
        Assertions.assertTrue(report.getCombinedErrorMessages().contains("not match"),
                "expected error message - not match but was " + report.getCombinedErrorMessages());
    }

    @Test
    void canConfigureStringToMatchMultipleRules(){

        final Field field = Field.is("field", FieldType.STRING).
                makeMandatory().
                withDefaultValue("").
                withValidation(VRule.maximumLength(10),
                        VRule.satisfiesRegex("^Bug:"),
                        VRule.matchesRegex(".*ort$")
                );

        final ValidationReport report =
                field.validate(
                        FieldValue.is(field, "Bug: short"));

        Assertions.assertTrue(report.isValid(), report.getCombinedErrorMessages());
    }

    @Test
    void byDefaultTheStringHasNoValidationRules(){

        final Field field = Field.is("field", FieldType.STRING);

        Assertions.assertNotNull(field.validationRules());
        Assertions.assertEquals(0,
                field.validationRules().size());
    }

    @Test
    void stringFieldTruncation(){

        final Field field = Field.is("field", FieldType.STRING).
                truncateStringTo(4);

        Assertions.assertEquals("1234",
                field.getActualValueToAdd(
                    FieldValue.is(field, "12345")));

        Assertions.assertEquals("123",
                field.getActualValueToAdd(
                        FieldValue.is(field, "123")));
    }
}
