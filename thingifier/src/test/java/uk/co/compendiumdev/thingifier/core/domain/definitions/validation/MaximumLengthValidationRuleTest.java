package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

class MaximumLengthValidationRuleTest {

    @Test
    void ruleFailsWhenNotSatisfied(){

        final ValidationRule rule;
        rule = new MaximumLengthValidationRule(10);

        Assertions.assertFalse(
                rule.validates(
                        FieldValue.is("name","1234567890 Too Long"))
        );
    }

    @Test
    void maxLengthExceedsBoundaryTesting(){

        final ValidationRule rule;
        rule = new MaximumLengthValidationRule(10);

        Assertions.assertFalse(
                rule.validates(
                        FieldValue.is("name","12345678901"))
        );
    }

    @Test
    void canPassExactLength(){

        final ValidationRule rule;
        rule = new MaximumLengthValidationRule(10);

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is("name","1234567890"))
        );
    }

    @Test
    void canPassOneLess(){

        final ValidationRule rule;
        rule = new MaximumLengthValidationRule(10);

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is("name","123456789"))
        );
    }

    @Test
    void emptyStringIsFine(){

        final ValidationRule rule;
        rule = new MaximumLengthValidationRule(10);

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is("name",""))
        );
    }

    @Test
    void nullStringIsFine(){

        final ValidationRule rule;
        rule = new MaximumLengthValidationRule(10);

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is("name",(String)null))
        );
    }


    @Test
    void validationMessageReturnsText(){
        final ValidationRule rule;
        rule = new MaximumLengthValidationRule(10);
        final String msg = rule.getErrorMessage(FieldValue.is("name", "bob"));

        Assertions.assertNotNull(msg);
        Assertions.assertTrue(msg.trim().length()>0);
        Assertions.assertTrue(msg.contains("10"));
    }

    @Test
    void canCreateRuleUsingVRule(){

        final ValidationRule rule;
        rule = VRule.maximumLength(10);

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is("name","12345"))
        );

        Assertions.assertFalse(
                rule.validates(
                        FieldValue.is("name","12345678901"))
        );
    }
}
