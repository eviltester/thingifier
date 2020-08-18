package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

class MatchesRegexValidationRuleTest {

    @Test
    void ruleFailsWhenNotSatisfied(){

        final ValidationRule rule;
        rule = new FindsRegexValidationRule(".*rules.*");

        Assertions.assertFalse(
                rule.validates(
                        FieldValue.is("name","bob rooles init"))
        );
    }

    @Test
    void canFindAStringInAText(){

        final ValidationRule rule;
        rule = new MatchesRegexValidationRule(".*rules.*");

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is("name","bob rules init"))
        );
    }

    @Test
    void canFindAStringAtStartOfText(){

        final ValidationRule rule;
        rule = new MatchesRegexValidationRule("^bob.*$");

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is("name","bob rules init"))
        );
    }

    @Test
    void canFindAStringAtEndOfText(){

        final ValidationRule rule;
        rule = new MatchesRegexValidationRule(".*init$");

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is("name","bob rules init"))
        );
    }

    @Test
    void canFindARegexInText(){

        final ValidationRule rule;
        rule = new MatchesRegexValidationRule(".*.n[ieo]t");

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is("name","bob rules init"))
        );
    }

    @Test
    void validationMessageReturnsText(){
        final ValidationRule rule;
        rule = new MatchesRegexValidationRule("ob");
        final String msg = rule.getErrorMessage(FieldValue.is("name", "bob"));

        Assertions.assertNotNull(msg);
        Assertions.assertTrue(msg.trim().length()>0);
        Assertions.assertTrue(msg.contains(" ob"));
        Assertions.assertTrue(msg.contains("name"));
    }

    @Test
    void canCreateRuleUsingVRule(){

        final ValidationRule rule;
        rule = VRule.matchesRegex(".*rules.*");

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is("name","bob rules init"))
        );
    }
}
