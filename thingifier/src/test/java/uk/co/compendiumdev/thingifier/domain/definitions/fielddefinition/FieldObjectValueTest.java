package uk.co.compendiumdev.thingifier.domain.definitions.fielddefinition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.domain.instances.InstanceFields;

public class FieldObjectValueTest {

    private Field field;

    @BeforeEach
    public void defineObjectField(){

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

        final FieldValue person =
                            FieldValue.is("person",
                                    new InstanceFields(
                                            field.getObjectDefinition()));

        person.asObject().setValue("firstname", "Connie");
        person.asObject().setValue("surname", "dobbs");

        Assertions.assertEquals("Connie",
                person.asObject().getFieldValue("firstname")
                        .asString());
    }

    @Test
    public void canCloneObjectField() {

        final FieldValue person =
                FieldValue.is("person",
                        new InstanceFields(
                                field.getObjectDefinition()));

        person.asObject().setValue("firstname", "Connie");
        person.asObject().setValue("surname", "dobbs");

        final FieldValue connieClone = person.cloned();

        Assertions.assertEquals("Connie",
                connieClone.asObject().getFieldValue("firstname")
                        .asString());

        Assertions.assertNotSame(person, connieClone);
    }
}
