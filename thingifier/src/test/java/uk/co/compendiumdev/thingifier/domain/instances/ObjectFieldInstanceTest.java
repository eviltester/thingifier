package uk.co.compendiumdev.thingifier.domain.instances;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.api.ValidationReport;
import uk.co.compendiumdev.thingifier.apiconfig.JsonOutputConfig;
import uk.co.compendiumdev.thingifier.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.domain.definitions.fielddefinition.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.fielddefinition.FieldType;
import uk.co.compendiumdev.thingifier.domain.definitions.validation.VRule;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;
import uk.co.compendiumdev.thingifier.reporting.XmlThing;

public class ObjectFieldInstanceTest {

    ThingDefinition defn;
    ThingInstance instance;

    @BeforeEach
    public void createThingWithNestedObjectField(){

        defn = ThingDefinition.
                create("thing", "things");

        defn.addField(Field.is("person", FieldType.OBJECT)
                .withField(
                        Field.is("firstname").
                                withExample("Bob")).
                        withField(
                                Field.is("surname").withExample("D'obbs")
                        ));


        instance = new ThingInstance(defn);
        instance.setValue("person.firstname", "Connie");
        instance.setValue("person.surname", "Dobbs");
    }

    @Test
    public void canCreateObjectField() {

        Assertions.assertEquals("Connie",
                instance.getFieldValue("person").asObject().
                        getFieldValue("firstname").asString());
    }

    @Test
    public void canValidateAtObjectFieldLevel() {

        defn.getField("person").
                getObjectDefinition().
                getField("surname").makeMandatory();

        instance = new ThingInstance(defn);
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

        instance = new ThingInstance(defn);
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

        instance = new ThingInstance(defn);
        instance.setValue("person.firstname", "Eris");

        final IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class,
                () -> {
                    instance.setValue("person.surname", "");
                }
        );

        Assertions.assertTrue(e.getMessage().contains("surname : can not be empty"));
    }


    @Test
    public void outputAsJson(){

        final JsonThing jsonThing = new JsonThing(new JsonOutputConfig());

        final JsonObject jsonObj = jsonThing.asJsonObject(instance);

        final String jsonOutput = new GsonBuilder().setPrettyPrinting().create().toJson(jsonObj);

        System.out.println(jsonOutput);

        //"person": {
        // "firstname": "Connie",
        //  "surname": "Dobbs"
        Assertions.assertTrue(jsonOutput.contains("\"person\": {"));
        Assertions.assertTrue(jsonOutput.contains("\"firstname\": \"Connie\","));
        Assertions.assertTrue(jsonOutput.contains("\"surname\": \"Dobbs\""));
    }

    @Test
    public void outputAsXml(){
        final JsonThing jsonThing = new JsonThing(new JsonOutputConfig());

        final String xmlOutput = new XmlThing(jsonThing).getSingleObjectXml(instance);
        System.out.println(xmlOutput);

        Assertions.assertTrue(xmlOutput.contains("<thing>"));
        Assertions.assertTrue(xmlOutput.contains("<person>"));
        Assertions.assertTrue(xmlOutput.contains("<firstname>Connie</firstname>"));
        Assertions.assertTrue(xmlOutput.contains("<surname>Dobbs</surname>"));
    }

    }
