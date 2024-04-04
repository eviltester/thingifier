package uk.co.compendiumdev.thingifier.swaggerizer;

import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;

public class SwaggerizerTest {

    @Test
    public void canCreateSwagger(){
        Thingifier t = new TodoListThingifierTestModel().get();

        ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.setThingifier(t);

        apiDefn.addRouteToDocumentation(new RoutingDefinition(
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
