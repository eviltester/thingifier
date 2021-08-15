package uk.co.compendiumdev.thingifier.core.domain.instances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

public class ThingInstanceDocumentationTest {

    private ThingDefinition entityTestSession;

    @BeforeEach
    public void createEntity() {

        entityTestSession = new ThingDefinition("Test Session", "Test Sessions");

        entityTestSession.addField(Field.is("Title"));
        entityTestSession.addFields(Field.is("CompletedStatus").withDefaultValue("Not Completed"));
        entityTestSession.addFields(Field.is("review", FieldType.BOOLEAN).withDefaultValue("TRUE"));
        entityTestSession.addFields(Field.is("falsey", FieldType.BOOLEAN));
        entityTestSession.addFields(Field.is("anid", FieldType.ID));
    }

    @Test
    public void canCreateNewEntityInstance() {
        // thing instance for documentation has no ids or guids
        final ThingInstance session = new ThingInstance(entityTestSession);
        Assertions.assertNull(session.getFieldValue("guid"));
        Assertions.assertNull(session.getFieldValue("anid"));
    }
}
