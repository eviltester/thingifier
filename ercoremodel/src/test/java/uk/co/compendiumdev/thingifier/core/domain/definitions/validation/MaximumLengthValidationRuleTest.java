package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

class MaximumLengthValidationRuleTest {

    @Test
    void ruleFailsWhenNotSatisfied(){

        Field field = Field.is("name", FieldType.STRING);

        final ValidationRule rule;
        rule = new MaximumLengthValidationRule(10);

        Assertions.assertFalse(
                rule.validates(
                        FieldValue.is(field,"1234567890 Too Long"))
        );
    }

    @Test
    void maxLengthExceedsBoundaryTesting(){

        Field field = Field.is("name", FieldType.STRING);

        final ValidationRule rule;
        rule = new MaximumLengthValidationRule(10);

        Assertions.assertFalse(
                rule.validates(
                        FieldValue.is(field,"12345678901"))
        );
    }

    @Test
    void canPassExactLength(){

        Field field = Field.is("name", FieldType.STRING);

        final ValidationRule rule;
        rule = new MaximumLengthValidationRule(10);

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is(field,"1234567890"))
        );
    }

    @Test
    void canPassOneLess(){

        Field field = Field.is("name", FieldType.STRING);

        final ValidationRule rule;
        rule = new MaximumLengthValidationRule(10);

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is(field,"123456789"))
        );
    }

    @Test
    void emptyStringIsFine(){

        Field field = Field.is("name", FieldType.STRING);

        final ValidationRule rule;
        rule = new MaximumLengthValidationRule(10);

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is(field,""))
        );
    }

    @Test
    void nullStringIsFine(){

        Field field = Field.is("name", FieldType.STRING);

        final ValidationRule rule;
        rule = new MaximumLengthValidationRule(10);

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is(field,(String)null))
        );
    }


    @Test
    void validationMessageReturnsText(){

        Field field = Field.is("name", FieldType.STRING);

        final ValidationRule rule;
        rule = new MaximumLengthValidationRule(10);
        final String msg = rule.getErrorMessage(FieldValue.is(field, "bob"));

        Assertions.assertNotNull(msg);
        Assertions.assertTrue(msg.trim().length()>0);
        Assertions.assertTrue(msg.contains("10"));
    }

    @Test
    void canCreateRuleUsingVRule(){

        Field field = Field.is("name", FieldType.STRING);

        final ValidationRule rule;
        rule = VRule.maximumLength(10);

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is(field,"12345"))
        );

        Assertions.assertFalse(
                rule.validates(
                        FieldValue.is(field,"12345678901"))
        );
    }
}
