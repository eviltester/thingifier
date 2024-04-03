package uk.co.compendiumdev.thingifier.application;

import spark.Route;
import uk.co.compendiumdev.thingifier.api.ThingifierApiDefn;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;
import uk.co.compendiumdev.thingifier.spark.SimpleRouteConfig;

import java.util.Arrays;

/*
    The API documentation is auto generated.

    This class allows us to add adhoc Urls into the documentation which are implemented via a handler or specific status

 */
public class AdhocDocumentedSparkRouteConfig {
    private final ThingifierApiDefn apiDefn;

    public AdhocDocumentedSparkRouteConfig(final ThingifierApiDefn apiDefn) {
        this.apiDefn = apiDefn;
    }

    public AdhocDocumentedSparkRouteConfig add(
            final String endpoint, final RoutingVerb verb,
            final int statusCode, final String documentation,
            Route routeHandler) {

        SimpleRouteConfig.addHandler(endpoint, verb.name(), routeHandler);

        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                        verb,
                        endpoint,
                        RoutingStatus.returnedFromCall(),
                        null).addDocumentation(documentation).
                        addPossibleStatuses(statusCode));

        return this;
    }

    public AdhocDocumentedSparkRouteConfig add(
            final String endpoint, final RoutingVerb verb,
            final int statusCode, final String documentation) {

        SimpleRouteConfig.routeStatus(statusCode, endpoint, true, Arrays.asList(verb.name()));

        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(
                        verb,
                        endpoint,
                        RoutingStatus.returnValue(statusCode),
                        null).addDocumentation(documentation).
                        addPossibleStatuses(statusCode));

        return this;
    }
}
