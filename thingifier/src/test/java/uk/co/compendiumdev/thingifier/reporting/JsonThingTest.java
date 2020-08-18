package uk.co.compendiumdev.thingifier.reporting;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.apiconfig.JsonOutputConfig;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.ThingInstance;

public class JsonThingTest {

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


        instance = ThingInstance.create(defn);
        instance.setValue("person.firstname", "Connie");
        instance.setValue("person.surname", "Dobbs");
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
}
