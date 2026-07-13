package uk.co.compendiumdev.thingifier.api.non_http;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.api.restapihandlers.EntityInstanceBulkUpdater;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

public class BulkUpdateEntityInstanceTest {

    private EntityDefinition entityTestSession;

    @BeforeEach
    public void createEntity() {

        entityTestSession = new EntityDefinition("Test Session", "Test Sessions");

        entityTestSession.addField(Field.is("Title", FieldType.STRING));
        entityTestSession.addFields(
                Field.is("CompletedStatus", FieldType.STRING).withDefaultValue("Not Completed"));
        entityTestSession.addFields(Field.is("review", FieldType.BOOLEAN).withDefaultValue("TRUE"));
        entityTestSession.addFields(Field.is("falsey", FieldType.BOOLEAN));
        entityTestSession.addFields(Field.is("anid", FieldType.AUTO_INCREMENT));
    }

    @Test
    public void canSetByList() {

        final EntityInstance session =
                uk.co.compendiumdev.thingifier.core.repository.MutableEntityInstance
                        .snapshotFromDraft(EntityInstanceDraft.forEntity(entityTestSession));

        List<NamedValue> someFields = new ArrayList<>();
        someFields.add(new NamedValue("Title", "my title"));
        someFields.add(new NamedValue("falsey", "true"));
        EntityInstanceDraft draft =
                new EntityInstanceBulkUpdater(session).setFieldValuesFrom(someFields);
        EntityInstance updated =
                uk.co.compendiumdev.thingifier.core.repository.MutableEntityInstance
                        .snapshotFromDraft(draft);

        Assertions.assertEquals("my title", updated.getFieldValue("Title").asString());
        Assertions.assertEquals("true", updated.getFieldValue("falsey").asString());
    }

    @Test
    public void canNotSetSomeFieldsByList() {

        final EntityInstance session =
                uk.co.compendiumdev.thingifier.core.repository.MutableEntityInstance
                        .snapshotFromDraft(
                                EntityInstanceDraft.forEntity(entityTestSession)
                                        .withProtectedField("anid", "1"));

        List<NamedValue> someFields = new ArrayList<>();
        someFields.add(new NamedValue("anid", "12"));
        final RuntimeException e =
                Assertions.assertThrows(
                        RuntimeException.class,
                        () ->
                                new EntityInstanceBulkUpdater(session)
                                        .setFieldValuesFrom(someFields));

        Assertions.assertEquals("Can not amend anid from 1 to 12", e.getMessage());
    }

    @Test
    public void canIgnoreSomeSetSomeFieldsByListToAvoidTriggeringValidation() {

        final EntityInstance session =
                uk.co.compendiumdev.thingifier.core.repository.MutableEntityInstance
                        .snapshotFromDraft(EntityInstanceDraft.forEntity(entityTestSession));

        List<NamedValue> someFields = new ArrayList<>();
        someFields.add(new NamedValue("anid", "12"));
        someFields.add(new NamedValue("Title", "set Title"));

        List<String> ignoring = new ArrayList<>();
        ignoring.add("anid");

        EntityInstanceDraft draft =
                new EntityInstanceBulkUpdater(session)
                        .setFieldValuesFromArgsIgnoring(someFields, ignoring);
        EntityInstance updated =
                uk.co.compendiumdev.thingifier.core.repository.MutableEntityInstance
                        .snapshotFromDraft(draft);

        Assertions.assertEquals("set Title", updated.getFieldValue("Title").asString());
    }

    @Test
    public void canIgnoreSomeOverrideFieldsWithListToAIgnore() {

        final EntityInstance session =
                uk.co.compendiumdev.thingifier.core.repository.MutableEntityInstance
                        .snapshotFromDraft(EntityInstanceDraft.forEntity(entityTestSession));

        List<NamedValue> someFields = new ArrayList<>();
        someFields.add(new NamedValue("anid", "12"));
        someFields.add(new NamedValue("Title", "set Title"));
        someFields.add(new NamedValue("falsey", "true"));

        List<String> ignoring = new ArrayList<>();
        ignoring.add("falsey");

        EntityInstanceDraft draft =
                new EntityInstanceBulkUpdater(session)
                        .overrideFieldValuesFromArgsIgnoring(someFields, ignoring);
        EntityInstance updated =
                uk.co.compendiumdev.thingifier.core.repository.MutableEntityInstance
                        .snapshotFromDraft(draft);

        Assertions.assertEquals("set Title", updated.getFieldValue("Title").asString());
        Assertions.assertEquals("12", updated.getFieldValue("anId").asString());
        Assertions.assertEquals("false", updated.getFieldValue("falsey").asString());
    }
}
