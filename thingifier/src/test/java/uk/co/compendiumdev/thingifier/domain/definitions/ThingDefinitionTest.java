package uk.co.compendiumdev.thingifier.domain.definitions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.domain.definitions.relationship.RelationshipDefinition;
import uk.co.compendiumdev.thingifier.domain.definitions.relationship.RelationshipVector;


public class ThingDefinitionTest {

    @Test
    public void entityDefinitionCreation() {
        ThingDefinition eDefn;
        eDefn = ThingDefinition.create("Requirement", "Requirements");

        Assertions.assertEquals("Requirement", eDefn.getName());
        Assertions.assertEquals("Requirements", eDefn.getPlural());

        Assertions.assertEquals(1, eDefn.getFieldNames().size()); // guid exists
        Assertions.assertTrue(eDefn.getFieldNames().contains("guid"));

        System.out.println(eDefn.toString());
    }

    @Test
    public void addFieldToEntityDefinition() {
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
    public void addMultipleFieldsToEntityDefinition() {
        ThingDefinition eDefn;
        eDefn = ThingDefinition.create("Requirement", "Requirements");

        eDefn.addFields(Field.is("Title"), Field.is("Description"));

        Assertions.assertEquals(3, eDefn.getFieldNames().size());

        Assertions.assertTrue(eDefn.getFieldNames().contains("Title"));
        Assertions.assertTrue(eDefn.getFieldNames().contains("Description"));

        System.out.println(eDefn.toString());
    }

    @Test
    public void fieldsOrderingIsTheOrderWhenDefined() {

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
    public void entityDoesNotNeedToHaveIdFields() {
        ThingDefinition eDefn;
        eDefn = ThingDefinition.create("Requirement", "Requirements");

        Assertions.assertFalse(eDefn.hasIDField());
    }
    @Test
    public void reportWhenItHasIdFields() {
        ThingDefinition eDefn;

        eDefn = ThingDefinition.create("Requirement", "Requirements");

        eDefn.addField(Field.is("anid", FieldType.ID));

        Assertions.assertTrue(eDefn.hasIDField());
    }

    @Test
    public void returnFirstIDField() {
        ThingDefinition eDefn;

        eDefn = ThingDefinition.create("Requirement", "Requirements");

        eDefn.addFields(Field.is("firstid", FieldType.ID),
                Field.is("secondid", FieldType.ID));

        Assertions.assertNotNull(eDefn.getIDField());
        Assertions.assertEquals("secondid", eDefn.getIDField().getName());
    }
}
