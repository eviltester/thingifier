package uk.co.compendiumdev.thingifier.core.domain.definitions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

import java.util.ArrayList;
import java.util.List;

class DefinedFieldsTest {


    private DefinedFields multipleFields;

    @Test
    void initiallyNoFields(){
        DefinedFields fields = new DefinedFields();

        final List<String> orderedFieldNames = fields.getFieldNames();
        Assertions.assertEquals(0, orderedFieldNames.size());
    }

    @Test
    void fieldNamesAreAddedInOrder(){

        DefinedFields fields = new DefinedFields();
        fields.addField(Field.is("zacharay"));
        fields.addField(Field.is("middle"));
        fields.addField(Field.is("abc"));

        final List<String> orderedFieldNames = fields.getFieldNames();

        Assertions.assertEquals("zacharay", orderedFieldNames.get(0));
        Assertions.assertEquals("middle", orderedFieldNames.get(1));
        Assertions.assertEquals("abc", orderedFieldNames.get(2));
        Assertions.assertEquals(3, orderedFieldNames.size());

        System.out.println(fields.toString());
    }

    @Test
    void canAddFieldsInBulk(){

        DefinedFields fields = new DefinedFields();
        fields.addField(Field.is("zacharay"));

        fields.addFields(Field.is("middle"), Field.is("abc"));

        final List<String> orderedFieldNames = fields.getFieldNames();

        Assertions.assertEquals("zacharay", orderedFieldNames.get(0));
        Assertions.assertEquals("middle", orderedFieldNames.get(1));
        Assertions.assertEquals("abc", orderedFieldNames.get(2));
        Assertions.assertEquals(3, orderedFieldNames.size());
    }

    @Test
    void canReportOnFieldExistence(){

        DefinedFields fields = new DefinedFields();
        fields.addField(Field.is("zacharay"));

        Assertions.assertTrue(fields.hasFieldNameDefined("zacharay"));
        Assertions.assertFalse(fields.hasFieldNameDefined("bob"));
    }

    @Test
    void canGetExistingFields(){

        DefinedFields fields = new DefinedFields();
        fields.addField(Field.is("zacharay"));
        fields.addField(Field.is("dobbs"));

        Field zach = fields.getField("zacharay");

        Assertions.assertNotNull(zach);
        Assertions.assertEquals("zacharay", zach.getName());

        Assertions.assertNull(fields.getField("bob"));
    }


    @BeforeEach
    public void multipleFieldsDefined(){
        multipleFields = new DefinedFields();
        multipleFields.addField(Field.is("string1", FieldType.STRING));
        multipleFields.addField(Field.is("bob", FieldType.GUID));
        multipleFields.addField(Field.is("string2", FieldType.STRING));
        multipleFields.addField(Field.is("dobbs", FieldType.ID));
        multipleFields.addField(Field.is("string3", FieldType.STRING));
    }

    @Test
    void canGetSpecificFieldTypes(){

        final List<Field> strings = multipleFields.getFieldsOfType(FieldType.STRING);

        Assertions.assertEquals(3, strings.size());

        // names are not guaranteed to be in order
        List<String> stringNames = new ArrayList<>();
        for(Field string : strings){
            stringNames.add(string.getName());
        }
        Assertions.assertTrue(stringNames.contains("string1"));
        Assertions.assertTrue(stringNames.contains("string2"));
        Assertions.assertTrue(stringNames.contains("string3"));

    }

    @Test
    void canGetNamesOfSpecificFieldTypes(){

        final List<String> strings = multipleFields.getFieldNamesOfType(FieldType.STRING);

        Assertions.assertEquals(3, strings.size());

        Assertions.assertTrue(strings.contains("string1"));
        Assertions.assertTrue(strings.contains("string2"));
        Assertions.assertTrue(strings.contains("string3"));
    }

    @Test
    void canGetMultipleFieldTypes(){

        final List<Field> others = multipleFields.getFieldsOfType(
                                                FieldType.GUID,
                                                FieldType.ID);

        List<String> othersNames = new ArrayList<>();
        for(Field field : others){
            othersNames.add(field.getName());
        }
        Assertions.assertTrue(othersNames.contains("bob"));
        Assertions.assertTrue(othersNames.contains("dobbs"));
    }

    @Test
    void noFieldsReturnsNoAdditionalResults(){

        final List<Field> others = multipleFields.getFieldsOfType(
                FieldType.GUID,
                FieldType.ID,
                FieldType.FLOAT,
                FieldType.INTEGER);

        List<String> othersNames = new ArrayList<>();
        for(Field field : others){
            othersNames.add(field.getName());
        }
        Assertions.assertTrue(othersNames.contains("bob"));
        Assertions.assertTrue(othersNames.contains("dobbs"));
    }

    @Test
    void noFieldsReturnsNoResults(){

        final List<Field> none = multipleFields.getFieldsOfType(
                FieldType.FLOAT,
                FieldType.INTEGER,
                FieldType.DATE,
                FieldType.OBJECT);

        Assertions.assertNotNull(none);
        Assertions.assertEquals(0, none.size());

    }

}
