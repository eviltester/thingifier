package uk.co.compendiumdev.thingifier.core.domain.definitions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.Thing;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.relationship.RelationshipVector;
import uk.co.compendiumdev.thingifier.core.domain.instances.InstanceFields;

import java.util.ArrayList;
import java.util.List;


class ThingDefinitionTest {

    @Test
    void entityDefinitionCreation() {
        ThingDefinition eDefn;
        eDefn = ThingDefinition.create("Requirement", "Requirements");

        Assertions.assertEquals("Requirement", eDefn.getName());
        Assertions.assertEquals("Requirements", eDefn.getPlural());

        Assertions.assertEquals(1, eDefn.getFieldNames().size()); // guid exists
        Assertions.assertTrue(eDefn.getFieldNames().contains("guid"));

        System.out.println(eDefn.toString());
    }

    @Test
    void addFieldToEntityDefinition() {
        ThingDefinition eDefn;
        eDefn = ThingDefinition.create("Requirement", "Requirements");

        eDefn.addField(Field.is("Title"));

        Assertions.assertEquals(2, eDefn.getFieldNames().size());

        Assertions.assertTrue(eDefn.getFieldNames().contains("Title"));

        Assertions.assertEquals(FieldType.STRING, eDefn.getField("Title").getType());

        Assertions.assertTrue(eDefn.hasFieldNameDefined("Title"));
        Assertions.assertFalse(eDefn.hasFieldNameDefined("Description"));

        System.out.println(eDefn.toString());
    }

    @Test
    void addMultipleFieldsToEntityDefinition() {
        ThingDefinition eDefn;
        eDefn = ThingDefinition.create("Requirement", "Requirements");

        eDefn.addFields(Field.is("Title"), Field.is("Description"));

        Assertions.assertEquals(3, eDefn.getFieldNames().size());

        Assertions.assertTrue(eDefn.getFieldNames().contains("Title"));
        Assertions.assertTrue(eDefn.getFieldNames().contains("Description"));

        System.out.println(eDefn.toString());
    }

    @Test
    void canGetFieldsOfType() {
        ThingDefinition eDefn;
        eDefn = ThingDefinition.create("Requirement", "Requirements");

        eDefn.addFields(Field.is("Title"),
                        Field.is("Description"));

        final Field anIdField = Field.is("anId", FieldType.ID);
        final Field anotherIdField = Field.is("anotherID", FieldType.ID);
        eDefn.addFields(anIdField, anotherIdField);

        final List<String> stringFieldNames = eDefn.getFieldNamesOfType(FieldType.STRING);
        Assertions.assertEquals(2, stringFieldNames.size());

        final List<Field> fields = eDefn.getFieldsOfType(FieldType.ID);

        Assertions.assertEquals(2, fields.size());
        Assertions.assertTrue(fields.contains(anIdField));
        Assertions.assertTrue(fields.contains(anotherIdField));


    }

    @Test
    void fieldsOrderingIsTheOrderWhenDefined() {

        ThingDefinition defn = ThingDefinition.create("thing", "things");

        // add 19 fields
        for (int x = 1; x < 20; x++) {
            defn.addField(Field.is("field" + x));
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

        final Thing stress = Thing.create("stress", "stress");
        final Thing slack = Thing.create("slack", "slack");

        final RelationshipVector vec =
                new RelationshipVector(stress,
                        "withbob",
                        slack,
                        Cardinality.ONE_TO_MANY);
        final RelationshipDefinition defn = RelationshipDefinition.create(vec);


        final DefinedRelationships rels = new DefinedRelationships();
        rels.addRelationship(vec);

        Assertions.assertNull(slack.definition().getNamedRelationshipTo("pink", stress.definition()));

        Assertions.assertNull(slack.definition().getNamedRelationshipTo("withbob", stress.definition()));

        Assertions.assertNotNull(stress.definition().getNamedRelationshipTo("withbob", slack.definition()));
        Assertions.assertEquals(vec, stress.definition().getNamedRelationshipTo("withbob", slack.definition()));
    }


    @Test
    void canInstantiateFieldDefinitions() {
        ThingDefinition eDefn;
        eDefn = ThingDefinition.create("Requirement", "Requirements");

        eDefn.addFields(Field.is("anId", FieldType.ID));

        final InstanceFields instance1 = eDefn.instantiateFields();

        // all this does is associate the instances with the definitions
        // the values are null

        Assertions.assertNull(instance1.getFieldValue("anId"));
    }


    @Test
    void canIncreaseNextIds() {
        ThingDefinition eDefn;
        eDefn = ThingDefinition.create("Requirement", "Requirements");

        eDefn.addFields(Field.is("anId", FieldType.ID));
        eDefn.addFields(Field.is("anotherId", FieldType.ID));

        List<FieldValue> nextIds = new ArrayList<>();
        nextIds.add(FieldValue.is("anId", "46"));
        nextIds.add(FieldValue.is("anotherId", "71"));

        eDefn.setNextIdsToAccomodate(nextIds);
        Assertions.assertEquals("47",
                eDefn.getField("anId").getNextIdValue());
        Assertions.assertEquals("72",
                eDefn.getField("anotherId").getNextIdValue());

        final InstanceFields instance1 = eDefn.instantiateFields();
        instance1.addIdsToInstance();

        Assertions.assertEquals(48,
                instance1.getFieldValue("anId").asInteger());
        Assertions.assertEquals(73,
                instance1.getFieldValue("anotherId").asInteger());


    }

}
