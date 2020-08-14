package uk.co.compendiumdev.thingifier.core;

import org.junit.jupiter.api.Test;

public class EntityRelModelTest {

    // Core needs a central class which 'manages' the Entities (Things)
    // and the Relationships - this should be separate from the
    // Thingifier (which also has the API and the Data Gen etc.)
    // the main class will be built by TDD, and refactoring in
    // code from the Thingifier

    @Test
    public void canCreateAnEntityRelModel(){

        EntityRelModel erm = new EntityRelModel();
    }
}
