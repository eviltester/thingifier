package uk.co.compendiumdev.thingifier.adapter.http.messagehooks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.adapter.hooks.HookScope;
import uk.co.compendiumdev.thingifier.adapter.hooks.ScopedHook;

public final class HttpApiHookRegistry {

    private final List<ScopedHook<HttpApiRequestHook>> requestHooks;
    private final List<ScopedHook<HttpApiResponseHook>> responseHooks;

    public HttpApiHookRegistry() {
        requestHooks = new ArrayList<>();
        responseHooks = new ArrayList<>();
    }

    public void registerRequestHook(final HttpApiRequestHook hook) {
        registerRequestHook(HookScope.any(), hook);
    }

    public void registerRequestHook(final HookScope scope, final HttpApiRequestHook hook) {
        requestHooks.add(ScopedHook.forScope(scope, hook));
    }

    public void registerResponseHook(final HttpApiResponseHook hook) {
        registerResponseHook(HookScope.any(), hook);
    }

    public void registerResponseHook(final HookScope scope, final HttpApiResponseHook hook) {
        responseHooks.add(ScopedHook.forScope(scope, hook));
    }

    public List<ScopedHook<HttpApiRequestHook>> requestHooks() {
        return Collections.unmodifiableList(requestHooks);
    }

    public List<ScopedHook<HttpApiResponseHook>> responseHooks() {
        return Collections.unmodifiableList(responseHooks);
    }
}
