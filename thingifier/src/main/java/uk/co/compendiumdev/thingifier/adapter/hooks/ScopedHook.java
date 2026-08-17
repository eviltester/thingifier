package uk.co.compendiumdev.thingifier.adapter.hooks;

import java.util.Objects;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;

public final class ScopedHook<T> {

    private final HookScope scope;
    private final T hook;

    private ScopedHook(final HookScope scope, final T hook) {
        this.scope = scope == null ? HookScope.any() : scope;
        this.hook = Objects.requireNonNull(hook, "hook");
    }

    public static <T> ScopedHook<T> any(final T hook) {
        return new ScopedHook<>(HookScope.any(), hook);
    }

    public static <T> ScopedHook<T> forScope(final HookScope scope, final T hook) {
        return new ScopedHook<>(scope, hook);
    }

    public boolean matches(
            final String candidatePath, final RoutingVerb verb, final String apiPathPrefix) {
        return scope.matches(candidatePath, verb, apiPathPrefix);
    }

    public T hook() {
        return hook;
    }
}
