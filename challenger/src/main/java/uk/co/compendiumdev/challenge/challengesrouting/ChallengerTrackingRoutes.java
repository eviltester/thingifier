package uk.co.compendiumdev.challenge.challengesrouting;

import com.google.gson.Gson;
import spark.Route;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitions;
import uk.co.compendiumdev.challenge.persistence.PersistenceLayer;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsJson;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseError;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;
import uk.co.compendiumdev.thingifier.spark.SimpleSparkRouteCreator;

import java.util.*;

import static spark.Spark.*;

public class ChallengerTrackingRoutes {

    final int MAX_CHALLENGERS_PER_IP=100;


    public void configure(final Challengers challengers,
                          final boolean single_player_mode,
                          final ThingifierApiDocumentationDefn apiDefn,
                          final PersistenceLayer persistenceLayer,
                          final Thingifier thingifier,
                          ChallengeDefinitions challengeDefinitions){


        // todo: control max challengers and ip address limiting dynamically through admin interface and via environment variables
        // until then, leave this off by passing in false
        ChallengerIpAddressTracker ipAddressTracker = new ChallengerIpAddressTracker(MAX_CHALLENGERS_PER_IP, false);



        Route getChallengerId = (request, result) -> {

            ChallengerAuthData challenger=null;
            String xChallengerGuid = request.params("id");

            if(xChallengerGuid != null && !xChallengerGuid.trim().isEmpty()){
                challenger = challengers.getChallenger(xChallengerGuid);
                if(challenger!=null){
                    challenger.touch();
                    thingifier.ensureCreatedAndPopulatedInstanceDatabaseNamed(challenger.getXChallenger());
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
        };

        SimpleSparkRouteCreator.addHandler("/challenger/:id", "options", (request, result) ->{
            result.status(204);
            // disallow POST, DELETE, PATCH, TRACE
            result.header("Allow", "GET, PUT, HEAD, OPTIONS");
            return "";
        });

        SimpleSparkRouteCreator.routeStatusWhenNot(405, "/challenger/:id", List.of("get", "put", "head", "options"));

        // refresh challenger to avoid purging
        get("/challenger/:id", (request, result) -> {
            return getChallengerId.handle(request, result);
        });

        head("/challenger/:id", (request, result) -> {
            getChallengerId.handle(request, result);
            return "";
        });

        // Document the endpoint
        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                        RoutingVerb.GET,
                        "/challenger/:guid",
                        RoutingStatus.returnedFromCall(),
                        null).addDocumentation("Get a challenger in Json format to allow continued tracking of challenges.")
                        .addPossibleStatuses(200,404));

        // endpoint to restore a saved challenger status from UI
        put("/challenger/:id", (request, result) -> {
            ChallengerAuthData challenger;

            // for which challenger?
            String xChallengerGuid = request.params("id");

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
            // but first... have a limit on the number of challengers an IP address can create
            challengers.purgeOldAuthData();
            ipAddressTracker.purgeEmptyIpAddresses(challengers.getChallengerGuids());
            if(ipAddressTracker.hasLimitBeenReachedFor(request.ip())){
                result.status(429);
                result.header("content-type", "application/json");
                result.header("X-CHALLENGER", xChallengerGuid);
                return ApiResponseError.asAppropriate(request.headers("accept"), "Attempted to create too many challengers, wait and try again later.");
            }


            // create a challenger from the JSON and instantiate the database
            ChallengerAuthData newChallenger = new ChallengerAuthData(challengeDefinitions.getDefinedChallenges()).fromData(challenger, challengeDefinitions.getDefinedChallenges());
            challengers.put(newChallenger);
            thingifier.ensureCreatedAndPopulatedInstanceDatabaseNamed(challenger.getXChallenger());

            // track challenger against IP
            ipAddressTracker.trackAgainstThisIp(request.ip(), xChallengerGuid);


            result.status(201);
            result.header("content-type", "application/json");
            result.header("X-CHALLENGER", xChallengerGuid);
            return "";
        });

        // Document the endpoint
        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                        RoutingVerb.PUT,
                        "/challenger/:guid",
                        RoutingStatus.returnedFromCall(),
                        null).addDocumentation("Restore a saved challenger matching the supplied X-CHALLENGER guid to allow continued tracking of challenges.")
                        .addPossibleStatuses(200,201,400));


        /*
            / challenger

         */

        SimpleSparkRouteCreator.addHandler("/challenger", "options", (request, result) ->{
            result.status(204);
            // disallow POST, DELETE, PATCH, TRACE
            result.header("Allow", "POST, OPTIONS");
            return "";
        });

        // create a challenger
        post("/challenger", (request, result) -> {

            if(single_player_mode){
                XChallengerHeader.setResultHeaderBasedOnChallenger(result, challengers.SINGLE_PLAYER.getXChallenger());
                result.raw().setHeader("Location", "/gui/challenges");
                result.status(201);
                return "";
            }

            String xChallengerGuid = request.headers("X-CHALLENGER");
            if(xChallengerGuid == null || xChallengerGuid.trim().isEmpty()){
                // create a new challenger with a database
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
                    // challenger already exists, ensure the database does
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

        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                        RoutingVerb.POST,
                        "/challenger",
                        RoutingStatus.returnedFromCall(),
                        null).
                        addDocumentation("Create a challenger using the X-CHALLENGER guid header.").
                        addPossibleStatuses(200,400,405));

        SimpleSparkRouteCreator.routeStatusWhenNot(405, "/challenger", List.of("post", "options"));

        /*
            The todos restore endpoint
         */

        // Document the endpoint
        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                        RoutingVerb.GET,
                        "/challenger/database/:guid",
                        RoutingStatus.returnedFromCall(),
                        null).addDocumentation("Get the todo data for the supplied X-CHALLENGER guid to allow later restoration of the todos.")
                        .addPossibleStatuses(200,400,404));

        Route getChallengerDatabaseId = (request, result) -> {
            ChallengerAuthData challenger;

            // for which challenger?
            String xChallengerGuid = request.params("id");

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
        };

        SimpleSparkRouteCreator.addHandler("/challenger/database/:id", "options", (request, result) ->{
            result.status(204);
            // disallow POST, DELETE, PATCH, TRACE
            result.header("Allow", "GET, PUT, HEAD, OPTIONS");
            return "";
        });

        SimpleSparkRouteCreator.routeStatusWhenNot(405, "/challenger/database/:id", List.of("get", "put", "head", "options"));

        // add a GET challenger/database/:id in the proper database format

        get("/challenger/database/:id", (request, result) -> {
            return getChallengerDatabaseId.handle(request, result);
        });

        head("/challenger/database/:id", (request, result) -> {
            getChallengerDatabaseId.handle(request, result);
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

        // Document the endpoint
        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                        RoutingVerb.PUT,
                        "/challenger/database/:guid",
                        RoutingStatus.returnedFromCall(),
                        null).addDocumentation("Restore a saved set of todos for a challenger matching the supplied X-CHALLENGER guid.")
                        .addPossibleStatuses(204,400));


        // TODO: add a protected admin page with an environment variable protection as password
    }


}
