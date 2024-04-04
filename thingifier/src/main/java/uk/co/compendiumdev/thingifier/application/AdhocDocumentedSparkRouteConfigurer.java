package uk.co.compendiumdev.thingifier.application;

import spark.Route;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.spark.SimpleRouteConfig;

import java.util.Arrays;

/*
    The API documentation is auto generated.

    This class allows us to add adhoc Urls into the documentation which are implemented via a handler or specific status

 */
public class AdhocDocumentedSparkRouteConfigurer {
    private final ThingifierApiDocumentationDefn apiDefn;

    public AdhocDocumentedSparkRouteConfigurer(final ThingifierApiDocumentationDefn apiDefn) {
        this.apiDefn = apiDefn;
    }

    public AdhocDocumentedSparkRouteConfigurer add(
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

    public AdhocDocumentedSparkRouteConfigurer add(
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
