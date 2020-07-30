package uk.co.compendiumdev.thingifier.swaggerizer;

import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;

public class SwaggerizerTest {

    @Test
    public void canCreateSwagger(){
        Thingifier t = new TodoListThingifierTestModel().get();

        System.out.println(new Swaggerizer(t).asJson());
    }
}
