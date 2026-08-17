package uk.co.compendiumdev.thingifier.api.spec;

import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;

public final class ThingifierApiPathRule {

    private final ThingifierApiSpec apiSpec;
    private final String pathPattern;

    ThingifierApiPathRule(final ThingifierApiSpec apiSpec, final String pathPattern) {
        this.apiSpec = apiSpec;
        this.pathPattern = pathPattern;
    }

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
}
