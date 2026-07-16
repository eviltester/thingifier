package uk.co.compendiumdev.thingifier.adapter.httpserver;

import java.util.List;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;

/*
   The API documentation is auto generated.

   This class allows us to add adhoc Urls into the documentation which are implemented via a handler or specific status

*/
public class AdhocDocumentedHttpRouteConfigurer {
    private final ThingifierApiDocumentationDefn apiDefn;

    public AdhocDocumentedHttpRouteConfigurer(final ThingifierApiDocumentationDefn apiDefn) {
        this.apiDefn = apiDefn;
    }

    public AdhocDocumentedHttpRouteConfigurer add(
            final String endpoint,
            final RoutingVerb verb,
            final int statusCode,
            final String documentation,
            HttpRouteHandler routeHandler) {

        SimpleHttpRouteCreator.addHandler(endpoint, verb.name(), routeHandler);

        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(verb, endpoint, RoutingStatus.returnedFromCall(), null)
                        .addDocumentation(documentation)
                        .addPossibleStatuses(statusCode));

        return this;
    }

    public AdhocDocumentedHttpRouteConfigurer add(
            final String endpoint,
            final RoutingVerb verb,
            final int statusCode,
            final String documentation) {

        SimpleHttpRouteCreator.routeStatus(statusCode, endpoint, true, List.of(verb.name()));

        apiDefn.addRouteToDocumentation(
                new RoutingDefinition(verb, endpoint, RoutingStatus.returnValue(statusCode), null)
                        .addDocumentation(documentation)
                        .addPossibleStatuses(statusCode));

        return this;
    }

    public ThingifierApiDocumentationDefn getApiDocDefn() {
        return apiDefn;
    }
}
