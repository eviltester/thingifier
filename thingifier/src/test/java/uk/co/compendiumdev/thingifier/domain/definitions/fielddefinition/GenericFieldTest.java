package uk.co.compendiumdev.thingifier.domain.definitions.fielddefinition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.domain.definitions.fielddefinition.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GenericFieldTest {

    @Test
    public void byDefaultAFieldIsString(){

        final Field field = Field.is("CompletedStatus");
        Assertions.assertEquals(FieldType.STRING, field.getType());
    }

    @Test
    public void defaultValuesAreReturned(){

        final Field field = Field.is("CompletedStatus").withDefaultValue("Not Completed");
        Assertions.assertEquals("Not Completed", field.getDefaultValue().asString());
    }

    @Test
    public void canCreateFieldWithExamples(){

        final Field field = Field.is("complicated").withExample("nuclear physics");

        Assertions.assertEquals(1,
                field.getExamples().size());

        Assertions.assertEquals("nuclear physics",
                        field.getRandomExampleValue());
    }

    @Test
    public void examplesAreDefaultsWhenNoExamples(){

        final Field field = Field.is("example").withDefaultValue("default");

        Assertions.assertEquals(
                1,field.getExamples().size());

        String randomExample = field.getRandomExampleValue();
        Assertions.assertEquals("default", randomExample);
    }

    @Test
    public void randomExamplesReturnsAllExamples(){

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
