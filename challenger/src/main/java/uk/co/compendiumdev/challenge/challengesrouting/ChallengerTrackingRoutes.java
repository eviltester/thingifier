package uk.co.compendiumdev.challenge.challengesrouting;

import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenge.persistence.PersistenceLayer;
import uk.co.compendiumdev.challenge.persistence.PersistenceResponse;
import uk.co.compendiumdev.thingifier.api.ThingifierApiDefn;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;
import uk.co.compendiumdev.thingifier.spark.SimpleRouteConfig;

import java.util.List;

import static spark.Spark.get;
import static spark.Spark.post;

public class ChallengerTrackingRoutes {

    public void configure(final Challengers challengers,
                          final boolean single_player_mode,
                          final ThingifierApiDefn apiDefn,
                          final PersistenceLayer persistenceLayer){

        // refresh challenger to avoid purging
        get("/challenger/*", (request, result) -> {
            String xChallengerGuid =null;
            if(request.splat().length>0) {
                xChallengerGuid = request.splat()[0];
            }
            if(xChallengerGuid != null && xChallengerGuid.trim()!=""){
                ChallengerAuthData challenger = challengers.getChallenger(xChallengerGuid);
                if(challenger!=null){
                    challenger.touch();
                    result.status(204);
                    result.raw().setHeader("X-CHALLENGER",challenger.getXChallenger());
                }else{
                    result.status(404);
                    result.raw().setHeader("X-CHALLENGER", "UNKNOWN CHALLENGER - Challenger not found");
                }
            }else{
                result.status(404);
                result.raw().setHeader("X-CHALLENGER", "UNKNOWN CHALLENGER - Challenger not found");
            }
            return "";
        });

        SimpleRouteConfig.
                routeStatusWhenNot(
                        405, "/challenger/*", "get");

        // create a challenger
        post("/challenger", (request, result) -> {

            if(single_player_mode){
                result.raw().setHeader("X-CHALLENGER", challengers.SINGLE_PLAYER.getXChallenger());
                result.raw().setHeader("Location", "/gui/challenges");
                result.status(201);
                return "";
            }

            String xChallengerGuid = request.headers("X-CHALLENGER");
            if(xChallengerGuid == null || xChallengerGuid.trim()==""){
                // create a new challenger
                final ChallengerAuthData challenger = challengers.createNewChallenger();
                result.raw().setHeader("X-CHALLENGER", challenger.getXChallenger());
                result.raw().setHeader("Location", "/gui/challenges/" + challenger.getXChallenger());
                result.status(201);
                return "";
            }else {
                ChallengerAuthData challenger = challengers.getChallenger(xChallengerGuid);
                if(challenger==null){
                    // if X-CHALLENGER header exists, and is not a known UUID,
                    // return 404, challenger ID not valid
                    result.raw().setHeader("X-CHALLENGER", "UNKNOWN CHALLENGER - Challenger not found");
                    result.status(404);
                    return "";
                }else{
                    // if X-CHALLENGER header exists, and has a valid UUID, and UUID exists, then return 200
                    result.raw().setHeader("X-CHALLENGER", challenger.getXChallenger());
                    result.raw().setHeader("Location", "/gui/challenges/" + challenger.getXChallenger());
                    result.status(200);
                    return "";
                }
            }
        });

        SimpleRouteConfig.
                routeStatusWhenNot(
                        405, "/challenger", "post");

        String explanation = "";
        if(single_player_mode) {
            explanation =  " Not necessary in single user mode.";
        }

        apiDefn.addAdditionalRoute(
            new RoutingDefinition(
                RoutingVerb.POST,
                "/challenger",
                RoutingStatus.returnedFromCall(),
                null).
                addDocumentation("Create an X-CHALLENGER guid to allow tracking challenges, use the X-CHALLENGER header in all requests to track challenge completion for multi-user tracking." + explanation).
                    addPossibleStatuses(200,400,404));

        apiDefn.addAdditionalRoute(
            new RoutingDefinition(
                RoutingVerb.GET,
                "/challenger/:guid",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Restore a saved challenger matching the supplied X-CHALLENGER guid to allow continued tracking of challenges." + explanation)
                    .addPossibleStatuses(204,404));

    }
}
