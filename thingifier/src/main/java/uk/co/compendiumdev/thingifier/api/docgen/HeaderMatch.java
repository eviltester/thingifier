package uk.co.compendiumdev.thingifier.api.docgen;

public class HeaderMatch {
    public final RoutingDefinition routingDefn;
    public final HeaderDefinition headerDefn;

    public HeaderMatch(RoutingDefinition routingDefinition, HeaderDefinition headerDefn) {
        this.routingDefn = routingDefinition;
        this.headerDefn = headerDefn;
    }
}
