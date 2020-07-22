package uk.co.compendiumdev.challenge;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;
import uk.co.compendiumdev.thingifier.application.ThingifierRestServer;
import uk.co.compendiumdev.thingifier.htmlgui.DefaultGUIHTML;
import uk.co.compendiumdev.thingifier.spark.SimpleRouteConfig;

import java.util.*;

import static spark.Spark.*;


public class ChallengeRouteHandler {
    private final Thingifier thingifier;
    List<RoutingDefinition> routes;
    ChallengeDefinitions challengeDefinitions;
    Challengers challengers;
    private boolean single_player_mode;

    public ChallengeRouteHandler(Thingifier thingifier){
        routes = new ArrayList();
        challengeDefinitions = new ChallengeDefinitions();
        this.thingifier = thingifier;
        challengers = new Challengers();
        single_player_mode = true;
    }

    public void setToMultiPlayerMode(){
        single_player_mode = false;
        challengers.setMultiPlayerMode();
    }

    public List<RoutingDefinition> getRoutes(){
        return routes;
    }

    public ChallengeRouteHandler configureRoutes() {

        configureChallengerTrackingRoutes();
        configureChallengesRoutes();
        configureHeartBeatRoutes();
        configureAuthRoutes();

        return this;
    }

    private void configureChallengesRoutes() {
        get("/challenges", (request, result) -> {
            result.status(200);
            result.type("application/json");

            ChallengerAuthData challenger = challengers.getChallenger(request.headers("X-CHALLENGER"));

            if(!single_player_mode){
                if(challenger!=null){
                    result.header("Location", "/gui/challenges/" + challenger.getXChallenger());
                }
            }else{
                result.header("Location", "/gui/challenges");
            }

            result.body(new ChallengesPayload(challengeDefinitions, challenger).getAsJson());
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

        SimpleRouteConfig.routeStatusWhenNot(
                405, "/challenges",
                "get", "head", "options");

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

        String endpoint ="/heartbeat";

        options(endpoint, (request, result) -> {
            result.status(204);
            result.header("Allow", "GET, HEAD, OPTIONS");
            return "";
        });

        new SimpleRouteConfig(endpoint).
            status(204, "get", "head").
            status(405,  "post", "delete", "put").
            status(500,  "patch").
            status(501, "trace");

        routes.add(new RoutingDefinition(
                RoutingVerb.GET,
                endpoint,
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Is the server running? YES == 204"));

        routes.add(new RoutingDefinition(
                RoutingVerb.OPTIONS,
                endpoint,
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Options for heartbeat endpoint"));

        routes.add(new RoutingDefinition(
                RoutingVerb.HEAD,
                endpoint,
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Headers for heartbeat endpoint"));
    }

    private void configureChallengerTrackingRoutes() {

        // refresh challenger to avoid purging
        get("/challenger/*", (request, result) -> {
            String xChallengerGuid = request.splat()[0];
            if(xChallengerGuid != null && xChallengerGuid.trim()!=""){
                ChallengerAuthData challenger = challengers.getChallenger(xChallengerGuid);
                if(challenger!=null){
                    challenger.touch();
                    result.status(204);
                }else{
                    result.status(404);
                }
            }else{
                result.status(404);
            }
            return "";
        });

        SimpleRouteConfig.
            routeStatusWhenNot(
            405, "/challenger/*", "get");

            // create a challenger
        post("/challenger", (request, result) -> {

            if(single_player_mode){
                result.header("X-CHALLENGER", challengers.SINGLE_PLAYER.getXChallenger());
                result.header("Location", "/gui/challenges");
                result.status(201);
                return "";
            }

            String xChallengerGuid = request.headers("X-CHALLENGER");
            if(xChallengerGuid == null || xChallengerGuid.trim()==""){
                // create a new challenger
                final ChallengerAuthData challenger = challengers.createNewChallenger();
                result.header("X-CHALLENGER", challenger.getXChallenger());
                result.header("Location", "/gui/challenges/" + challenger.getXChallenger());
                result.status(201);
                return "";
            }else {
                ChallengerAuthData challenger = challengers.getChallenger(xChallengerGuid);
                if(challenger==null){
                    // if X-CHALLENGER header exists, and is not a known UUID,
                    // return 410, challenger ID not valid
                    result.header("X-CHALLENGER", "Challenger not found");
                    result.status(422);
                }else{
                    // if X-CHALLENGER header exists, and has a valid UUID, and UUID exists, then return 200
                    result.header("X-CHALLENGER", challenger.getXChallenger());
                    result.header("Location", "/gui/challenges/" + challenger.getXChallenger());
                    result.status(200);
                }
                // todo: if X-CHALLENGER header exists, and has a valid UUID, and UUID does not exist, then use this to create the challenger, return 201


            }
            result.status(400);
            return "Unknown Challenger State";
        });

        SimpleRouteConfig.
                routeStatusWhenNot(
                        405, "/challenger", "post");

        if(!single_player_mode) {
            routes.add(new RoutingDefinition(
                    RoutingVerb.POST,
                    "/challenger",
                    RoutingStatus.returnedFromCall(),
                    null).addDocumentation("Create an X-CHALLENGER guid to allow tracking challenges, use the X-CHALLENGER header in all requests to track challenge completion for multi-user tracking."));
        }
    }

    private void configureAuthRoutes() {
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

            ChallengerAuthData challenger = challengers.getChallenger(request.headers("X-CHALLENGER"));

            if(challenger==null){
                result.status(401);
                result.header("X-CHALLENGER", "Challenger not recognised");
            }

            // if no header X-AUTH-TOKEN then grant one
            result.header("X-AUTH-TOKEN", challenger.getXAuthToken());
            result.status(201);
            return "";
        });

        SimpleRouteConfig.routeStatusWhenNot(
    405, "/secret/token", "post");


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

            ChallengerAuthData challenger = challengers.getChallenger(request.headers("X-CHALLENGER"));

            if(challenger==null){
                result.status(401);
                result.header("X-CHALLENGER", "Challenger not recognised");
            }

            if(!authToken.contentEquals(challenger.getXAuthToken())){
                result.status(403); // given token is not allowed to access anything
                return "";
            }

            result.status(200);
            result.header("Content-Type", "application/json");
            final JsonObject note = new JsonObject();
            note.addProperty("note", challenger.getNote());
            return new Gson().toJson(note);
        });

        post("/secret/note", (request, result) -> {

            final String authToken = request.headers("X-AUTH-TOKEN");
            if(authToken==null || authToken.length()==0){
                result.status(401);
                return "";
            }

            ChallengerAuthData challenger = challengers.getChallenger(request.headers("X-CHALLENGER"));

            if(challenger==null){
                result.status(401);
                result.header("X-CHALLENGER", "Challenger not recognised");
            }

            if(!authToken.contentEquals(challenger.getXAuthToken())){
                result.status(403); // given token is not allowed to access anything
                return "";
            }

            try{
                final HashMap body = new Gson().fromJson(request.body(), HashMap.class);
                if(body.containsKey("note")){
                    challenger.setNote((String)body.get("note"));
                }

                result.status(200);
                result.header("Content-Type", "application/json");
                final JsonObject note = new JsonObject();
                note.addProperty("note", challenger.getNote());
                return new Gson().toJson(note);

            }catch(Exception e){
                result.status(400);
                return "";
            }

        });

        SimpleRouteConfig.routeStatusWhenNot(
                405, "/secret/note", "get", "post");


    }



    public void addHooks(final ThingifierRestServer restServer) {

        restServer.registerPreRequestHook(new ChallengerSparkHTTPRequestHook(challengers));
        restServer.registerPostResponseHook(new ChallengerSparkHTTPResponseHook(challengers));
        restServer.registerHttpApiRequestHook(new ChallengerApiRequestHook(challengers));
        restServer.registerHttpApiResponseHook(new ChallengerApiResponseHook(challengers, thingifier));
    }

    public void setupGui(DefaultGUIHTML guiManagement) {
        guiManagement.appendMenuItem("Challenges", "/gui/challenges");
        guiManagement.removeMenuItem("Home");
        guiManagement.prefixMenuItem("Home", "/");

        guiManagement.setHomePageContent("    <h2 id=\"challenges\">Challenges</h2>\n" +
                "    <p>The challenges can be completed by issuing API requests to the API.</p>\n" +
                "    <p>e.g. <code>GET http://localhost:4567/todos</code> would complete the challenge to &quot;GET the list of todos&quot;</p>\n" +
                "    <p>You can also <code>GET http://localhost:4567/challenges</code> to get the list of challenges and their status as an API call. </p>\n"
                );

        guiManagement.setFooter(getChallengesFooter());

        // use the index.html to allow easier creation of docs and landing page
//        get("/", (request, result) -> {
//            result.redirect("/gui");
//            return "";
//        });

        // single user / default session
        get("/gui/challenges", (request, result) -> {
            result.type("text/html");
            result.status(200);

            StringBuilder html = new StringBuilder();
            html.append(guiManagement.getPageStart("Challenges"));
            html.append(guiManagement.getMenuAsHTML());

            // todo explain challenges - single user mode


            List<ChallengeData> reportOn = new ArrayList<>();

            if(single_player_mode){
                html.append(playerChallengesIntro());
                reportOn = new ChallengesPayload(challengeDefinitions, challengers.SINGLE_PLAYER).getAsChallenges();
            }else{
                html.append("<div style='clear:both'><p><strong>Unknown Challenger ID</strong></p></div>");
                html.append(multiUserShortHelp());

                reportOn = new ChallengesPayload(challengeDefinitions, challengers.DEFAULT_PLAYER_DATA).getAsChallenges();
            }

            html.append(renderChallengeData(reportOn));

            html.append(guiManagement.getPageFooter());
            html.append(guiManagement.getPageEnd());
            return html.toString();
        });

        // multi user
        get("/gui/challenges/*", (request, result) -> {
            result.type("text/html");
            result.status(200);

            StringBuilder html = new StringBuilder();
            html.append(guiManagement.getPageStart("Challenges"));
            html.append(guiManagement.getMenuAsHTML());

            html.append(playerChallengesIntro());


            List<ChallengeData> reportOn = null;

            String xChallenger = request.splat()[0];
            final ChallengerAuthData challenger = challengers.getChallenger(xChallenger);
            if(challenger==null){
                html.append("<p><strong>Unknown Challenger ID</strong></p>");
                html.append(multiUserShortHelp());
                reportOn = new ChallengesPayload(challengeDefinitions, challengers.DEFAULT_PLAYER_DATA).getAsChallenges();
            }else{
                reportOn = new ChallengesPayload(challengeDefinitions, challenger).getAsChallenges();
                html.append(refreshScriptFor(challenger.getXChallenger()));
            }

            html.append(renderChallengeData(reportOn));

            html.append(guiManagement.getPageFooter());
            html.append(guiManagement.getPageEnd());
            return html.toString();
        });
    }

    private String getChallengesFooter() {
        return "<p>&nbsp;</p><hr/><div class='footer'><p>Copyright Compendium Developments Ltd 2020 </p>\n" +
                "<ul class='footerlinks'><li><a href='https://eviltester.com/apichallenges'>API Challenges Info</a></li>\n" +
                "<li><a href='https://eviltester.com'>EvilTester.com</a></li>\n" +
                "</ul></div>";
    }

    private String playerChallengesIntro() {
        final StringBuilder html = new StringBuilder();
        html.append("<div style='clear:both'>");
        html.append("<p>Use the Descriptions of the challenges below to explore the API and solve the challenges. Remember to use the API documentation to see the format of POST requests.</p>");
        html.append("</div>");
        return html.toString();
    }

    private String multiUserShortHelp() {
        final StringBuilder html = new StringBuilder();
        html.append("<div style='clear:both' class='headertextblock'>");
        html.append("<p>To view your challenges status in multi-user mode, make sure you have registered as a challenger using a `POST` request to `/challenger` and are including an `X-CHALLENGER` header in all your requests.</p>");
        html.append("<p>Then view the challenges in the GUI by visiting `/gui/challenges/{GUID}`, where `{GUID}` is the value in the `X-CHALLENGER` header.<p>");
        html.append("<p>You can find more information about this on the <a href='multiuser.html'>Multi User Help Page</a><p>");
        html.append("</div>");
        return html.toString();
    }

    private String refreshScriptFor(final String xChallenger) {
        StringBuilder html = new StringBuilder();

        html.append("<script>");
        html.append("/* keep session alive */");
        html.append("setInterval(function(){");
        html.append("var oReq = new XMLHttpRequest();\n" +
                "oReq.open('GET', '/challenger/" + xChallenger +"');\n" +
                "oReq.send();");
        html.append("},300000);");
        html.append("</script>");
        return html.toString();
    }

    // todo: save challenge status in local storage
    // todo: post challenge status from local storage to current X-CHALLENGER session
    // todo: clear local storage challenge status

    private String renderChallengeData(final List<ChallengeData> reportOn) {
        StringBuilder html = new StringBuilder();

        html.append("<table>");
        html.append("<thead>");
        html.append("<tr>");

        html.append("<th>Challenge</th>");
        html.append("<th>Done</th>");
        html.append("<th>Description</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");

        for(ChallengeData challenge : reportOn){
            html.append("<tr>");
            html.append(String.format("<td>%s</td>", challenge.name));
            html.append(String.format("<td>%b</td>", challenge.status));
            html.append(String.format("<td>%s</td>", challenge.description));
            html.append("</tr>");
        }

        html.append("</tbody>");
        html.append("</table>");

        return html.toString();
    }
}
