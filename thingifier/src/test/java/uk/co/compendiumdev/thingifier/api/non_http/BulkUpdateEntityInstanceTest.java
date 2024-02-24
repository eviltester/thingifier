package uk.co.compendiumdev.thingifier.api.non_http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.api.restapihandlers.EntityInstanceBulkUpdater;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.AutoIncrement;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BulkUpdateEntityInstanceTest {

    private EntityDefinition entityTestSession;

    @BeforeEach
    public void createEntity() {

        entityTestSession = new EntityDefinition("Test Session", "Test Sessions");

        entityTestSession.addField(Field.is("Title", FieldType.STRING));
        entityTestSession.addFields(Field.is("CompletedStatus", FieldType.STRING).withDefaultValue("Not Completed"));
        entityTestSession.addFields(Field.is("review", FieldType.BOOLEAN).withDefaultValue("TRUE"));
        entityTestSession.addFields(Field.is("falsey", FieldType.BOOLEAN));
        entityTestSession.addFields(Field.is("anid", FieldType.AUTO_INCREMENT));
    }

    @Test
    public void canSetByList() {

        final EntityInstance session = new EntityInstance(entityTestSession);

        List<NamedValue> someFields = new ArrayList<>();
        someFields.add(new NamedValue("Title",  "my title"));
        someFields.add(new NamedValue("falsey",  "true"));
        new EntityInstanceBulkUpdater(session).setFieldValuesFrom(someFields);

        Assertions.assertEquals("my title",
                session.getFieldValue("Title").asString());
        Assertions.assertEquals("true",
                session.getFieldValue("falsey").asString());
    }

    @Test
    public void canNotSetSomeFieldsByList() {

        final EntityInstance session = new EntityInstance(entityTestSession);
        session.addAutoGUIDstoInstance();
        Map<String, AutoIncrement> autos = new HashMap<>();
        autos.put("anid", new AutoIncrement("anid", 1));
        session.addAutoIncrementIdsToInstance(autos);

        List<NamedValue> someFields = new ArrayList<>();
        someFields.add(new NamedValue("anid",  "12"));
        final RuntimeException e = Assertions.assertThrows(RuntimeException.class,
                () ->  new EntityInstanceBulkUpdater(session).setFieldValuesFrom(someFields));

        Assertions.assertEquals("Can not amend anid from 1 to 12",
                e.getMessage());
    }

    @Test
    public void canIgnoreSomeSetSomeFieldsByListToAvoidTriggeringValidation() {

        final EntityInstance session = new EntityInstance(entityTestSession);

        List<NamedValue> someFields = new ArrayList<>();
        someFields.add(new NamedValue("anid",  "12"));
        someFields.add(new NamedValue("Title", "set Title"));

        List<String> ignoring = new ArrayList<>();
        ignoring.add("anid");

        new EntityInstanceBulkUpdater(session).setFieldValuesFromArgsIgnoring(someFields, ignoring);

        Assertions.assertEquals("set Title",
                session.getFieldValue("Title").asString());
    }

    @Test
    public void canIgnoreSomeOverrideFieldsWithListToAIgnore() {

        final EntityInstance session = new EntityInstance(entityTestSession);

        List<NamedValue> someFields = new ArrayList<>();
        someFields.add(new NamedValue("anid",  "12"));
        someFields.add(new NamedValue("Title", "set Title"));
        someFields.add(new NamedValue("falsey",  "true"));

        List<String> ignoring = new ArrayList<>();
        ignoring.add("falsey");

        new EntityInstanceBulkUpdater(session).overrideFieldValuesFromArgsIgnoring(someFields, ignoring);

        Assertions.assertEquals("set Title",
                session.getFieldValue("Title").asString());
        Assertions.assertEquals("12",
                session.getFieldValue("anId").asString());
        Assertions.assertEquals("false",
                session.getFieldValue("falsey").asString());
    }

}