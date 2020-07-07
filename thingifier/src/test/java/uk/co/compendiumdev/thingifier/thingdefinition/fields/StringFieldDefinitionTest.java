package uk.co.compendiumdev.thingifier.thingdefinition.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.definitions.ThingDefinition;

public class StringFieldDefinitionTest {

    ThingDefinition entityTestSession;

    @BeforeEach
    public void createEntity(){

        entityTestSession = ThingDefinition.create("Test Session", "Test Sessions");
        entityTestSession.addFields(Field.is("defaultString"));
    }

    @Test
    public void byDefaultAFieldIsAnOptionalString(){

        ThingDefinition stringFieldEntity = ThingDefinition.create("Test Session", "Test Sessions");
        stringFieldEntity.addFields(Field.is("defaultString"));

        final Field field = stringFieldEntity.getField("defaultString");

        Assertions.assertEquals(FieldType.STRING, field.getType());
        Assertions.assertFalse(field.isMandatory());
    }

    @Test
    public void canHaveStringFieldWithExamples(){

        ThingDefinition stringFieldEntity = ThingDefinition.create("entity", "entities");
        stringFieldEntity.addField(Field.is("example").withExample("Eris").withExample("Dukes"));

        Assertions.assertEquals(
                2,stringFieldEntity.getField("example").getExamples().size());

        String randomExample = stringFieldEntity.getField("example").getRandomExampleValue();
        Assertions.assertTrue("|Eris|Dukes|".contains(String.format("|%s|", randomExample)),
                "Did not expect " + randomExample);
    }

    @Test
    public void examplesAreDefaultsWhenNoExamples(){

        ThingDefinition stringFieldEntity = ThingDefinition.create("entity", "entities");
        stringFieldEntity.addField(Field.is("example").withDefaultValue("default"));

        Assertions.assertEquals(
                1,stringFieldEntity.getField("example").getExamples().size());

        String randomExample = stringFieldEntity.getField("example").getRandomExampleValue();
        Assertions.assertEquals("default", randomExample);
    }


}
