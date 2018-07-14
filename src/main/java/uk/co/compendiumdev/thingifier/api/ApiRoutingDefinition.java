package uk.co.compendiumdev.thingifier.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ApiRoutingDefinition {

    private List<RoutingDefinition> routings;

    public ApiRoutingDefinition(){
        this.routings = new ArrayList<>();
    }

    public void addRouting(RoutingVerb verb, String url, RoutingStatus routingStatus) {

        routings.add(new RoutingDefinition(verb, url, routingStatus, null));


    }

    public Collection<RoutingDefinition> definitions() {
        return routings;
    }

    public void addRouting(RoutingVerb verb, String url, RoutingStatus routingStatus, ResponseHeader header) {
        routings.add(new RoutingDefinition(verb, url, routingStatus, header));
    }
}
