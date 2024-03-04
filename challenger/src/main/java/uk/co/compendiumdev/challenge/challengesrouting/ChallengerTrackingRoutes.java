package uk.co.compendiumdev.challenge.challengesrouting;

import com.google.gson.Gson;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitions;
import uk.co.compendiumdev.challenge.persistence.PersistenceLayer;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ThingifierApiDefn;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsJson;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;
import uk.co.compendiumdev.thingifier.spark.SimpleRouteConfig;

import java.util.UUID;

import static spark.Spark.*;

public class ChallengerTrackingRoutes {

    public void configure(final Challengers challengers,
                          final boolean single_player_mode,
                          final ThingifierApiDefn apiDefn,
                          final PersistenceLayer persistenceLayer,
                          final Thingifier thingifier,
                          ChallengeDefinitions challengeDefinitions){

        // add a GET challenger/database/:id in the proper database format
        get("/challenger/database/:id", (request, result) -> {
            String xChallengerGuid =null;
            ChallengerAuthData challenger=null;

            // for which challenger?
            xChallengerGuid = request.params("id");

            if(xChallengerGuid==null){
                result.status(400);
                return ApiResponseAsJson.getErrorMessageJson("Invalid Challenger GUID");
            }

            if(!single_player_mode) {
                try {
                    UUID.fromString(xChallengerGuid);
                } catch (Exception e) {
                    result.status(400);
                    return ApiResponseAsJson.getErrorMessageJson("Invalid Challenger GUID " + e.getMessage());
                }
            }

            challenger = challengers.getChallenger(xChallengerGuid);
            XChallengerHeader.setResultHeaderBasedOnChallenger(result,challenger);

            if(challenger!=null){
                challenger.touch();
                result.header("content-type", "application/json");

                ERInstanceData instanceData = challengers.getErModel().getInstanceData(xChallengerGuid);
                if(instanceData==null){
                    result.status(404);
                    return ApiResponseAsJson.getErrorMessageJson("Challenger database not instantiated " + xChallengerGuid);
                }

                result.status(200);
                return challengers.getErModel().getInstanceData(xChallengerGuid).asJson();
            }else{
                result.status(404);
                return ApiResponseAsJson.getErrorMessageJson("Challenger not found " + xChallengerGuid);
            }
        });

        // refresh challenger to avoid purging
        get("/challenger/*", (request, result) -> {
            String xChallengerGuid =null;
            ChallengerAuthData challenger=null;
            if(request.splat().length>0) {
                xChallengerGuid = request.splat()[0];
            }
            if(xChallengerGuid != null && xChallengerGuid.trim()!=""){
                challenger = challengers.getChallenger(xChallengerGuid);
                if(challenger!=null){
                    challenger.touch();
                    result.status(200);
                    result.header("content-type", "application/json");
                    XChallengerHeader.setResultHeaderBasedOnChallenger(result,challenger);
                    return challenger.asJson();
                }else{
                    result.status(404);
                }
            }else{
                result.status(404);
            }
            XChallengerHeader.setResultHeaderBasedOnChallenger(result,challenger);
            return "";
        });


        // endpoint to restore a saved challenger status from UI
        put("/challenger/:id", (request, result) -> {
            String xChallengerGuid =null;
            ChallengerAuthData challenger=null;

            // for which challenger?
            xChallengerGuid = request.params("id");

            if(xChallengerGuid==null){
                result.status(400);
                return ApiResponseAsJson.getErrorMessageJson("Invalid Challenger GUID");
            }

            if(!single_player_mode) {
                try {
                    UUID.fromString(xChallengerGuid);
                } catch (Exception e) {
                    result.status(400);
                    return ApiResponseAsJson.getErrorMessageJson("Invalid Challenger GUID " + e.getMessage());
                }
            }

            // try and parse payload, fail if nonsense
            try {
                challenger = new Gson().fromJson(request.body(), ChallengerAuthData.class);
            }catch (Exception e){
                result.status(400);
                return ApiResponseAsJson.getErrorMessageJson(e.getMessage());
            }

            XChallengerHeader.setResultHeaderBasedOnChallenger(result,challenger);

            if(challenger==null){
                result.status(400);
                return ApiResponseAsJson.getErrorMessageJson("Invalid Payload");
            }

            // check payload against id
            if(!challenger.getXChallenger().equals(xChallengerGuid)){
                result.status(400);
                return ApiResponseAsJson.getErrorMessageJson("URL GUID does not match payload X-CHALLENGER");
            }

            // does id exist in memory, if so just replace state data
            if(challengers.inMemory(xChallengerGuid)){
                ChallengerAuthData existingChallenger = challengers.getChallenger(xChallengerGuid);
                existingChallenger.fromData(challenger, challengeDefinitions.getDefinedChallenges());
                result.status(200);
                result.header("content-type", "application/json");
                XChallengerHeader.setResultHeaderBasedOnChallenger(result,challenger);
                return existingChallenger.asJson();
            }

            // need to create a new challenger in memory with this state

            ChallengerAuthData newChallenger = new ChallengerAuthData(challengeDefinitions.getDefinedChallenges()).fromData(challenger, challengeDefinitions.getDefinedChallenges());
            challengers.put(newChallenger);
            result.status(201);
            result.header("content-type", "application/json");
            result.header("X-CHALLENGER", xChallengerGuid);
            return "";
        });


        // endpoint to restore a saved challenger database from UI
        put("/challenger/database/:id", (request, result) -> {

            String xChallengerGuid = request.params("id");

            // challenger uuid must exist and be valid
            if(xChallengerGuid==null){
                result.status(400);
                return ApiResponseAsJson.getErrorMessageJson("Invalid Challenger GUID");
            }

            if(!single_player_mode) {
                try {
                    UUID.fromString(xChallengerGuid);
                } catch (Exception e) {
                    result.status(400);
                    return ApiResponseAsJson.getErrorMessageJson("Invalid Challenger GUID " + e.getMessage());
                }
            }

            if(!challengers.inMemory(xChallengerGuid)){
                result.status(404);
                return ApiResponseAsJson.getErrorMessageJson("Unknown Challenger GUID - have you created or loaded the challenger state");
            }

            // thingifier allows loading database from json extract
            try {
                thingifier.ensureCreatedAndPopulatedInstanceDatabaseFromJson(xChallengerGuid.trim(), request.body());
            }catch (Exception e){
                result.status(400);
                return ApiResponseAsJson.getErrorMessageJson(e.getMessage());
            }


            if(challengers.getErModel().getDatabaseNames().contains(xChallengerGuid)){
                result.status(204);
                result.header("content-type", "application/json");
                result.header("X-CHALLENGER", xChallengerGuid);
                return "";
            }else{
                result.status(500);
                return ApiResponseAsJson.getErrorMessageJson("Unknown error, database not found");
            }
        });


        // TODO: add option for some ip based limits on number of challenges associated with an IP

        // TODO: add a protected admin page with an environment variable protection as password


        SimpleRouteConfig.
                routeStatusWhenNot(
                        405, "/challenger/*", "get", "put");

        // create a challenger
        post("/challenger", (request, result) -> {

            if(single_player_mode){
                XChallengerHeader.setResultHeaderBasedOnChallenger(result, challengers.SINGLE_PLAYER.getXChallenger());
                result.raw().setHeader("Location", "/gui/challenges");
                result.status(201);
                return "";
            }

            String xChallengerGuid = request.headers("X-CHALLENGER");
            if(xChallengerGuid == null || xChallengerGuid.trim()==""){
                // create a new
                final ChallengerAuthData challenger = challengers.createNewChallenger();
                // create the database for the user
                thingifier.ensureCreatedAndPopulatedInstanceDatabaseNamed(challenger.getXChallenger());
                XChallengerHeader.setResultHeaderBasedOnChallenger(result, challenger);
                result.raw().setHeader("Location", "/gui/challenges/" + challenger.getXChallenger());
                result.status(201);
            }else {
                ChallengerAuthData challenger = challengers.getChallenger(xChallengerGuid);
                if(challenger==null){
                    // if X-CHALLENGER header exists, and is not a known UUID,
                    // return 404, challenger ID not valid
                    result.status(404);
                }else{
                    // create the database for the user
                    thingifier.ensureCreatedAndPopulatedInstanceDatabaseNamed(challenger.getXChallenger());
                    // if X-CHALLENGER header exists, and has a valid UUID, and UUID exists, then return 200
                    result.raw().setHeader("Location", "/gui/challenges/" + challenger.getXChallenger());
                    result.status(200);
                }
                XChallengerHeader.setResultHeaderBasedOnChallenger(result, challenger);
            }
            return "";
        });

        SimpleRouteConfig.
                routeStatusWhenNot(
                        405, "/challenger", "post");

        String explanation = "";
        if(single_player_mode) {
            explanation =  " Not necessary in single user mode.";
        }

        apiDefn.addRouteToDocumentation(
            new RoutingDefinition(
                RoutingVerb.POST,
                "/challenger",
                RoutingStatus.returnedFromCall(),
                null).
                addDocumentation("Create an X-CHALLENGER guid to allow tracking challenges, use the X-CHALLENGER header in all requests to track challenge completion for multi-user tracking." + explanation).
                    addPossibleStatuses(200,400,404));

        apiDefn.addRouteToDocumentation(
            new RoutingDefinition(
                RoutingVerb.GET,
                "/challenger/:guid",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Restore a saved challenger matching the supplied X-CHALLENGER guid to allow continued tracking of challenges." + explanation)
                    .addPossibleStatuses(204,404));

    }
}
