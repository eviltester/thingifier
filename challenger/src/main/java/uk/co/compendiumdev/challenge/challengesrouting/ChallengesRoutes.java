package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.ChallengesPayload;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitions;
import uk.co.compendiumdev.thingifier.api.ThingifierApiDefn;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;
import uk.co.compendiumdev.thingifier.spark.SimpleRouteConfig;

import java.util.List;

import static spark.Spark.*;

public class ChallengesRoutes {

    public void configure(final Challengers challengers, final boolean single_player_mode,
                          final ThingifierApiDefn apiDefn,
                          final ChallengeDefinitions challengeDefinitions){
        get("/challenges", (request, result) -> {
            result.status(200);
            result.type("application/json");

            ChallengerAuthData challenger = challengers.getChallenger(request.headers("X-CHALLENGER"));

            if(!single_player_mode){
                if(challenger!=null){
                    result.raw().setHeader("Location", "/gui/challenges/" + challenger.getXChallenger());
                }
            }else{
                result.raw().setHeader("Location", "/gui/challenges");
            }

            result.body(new ChallengesPayload(challengeDefinitions, challenger).getAsJson());
            return "";
        });

        // todo: allow filtering challenges e.g. find all done, not done, etc.

        head("/challenges", (request, result) -> {
            result.status(200);
            result.type("application/json");
            return "";
        });

        options("/challenges", (request, result) -> {
            result.status(200);
            result.type("application/json");
            result.header("Allow", "GET, HEAD, OPTIONS");
            return "";
        });

        SimpleRouteConfig.routeStatusWhenNot(
                405, "/challenges",
                "get", "head", "options");

        apiDefn.addAdditionalRoute(
                new RoutingDefinition(
                RoutingVerb.GET,
                "/challenges",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Get list of challenges and their completion status").
                        addPossibleStatuses(200));

        apiDefn.addAdditionalRoute(
                new RoutingDefinition(
                RoutingVerb.OPTIONS,
                "/challenges",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Options for list of challenges endpoint").
                        addPossibleStatuses(200));

        apiDefn.addAdditionalRoute(
                new RoutingDefinition(
                RoutingVerb.HEAD,
                "/challenges",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Headers for list of challenges endpoint")
                        .addPossibleStatuses(200));
    }

}
