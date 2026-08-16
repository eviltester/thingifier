package uk.co.compendiumdev.thingifier.adapter.hooks;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.spec.ApiRoutePathMatcher;

public final class HookScope {

    private final String pathPattern;
    private final Set<RoutingVerb> verbs;

    private HookScope(final String pathPattern, final Set<RoutingVerb> verbs) {
        this.pathPattern = pathPattern;
        this.verbs = verbs;
    }

    public static HookScope any() {
        return new HookScope(null, Set.of());
    }

    public static HookScope endpoint(final String pathPattern) {
        return new HookScope(pathPattern, Set.of());
    }

    public static HookScope verbs(final RoutingVerb... verbs) {
        return new HookScope(null, verbsFrom(verbs));
    }

    public static HookScope endpointAndVerbs(final String pathPattern, final RoutingVerb... verbs) {
        return new HookScope(pathPattern, verbsFrom(verbs));
    }

    public static HookScope endpointAndVerbs(
            final String pathPattern, final Collection<RoutingVerb> verbs) {
        return new HookScope(pathPattern, verbsFrom(verbs));
    }

    public boolean matches(
            final String candidatePath, final RoutingVerb verb, final String apiPathPrefix) {
        if (!verbs.isEmpty() && (verb == null || !verbs.contains(verb))) {
            return false;
        }

        if (pathPattern == null || pathPattern.trim().isEmpty()) {
            return true;
        }

        return ApiRoutePathMatcher.pathsMatch(pathPattern, candidatePath, apiPathPrefix);
    }

    private static Set<RoutingVerb> verbsFrom(final RoutingVerb... verbs) {
        final EnumSet<RoutingVerb> selected = EnumSet.noneOf(RoutingVerb.class);
        if (verbs != null) {
            Collections.addAll(selected, verbs);
        }
        if (selected.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(selected));
    }

    private static Set<RoutingVerb> verbsFrom(final Collection<RoutingVerb> verbs) {
        final EnumSet<RoutingVerb> selected = EnumSet.noneOf(RoutingVerb.class);
        if (verbs != null) {
            for (RoutingVerb verb : verbs) {
                if (verb != null) {
                    selected.add(verb);
                }
            }
        }
        if (selected.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(selected));
    }
}
