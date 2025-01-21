package uk.co.compendiumdev.thingifier.api.docgen;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ThingifierRestAPIHandler;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    Used to define some documentation for the Thingifiers

    e.g. document custom added routes that should be part of the Thingifier documentation

 */
public class ThingifierApiDocumentationDefn {

    private Thingifier thingifier;
    private final ArrayList<RoutingDefinition> additionalRoutes;
    private List<ApiServer> servers;
    private String version;
    private String title="";
    private String description="";
    private String pathPrefix="";
    private Map<String, HeaderMatch> customHeadersForRoutesThatDoNotMatch;


    // todo: convert internal documentation to use a ThingifierApiDefn rather than a direct thingifier and additional routes

    public ThingifierApiDocumentationDefn() {
        this.additionalRoutes = new ArrayList<RoutingDefinition>();
        this.servers = new ArrayList<>();
        this.version="1.0.0";
        this.thingifier = new Thingifier(); // basically an empty thingifier
        // todo: support optional swagger info
        // terms of service, contact, license, extensions
        // https://swagger.io/specification/

        // this is a bit of hack, really I want a list of rules to apply
        // but I will start with this and see how often I use it
        customHeadersForRoutesThatDoNotMatch = new HashMap<>();
    }

    public ThingifierApiDocumentationDefn setThingifier(Thingifier thingifier)
    {
        this.thingifier = thingifier;
        return this;
    }

    public Thingifier getThingifier() {
        return this.thingifier;
    }

    /*
        Add an additional route for documentation, not managed or defined by the thingifier model itself
        but for documentation purposes (swagger, docs, code generation, testing etc.),
        should be considered part of the API
     */
    public ThingifierApiDocumentationDefn addRouteToDocumentation(final RoutingDefinition routingDefinition) {
        additionalRoutes.add(addAnyGlobalHeaders(routingDefinition));
        return this;
    }

    public ThingifierApiDocumentationDefn setVersion(final String version) {
        this.version = version;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public List<RoutingDefinition> getAdditionalRoutes() {
        return additionalRoutes;
    }

    public List<ApiServer> getServers() {
        return servers;
    }

    public ThingifierApiDocumentationDefn addRoutesToDocumentation(final List<RoutingDefinition> routes) {
        for(RoutingDefinition aRoute : routes){
            addRouteToDocumentation(aRoute);
        }
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription(){
        return description;
    }

    public void setPathPrefix(String prefix) {
        String slash = "";
        if(prefix!=null && !prefix.startsWith("/")){
            slash = "/";
        }
        pathPrefix = slash + prefix;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    /**
     * Given a skeletal RoutingDefinition used to match the verb and the endpoint
     * If
     * @param routingDefinition
     */
    public void addCustomHeaderWhenRouteNotMatches(RoutingDefinition routingDefinition, HeaderDefinition header) {

        customHeadersForRoutesThatDoNotMatch.put(routingDefinition.verb() + routingDefinition.url(), new HeaderMatch(routingDefinition, header));

        // scan any existing routes and add the custom header
        for(RoutingDefinition aRoute : additionalRoutes){

        }

    }

    public RoutingDefinition addAnyGlobalHeaders(RoutingDefinition route){
        return addCustomHeadersForRouteNotMatches(route);
    }

    private RoutingDefinition addCustomHeadersForRouteNotMatches(RoutingDefinition aRoute) {
        for(HeaderMatch match : customHeadersForRoutesThatDoNotMatch.values()){
            if(!(aRoute.verb() == match.routingDefn.verb() && aRoute.url().equals(match.routingDefn.url()))){
                if(!aRoute.hasCustomHeaderNamed(match.headerDefn.headerName)){
                    aRoute.addCustomHeader(match.headerDefn.headerName, match.headerDefn.headerType);
                }
            }
        }
        return aRoute;
    }


    public class ApiServer{
        public final String url;
        public final String description;

        public ApiServer(String url, String description){
            this.url = url;
            this.description = description;
        }
    }

    public ThingifierApiDocumentationDefn addServer(final String url, final String description) {
        servers.add(new ApiServer(url, description));
        return this;
    }
}
