package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

class NotEmptyValidationRuleTest {

    @Test
    void ruleFailsWhenNotSatisfied(){

        final ValidationRule rule;
        rule = new NotEmptyValidationRule();

        Assertions.assertFalse(
                rule.validates(
                        FieldValue.is("name",""))
        );
    }

    @Test
    void ruleFailsWhenNull(){

        final ValidationRule rule;
        rule = new NotEmptyValidationRule();

        Assertions.assertFalse(
                rule.validates(
                        FieldValue.is("name",(String)null))
        );
    }

    @Test
    void rulePassesWithText(){

        final ValidationRule rule;
        rule = new NotEmptyValidationRule();

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is("name","A name"))
        );
    }



    @Test
    void validationMessageReturnsText(){
        final ValidationRule rule;
        rule = new NotEmptyValidationRule();
        final String msg = rule.getErrorMessage(FieldValue.is("name", "bob"));

        Assertions.assertNotNull(msg);
        Assertions.assertTrue(msg.trim().length()>0);
        Assertions.assertTrue(msg.contains("name"));
    }

    @Test
    void canCreateRuleUsingVRule(){

        final ValidationRule rule;
        rule = VRule.notEmpty();

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is("name","12345"))
        );

        Assertions.assertFalse(
                rule.validates(
                        FieldValue.is("name",""))
        );
    }
}
