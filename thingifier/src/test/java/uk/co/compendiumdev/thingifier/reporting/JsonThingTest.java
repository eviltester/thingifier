package uk.co.compendiumdev.thingifier.reporting;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.apiconfig.JsonOutputConfig;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;

public class JsonThingTest {

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
