package uk.co.compendiumdev.thingifier.core.domain.definitions.relationship;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OptionalityTest {

    @Test
    void mandatoryFromStringM() {
        Assertions.assertEquals(
                Optionality.MANDATORY_RELATIONSHIP,
                Optionality.from("M"));

        Assertions.assertEquals(
                Optionality.MANDATORY_RELATIONSHIP,
                Optionality.from("m"));
    }

    @Test
    void optionalFromStringO() {

        Assertions.assertEquals(
                Optionality.OPTIONAL_RELATIONSHIP,
                Optionality.from("o"));

        Assertions.assertEquals(
                Optionality.OPTIONAL_RELATIONSHIP,
                Optionality.from("O"));
    }

    @Test
    void optionalFromUnknownString() {
        Assertions.assertEquals(
                Optionality.OPTIONAL_RELATIONSHIP,
                Optionality.from("anyThingReally"));
    }

    @Test
    void optionalFromNullString() {
        Assertions.assertEquals(
                Optionality.OPTIONAL_RELATIONSHIP,
                Optionality.from(null));
    }
}
