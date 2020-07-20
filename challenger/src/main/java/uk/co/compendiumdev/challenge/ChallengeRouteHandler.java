package uk.co.compendiumdev.challenge;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;
import uk.co.compendiumdev.thingifier.application.ThingifierRestServer;
import uk.co.compendiumdev.thingifier.htmlgui.DefaultGUIHTML;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static spark.Spark.*;


public class ChallengeRouteHandler {
    private final Thingifier thingifier;
    List<RoutingDefinition> routes;
    // todo: create a map of challenges where key is the Challenges guid
    // todo: create a key of 'global' with a Challenges to use as default
    // todo: delete any Challenges which have not been 'accessed' in 15 minutes
    // todo: associate challenges GUID with a session id - let spark manage sessions
    // todo: if session has no challenge guid associated then associate it with "global"

    Challenges challenges;
    Map<String,ChallengeAuthData> authData;

    public ChallengeRouteHandler(Thingifier thingifier){
        routes = new ArrayList();
        challenges = new Challenges(); //default global challenges
        this.thingifier = thingifier;
        authData = new ConcurrentHashMap<>();
    }

    public List<RoutingDefinition> getRoutes(){
        return routes;
    }

    public ChallengeRouteHandler configureRoutes() {

        // TODO: create some thingifier helper methods for setting up 405 endpoints with range of verbs
        configureChallengesRoutes();
        configureHeartBeatRoutes();
        configureAuthRoutes();

        return this;
    }

    private void configureChallengesRoutes() {
        get("/challenges", (request, result) -> {
            result.status(200);
            result.type("application/json");
            result.body(challenges.getAsJson());
            return "";
        });

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

        routes.add(new RoutingDefinition(
                RoutingVerb.GET,
                "/challenges",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Get list of challenges and their completion status"));

        routes.add(new RoutingDefinition(
                RoutingVerb.OPTIONS,
                "/challenges",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Options for list of challenges endpoint"));

        routes.add(new RoutingDefinition(
                RoutingVerb.HEAD,
                "/challenges",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Headers for list of challenges endpoint"));
    }

    private void configureHeartBeatRoutes() {
        get("/heartbeat", (request, result) -> {
            result.status(204);
            return "";
        });

        head("/heartbeat", (request, result) -> {
            result.status(204);
            return "";
        });

        options("/heartbeat", (request, result) -> {
            result.status(204);
            result.header("Allow", "GET, HEAD, OPTIONS");
            return "";
        });

        post("/heartbeat", (request, result) -> {
            result.status(405);
            return "";
        });

        delete("/heartbeat", (request, result) -> {
            result.status(405);
            return "";
        });

        put("/heartbeat", (request, result) -> {
            result.status(405);
            return "";
        });

        patch("/heartbeat", (request, result) -> {
            result.status(500);
            return "";
        });

        trace("/heartbeat", (request, result) -> {
            result.status(501);
            return "";
        });

        routes.add(new RoutingDefinition(
                RoutingVerb.GET,
                "/heartbeat",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Is the server running? YES == 204"));

        routes.add(new RoutingDefinition(
                RoutingVerb.OPTIONS,
                "/heartbeat",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Options for heartbeat endpoint"));

        routes.add(new RoutingDefinition(
                RoutingVerb.HEAD,
                "/heartbeat",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Headers for heartbeat endpoint"));
    }

    private void configureAuthRoutes() {
        // todo: delete out of date and not used for 15 minutes data prior to every http request
        // authentication and authorisation
        // - create a 'secret' note which can be stored against session using an auth token

        // POST /secret/token with basic auth or {username, password} payload to get a secret/token
        post("/secret/token", (request, result) -> {

            BasicAuthHeader basicAuth = new BasicAuthHeader(request.headers("Authorization"));

            // admin/password as default username:password
            if(!basicAuth.matches("admin","password")){
                result.header("WWW-Authenticate","Basic realm=\"User Visible Realm\"");
                result.status(401);
                return "";
            }

            final ChallengeAuthData secret = new ChallengeAuthData();
            authData.put(secret.getGuid(), secret);
            // if no header X-AUTH-TOKEN then grant one
            result.header("X-AUTH-TOKEN", secret.getGuid());
            result.status(201);
            return "";
        });

        // GET /secret/token returns the secret token or 401 if not authenticated
        // POST /secret/note GET /secret/note - limit note to 100 chars
        // no auth token will receive a 403
        // auth token which does not match the session will receive a 401
        // header X-AUTH-TOKEN: token given - if token not found (then) 401

        get("/secret/note", (request, result) -> {

            final String authToken = request.headers("X-AUTH-TOKEN");
            if(authToken==null || authToken.length()==0){
                result.status(401);
                return "";
            }
            ChallengeAuthData data = authData.get(authToken);
            if(data==null){
                result.status(403); // given token is not allowed to access anything
                return "";
            }

            result.status(200);
            result.header("Content-Type", "application/json");
            final JsonObject note = new JsonObject();
            note.addProperty("note", data.getNote());
            return new Gson().toJson(note);
        });

        post("/secret/note", (request, result) -> {

            final String authToken = request.headers("X-AUTH-TOKEN");
            if(authToken==null || authToken.length()==0){
                result.status(401);
                return "";
            }
            ChallengeAuthData data = authData.get(authToken);
            if(data==null){
                result.status(403); // given token is not allowed to access anything
                return "";
            }

            try{
                final HashMap body = new Gson().fromJson(request.body(), HashMap.class);
                if(body.containsKey("note")){
                    data.setNote((String)body.get("note"));
                }

                result.status(200);
                result.header("Content-Type", "application/json");
                final JsonObject note = new JsonObject();
                note.addProperty("note", data.getNote());
                return new Gson().toJson(note);

            }catch(Exception e){
                result.status(400);
                return "";
            }

        });


    }

    public void addHooks(final ThingifierRestServer restServer) {

        restServer.registerPreRequestHook(new ChallengerSparkHTTPRequestHook(challenges, authData));
        restServer.registerPostResponseHook(new ChallengerSparkHTTPResponseHook(challenges));
        restServer.registerHttpApiRequestHook(new ChallengerApiRequestHook(challenges));
        restServer.registerHttpApiResponseHook(new ChallengerApiResponseHook(challenges, thingifier));
    }

    public void setupGui(DefaultGUIHTML guiManagement) {
        guiManagement.addMenuItem("Challenges", "/gui/challenges");

        guiManagement.setHomePageContent("    <h2 id=\"challenges\">Challenges</h2>\n" +
                "    <p>The challenges can be completed by issuing API requests to the API.</p>\n" +
                "    <p>e.g. <code>GET http://localhost:4567/todos</code> would complete the challenge to &quot;GET the list of todos&quot;</p>\n" +
                "    <p>You can also <code>GET http://localhost:4567/challenges</code> to get the list of challenges and their status as an API call. </p>\n"
                );

        get("/", (request, result) -> {
            result.redirect("/gui");
            return "";
        });

        get("/gui/challenges", (request, result) -> {
            result.type("text/html");
            result.status(200);
            StringBuilder html = new StringBuilder();
            html.append(guiManagement.getPageStart("Challenges"));
            html.append(guiManagement.getMenuAsHTML());

            html.append("<table>");
            html.append("<thead>");
            html.append("<tr>");

            html.append("<th>Challenge</th>");
            html.append("<th>Done</th>");
            html.append("<th>Description</th>");
            html.append("</tr>");
            html.append("</thead>");
            html.append("<tbody>");

            for(ChallengeData challenge : challenges.getChallenges()){
                html.append("<tr>");
                html.append(String.format("<td>%s</td>", challenge.name));
                html.append(String.format("<td>%b</td>", challenge.status));
                html.append(String.format("<td>%s</td>", challenge.description));
                html.append("</tr>");
            }

            html.append("</tbody>");
            html.append("</table>");

            html.append(guiManagement.getPageFooter());
            html.append(guiManagement.getPageEnd());
            return html.toString();
        });




    }
}
