package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

class NotEmptyValidationRuleTest {

    @Test
    void ruleFailsWhenNotSatisfied(){

        Field field = Field.is("name", FieldType.STRING);

        final ValidationRule rule;
        rule = new NotEmptyValidationRule();

        Assertions.assertFalse(
                rule.validates(
                        FieldValue.is(field,""))
        );
    }

    @Test
    void ruleFailsWhenNull(){

        Field field = Field.is("name", FieldType.STRING);

        final ValidationRule rule;
        rule = new NotEmptyValidationRule();

        Assertions.assertFalse(
                rule.validates(
                        FieldValue.is(field,(String)null))
        );
    }

    @Test
    void rulePassesWithText(){

        Field field = Field.is("name", FieldType.STRING);

        final ValidationRule rule;
        rule = new NotEmptyValidationRule();

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is(field,"A name"))
        );
    }



    @Test
    void validationMessageReturnsText(){

        Field field = Field.is("name", FieldType.STRING);

        final ValidationRule rule;
        rule = new NotEmptyValidationRule();
        final String msg = rule.getErrorMessage(FieldValue.is(field, "bob"));

        Assertions.assertNotNull(msg);
        Assertions.assertTrue(msg.trim().length()>0);
        Assertions.assertTrue(msg.contains("name"));
    }

    @Test
    void canCreateRuleUsingVRule(){

        Field field = Field.is("name", FieldType.STRING);

        final ValidationRule rule;
        rule = VRule.notEmpty();

        Assertions.assertTrue(
                rule.validates(
                        FieldValue.is(field,"12345"))
        );

        Assertions.assertFalse(
                rule.validates(
                        FieldValue.is(field,""))
        );
    }
}
