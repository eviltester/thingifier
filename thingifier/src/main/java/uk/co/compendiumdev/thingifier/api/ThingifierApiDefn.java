package uk.co.compendiumdev.thingifier.api;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;

import java.util.ArrayList;
import java.util.List;

public class ThingifierApiDefn {

    private Thingifier thingifier;
    private final ArrayList<RoutingDefinition> additionalRoutes;
    private List<ApiServer> servers;
    private String version;

    // todo: convert internal documentation to use a ThingifierApiDefn

    public ThingifierApiDefn() {
        this.additionalRoutes = new ArrayList<RoutingDefinition>();
        this.servers = new ArrayList<>();
        this.version="1.0.0";
        // todo: support optional swagger info
        // terms of service, contact, license, extensions
        // https://swagger.io/specification/
    }

    public ThingifierApiDefn setThingifier(Thingifier thingifier)
    {
        this.thingifier = thingifier;
        return this;
    }

    public Thingifier getThingifier() {
        return this.thingifier;
    }

    /*
        Add an additional route, not managed or defined by the thingifier model itself
        but for documentation purposes (swagger, docs, code generation, testin etc.),
        should be considered part of the API
     */
    public ThingifierApiDefn addAdditionalRoute(final RoutingDefinition routingDefinition) {
        additionalRoutes.add(routingDefinition);
        return this;
    }

    public ThingifierApiDefn setVersion(final String version) {
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

    public ThingifierApiDefn addAdditionalRoutes(final List<RoutingDefinition> routes) {
        additionalRoutes.addAll(routes);
        return this;
    }

    public class ApiServer{
        public final String url;
        public final String description;

        public ApiServer(String url, String description){
            this.url = url;
            this.description = description;
        }
    }

    public ThingifierApiDefn addServer(final String url, final String description) {
        servers.add(new ApiServer(url, description));
        return this;
    }
}
