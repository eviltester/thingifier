package uk.co.compendiumdev.thingifier.core.domain.definitions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVectorDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.InstanceFields;

import java.util.ArrayList;
import java.util.List;


class ThingDefinitionTest {

    @Test
    void entityDefinitionCreation() {
        EntityDefinition eDefn;
        eDefn = new EntityDefinition("Requirement", "Requirements");
        eDefn.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));

        Assertions.assertEquals("Requirement", eDefn.getName());
        Assertions.assertEquals("Requirements", eDefn.getPlural());

        Assertions.assertEquals(1, eDefn.getFieldNames().size()); // guid exists
        Assertions.assertTrue(eDefn.getFieldNames().contains("guid"));

        System.out.println(eDefn.toString());
    }

    @Test
    void addFieldToEntityDefinition() {
        EntityDefinition eDefn;
        eDefn = new EntityDefinition("Requirement", "Requirements");

        eDefn.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        eDefn.addField(Field.is("Title", FieldType.STRING));

        Assertions.assertEquals(2, eDefn.getFieldNames().size());

        Assertions.assertTrue(eDefn.getFieldNames().contains("Title"));

        Assertions.assertEquals(FieldType.STRING, eDefn.getField("Title").getType());

        Assertions.assertTrue(eDefn.hasFieldNameDefined("Title"));
        Assertions.assertFalse(eDefn.hasFieldNameDefined("Description"));

        System.out.println(eDefn.toString());
    }

    @Test
    void addMultipleFieldsToEntityDefinition() {
        EntityDefinition eDefn;
        eDefn = new EntityDefinition("Requirement", "Requirements");

        eDefn.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));
        eDefn.addFields(Field.is("Title", FieldType.STRING), Field.is("Description", FieldType.STRING));

        Assertions.assertEquals(3, eDefn.getFieldNames().size());

        Assertions.assertTrue(eDefn.getFieldNames().contains("Title"));
        Assertions.assertTrue(eDefn.getFieldNames().contains("Description"));

        System.out.println(eDefn.toString());
    }

    @Test
    void canGetFieldsOfType() {
        EntityDefinition eDefn;
        eDefn = new EntityDefinition("Requirement", "Requirements");

        eDefn.addFields(Field.is("Title", FieldType.STRING),
                Field.is("Description", FieldType.STRING));

        final Field anIdField = Field.is("anId", FieldType.AUTO_INCREMENT);
        final Field anotherIdField = Field.is("anotherID", FieldType.AUTO_INCREMENT);
        eDefn.addFields(anIdField, anotherIdField);

        final List<String> stringFieldNames = eDefn.getFieldNamesOfType(FieldType.STRING);
        Assertions.assertEquals(2, stringFieldNames.size());

        final List<Field> fields = eDefn.getFieldsOfType(FieldType.AUTO_INCREMENT);

        Assertions.assertEquals(2, fields.size());
        Assertions.assertTrue(fields.contains(anIdField));
        Assertions.assertTrue(fields.contains(anotherIdField));


    }

    @Test
    void fieldsOrderingIsTheOrderWhenDefined() {

        EntityDefinition defn = new EntityDefinition("thing", "things");
        defn.addAsPrimaryKeyField(Field.is("guid", FieldType.AUTO_GUID));

        // add 19 fields
        for (int x = 1; x < 20; x++) {
            defn.addField(Field.is("field" + x, FieldType.STRING));
        }

        // guid added automatically - making 20 fields
        Assertions.assertEquals(20, defn.getFieldNames().size());

        // check order
        int expectedFieldPostfix = 0;
        for (String fieldName : defn.getFieldNames()) {
            if (expectedFieldPostfix == 0) {
                // first field is GUID
                Assertions.assertEquals("guid", fieldName);
            } else {
                Assertions.assertEquals("field" + expectedFieldPostfix, fieldName);
            }
            expectedFieldPostfix++;
        }
    }

    @Test
    void canHaveNamedRelationshipBetweenThings() {

        final EntityDefinition stress = new EntityDefinition("stress", "stress");
        final EntityDefinition slack = new EntityDefinition("slack", "slack");

        final RelationshipVectorDefinition vec =
                new RelationshipVectorDefinition(stress,
                        "withbob",
                        slack,
                        Cardinality.ONE_TO_MANY());
        final RelationshipDefinition defn = RelationshipDefinition.create(vec);


        final DefinedRelationships rels = new DefinedRelationships();
        rels.addRelationship(vec);

        Assertions.assertNull(slack.getNamedRelationshipTo("pink", stress));

        Assertions.assertNull(slack.getNamedRelationshipTo("withbob", stress));

        Assertions.assertNotNull(stress.getNamedRelationshipTo("withbob", slack));
        Assertions.assertEquals(vec, stress.getNamedRelationshipTo("withbob", slack));
    }


    @Test
    void canInstantiateFieldDefinitions() {
        EntityDefinition eDefn;
        eDefn = new EntityDefinition("Requirement", "Requirements");

        eDefn.addFields(Field.is("anId", FieldType.AUTO_INCREMENT));

        final InstanceFields instance1 = eDefn.instantiateFields();

        // all this does is associate the instances with the definitions

        Assertions.assertEquals(1, instance1.getFieldValue("anId").asInteger());
    }



}
