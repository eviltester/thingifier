package uk.co.compendiumdev.thingifier.core.domain.instances;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

public class ThingInstanceDocumentationTest {

    private EntityDefinition entityTestSession;

    @BeforeEach
    public void createEntity() {

        entityTestSession = new EntityDefinition("Test Session", "Test Sessions");

        entityTestSession.addField(Field.is("Title", FieldType.STRING));
        entityTestSession.addFields(Field.is("CompletedStatus", FieldType.STRING).withDefaultValue("Not Completed"));
        entityTestSession.addFields(Field.is("review", FieldType.BOOLEAN).withDefaultValue("TRUE"));
        entityTestSession.addFields(Field.is("falsey", FieldType.BOOLEAN));
        entityTestSession.addFields(Field.is("anid", FieldType.ID));
    }

    @Test
    public void canCreateNewEntityInstance() {
        // thing instance for documentation has no ids or guids
        final EntityInstance session = new EntityInstance(entityTestSession);
        Assertions.assertNull(session.getFieldValue("guid"));
        Assertions.assertNull(session.getFieldValue("anid"));
    }
}
