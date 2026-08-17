package uk.co.compendiumdev.thingifier.api.http.hooks;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.hooks.HookScope;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiHookRegistry;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.JsonOutputConfig;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

class ThingifierHttpApiRequestHooksTest {

    JsonThing jsonThing = new JsonThing(new JsonOutputConfig());

    @Test
    void requestHookCanBypassRequestProcessing() {

        List<HttpApiRequestHook> requestHooks = new ArrayList<>();
        requestHooks.add(new Instant500Error());

        Thingifier thingifier = new Thingifier();
        thingifier.apiConfig().setApiToEnforceAcceptHeaderForResponses(false);

        final ThingifierHttpApi api = new ThingifierHttpApi(thingifier, requestHooks, null);

        final HttpApiResponse response = api.get(new HttpApiRequest("/bob"));
        Assertions.assertEquals(500, response.getStatusCode());
    }

    private class Instant500Error implements HttpApiRequestHook {
        @Override
        public HttpApiResponse run(final HttpApiRequest request, ThingifierApiConfig config) {
            return new HttpApiResponse(
                    new HttpHeadersBlock(),
                    ApiResponse.error(500, "bypassed all processing"),
                    jsonThing,
                    config);
        }
    }

    @Test
    void requestHookCanAmendTheRequest() {

        List<HttpApiRequestHook> requestHooks = new ArrayList<>();
        requestHooks.add(new AddAdditionalHeader());

        Thingifier thingifier = new Thingifier();
        thingifier.apiConfig().setApiToEnforceAcceptHeaderForResponses(false);

        final ThingifierHttpApi api = new ThingifierHttpApi(thingifier, requestHooks, null);

        HttpApiRequest request = new HttpApiRequest("/bob");
        final HttpApiResponse response = api.get(request);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("dobbs", request.getHeader("X-BOB"));
    }

    private class AddAdditionalHeader implements HttpApiRequestHook {
        @Override
        public HttpApiResponse run(final HttpApiRequest request, ThingifierApiConfig config) {
            request.addHeader("X-BOB", "dobbs");
            return null;
        }
    }

    @Test
    void globalRequestHookStillRunsForEveryApiRequest() {
        CountingRequestHook hook = new CountingRequestHook();
        HttpApiHookRegistry hooks = new HttpApiHookRegistry();
        hooks.registerRequestHook(hook);

        ThingifierHttpApi api = new ThingifierHttpApi(thingifier(), hooks);

        api.get(new HttpApiRequest("/todos"));
        api.post(new HttpApiRequest("/todos"));

        Assertions.assertEquals(2, hook.callCount);
    }

    @Test
    void endpointScopedRequestHookRunsOnlyForMatchingPaths() {
        CountingRequestHook hook = new CountingRequestHook();
        HttpApiHookRegistry hooks = new HttpApiHookRegistry();
        hooks.registerRequestHook(HookScope.endpoint("/api/todos"), hook);

        ThingifierHttpApi api = new ThingifierHttpApi(prefixedThingifier(), hooks);

        api.get(new HttpApiRequest("/api/todos"));
        api.get(new HttpApiRequest("/api/projects"));

        Assertions.assertEquals(1, hook.callCount);
    }

    @Test
    void verbScopedRequestHookRunsOnlyForMatchingVerbs() {
        CountingRequestHook hook = new CountingRequestHook();
        HttpApiHookRegistry hooks = new HttpApiHookRegistry();
        hooks.registerRequestHook(HookScope.verbs(RoutingVerb.POST), hook);

        ThingifierHttpApi api = new ThingifierHttpApi(thingifier(), hooks);

        api.get(new HttpApiRequest("/todos"));
        api.post(new HttpApiRequest("/todos"));

        Assertions.assertEquals(1, hook.callCount);
    }

    @Test
    void requestHookEndpointScopeMatchesCurlyBraceParameter() {
        CountingRequestHook hook = new CountingRequestHook();
        HttpApiHookRegistry hooks = new HttpApiHookRegistry();
        hooks.registerRequestHook(HookScope.endpoint("todos/{id}"), hook);

        ThingifierHttpApi api = new ThingifierHttpApi(thingifier(), hooks);

        api.get(new HttpApiRequest("/todos/123"));

        Assertions.assertEquals(1, hook.callCount);
    }

    @Test
    void requestHookEndpointScopeMatchesColonParameter() {
        CountingRequestHook hook = new CountingRequestHook();
        HttpApiHookRegistry hooks = new HttpApiHookRegistry();
        hooks.registerRequestHook(HookScope.endpoint("todos/:id"), hook);

        ThingifierHttpApi api = new ThingifierHttpApi(thingifier(), hooks);

        api.get(new HttpApiRequest("/todos/123"));

        Assertions.assertEquals(1, hook.callCount);
    }

    @Test
    void requestHookVerbScopeUsesEffectiveMethodOverrideVerb() {
        CountingRequestHook hook = new CountingRequestHook();
        HttpApiHookRegistry hooks = new HttpApiHookRegistry();
        hooks.registerRequestHook(HookScope.verbs(RoutingVerb.DELETE), hook);

        ThingifierHttpApi api = new ThingifierHttpApi(thingifier(), hooks);
        HttpApiRequest request =
                new HttpApiRequest("/todos/123")
                        .setVerb(HttpApiRequest.VERB.POST)
                        .addHeader("X-HTTP-Method-Override", "DELETE");

        api.post(request);

        Assertions.assertEquals(1, hook.callCount);
    }

    @Test
    void nonMatchingRequestHookDoesNotShortCircuitRequest() {
        HttpApiHookRegistry hooks = new HttpApiHookRegistry();
        hooks.registerRequestHook(HookScope.endpoint("todos"), new Instant500Error());

        ThingifierHttpApi api = new ThingifierHttpApi(thingifier(), hooks);

        HttpApiResponse response = api.get(new HttpApiRequest("/projects"));

        Assertions.assertNotEquals(500, response.getStatusCode());
    }

    private Thingifier thingifier() {
        Thingifier thingifier = new Thingifier();
        thingifier.apiConfig().setApiToEnforceAcceptHeaderForResponses(false);
        thingifier.apiConfig().setApiToEnforceContentTypeForRequests(false);
        thingifier.defineThing("todo", "todos");
        return thingifier;
    }

    private Thingifier prefixedThingifier() {
        Thingifier thingifier = thingifier();
        thingifier.configureWithProfile(
                thingifier.apiConfigProfiles().create("api", "API profile", "/api"));
        return thingifier;
    }

    private static class CountingRequestHook implements HttpApiRequestHook {
        private int callCount;

        @Override
        public HttpApiResponse run(final HttpApiRequest request, final ThingifierApiConfig config) {
            callCount++;
            return null;
        }
    }
}
