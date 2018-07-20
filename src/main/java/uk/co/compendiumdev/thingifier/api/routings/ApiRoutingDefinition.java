package uk.co.compendiumdev.thingifier.api.routings;

import uk.co.compendiumdev.thingifier.api.response.ResponseHeader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ApiRoutingDefinition {

    private List<RoutingDefinition> routings;

    public ApiRoutingDefinition(){
        this.routings = new ArrayList<>();
    }

    public Collection<RoutingDefinition> definitions() {
        return routings;
    }

    public void addRouting(String documentation, RoutingVerb verb, String url, RoutingStatus routingStatus) {
        routings.add(new RoutingDefinition(verb, url, routingStatus, null).addDocumentation(documentation));
    }

    public void addRouting(String documentation, RoutingVerb verb, String url, RoutingStatus routingStatus, ResponseHeader header) {
        routings.add(new RoutingDefinition(verb, url, routingStatus, header).addDocumentation(documentation));
    }
}
