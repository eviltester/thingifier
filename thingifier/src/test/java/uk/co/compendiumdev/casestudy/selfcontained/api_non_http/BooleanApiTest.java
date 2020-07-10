package uk.co.compendiumdev.casestudy.selfcontained.api_non_http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.generic.FieldType;
import uk.co.compendiumdev.thingifier.generic.definitions.Field;
import uk.co.compendiumdev.thingifier.generic.definitions.validation.VRule;

import static uk.co.compendiumdev.thingifier.generic.FieldType.STRING;

public class BooleanApiTest {


    @BeforeEach
    public void createThingifier(){
        Thingifier thingifier = new Thingifier();

        Thing thing = thingifier.createThing("thing", "things");

        thing.definition()
                .addFields( Field.is("id", FieldType.ID),
                        Field.is("text", STRING));
        ;
    }


    @Disabled("We have a lot of gaps - create simple thingifier models and use for highly diverse test coverage and condition toggling")
    @Test
    public void getThingWithId(){

    }
}
