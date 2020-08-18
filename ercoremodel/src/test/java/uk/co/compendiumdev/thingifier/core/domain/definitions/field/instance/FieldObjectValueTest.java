package uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.InstanceFields;

class FieldObjectValueTest {

    private Field field;

    @BeforeEach
    void defineObjectField(){

        field = Field.is("person", FieldType.OBJECT)
                .withField(
                        Field.is("firstname").
                                withExample("Bob")).
                        withField(
                                Field.is("surname").withExample("D'obbs")
                        );

    }

    @Test
    void canCreateObjectField() {

        final FieldValue person =
                            FieldValue.is("person",
                                    new InstanceFields(
                                            field.getObjectDefinition()));

        person.asObject().setValue("firstname", "Connie");
        person.asObject().setValue("surname", "dobbs");

        System.out.println(person);

        Assertions.assertEquals("Connie",
                person.asObject().getFieldValue("firstname")
                        .asString());
    }

    @Test
    void canCloneObjectField() {

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
