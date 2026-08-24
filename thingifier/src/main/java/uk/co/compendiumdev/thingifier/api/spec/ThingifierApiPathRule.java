package uk.co.compendiumdev.thingifier.api.spec;

import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;

/**
 * Configures all route rules for a single path pattern.
 *
 * <p>This builder is a convenience layer over the one-verb route rules. It is useful when the
 * public API decision is path-oriented, such as declaring several generated methods unavailable for
 * the same endpoint.
 */
public final class ThingifierApiPathRule {

    private final ThingifierApiSpec apiSpec;
    private final String pathPattern;

    ThingifierApiPathRule(final ThingifierApiSpec apiSpec, final String pathPattern) {
        this.apiSpec = apiSpec;
        this.pathPattern = pathPattern;
    }

    /**
     * Marks one or more generated methods on this path as HTTP 405 Method Not Allowed.
     *
     * <p>The route is still visible and routable so OPTIONS and generated documentation can explain
     * the public surface. Use {@link ThingifierApiRouteRule#disable()} when the route should behave
     * as absent instead.
     *
     * @param verbs generated methods to reject with 405
     * @return this path rule so more path-level configuration can be chained
     */
    public ThingifierApiPathRule methodNotAllowed(final RoutingVerb... verbs) {
        if (verbs == null || verbs.length == 0) {
            throw new IllegalArgumentException("methodNotAllowed requires at least one verb");
        }
        for (RoutingVerb verb : verbs) {
            if (verb == null) {
                throw new IllegalArgumentException("methodNotAllowed requires non-null verbs");
            }
            apiSpec.routeFor(verb, pathPattern).methodNotAllowed();
        }
        return this;
    }

    /**
     * Allows selected methods on this path to use the default data scope when the scoped-session
     * credential is absent.
     *
     * @param verbs generated methods that should allow anonymous default-scope access
     * @return this path rule so more path-level configuration can be chained
     */
    public ThingifierApiPathRule allowAnonymousUsingDefaultScope(final RoutingVerb... verbs) {
        requireVerbs("allowAnonymousUsingDefaultScope", verbs);
        for (RoutingVerb verb : verbs) {
            apiSpec.routeFor(verb, pathPattern).allowAnonymousUsingDefaultScope();
        }
        return this;
    }

    /**
     * Requires a named scoped session for selected methods on this path.
     *
     * @param sessionName named scoped-session definition
     * @param verbs generated methods that should require the scoped session
     * @return this path rule so more path-level configuration can be chained
     */
    public ThingifierApiPathRule requireScopedSession(
            final String sessionName, final RoutingVerb... verbs) {
        requireVerbs("requireScopedSession", verbs);
        for (RoutingVerb verb : verbs) {
            apiSpec.routeFor(verb, pathPattern).requireScopedSession(sessionName);
        }
        return this;
    }

    /**
     * Disables scoped-session resolution for selected methods on this path.
     *
     * @param verbs generated methods that should ignore scoped-session contract defaults
     * @return this path rule so more path-level configuration can be chained
     */
    public ThingifierApiPathRule disableScopedSession(final RoutingVerb... verbs) {
        requireVerbs("disableScopedSession", verbs);
        for (RoutingVerb verb : verbs) {
            apiSpec.routeFor(verb, pathPattern).disableScopedSession();
        }
        return this;
    }

    private void requireVerbs(final String methodName, final RoutingVerb... verbs) {
        if (verbs == null || verbs.length == 0) {
            throw new IllegalArgumentException(methodName + " requires at least one verb");
        }
        for (RoutingVerb verb : verbs) {
            if (verb == null) {
                throw new IllegalArgumentException(methodName + " requires non-null verbs");
            }
        }
    }
}
