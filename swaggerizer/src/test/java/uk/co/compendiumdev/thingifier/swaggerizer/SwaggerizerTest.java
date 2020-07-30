package uk.co.compendiumdev.thingifier.swaggerizer;

import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ThingifierApiDefn;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;

public class SwaggerizerTest {

    @Test
    public void canCreateSwagger(){
        Thingifier t = new TodoListThingifierTestModel().get();

        ThingifierApiDefn apiDefn = new ThingifierApiDefn();
        apiDefn.setThingifier(t);

        apiDefn.addAdditionalRoute(new RoutingDefinition(
                RoutingVerb.POST,
                "/plan",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Create a plan").
                addPossibleStatuses(200,400));


        apiDefn.addServer("https://apichallenges.herokuapp.com", "heroku hosted version");
        apiDefn.addServer("http://localhost:4567", "local execution");
        apiDefn.setVersion("1.0.1");

        System.out.println(new Swaggerizer(apiDefn).asJson());


    }
}
