package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.logging.Level;
import java.util.logging.Logger;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleContext;
import uk.co.compendiumdev.thingifier.api.callbacks.CallbackFailurePolicy;
import uk.co.compendiumdev.thingifier.api.callbacks.ThingifierApiOperationCallbackDefinition;
import uk.co.compendiumdev.thingifier.api.callbacks.ThingifierApiOperationContext;
import uk.co.compendiumdev.thingifier.api.callbacks.ThingifierApiOperationResult;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ApiRequestEnvelope;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiRouteRule;
import uk.co.compendiumdev.thingifier.application.ThingCommandResult;

/**
 * Runs route-level operation callbacks after Thingifier has produced a route-shaped response.
 *
 * <p>The applier is shared by direct and HTTP-backed processing. It keeps callbacks separate from
 * response policies: policies shape the response first, callbacks observe the selected route/result
 * and can perform trusted application side effects before legacy response hooks run.
 */
public final class RouteOperationCallbackApplier {

    private static final Logger LOGGER =
            Logger.getLogger(RouteOperationCallbackApplier.class.getName());

    private final RouteCallbackContextFactory contextFactory;

    /**
     * Creates an applier for the current API runtime.
     *
     * @param runtime runtime used to find route rules and route metadata
     */
    public RouteOperationCallbackApplier(final ThingifierApiRuntime runtime) {
        this.contextFactory = new RouteCallbackContextFactory(runtime);
    }

    /**
     * Runs callbacks registered on the matched route rule.
     *
     * @param verb route verb for route-rule lookup
     * @param publicPath public request path
     * @param response route-shaped API response
     * @param requestContext active request context
     * @param lifecycle lifecycle context when processing an HTTP/lifecycle request, otherwise null
     * @param request parsed request envelope when available
     * @return original response, or a callback failure response when configured to fail the request
     */
    public ApiResponse apply(
            final RoutingVerb verb,
            final String publicPath,
            final ApiResponse response,
            final ThingifierRequestContext requestContext,
            final ThingifierApiLifecycleContext lifecycle,
            final ApiRequestEnvelope request) {
        if (response == null) {
            return null;
        }

        final ThingifierApiRouteRule routeRule =
                contextFactory.routeRuleFor(verb, publicPath).orElse(null);
        if (routeRule == null || !routeRule.hasOperationCallbacks()) {
            return response;
        }

        final ThingRoute route = contextFactory.route(lifecycle, verb, publicPath);
        final ThingifierApiOperationContext context =
                contextFactory.contextFor(
                        verb, publicPath, route, routeRule, requestContext, lifecycle, request);
        final ThingifierApiOperationResult result =
                resultFor(response, lifecycle, contextFactory.operationTypeFor(verb, lifecycle));

        for (ThingifierApiOperationCallbackDefinition definition : routeRule.operationCallbacks()) {
            if (!definition.matches(result)) {
                continue;
            }
            try {
                definition.callback().run(context, result);
            } catch (Exception exception) {
                logCallbackFailure(definition, verb, publicPath, response, exception);
                if (definition.failurePolicy() == CallbackFailurePolicy.FAIL_REQUEST) {
                    return ApiResponse.error(
                            500, callbackFailureMessage(definition, verb, publicPath));
                }
            }
        }
        return response;
    }

    private ThingifierApiOperationResult resultFor(
            final ApiResponse response,
            final ThingifierApiLifecycleContext lifecycle,
            final String operationType) {
        final ThingCommandResult writeResult =
                lifecycle == null ? null : lifecycle.writeCommandResult();
        return new ThingifierApiOperationResult(
                response.getStatusCode(), operationType, response, writeResult);
    }

    private void logCallbackFailure(
            final ThingifierApiOperationCallbackDefinition definition,
            final RoutingVerb verb,
            final String publicPath,
            final ApiResponse response,
            final Exception exception) {
        LOGGER.log(
                Level.SEVERE,
                String.format(
                        "Route operation callback '%s' failed for %s %s after status %d",
                        definition.name(), verb, publicPath, response.getStatusCode()),
                exception);
    }

    private String callbackFailureMessage(
            final ThingifierApiOperationCallbackDefinition definition,
            final RoutingVerb verb,
            final String publicPath) {
        return String.format(
                "Route operation callback '%s' failed for %s %s",
                definition.name(), verb, publicPath);
    }
}
