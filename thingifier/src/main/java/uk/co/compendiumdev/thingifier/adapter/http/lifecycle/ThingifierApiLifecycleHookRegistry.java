package uk.co.compendiumdev.thingifier.adapter.http.lifecycle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.adapter.hooks.HookScope;
import uk.co.compendiumdev.thingifier.adapter.hooks.ScopedHook;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;

public final class ThingifierApiLifecycleHookRegistry {

    private final List<ScopedHook<RouteMatchedHook>> routeMatchedHooks;
    private final List<ScopedHook<BodyParsedHook>> bodyParsedHooks;
    private final List<ScopedHook<BeforeValidationHook>> beforeValidationHooks;
    private final List<ScopedHook<AfterValidationHook>> afterValidationHooks;
    private final List<ScopedHook<BeforeActionHook>> beforeActionHooks;
    private final List<ScopedHook<AfterActionHook>> afterActionHooks;

    public ThingifierApiLifecycleHookRegistry() {
        routeMatchedHooks = new ArrayList<>();
        bodyParsedHooks = new ArrayList<>();
        beforeValidationHooks = new ArrayList<>();
        afterValidationHooks = new ArrayList<>();
        beforeActionHooks = new ArrayList<>();
        afterActionHooks = new ArrayList<>();
    }

    public void registerRouteMatchedHook(final RouteMatchedHook hook) {
        registerRouteMatchedHook(HookScope.any(), hook);
    }

    public void registerRouteMatchedHook(final HookScope scope, final RouteMatchedHook hook) {
        routeMatchedHooks.add(ScopedHook.forScope(scope, hook));
    }

    public void registerRouteMatchedHook(final String pathPattern, final RouteMatchedHook hook) {
        registerRouteMatchedHook(HookScope.endpoint(pathPattern), hook);
    }

    public void registerRouteMatchedHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final RouteMatchedHook hook) {
        registerRouteMatchedHook(HookScope.endpointAndVerbs(pathPattern, verbs), hook);
    }

    public void registerBodyParsedHook(final BodyParsedHook hook) {
        registerBodyParsedHook(HookScope.any(), hook);
    }

    public void registerBodyParsedHook(final HookScope scope, final BodyParsedHook hook) {
        bodyParsedHooks.add(ScopedHook.forScope(scope, hook));
    }

    public void registerBodyParsedHook(final String pathPattern, final BodyParsedHook hook) {
        registerBodyParsedHook(HookScope.endpoint(pathPattern), hook);
    }

    public void registerBodyParsedHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final BodyParsedHook hook) {
        registerBodyParsedHook(HookScope.endpointAndVerbs(pathPattern, verbs), hook);
    }

    public void registerBeforeValidationHook(final BeforeValidationHook hook) {
        registerBeforeValidationHook(HookScope.any(), hook);
    }

    public void registerBeforeValidationHook(
            final HookScope scope, final BeforeValidationHook hook) {
        beforeValidationHooks.add(ScopedHook.forScope(scope, hook));
    }

    public void registerBeforeValidationHook(
            final String pathPattern, final BeforeValidationHook hook) {
        registerBeforeValidationHook(HookScope.endpoint(pathPattern), hook);
    }

    public void registerBeforeValidationHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final BeforeValidationHook hook) {
        registerBeforeValidationHook(HookScope.endpointAndVerbs(pathPattern, verbs), hook);
    }

    public void registerAfterValidationHook(final AfterValidationHook hook) {
        registerAfterValidationHook(HookScope.any(), hook);
    }

    public void registerAfterValidationHook(final HookScope scope, final AfterValidationHook hook) {
        afterValidationHooks.add(ScopedHook.forScope(scope, hook));
    }

    public void registerAfterValidationHook(
            final String pathPattern, final AfterValidationHook hook) {
        registerAfterValidationHook(HookScope.endpoint(pathPattern), hook);
    }

    public void registerAfterValidationHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final AfterValidationHook hook) {
        registerAfterValidationHook(HookScope.endpointAndVerbs(pathPattern, verbs), hook);
    }

    public void registerBeforeActionHook(final BeforeActionHook hook) {
        registerBeforeActionHook(HookScope.any(), hook);
    }

    public void registerBeforeActionHook(final HookScope scope, final BeforeActionHook hook) {
        beforeActionHooks.add(ScopedHook.forScope(scope, hook));
    }

    public void registerBeforeActionHook(final String pathPattern, final BeforeActionHook hook) {
        registerBeforeActionHook(HookScope.endpoint(pathPattern), hook);
    }

    public void registerBeforeActionHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final BeforeActionHook hook) {
        registerBeforeActionHook(HookScope.endpointAndVerbs(pathPattern, verbs), hook);
    }

    public void registerAfterActionHook(final AfterActionHook hook) {
        registerAfterActionHook(HookScope.any(), hook);
    }

    public void registerAfterActionHook(final HookScope scope, final AfterActionHook hook) {
        afterActionHooks.add(ScopedHook.forScope(scope, hook));
    }

    public void registerAfterActionHook(final String pathPattern, final AfterActionHook hook) {
        registerAfterActionHook(HookScope.endpoint(pathPattern), hook);
    }

    public void registerAfterActionHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final AfterActionHook hook) {
        registerAfterActionHook(HookScope.endpointAndVerbs(pathPattern, verbs), hook);
    }

    public List<ScopedHook<RouteMatchedHook>> routeMatchedHooks() {
        return Collections.unmodifiableList(routeMatchedHooks);
    }

    public List<ScopedHook<BodyParsedHook>> bodyParsedHooks() {
        return Collections.unmodifiableList(bodyParsedHooks);
    }

    public List<ScopedHook<BeforeValidationHook>> beforeValidationHooks() {
        return Collections.unmodifiableList(beforeValidationHooks);
    }

    public List<ScopedHook<AfterValidationHook>> afterValidationHooks() {
        return Collections.unmodifiableList(afterValidationHooks);
    }

    public List<ScopedHook<BeforeActionHook>> beforeActionHooks() {
        return Collections.unmodifiableList(beforeActionHooks);
    }

    public List<ScopedHook<AfterActionHook>> afterActionHooks() {
        return Collections.unmodifiableList(afterActionHooks);
    }

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

    private boolean matches(final ScopedHook<?> hook, final ThingifierApiLifecycleContext context) {
        return hook.matches(context.path(), context.routingVerb(), context.apiPathPrefix());
    }
}
