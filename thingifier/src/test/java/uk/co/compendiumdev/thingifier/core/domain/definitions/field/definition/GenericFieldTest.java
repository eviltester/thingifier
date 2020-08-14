package uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class GenericFieldTest {

    @Test
    void byDefaultAFieldIsString(){

        final Field field = Field.is("CompletedStatus");
        Assertions.assertEquals(FieldType.STRING, field.getType());
    }

    @Test
    void defaultValuesAreReturned(){

        final Field field = Field.is("CompletedStatus");

        Assertions.assertFalse(field.hasDefaultValue());

        field.withDefaultValue("Not Completed");

        Assertions.assertEquals("Not Completed", field.getDefaultValue().asString());
    }

    @Test
    void canCreateFieldWithExamples(){

        final Field field = Field.is("complicated").withExample("nuclear physics");

        Assertions.assertEquals(1,
                field.getExamples().size());

        Assertions.assertEquals("nuclear physics",
                        field.getRandomExampleValue());
    }

    @Test
    void examplesAreDefaultsWhenNoExamples(){

        final Field field = Field.is("example").withDefaultValue("default");

        Assertions.assertEquals(
                1,field.getExamples().size());

        String randomExample = field.getRandomExampleValue();
        Assertions.assertEquals("default", randomExample);
    }

    @Test
    void randomExamplesReturnsAllExamples(){

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("nuclear physics");
        expectedValues.add("people");
        expectedValues.add("decisions");

        final Field field = Field.is("complicated").
                withExample("nuclear physics").
                withExample("people").
                withExample("decisions");

        Assertions.assertEquals(3, field.getExamples().size());

        int tries = 0;
        // potential for intermittency,
        // but it really shouldn't take 100 tries to generate all out of 3
        while(expectedValues.size()!=0 && tries<100){

            String randomValue = field.getRandomExampleValue();
            System.out.println(randomValue);
            expectedValues.remove(randomValue);

            tries++;
        }

        System.out.println("all random examples generated in " + tries);
        Assertions.assertEquals(0, expectedValues.size());
    }


}
