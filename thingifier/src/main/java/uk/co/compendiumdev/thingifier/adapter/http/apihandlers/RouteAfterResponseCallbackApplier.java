package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.logging.Level;
import java.util.logging.Logger;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleContext;
import uk.co.compendiumdev.thingifier.api.callbacks.ThingifierApiFinalResponse;
import uk.co.compendiumdev.thingifier.api.callbacks.ThingifierApiOperationContext;
import uk.co.compendiumdev.thingifier.api.callbacks.ThingifierApiResponseCallbackDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ApiRequestEnvelope;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiRouteRule;

/**
 * Runs route-level final-response callbacks after HTTP rendering and content negotiation.
 *
 * <p>Callbacks are observational in v1. If application code throws, the exception is logged and the
 * already-produced response is preserved so metrics or challenge-completion code cannot
 * accidentally corrupt the HTTP outcome.
 */
public final class RouteAfterResponseCallbackApplier {

    private static final Logger LOGGER =
            Logger.getLogger(RouteAfterResponseCallbackApplier.class.getName());

    private final RouteCallbackContextFactory contextFactory;

    /**
     * Creates an applier for the current API runtime.
     *
     * @param runtime runtime used to find route rules and route metadata
     */
    public RouteAfterResponseCallbackApplier(final ThingifierApiRuntime runtime) {
        this.contextFactory = new RouteCallbackContextFactory(runtime);
    }

    /**
     * Runs final-response callbacks registered on the matched route rule.
     *
     * @param verb route verb for route-rule lookup
     * @param publicPath public request path
     * @param response final rendered HTTP response
     * @param requestContext active request context, or null to derive it from lifecycle
     * @param lifecycle lifecycle context when processing an HTTP/lifecycle request
     * @param request parsed request envelope when available
     * @param bodyAvailable false when response body access should not be exposed
     * @return original final response
     */
    public HttpApiResponse apply(
            final RoutingVerb verb,
            final String publicPath,
            final HttpApiResponse response,
            final ThingifierRequestContext requestContext,
            final ThingifierApiLifecycleContext lifecycle,
            final ApiRequestEnvelope request,
            final boolean bodyAvailable) {
        if (response == null) {
            return null;
        }

        final ThingifierApiRouteRule routeRule =
                contextFactory.routeRuleFor(verb, publicPath).orElse(null);
        if (routeRule == null || !routeRule.hasResponseCallbacks()) {
            return response;
        }

        final ThingRoute route = contextFactory.route(lifecycle, verb, publicPath);
        final ThingifierApiOperationContext context =
                contextFactory.contextFor(
                        verb, publicPath, route, routeRule, requestContext, lifecycle, request);
        final ThingifierApiFinalResponse finalResponse =
                ThingifierApiFinalResponse.from(response, bodyAvailable);

        for (ThingifierApiResponseCallbackDefinition definition : routeRule.responseCallbacks()) {
            try {
                definition.callback().run(context, finalResponse);
            } catch (Exception exception) {
                logCallbackFailure(definition, verb, publicPath, finalResponse, exception);
            }
        }
        return response;
    }

    private void logCallbackFailure(
            final ThingifierApiResponseCallbackDefinition definition,
            final RoutingVerb verb,
            final String publicPath,
            final ThingifierApiFinalResponse response,
            final Exception exception) {
        LOGGER.log(
                Level.SEVERE,
                String.format(
                        "Route final response callback '%s' failed for %s %s after status %d",
                        definition.name(), verb, publicPath, response.statusCode()),
                exception);
    }
}
