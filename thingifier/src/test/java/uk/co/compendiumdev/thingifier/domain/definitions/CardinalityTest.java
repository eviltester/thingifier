package uk.co.compendiumdev.thingifier.domain.definitions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CardinalityTest {

    @Test
    void canGetLeftAndRightValues(){
        Assertions.assertEquals("1", Cardinality.ONE_TO_MANY.left());
        Assertions.assertEquals("*", Cardinality.ONE_TO_MANY.right());
    }
}
