package uk.co.compendiumdev.thingifier.core.domain.instances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;

import java.util.ArrayList;
import java.util.List;

public class ThingInstanceFieldAccessByListTest {

    private ThingDefinition entityTestSession;

    @BeforeEach
    public void createEntity() {

        entityTestSession = ThingDefinition.create("Test Session", "Test Sessions");

        entityTestSession.addField(Field.is("Title"));
        entityTestSession.addFields(Field.is("CompletedStatus").withDefaultValue("Not Completed"));
        entityTestSession.addFields(Field.is("review", FieldType.BOOLEAN).withDefaultValue("TRUE"));
        entityTestSession.addFields(Field.is("falsey", FieldType.BOOLEAN));
        entityTestSession.addFields(Field.is("anid", FieldType.ID));
    }

    @Test
    public void canSetByList() {

        final ThingInstance session = ThingInstance.create(entityTestSession);

        List<FieldValue> someFields = new ArrayList<>();
        someFields.add(FieldValue.is("Title", "my title"));
        someFields.add(FieldValue.is("falsey", "true"));
        session.setFieldValuesFrom(someFields);

        Assertions.assertEquals("my title",
                session.getFieldValue("Title").asString());
        Assertions.assertEquals("true",
                session.getFieldValue("falsey").asString());
    }

    @Test
    public void canNotSetSomeFieldsByList() {

        final ThingInstance session = ThingInstance.create(entityTestSession);

        List<FieldValue> someFields = new ArrayList<>();
        someFields.add(FieldValue.is("anid", "12"));
        final RuntimeException e = Assertions.assertThrows(RuntimeException.class,
                () -> session.setFieldValuesFrom(someFields));

        Assertions.assertEquals("Can not amend anid from 1 to 12",
                e.getMessage());
    }

    @Test
    public void canIgnoreSomeSetSomeFieldsByListToAvoidTriggeringValidation() {

        final ThingInstance session = ThingInstance.create(entityTestSession);

        List<FieldValue> someFields = new ArrayList<>();
        someFields.add(FieldValue.is("anid", "12"));
        someFields.add(FieldValue.is("Title", "set Title"));

        List<String> ignoring = new ArrayList<>();
        ignoring.add("anid");

        session.setFieldValuesFromArgsIgnoring(someFields, ignoring);

        Assertions.assertEquals("set Title",
                session.getFieldValue("Title").asString());
    }

    @Test
    public void canIgnoreSomeOverrideFieldsWithListToAIgnore() {

        final ThingInstance session = ThingInstance.create(entityTestSession);

        List<FieldValue> someFields = new ArrayList<>();
        someFields.add(FieldValue.is("anid", "12"));
        someFields.add(FieldValue.is("Title", "set Title"));
        someFields.add(FieldValue.is("falsey", "true"));

        List<String> ignoring = new ArrayList<>();
        ignoring.add("falsey");

        session.overrideFieldValuesFromArgsIgnoring(someFields, ignoring);

        Assertions.assertEquals("set Title",
                session.getFieldValue("Title").asString());
        Assertions.assertEquals("12",
                session.getFieldValue("anId").asString());
        Assertions.assertEquals("false",
                session.getFieldValue("falsey").asString());
    }

    @Test
    public void canClearFields() {

        final ThingInstance session = ThingInstance.create(entityTestSession);

        session.setValue("Title", "set Title");
        session.setValue("falsey", "true");

        session.clearAllFields();

        Assertions.assertEquals("",
                session.getFieldValue("Title").asString());
        Assertions.assertEquals("false",
                session.getFieldValue("falsey").asString());
    }
}