package uk.co.compendiumdev.thingifier.htmlgui.routing;

import spark.Request;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGuiHtmlPages;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;

public class DefaultGuiRoutings {

    // TODO: templates or tidier way to create the default GUI pages with styling
    // todo: support filters in the GUI Urls
    private final DefaultGUIHTML templates;

    private DefaultGuiHtmlPages htmlPages;
    private final Thingifier thingifier;

    public DefaultGuiRoutings(final Thingifier thingifier, DefaultGUIHTML defaultGui) {
        this.templates = defaultGui;
        this.thingifier = thingifier;

    }

    // we support a top level path e.g. /apichallenges/gui, /simpleapi/gui because we should support multiple GUI views with multiple thingifiers
    public DefaultGuiRoutings configureRoutes(String urlPrefixPath){

        this.htmlPages = new DefaultGuiHtmlPages(templates, thingifier, urlPrefixPath);

        // TODO: separate the HTML rendering from the HTTP routing with a class e.g. DefaultEntitesExplorerHTML
        get("%s".formatted(urlPrefixPath), (request, response) -> {
            response.type("text/html");
            response.status(200);

            return htmlPages.getHomePageHtml("GUI", "", urlPrefixPath);
        });

        get("%s/entities".formatted(urlPrefixPath), (request, response) -> {
            response.type("text/html");
            response.status(200);

            String database = getDatabaseNameFromRequest(request);
            // by default the GUI does not set the cookie, the 'app' does that
            //response.cookie("X-THINGIFIER-DATABASE-NAME", database);

            return htmlPages.getEntitiesListPage(database);
        });

        get("%s/instances".formatted(urlPrefixPath), (request, response) -> {
            response.type("text/html");
            response.status(200);


            String database = getDatabaseNameFromRequest(request);

            String entityName = request.queryParams("entity");

            return htmlPages.getInstancesListPage(database, entityName);


        });

        get("%s/instance".formatted(urlPrefixPath), (request, response) -> {
            response.type("text/html");
            response.status(200);


            String database = getDatabaseNameFromRequest(request);

            String entityName = "";
            for (String queryParam : request.queryParams()) {
                if (queryParam.contentEquals("entity")) {
                    entityName = request.queryParams("entity");
                }
            }

            Map<String,String> instanceQueryParams = new HashMap<>();

            for (String queryParam : request.queryParams()) {
                if (!queryParam.equals("entity")) {
                    instanceQueryParams.put(queryParam, request.queryParams(queryParam));
                }
            }

            return htmlPages.getInstanceDetailsPage(database, entityName, instanceQueryParams);


        });

        return this;
    }


    // TODO: this is where we would 'inject' or handle the authentication process - could be injected with a Thingifier specific handler
    // TODO: multiple thingifiers would require different cookie names - give Thingifier a name and include in cookie
    // e.g. X-APICHALLENGES-THINGIFIER-DATABASE-NAME, X-SIMPLEAPI-THINGIFIER-DATABASE-NAME
    private String getDatabaseNameFromRequest(Request request) {

        if(!thingifier.apiConfig().supportsMultipleDatabases()){
            return EntityRelModel.DEFAULT_DATABASE_NAME;
        }

        String xdatabasename = "";

        if(request.cookie("X-THINGIFIER-DATABASE-NAME")!=null){
            xdatabasename=request.cookie("X-THINGIFIER-DATABASE-NAME");
        }

        if(request.queryParams("database")!=null){
            xdatabasename = request.queryParams("database");
        }

        if(xdatabasename.equals("")){
            xdatabasename = EntityRelModel.DEFAULT_DATABASE_NAME;
        }

        return xdatabasename;
    }

}


