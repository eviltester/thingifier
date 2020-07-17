package uk.co.compendiumdev.challenge;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;
import uk.co.compendiumdev.thingifier.application.ThingifierRestServer;
import uk.co.compendiumdev.thingifier.htmlgui.DefaultGUIHTML;

import java.util.*;

import static spark.Spark.get;

public class ChallengeRouteHandler {
    private final Thingifier thingifier;
    List<RoutingDefinition> routes;
    Challenges challenges;

    public ChallengeRouteHandler(Thingifier thingifier){
        routes = new ArrayList();
        challenges = new Challenges();
        this.thingifier = thingifier;
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

        restServer.registerPreRequestHook(new ChallengerSparkHTTPRequestHook(challenges));
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
