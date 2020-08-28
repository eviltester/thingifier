package uk.co.compendiumdev.challenge.challengesrouting;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import uk.co.compendiumdev.challenge.BasicAuthHeaderParser;
import uk.co.compendiumdev.challenge.BearerAuthHeaderParser;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.api.ThingifierApiDefn;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;
import uk.co.compendiumdev.thingifier.spark.SimpleRouteConfig;

import java.util.HashMap;

import static spark.Spark.get;
import static spark.Spark.post;

public class AuthRoutes {
    public void configure(final Challengers challengers,
                          final ThingifierApiDefn apiDefn) {
        // authentication and authorisation
        // - create a 'secret' note which can be stored against session using an auth token


        // POST /secret/token with basic auth to get a secret/token to use as X-AUTH-TOKEN header
        // todo: or {username, password} payload
        post("/secret/token", (request, result) -> {

            BasicAuthHeaderParser basicAuth = new BasicAuthHeaderParser(request.headers("Authorization"));

            // admin/password as default username:password
            if(!basicAuth.matches("admin","password")){
                result.raw().setHeader("WWW-Authenticate","Basic realm=\"User Visible Realm\"");
                result.status(401);
                return "";
            }

            ChallengerAuthData challenger = challengers.getChallenger(request.headers("X-CHALLENGER"));

            if(challenger==null){
                result.status(401);
                result.raw().setHeader("X-CHALLENGER", "Challenger not recognised");
                return "";
            }

            // if no header X-AUTH-TOKEN then grant one
            result.raw().setHeader("X-AUTH-TOKEN", challenger.getXAuthToken());
            result.status(201);
            return "";
        });

        SimpleRouteConfig.routeStatusWhenNot(
                405, "/secret/token", "post");

        apiDefn.addAdditionalRoute(
                new RoutingDefinition(
                RoutingVerb.POST,
                "/secret/token",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("POST /secret/token with basic auth to get a secret/token to use as X-AUTH-TOKEN header, to allow access to the /secret/note end points.").
                    addPossibleStatuses(201,401));

        // todo: GET /secret/token returns the secret token or 401 if not authenticated


        // POST /secret/note GET /secret/note - limit note to 100 chars
        // no auth token will receive a 403
        // auth token which does not match the session will receive a 401
        // header X-AUTH-TOKEN: token given - if token not found (then) 401

        get("/secret/note", (request, result) -> {

            // todo: allow the X-AUTH-TOKEN to be sent as an Authorization: Bearer <token>
            // todo: the bearer token should take precendence
            String authToken = request.headers("X-AUTH-TOKEN");
            final String authorization = request.headers("Authorization");

            result.header("Content-Type", "application/json");

            ChallengerAuthData challenger = challengers.getChallenger(request.headers("X-CHALLENGER"));

            // todo: if no X-CHALLENGER provided then, search memory for authToken and use associated
            //       challenger

            if(challenger==null){
                result.status(401);
                result.raw().setHeader("X-CHALLENGER", "Challenger not recognised");
                return "";
            }

            // authorization bearer token will take precedence over X-AUTH-HEADER
            if(authorization!=null && authorization.length()!=0){
                final BearerAuthHeaderParser bearerToken = new BearerAuthHeaderParser(authorization);
                if(bearerToken.isBearerToken() && bearerToken.isValid()){
                    authToken = bearerToken.getToken();
                }
            }

            if(authToken==null || authToken.length()==0){
                result.status(401);
                return "";
            }

            if(!authToken.contentEquals(challenger.getXAuthToken())){
                result.status(403); // given token is not allowed to access anything
                return "";
            }

            result.status(200);
            final JsonObject note = new JsonObject();
            note.addProperty("note", challenger.getNote());
            return new Gson().toJson(note);
        });

        apiDefn.addAdditionalRoute(
                new RoutingDefinition(
                RoutingVerb.GET,
                "/secret/note",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation(
                        "GET /secret/note with X-AUTH-TOKEN to return the secret note for the user.").
                        addPossibleStatuses(200,401,403));


        post("/secret/note", (request, result) -> {

            final String authorization = request.headers("Authorization");
            String authToken = request.headers("X-AUTH-TOKEN");

            // todo: if no X-CHALLENGER provided then, search memory for authToken and use associated
            //       challenger
            ChallengerAuthData challenger = challengers.getChallenger(request.headers("X-CHALLENGER"));

            if(challenger==null){
                result.status(401);
                result.raw().setHeader("X-CHALLENGER", "Challenger not recognised");
                return "";
            }

            result.raw().setHeader("X-CHALLENGER", challenger.getXChallenger());
            result.header("Content-Type", "application/json");

            // authorization bearer token will take precedence over X-AUTH-HEADER
            if(authorization!=null && authorization.length()!=0){
                final BearerAuthHeaderParser bearerToken = new BearerAuthHeaderParser(authorization);
                if(bearerToken.isBearerToken() && bearerToken.isValid()){
                    authToken = bearerToken.getToken();
                }
            }

            if(authToken==null || authToken.length()==0){
                result.status(401);
                return "";
            }

            if(!authToken.contentEquals(challenger.getXAuthToken())){
                result.status(403); // given token is not allowed to access anything
                return "";
            }

            final HashMap body = new Gson().fromJson(request.body(), HashMap.class);

            // could not parse input
            if(body==null){
                result.status(400);
                return "";
            }

            if(body.containsKey("note")){
                challenger.setNote((String)body.get("note"));
            }else{
                result.status(400);
                return "";
            }

            result.status(200);
            final JsonObject note = new JsonObject();
            note.addProperty("note", challenger.getNote());
            return new Gson().toJson(note);

        });

        SimpleRouteConfig.routeStatusWhenNot(
                405, "/secret/note", "get", "post");

        apiDefn.addAdditionalRoute(
                new RoutingDefinition(
                RoutingVerb.POST,
                "/secret/note",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("POST /secret/note with X-AUTH-TOKEN, and a payload of `{'note':'contents of note'}` to amend the contents of the secret note.").
                addPossibleStatuses(200,400,401,403));
    }
}
