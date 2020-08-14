package uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

import java.util.List;

class EnumFieldTest {

    @Test
    void byDefaultAnEnumFieldIsEmpty(){

        final Field field = Field.is("enum", FieldType.ENUM);

        Assertions.assertEquals("", field.getDefaultValue().asString());
    }

    @Test
    void enumFieldCanHaveADefault(){
        // todo: currently an enum is constructed by default and examples
        //  should it be withValue(), withDefaultValue()? and example is drawn from values
        final Field field = Field.is("enum", FieldType.ENUM).
                withDefaultValue("bob").
                withExample("connie");

        Assertions.assertEquals("bob", field.getDefaultValue().asString());
    }

    @Test
    void examplesAreAllEnumValues(){

        final Field field = Field.is("enum", FieldType.ENUM).
                withDefaultValue("bob").
                withExample("connie").
                withExample("eris");

        final List<String> examples = field.getExamples();

        Assertions.assertEquals(3, examples.size());

        Assertions.assertTrue(examples.contains("bob"), "Missing bob");
        Assertions.assertTrue(examples.contains("connie"), "Missing connie");
        Assertions.assertTrue(examples.contains("eris"), "Missing eris");
    }

    @Test
    void randomExampleIsOneOfTheEnumValues(){

        final Field field = Field.is("enum", FieldType.ENUM).
                withDefaultValue("bob").
                withExample("connie").
                withExample("eris");

        final String validValues = "bob,connie,eris,";
        for(int x=0; x<100;x++){
            String randomValue = field.getRandomExampleValue();
            Assertions.assertTrue(
                    validValues.contains( randomValue + ","),
                        "Did not expect example: " + randomValue);
        }
    }

    // todo: we should have a model verification stage that reports this as an error
    @Test
    void whenNoExamplesSetupRandomIsEmpty(){

        final Field field = Field.is("enum", FieldType.ENUM);

        Assertions.assertEquals("",
                field.getRandomExampleValue());
    }

    @Test
    void canValidateAgainstDefaultAndExamples(){

        final Field field = Field.is("enum", FieldType.ENUM).
                withDefaultValue("bob").
                withExample("connie").
                withExample("eris");

        Assertions.assertTrue(
                field.validate(
                        FieldValue.is("enum", "bob"))
                        .isValid()
        );

        Assertions.assertTrue(
                field.validate(
                        FieldValue.is("enum", "connie"))
                        .isValid()
        );

        Assertions.assertTrue(
                field.validate(
                        FieldValue.is("enum", "eris"))
                        .isValid()
        );

        Assertions.assertFalse(
                field.validate(
                        FieldValue.is("enum", "dobbs"))
                        .isValid()
        );


    }
}
