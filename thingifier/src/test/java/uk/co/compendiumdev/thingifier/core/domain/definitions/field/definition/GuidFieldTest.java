package uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class GuidFieldTest {

    @Test
    void canValidateExampleGuid(){

        Field field = Field.is("guid", FieldType.GUID);

        try {
            UUID.fromString(field.getRandomExampleValue());
        }catch(IllegalArgumentException e){
            Assertions.fail("Should have converted example GUID");
        }
    }
}
