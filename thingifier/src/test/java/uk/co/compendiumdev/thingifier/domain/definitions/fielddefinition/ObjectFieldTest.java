package uk.co.compendiumdev.thingifier.domain.definitions.fielddefinition;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.apiconfig.JsonOutputConfig;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.domain.definitions.validation.VRule;
import uk.co.compendiumdev.thingifier.domain.instances.InstanceFields;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;
import uk.co.compendiumdev.thingifier.reporting.XmlThing;

public class ObjectFieldTest {

    private Field field;

    @BeforeEach
    public void createThingWithNestedObjectField(){

        field = Field.is("person", FieldType.OBJECT)
                .withField(
                        Field.is("firstname").
                                withExample("Bob")).
                        withField(
                                Field.is("surname").withExample("D'obbs")
                        );

    }

    @Test
    public void canCreateObjectField() {

        Assertions.assertEquals(FieldType.OBJECT, field.getType());
        Assertions.assertEquals("Bob",
                            field.getObjectDefinition().
                                getField("firstname").
                                    getRandomExampleValue());
    }

    @Test
    public void canValidateAtObjectFieldLevel() {

        field.
                getObjectDefinition().
                getField("surname").makeMandatory();

        final FieldValue value = FieldValue.is("person",
                new InstanceFields(field.getObjectDefinition()));

        final ValidationReport validation = field.validate(value);

        Assertions.assertFalse(validation.isValid(), "surname should fail validation");
        Assertions.assertTrue(validation.getCombinedErrorMessages().contains("surname : field is mandatory"));
    }

    @Test
    public void canEnforceObjectValuesWithValidationRules() {

        field.
                getObjectDefinition().
                getField("surname").makeMandatory().
                withValidation(VRule.notEmpty());

        final FieldValue value = FieldValue.is("person",
                new InstanceFields(field.getObjectDefinition()));
        value.asObject().setValue("firstname", "Eris");

        Assertions.assertThrows(IllegalArgumentException.class,
                () ->{value.asObject().setValue("surname", "");}
                );
    }

    @Test
    public void canValidateAtObjectFieldLevelWithValidationRules() {

        field.
                getObjectDefinition().
                getField("surname").makeMandatory().
                withValidation(VRule.notEmpty());

        final FieldValue value = FieldValue.is("person",
                new InstanceFields(field.getObjectDefinition()));
        value.asObject().setValue("firstname", "Eris");

        // bypass validation with put
        value.asObject().putValue("surname", "");

        final ValidationReport validation = field.validate(value);
        Assertions.assertFalse(validation.isValid(), "surname should fail validation");
        Assertions.assertTrue(validation.getCombinedErrorMessages().contains("surname : can not be empty"));
    }


    @Test
    public void canCloneAnObjectField(){

        final FieldValue value = FieldValue.is("person",
                new InstanceFields(field.getObjectDefinition()));
        value.asObject().setValue("firstname", "Connie");
        value.asObject().setValue("surname", "");


        final FieldValue connieClone = value.cloned();

        System.out.println(value);

        Assertions.assertEquals(
                connieClone.getName(),
                value.getName()
        );

        Assertions.assertEquals(
                connieClone.asObject().getFieldValue("firstname").asString(),
                value.asObject().getFieldValue("firstname").asString()
                );

        Assertions.assertEquals(
                connieClone.asObject().getFieldValue("surname").asString(),
                value.asObject().getFieldValue("surname").asString()
        );

        Assertions.assertNotSame(connieClone, value);
    }


}
