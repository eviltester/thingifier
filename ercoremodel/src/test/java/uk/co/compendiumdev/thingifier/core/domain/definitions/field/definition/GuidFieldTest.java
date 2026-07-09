package uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition;

import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GuidFieldTest {

    @Test
    void canValidateExampleGuid() {

        Field field = Field.is("guid", FieldType.AUTO_GUID);

        try {
            UUID.fromString(field.getRandomExampleValue());
        } catch (IllegalArgumentException e) {
            Assertions.fail("Should have converted example GUID");
        }
    }
}
