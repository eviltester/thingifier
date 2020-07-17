package uk.co.compendiumdev.challenge;

import com.google.gson.Gson;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;
import uk.co.compendiumdev.thingifier.application.ThingifierRestServer;
import uk.co.compendiumdev.thingifier.application.routehandlers.ShutdownRouteHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.get;

public class ChallengeRouteHandler {
    List<RoutingDefinition> routes;
    Challenges challenges;

    public ChallengeRouteHandler(){
        routes = new ArrayList();
        challenges = new Challenges();
    }

    public List<RoutingDefinition> getRoutes(){
        return routes;
    }

    public ChallengeRouteHandler configureRoutes() {

        get("/challenges", (request, result) -> {
            result.status(200);
            result.body(challenges.getAsJson());
            return "";
        });

        routes.add(new RoutingDefinition(
                RoutingVerb.GET,
                "/challenges",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Get list of challenges and their completion status"));

        return this;
    }

    public void addHooks(final ThingifierRestServer restServer) {

        restServer.registerHttpApiRequestHook(new ChallengerApiRequestHook(challenges));
        restServer.registerHttpApiResponseHook(new ChallengerApiResponseHook(challenges));
    }
}
