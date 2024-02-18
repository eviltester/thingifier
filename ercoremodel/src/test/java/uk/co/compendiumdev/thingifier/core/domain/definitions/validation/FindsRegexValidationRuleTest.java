package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

class FindsRegexValidationRuleTest {

    @Test
    void ruleFailsWhenNotSatisfied(){

        Field field = Field.is("name", FieldType.STRING);

        final ValidationRule rule;
        rule = new FindsRegexValidationRule("rules");

        Assertions.assertFalse(
                rule.validates(
                        FieldValue.is(field,"bob rooles init"))
        );
    }

    @Test
    void canFindAStringInAText(){

        Field field = Field.is("name", FieldType.STRING);

        final ValidationRule rule;
        rule = new FindsRegexValidationRule("rules");

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is(field,"bob rules init"))
        );
    }

    @Test
    void canFindAStringAtStartOfText(){

        Field field = Field.is("name", FieldType.STRING);

        final ValidationRule rule;
        rule = new FindsRegexValidationRule("bob");

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is(field ,"bob rules init"))
        );
    }

    @Test
    void canFindAStringAtEndOfText(){

        Field field = Field.is("name", FieldType.STRING);

        final ValidationRule rule;
        rule = new FindsRegexValidationRule("init");

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is(field,"bob rules init"))
        );
    }

    @Test
    void canFindARegexInText(){

        Field field = Field.is("name", FieldType.STRING);

        final ValidationRule rule;
        rule = new FindsRegexValidationRule(".n[ieo]t");

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is(field,"bob rules init"))
        );
    }

    @Test
    void validationMessageReturnsText(){

        Field field = Field.is("name", FieldType.STRING);

        final ValidationRule rule;
        rule = new FindsRegexValidationRule("ob");
        final String msg = rule.getErrorMessage(FieldValue.is(field, "bob"));

        Assertions.assertNotNull(msg);
        Assertions.assertTrue(msg.trim().length()>0);
        Assertions.assertTrue(msg.contains(" ob"));
        Assertions.assertTrue(msg.contains("name"));
    }

    @Test
    void canCreateRuleUsingVRule(){

        Field field = Field.is("name", FieldType.STRING);

        final ValidationRule rule;
        rule = VRule.satisfiesRegex("rules");

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is(field,"bob rules init"))
        );
    }
}
