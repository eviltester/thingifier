package uk.co.compendiumdev.thingifier.domain.definitions;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.apiconfig.JsonOutputConfig;
import uk.co.compendiumdev.thingifier.domain.FieldType;
import uk.co.compendiumdev.thingifier.domain.instances.InstanceFields;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;
import uk.co.compendiumdev.thingifier.reporting.XmlThing;

public class ObjectFieldTest {

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
        final InstanceFields person = instance.getObjectInstance("person");
        person.addValue("firstname", "Connie");
        person.addValue("surname", "Dobbs");

    }


    @Test
    public void canCreateObjectField() {

        Assertions.assertEquals("Connie",
                instance.getObjectInstance("person").getValue("firstname"));
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

        Assertions.assertTrue(xmlOutput.contains("<person>"));
        Assertions.assertTrue(xmlOutput.contains("<firstname>Connie</firstname>"));
        Assertions.assertTrue(xmlOutput.contains("<surname>Dobbs</surname>"));
    }

    // todo REST API input of object fields

    }
