package uk.co.compendiumdev.thingifier.core.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

class MutableEntityInstanceTest {

    @ParameterizedTest
    @CsvSource({"false,false", "faLSE,false", "true,true", "TRUE,true"})
    void materializesValidatedDraftValuesAsEntitySnapshot(
            final String value, final String expected) {

        EntityInstance instance =
                MutableEntityInstance.snapshotFromDraft(
                        EntityInstanceDraft.forEntity(entity()).withField("review", value));

        Assertions.assertEquals(expected, instance.getFieldValue("review").asString());
    }

    @Test
    void materializedEntityInstanceIsASnapshotOfMutableState() {

        MutableEntityInstance mutable =
                MutableEntityInstance.fromDraft(
                        EntityInstanceDraft.forEntity(entity()).withField("review", "true"));

        EntityInstance snapshot = mutable.toEntityInstance();

        mutable.setValue("review", "false");

        Assertions.assertEquals("true", snapshot.getFieldValue("review").asString());
        Assertions.assertEquals(
                "false", mutable.toEntityInstance().getFieldValue("review").asString());
    }

    private EntityDefinition entity() {
        EntityDefinition entity = new EntityDefinition("thing", "things");
        entity.addField(Field.is("review", FieldType.BOOLEAN));
        return entity;
    }
}
