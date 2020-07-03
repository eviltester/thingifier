package uk.co.compendiumdev.thingifier.api.routings;

import uk.co.compendiumdev.thingifier.api.response.ResponseHeader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final public class ApiRoutingDefinition {

    private List<RoutingDefinition> routings;

    public ApiRoutingDefinition() {
        this.routings = new ArrayList<>();
    }

    public Collection<RoutingDefinition> definitions() {
        return routings;
    }

    public void addRouting(final String documentation, final RoutingVerb verb, final String url, final RoutingStatus routingStatus) {
        routings.add(new RoutingDefinition(verb, url, routingStatus, null).addDocumentation(documentation));
    }

    public void addRouting(final String documentation, final RoutingVerb verb, final String url, final RoutingStatus routingStatus, final ResponseHeader header) {
        routings.add(new RoutingDefinition(verb, url, routingStatus, header).addDocumentation(documentation));
    }
}
