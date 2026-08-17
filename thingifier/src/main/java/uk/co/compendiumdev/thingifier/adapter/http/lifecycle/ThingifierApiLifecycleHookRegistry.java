package uk.co.compendiumdev.thingifier.adapter.http.lifecycle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.adapter.hooks.HookScope;
import uk.co.compendiumdev.thingifier.adapter.hooks.ScopedHook;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;

/**
 * Registry for Thingifier API lifecycle hooks.
 *
 * <p>The registry stores hooks by lifecycle phase and optional scope. Runtime handlers call the run
 * methods in phase order and stop a phase as soon as a hook short-circuits the request.
 */
public final class ThingifierApiLifecycleHookRegistry {

    private final List<ScopedHook<RouteMatchedHook>> routeMatchedHooks;
    private final List<ScopedHook<BodyParsedHook>> bodyParsedHooks;
    private final List<ScopedHook<BeforeValidationHook>> beforeValidationHooks;
    private final List<ScopedHook<AfterValidationHook>> afterValidationHooks;
    private final List<ScopedHook<BeforeActionHook>> beforeActionHooks;
    private final List<ScopedHook<AfterActionHook>> afterActionHooks;

    /** Creates an empty lifecycle hook registry. */
    public ThingifierApiLifecycleHookRegistry() {
        routeMatchedHooks = new ArrayList<>();
        bodyParsedHooks = new ArrayList<>();
        beforeValidationHooks = new ArrayList<>();
        afterValidationHooks = new ArrayList<>();
        beforeActionHooks = new ArrayList<>();
        afterActionHooks = new ArrayList<>();
    }

    /**
     * Registers a route-matched hook for all Thingifier API routes.
     *
     * @param hook hook to run
     */
    public void registerRouteMatchedHook(final RouteMatchedHook hook) {
        registerRouteMatchedHook(HookScope.any(), hook);
    }

    /**
     * Registers a route-matched hook with an explicit scope.
     *
     * @param scope route and verb scope
     * @param hook hook to run
     */
    public void registerRouteMatchedHook(final HookScope scope, final RouteMatchedHook hook) {
        routeMatchedHooks.add(ScopedHook.forScope(scope, hook));
    }

    /**
     * Registers a route-matched hook for one path pattern.
     *
     * @param pathPattern endpoint path pattern
     * @param hook hook to run
     */
    public void registerRouteMatchedHook(final String pathPattern, final RouteMatchedHook hook) {
        registerRouteMatchedHook(HookScope.endpoint(pathPattern), hook);
    }

    /**
     * Registers a route-matched hook for one path pattern and selected verbs.
     *
     * @param pathPattern endpoint path pattern
     * @param verbs verbs that should run the hook
     * @param hook hook to run
     */
    public void registerRouteMatchedHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final RouteMatchedHook hook) {
        registerRouteMatchedHook(HookScope.endpointAndVerbs(pathPattern, verbs), hook);
    }

    /**
     * Registers a body-parsed hook for all Thingifier API routes.
     *
     * @param hook hook to run
     */
    public void registerBodyParsedHook(final BodyParsedHook hook) {
        registerBodyParsedHook(HookScope.any(), hook);
    }

    /**
     * Registers a body-parsed hook with an explicit scope.
     *
     * @param scope route and verb scope
     * @param hook hook to run
     */
    public void registerBodyParsedHook(final HookScope scope, final BodyParsedHook hook) {
        bodyParsedHooks.add(ScopedHook.forScope(scope, hook));
    }

    /**
     * Registers a body-parsed hook for one path pattern.
     *
     * @param pathPattern endpoint path pattern
     * @param hook hook to run
     */
    public void registerBodyParsedHook(final String pathPattern, final BodyParsedHook hook) {
        registerBodyParsedHook(HookScope.endpoint(pathPattern), hook);
    }

    /**
     * Registers a body-parsed hook for one path pattern and selected verbs.
     *
     * @param pathPattern endpoint path pattern
     * @param verbs verbs that should run the hook
     * @param hook hook to run
     */
    public void registerBodyParsedHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final BodyParsedHook hook) {
        registerBodyParsedHook(HookScope.endpointAndVerbs(pathPattern, verbs), hook);
    }

    /**
     * Registers a before-validation hook for all Thingifier API routes.
     *
     * @param hook hook to run
     */
    public void registerBeforeValidationHook(final BeforeValidationHook hook) {
        registerBeforeValidationHook(HookScope.any(), hook);
    }

    /**
     * Registers a before-validation hook with an explicit scope.
     *
     * @param scope route and verb scope
     * @param hook hook to run
     */
    public void registerBeforeValidationHook(
            final HookScope scope, final BeforeValidationHook hook) {
        beforeValidationHooks.add(ScopedHook.forScope(scope, hook));
    }

    /**
     * Registers a before-validation hook for one path pattern.
     *
     * @param pathPattern endpoint path pattern
     * @param hook hook to run
     */
    public void registerBeforeValidationHook(
            final String pathPattern, final BeforeValidationHook hook) {
        registerBeforeValidationHook(HookScope.endpoint(pathPattern), hook);
    }

    /**
     * Registers a before-validation hook for one path pattern and selected verbs.
     *
     * @param pathPattern endpoint path pattern
     * @param verbs verbs that should run the hook
     * @param hook hook to run
     */
    public void registerBeforeValidationHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final BeforeValidationHook hook) {
        registerBeforeValidationHook(HookScope.endpointAndVerbs(pathPattern, verbs), hook);
    }

    /**
     * Registers an after-validation hook for all Thingifier API routes.
     *
     * @param hook hook to run
     */
    public void registerAfterValidationHook(final AfterValidationHook hook) {
        registerAfterValidationHook(HookScope.any(), hook);
    }

    /**
     * Registers an after-validation hook with an explicit scope.
     *
     * @param scope route and verb scope
     * @param hook hook to run
     */
    public void registerAfterValidationHook(final HookScope scope, final AfterValidationHook hook) {
        afterValidationHooks.add(ScopedHook.forScope(scope, hook));
    }

    /**
     * Registers an after-validation hook for one path pattern.
     *
     * @param pathPattern endpoint path pattern
     * @param hook hook to run
     */
    public void registerAfterValidationHook(
            final String pathPattern, final AfterValidationHook hook) {
        registerAfterValidationHook(HookScope.endpoint(pathPattern), hook);
    }

    /**
     * Registers an after-validation hook for one path pattern and selected verbs.
     *
     * @param pathPattern endpoint path pattern
     * @param verbs verbs that should run the hook
     * @param hook hook to run
     */
    public void registerAfterValidationHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final AfterValidationHook hook) {
        registerAfterValidationHook(HookScope.endpointAndVerbs(pathPattern, verbs), hook);
    }

    /**
     * Registers a before-action hook for all Thingifier API routes.
     *
     * @param hook hook to run
     */
    public void registerBeforeActionHook(final BeforeActionHook hook) {
        registerBeforeActionHook(HookScope.any(), hook);
    }

    /**
     * Registers a before-action hook with an explicit scope.
     *
     * @param scope route and verb scope
     * @param hook hook to run
     */
    public void registerBeforeActionHook(final HookScope scope, final BeforeActionHook hook) {
        beforeActionHooks.add(ScopedHook.forScope(scope, hook));
    }

    /**
     * Registers a before-action hook for one path pattern.
     *
     * @param pathPattern endpoint path pattern
     * @param hook hook to run
     */
    public void registerBeforeActionHook(final String pathPattern, final BeforeActionHook hook) {
        registerBeforeActionHook(HookScope.endpoint(pathPattern), hook);
    }

    /**
     * Registers a before-action hook for one path pattern and selected verbs.
     *
     * @param pathPattern endpoint path pattern
     * @param verbs verbs that should run the hook
     * @param hook hook to run
     */
    public void registerBeforeActionHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final BeforeActionHook hook) {
        registerBeforeActionHook(HookScope.endpointAndVerbs(pathPattern, verbs), hook);
    }

    /**
     * Registers an after-action hook for all Thingifier API routes.
     *
     * @param hook hook to run
     */
    public void registerAfterActionHook(final AfterActionHook hook) {
        registerAfterActionHook(HookScope.any(), hook);
    }

    /**
     * Registers an after-action hook with an explicit scope.
     *
     * @param scope route and verb scope
     * @param hook hook to run
     */
    public void registerAfterActionHook(final HookScope scope, final AfterActionHook hook) {
        afterActionHooks.add(ScopedHook.forScope(scope, hook));
    }

    /**
     * Registers an after-action hook for one path pattern.
     *
     * @param pathPattern endpoint path pattern
     * @param hook hook to run
     */
    public void registerAfterActionHook(final String pathPattern, final AfterActionHook hook) {
        registerAfterActionHook(HookScope.endpoint(pathPattern), hook);
    }

    /**
     * Registers an after-action hook for one path pattern and selected verbs.
     *
     * @param pathPattern endpoint path pattern
     * @param verbs verbs that should run the hook
     * @param hook hook to run
     */
    public void registerAfterActionHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final AfterActionHook hook) {
        registerAfterActionHook(HookScope.endpointAndVerbs(pathPattern, verbs), hook);
    }

    /**
     * Returns route-matched hooks in registration order.
     *
     * @return immutable view of registered hooks
     */
    public List<ScopedHook<RouteMatchedHook>> routeMatchedHooks() {
        return Collections.unmodifiableList(routeMatchedHooks);
    }

    /**
     * Returns body-parsed hooks in registration order.
     *
     * @return immutable view of registered hooks
     */
    public List<ScopedHook<BodyParsedHook>> bodyParsedHooks() {
        return Collections.unmodifiableList(bodyParsedHooks);
    }

    /**
     * Returns before-validation hooks in registration order.
     *
     * @return immutable view of registered hooks
     */
    public List<ScopedHook<BeforeValidationHook>> beforeValidationHooks() {
        return Collections.unmodifiableList(beforeValidationHooks);
    }

    /**
     * Returns after-validation hooks in registration order.
     *
     * @return immutable view of registered hooks
     */
    public List<ScopedHook<AfterValidationHook>> afterValidationHooks() {
        return Collections.unmodifiableList(afterValidationHooks);
    }

    /**
     * Returns before-action hooks in registration order.
     *
     * @return immutable view of registered hooks
     */
    public List<ScopedHook<BeforeActionHook>> beforeActionHooks() {
        return Collections.unmodifiableList(beforeActionHooks);
    }

    /**
     * Returns after-action hooks in registration order.
     *
     * @return immutable view of registered hooks
     */
    public List<ScopedHook<AfterActionHook>> afterActionHooks() {
        return Collections.unmodifiableList(afterActionHooks);
    }

    /**
     * Runs matching route-matched hooks until they complete or short-circuit.
     *
     * @param context lifecycle context for the request
     */
    public void runRouteMatchedHooks(final ThingifierApiLifecycleContext context) {
        for (ScopedHook<RouteMatchedHook> hook : routeMatchedHooks) {
            if (matches(hook, context)) {
                hook.hook().run(context);
                if (context.shouldShortCircuit()) {
                    return;
                }
            }
        }
    }

    /**
     * Runs matching body-parsed hooks until they complete or short-circuit.
     *
     * @param context lifecycle context for the request
     */
    public void runBodyParsedHooks(final ThingifierApiLifecycleContext context) {
        for (ScopedHook<BodyParsedHook> hook : bodyParsedHooks) {
            if (matches(hook, context)) {
                hook.hook().run(context);
                if (context.shouldShortCircuit()) {
                    return;
                }
            }
        }
    }

    /**
     * Runs matching before-validation hooks until they complete or short-circuit.
     *
     * @param context lifecycle context for the request
     */
    public void runBeforeValidationHooks(final ThingifierApiLifecycleContext context) {
        for (ScopedHook<BeforeValidationHook> hook : beforeValidationHooks) {
            if (matches(hook, context)) {
                hook.hook().run(context);
                if (context.shouldShortCircuit()) {
                    return;
                }
            }
        }
    }

    /**
     * Runs matching after-validation hooks until they complete or short-circuit.
     *
     * @param context lifecycle context for the request
     */
    public void runAfterValidationHooks(final ThingifierApiLifecycleContext context) {
        for (ScopedHook<AfterValidationHook> hook : afterValidationHooks) {
            if (matches(hook, context)) {
                hook.hook().run(context);
                if (context.shouldShortCircuit()) {
                    return;
                }
            }
        }
    }

    /**
     * Runs matching before-action hooks until they complete or short-circuit.
     *
     * @param context lifecycle context for the request
     */
    public void runBeforeActionHooks(final ThingifierApiLifecycleContext context) {
        for (ScopedHook<BeforeActionHook> hook : beforeActionHooks) {
            if (matches(hook, context)) {
                hook.hook().run(context);
                if (context.shouldShortCircuit()) {
                    return;
                }
            }
        }
    }

    /**
     * Runs matching after-action hooks until they complete or short-circuit.
     *
     * @param context lifecycle context for the request
     */
    public void runAfterActionHooks(final ThingifierApiLifecycleContext context) {
        for (ScopedHook<AfterActionHook> hook : afterActionHooks) {
            if (matches(hook, context)) {
                hook.hook().run(context);
                if (context.shouldShortCircuit()) {
                    return;
                }
            }
        }
    }

    /**
     * Checks whether a scoped hook applies to the current request.
     *
     * @param hook scoped hook wrapper
     * @param context lifecycle context for the request
     * @return true when the hook scope matches the request path and verb
     */
    private boolean matches(final ScopedHook<?> hook, final ThingifierApiLifecycleContext context) {
        return hook.matches(context.path(), context.routingVerb(), context.apiPathPrefix());
    }
}
