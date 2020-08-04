package uk.co.compendiumdev.thingifier.domain.definitions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.domain.FieldType;
import uk.co.compendiumdev.thingifier.domain.instances.ThingInstance;

public class ObjectFieldTest {

    @Test
    public void canCreateObjectField() {

        ThingDefinition defn = ThingDefinition.
                create("thing", "things");

        defn.addField(Field.is("person", FieldType.OBJECT)
                        .withField(
                                Field.is("firstname").
                                        withExample("Bob")).
                        withField(
                                Field.is("surname").withExample("D'obbs")
                        ));


        final ThingInstance instance = new ThingInstance(defn);
        final ThingInstance person = instance.getObjectInstance("person");
        person.setValue("firstname", "Connie");
        person.setValue("surname", "Dobbs");

        Assertions.assertEquals("Connie",
                instance.getObjectInstance("person").getValue("firstname"));
    }

    // TODO JsonThing handling for nested objects
}
