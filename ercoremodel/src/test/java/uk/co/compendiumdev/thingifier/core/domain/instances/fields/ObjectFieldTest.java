package uk.co.compendiumdev.thingifier.core.domain.instances.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.VRule;

public class ObjectFieldTest {

    EntityDefinition defn;
    EntityInstance instance;

    @BeforeEach
    public void createThingWithNestedObjectField(){

        defn = new EntityDefinition("thing", "things");

        defn.addField(Field.is("person", FieldType.OBJECT)
                .withField(
                        Field.is("firstname", FieldType.STRING).
                                withExample("Bob")).
                        withField(
                                Field.is("surname", FieldType.STRING).withExample("D'obbs")
                        ));


        instance = new EntityInstance(defn);
        instance.setValue("person.firstname", "Connie");
        instance.setValue("person.surname", "Dobbs");
    }

    @Test
    public void canCreateObjectField() {

        Assertions.assertEquals("Connie",
                instance.getFieldValue("person").asObject().
                        getFieldValue("firstname").asString());

        System.out.println(instance.toString());
    }

    @Test
    public void canValidateAtObjectFieldLevel() {

        defn.getField("person").
                getObjectDefinition().
                getField("surname").makeMandatory();

        instance = new EntityInstance(defn);
        instance.setValue("person.firstname", "Eris");

        final ValidationReport validation = instance.validate();

        Assertions.assertFalse(validation.isValid(), "surname should fail validation");
        Assertions.assertTrue(validation.getCombinedErrorMessages().contains("surname : field is mandatory"));
    }

    @Test
    public void canValidateAtObjectFieldLevelWithValidationRules() {

        defn.getField("person").
                getObjectDefinition().
                getField("surname").makeMandatory().
                withValidation(VRule.notEmpty());

        instance = new EntityInstance(defn);
        instance.setValue("person.firstname", "Eris");

        // bypass set validation rules and see if validation picks it up
        instance.overrideValue("person.surname", "");

        final ValidationReport validation = instance.validate();
        Assertions.assertFalse(validation.isValid(), "surname should fail validation");
        Assertions.assertTrue(validation.getCombinedErrorMessages().contains("surname : can not be empty"));
    }

    @Test
    public void canValidateAtObjectFieldLevelWhenSettingValues() {

        defn.getField("person").
                getObjectDefinition().
                getField("surname").
                withValidation(VRule.notEmpty());

        instance = new EntityInstance(defn);
        instance.setValue("person.firstname", "Eris");

        final IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class,
                () -> {
                    instance.setValue("person.surname", "");
                }
        );

        Assertions.assertTrue(e.getMessage().contains("surname : can not be empty"));
    }


}
