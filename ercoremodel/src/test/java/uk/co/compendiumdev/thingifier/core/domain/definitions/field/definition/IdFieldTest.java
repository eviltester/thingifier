package uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

import java.util.List;

class IdFieldTest {

    @Test
    void defaultValueForAnIdIsNull(){

        final Field field = Field.is("id", FieldType.ID);

        Assertions.assertNull(field.getDefaultValue().asString());
    }

    @Test
    void idFieldNextValueStartsAt_1(){

        final Field field = Field.is("id", FieldType.ID);

        String value = field.getNextIdValue();
        Assertions.assertEquals("1", value);
    }

    @Test
    void idAdjustedInFieldWhenHighValueEnsured(){

        final Field field = Field.is("id", FieldType.ID);

        field.ensureNextIdAbove("10");

        Assertions.assertEquals("11", field.getNextIdValue());
        Assertions.assertEquals("12", field.getNextIdValue());
    }

    @Test
    void normalValidateAgainstTypeForIdDoesNotAllowSetting(){

        final Field field = Field.is("id", FieldType.ID);

        final ValidationReport report = field.validate(
                                            FieldValue.is("id", "1"));
        Assertions.assertFalse(report.isValid());
    }

    @Test
    void canValidateForIdAllowingSetting(){
        // e.g. for cloning, and for setting objects
        final Field field = Field.is("id", FieldType.ID);

        final ValidationReport report =
                field.validate(FieldValue.is("id", "1"),
                        true);
        Assertions.assertTrue(report.isValid());
    }

    @Test
    void examplesIDIsOneExample() {

        final Field field = Field.is("id", FieldType.ID);

        final List<String> examples = field.getExamples();

        Assertions.assertEquals(1, examples.size());
    }

    @Test
    void exampleIDIsAnIntegerBetween_1_and_100(){

        final Field field = Field.is("id", FieldType.ID);

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
