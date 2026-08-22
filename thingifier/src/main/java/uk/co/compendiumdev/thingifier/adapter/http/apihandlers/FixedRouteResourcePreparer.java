package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.List;
import java.util.Optional;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.InstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.spec.FixedResourcePolicy;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiRouteRule;
import uk.co.compendiumdev.thingifier.application.ThingCommandResult;
import uk.co.compendiumdev.thingifier.application.command.CreateThingCommand;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

/**
 * Prepares fixed-instance route targets before normal command/query handling.
 *
 * <p>The default fixed route policy deliberately does nothing and lets generated instance-route
 * handling return 404. When a route opts into {@link FixedResourcePolicy#ENSURE_EXISTS}, this
 * helper creates the fixed target through the normal command service after authentication has
 * selected the active data scope and before validators or lifecycle action hooks run.
 */
public final class FixedRouteResourcePreparer {

    private final ThingifierApiRuntime runtime;

    /**
     * Creates a preparer backed by the active runtime.
     *
     * @param runtime runtime services used to resolve route policy and create instances
     */
    public FixedRouteResourcePreparer(final ThingifierApiRuntime runtime) {
        this.runtime = runtime;
    }

    /**
     * Ensures the fixed route target exists when the route policy asks for it.
     *
     * @param verb routing verb being processed
     * @param path public route path
     * @param route resolved internal route
     * @param context active request context and store
     * @return error response when the fixed resource cannot be prepared, otherwise null
     */
    public ApiResponse prepare(
            final RoutingVerb verb,
            final String path,
            final ThingRoute route,
            final ThingifierRequestContext context) {
        final Optional<ThingifierApiRouteRule> rule =
                runtime.apiSpec()
                        .fixedRouteRuleFor(verb, path, runtime.apiConfig().getApiEndPointPrefix());
        if (rule.isEmpty()
                || rule.get().fixedResourcePolicy() != FixedResourcePolicy.ENSURE_EXISTS) {
            return null;
        }
        if (!(route instanceof InstanceRoute)) {
            return ApiResponse.error(
                    500, "Fixed resource route " + path + " did not resolve to an instance");
        }

        final InstanceRoute instanceRoute = (InstanceRoute) route;
        final EntityDefinition entity =
                runtime.schema().definitionWithSingularOrPluralNamed(instanceRoute.entity().name());
        if (entity == null) {
            return ApiResponse.error(
                    500,
                    String.format(
                            "Fixed resource route %s maps to unknown entity %s",
                            path, instanceRoute.entity().name()));
        }
        if (!entity.hasPrimaryKeyField()) {
            return ApiResponse.error(
                    500,
                    String.format(
                            "Fixed resource route %s maps to %s but the entity has no primary key",
                            path, entity.getName()));
        }
        if (context.hasEntityInstanceWithIdentifier(entity, instanceRoute.identifier())) {
            return null;
        }

        final CreateThingCommand create =
                new CreateThingCommand(
                        entity.getName(), instanceRoute.identifier(), List.of(), List.of(), false);
        final ThingCommandResult result = runtime.commandService(context).execute(create);
        if (result.isSuccessful()) {
            return null;
        }
        return ApiResponse.error(
                500,
                String.format(
                        "Fixed resource route %s could not create %s(%s): %s",
                        path,
                        entity.getName(),
                        instanceRoute.identifier(),
                        result.getCombinedErrorMessage()));
    }
}
