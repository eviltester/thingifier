package uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

import java.util.List;

class AutoIncrementIdFieldTest {

    @Test
    void defaultValueForAnAutoIncrementIdIsOne(){

        final Field field = Field.is("id", FieldType.AUTO_INCREMENT);

        Assertions.assertEquals(1, field.getDefaultValue().asInteger());
    }


    @Test
    void normalValidateAgainstTypeForIdDoesNotAllowSetting(){

        final Field field = Field.is("id", FieldType.AUTO_INCREMENT);

        final ValidationReport report = field.validate(
                                            FieldValue.is(field, "1"));
        Assertions.assertFalse(report.isValid());
    }

    @Test
    void canValidateForIdAllowingSetting(){
        // e.g. for cloning, and for setting objects
        final Field field = Field.is("id", FieldType.AUTO_INCREMENT);

        final ValidationReport report =
                field.validate(FieldValue.is(field, "1"),
                        true);
        Assertions.assertTrue(report.isValid());
    }

    @Test
    void examplesIDIsOneExample() {

        final Field field = Field.is("id", FieldType.AUTO_INCREMENT);

        final List<String> examples = field.getExamples();

        Assertions.assertEquals(1, examples.size());
    }

    @Test
    void exampleIDIsAnIntegerBetween_1_and_100(){

        final Field field = Field.is("id", FieldType.AUTO_INCREMENT);

        for(int x=0; x<100; x++){

            final String example = field.getRandomExampleValue();

            final int exampleAsInteger = Integer.parseInt(example);

            Assertions.assertTrue(exampleAsInteger >= 1,
                    "example id should be greater than or equal to 1 but was "
                            + exampleAsInteger);

            Assertions.assertTrue(exampleAsInteger <= 100,
                    "example id should be less than or equal to 100 but was "
                            + exampleAsInteger);
        }

    }
}
