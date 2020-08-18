package uk.co.compendiumdev.thingifier.core.domain.instances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.DefinedFields;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class InstanceFieldsTest {

    @Test
    void canCreateInstanceFields() {

        InstanceFields instance = new InstanceFields(new DefinedFields());

        Assertions.assertNotNull(instance);

        Assertions.assertNotNull(instance.getDefinition());
        Assertions.assertTrue(instance.getDefinition().getFieldNames().isEmpty(), "expect no fields");

        System.out.println(instance.toString());
    }

    @Test
    void canDefineInstanceWithFields() {

        DefinedFields fieldsDefn = new DefinedFields();
        fieldsDefn.addField(Field.is("Ref"));

        InstanceFields instance = new InstanceFields(fieldsDefn);
        Assertions.assertEquals(1, instance.getDefinition().getFieldNames().size());

        System.out.println(instance.toString());
    }

    @Test
    void canDefineInstanceWithFieldsAndGetTypeDefaultValue() {

        DefinedFields fieldsDefn = new DefinedFields();
        fieldsDefn.addField(Field.is("Ref"));

        InstanceFields instance = new InstanceFields(fieldsDefn);

        Assertions.assertEquals("", instance.getFieldValue("Ref").asString(),
                "Expected default value empty");

        System.out.println(instance.toString());
    }

    @Test
    void canDefineInstanceWithFieldsAndGetDefaultValue() {

        DefinedFields fieldsDefn = new DefinedFields();
        fieldsDefn.addField(Field.is("Ref").withDefaultValue("bob"));

        InstanceFields instance = new InstanceFields(fieldsDefn);

        Assertions.assertEquals("bob", instance.getFieldValue("Ref").asString(),
                "Expected default value bob");
        System.out.println(instance.toString());
    }

    @Test
    void canSetAndGetValuesForAField() {

        DefinedFields fieldsDefn = new DefinedFields();
        fieldsDefn.addField(Field.is("Ref"));

        InstanceFields instance = new InstanceFields(fieldsDefn);

        instance.setValue("Ref", "Reference");

        Assertions.assertNotNull(instance.getAssignedValue("Ref"), "fields did not contain 'ref'");

        Assertions.assertEquals("Reference", instance.getFieldValue("Ref").asString());

        System.out.println(instance.toString());

    }

    @Test
    void byDefaultIdFieldsAreNotInstantiated() {

        DefinedFields fieldsDefn = new DefinedFields();
        fieldsDefn.addField(Field.is("id", FieldType.ID));

        InstanceFields instance = new InstanceFields(fieldsDefn);

        // todo: create a NULL field Value
        Assertions.assertNull(instance.getFieldValue("id"));
    }

    @Test
    void weCanInstantiateTheIdsAfterCreation() {

        DefinedFields fieldsDefn = new DefinedFields();
        fieldsDefn.addField(Field.is("id", FieldType.ID));

        InstanceFields instance = new InstanceFields(fieldsDefn);

        instance.addIdsToInstance();

        Assertions.assertNotNull(instance.getFieldValue("id"));
        Assertions.assertEquals("1", instance.getFieldValue("id").asString());
    }

    @Test
    void errorForGetUnknownField() {

        InstanceFields instance = new InstanceFields(new DefinedFields());

        // todo: create a NULL field Value
        final RuntimeException e = Assertions.assertThrows(RuntimeException.class,
                () -> instance.getFieldValue("id"));

        Assertions.assertEquals("Could not find field: id", e.getMessage());
    }

    @Test
    void cannotSetFieldThatDoesNotExist() {

        InstanceFields instance = new InstanceFields(new DefinedFields());

        final RuntimeException e = Assertions.assertThrows(RuntimeException.class,
                () -> instance.setValue("id", "bob"));

        Assertions.assertEquals("Could not find field: id", e.getMessage());
    }

    @Test
    void cannotPutFieldThatDoesNotExist() {

        InstanceFields instance = new InstanceFields(new DefinedFields());

        final RuntimeException e = Assertions.assertThrows(RuntimeException.class,
                () -> instance.putValue("id", "bob"));

        Assertions.assertEquals("Could not find field: id", e.getMessage());
    }

    @Test
    void cannotSetFieldThatFailsValidation() {

        DefinedFields fieldsDefn = new DefinedFields();
        fieldsDefn.addField(Field.is("int", FieldType.INTEGER));

        InstanceFields instance = new InstanceFields(fieldsDefn);

        final RuntimeException e = Assertions.assertThrows(RuntimeException.class,
                () -> instance.setValue("int", "bob"));

        Assertions.assertTrue(e.getMessage().contains("bob does not match type INTEGER"),
                e.getMessage());
    }

    @Test
    void canPutFieldToBypassValidation() {

        DefinedFields fieldsDefn = new DefinedFields();
        fieldsDefn.addField(Field.is("int", FieldType.INTEGER));

        InstanceFields instance = new InstanceFields(fieldsDefn);

        instance.putValue("int", "bob");

        Assertions.assertEquals("bob",
                instance.getFieldValue("int").asString());

        // but validation should fail
        final ValidationReport validation = instance.validateFields(new ArrayList<>(), false);
        Assertions.assertFalse(validation.isValid());
    }

    @Test
    void whenFieldIsNotAnObjectWeCannotSetFields() {

        DefinedFields fieldsDefn = new DefinedFields();
        fieldsDefn.addField(Field.is("intobj", FieldType.INTEGER));

        InstanceFields instance = new InstanceFields(fieldsDefn);

        final RuntimeException e = Assertions.assertThrows(RuntimeException.class,
                () -> instance.putValue("intobj.name", "bob"));

        Assertions.assertTrue(e.getMessage().contains(
                "Cannot reference fields on non object fields"),
                e.getMessage());
    }

    @Test
    void whenFieldIsAnObjectWeCanSetFields() {

        DefinedFields fieldsDefn = new DefinedFields();
        fieldsDefn.addField(
                Field.is("intobj", FieldType.OBJECT).
                        withField(Field.is("name"))
        );

        InstanceFields instance = new InstanceFields(fieldsDefn);

        instance.putValue("intobj.name", "bob");

        // todo: get field value should support path names
//        Assertions.assertEquals("bob",
//                instance.getFieldValue("intobj.name"));

        Assertions.assertEquals("bob",
                instance.getFieldValue("intobj").
                        asObject().getFieldValue("name").asString());
    }

    @Test
    void canDeleteFieldValues() {

        DefinedFields fieldsDefn = new DefinedFields();
        fieldsDefn.addFields(
                Field.is("firstname").withDefaultValue("bob"),
                Field.is("surname").withDefaultValue("dobbs"));

        InstanceFields instance = new InstanceFields(fieldsDefn);

        instance.setValue("firstname", "al");
        instance.setValue("surname", "me");

        Assertions.assertEquals("al",
                instance.getFieldValue("firstname").asString());
        Assertions.assertEquals("me",
                instance.getFieldValue("surname").asString());

        List<String> ignore = new ArrayList<>();
        ignore.add("surname");

        instance.deleteAllFieldValuesExcept(ignore);

        Assertions.assertEquals("bob",
                instance.getFieldValue("firstname").asString());
        Assertions.assertEquals("me",
                instance.getFieldValue("surname").asString());

    }

    @Test
    void canCloneInstanceFields() {

        DefinedFields fieldsDefn = new DefinedFields();
        fieldsDefn.addFields(
                Field.is("firstname").withDefaultValue("bob"),
                Field.is("surname").withDefaultValue("dobbs"));

        InstanceFields instance = new InstanceFields(fieldsDefn);

        instance.setValue("firstname", "al");
        instance.setValue("surname", "me");

        final InstanceFields clone = instance.cloned();
        Assertions.assertEquals("al",
                clone.getFieldValue("firstname").asString());
        Assertions.assertEquals("me",
                clone.getFieldValue("surname").asString());

        instance.setValue("firstname", "Bob");
        instance.setValue("surname", "Dobbs");

        // clone is unaffected by main instance change
        Assertions.assertEquals("al",
                clone.getFieldValue("firstname").asString());
        Assertions.assertEquals("me",
                clone.getFieldValue("surname").asString());


        clone.setValue("firstname", "BOBB");
        clone.setValue("surname", "DDOBBS");

        // clone is unaffected by main instance change
        Assertions.assertEquals("BOBB",
                clone.getFieldValue("firstname").asString());
        Assertions.assertEquals("DDOBBS",
                clone.getFieldValue("surname").asString());
    }

    @Test
    void canReportOnGuidAndIdDifferences() {

        DefinedFields fieldsDefn = new DefinedFields();
        fieldsDefn.addFields(
                Field.is("id", FieldType.ID),
                Field.is("guid", FieldType.GUID));

        InstanceFields instance = new InstanceFields(fieldsDefn);
        instance.addIdsToInstance();
        instance.putValue("guid", UUID.randomUUID().toString());

        List<FieldValue> values = new ArrayList<>();
        values.add(FieldValue.is("id", "4567"));
        List<String> errors = instance.findAnyGuidOrIdDifferences(values);
        Assertions.assertEquals(1, errors.size());
        Assertions.assertTrue(errors.get(0).contains(" id "), errors.get(0));

        values = new ArrayList<>();
        values.add(FieldValue.is("guid", "4567"));
        errors = instance.findAnyGuidOrIdDifferences(values);
        Assertions.assertEquals(1, errors.size());
        Assertions.assertTrue(errors.get(0).contains(" guid "), errors.get(0));

        values.add(FieldValue.is("id", "999999"));
        errors = instance.findAnyGuidOrIdDifferences(values);
        Assertions.assertEquals(2, errors.size());
        Assertions.assertTrue(errors.get(1).contains(" 999999"), errors.get(1));
    }

    @Test
    void noErrorsWhenNoGuidAndIdDifferences() {

        DefinedFields fieldsDefn = new DefinedFields();
        fieldsDefn.addFields(
                Field.is("id", FieldType.ID),
                Field.is("guid", FieldType.GUID));

        InstanceFields instance = new InstanceFields(fieldsDefn);
        instance.addIdsToInstance();
        String aGUID = UUID.randomUUID().toString();
        instance.putValue("guid", aGUID);
        instance.putValue("id", "2344");

        List<FieldValue> values = new ArrayList<>();
        values.add(FieldValue.is("id", "2344"));
        values.add(FieldValue.is("guid", aGUID));
        List<String> errors = instance.findAnyGuidOrIdDifferences(values);
        Assertions.assertEquals(0, errors.size());
    }
}
