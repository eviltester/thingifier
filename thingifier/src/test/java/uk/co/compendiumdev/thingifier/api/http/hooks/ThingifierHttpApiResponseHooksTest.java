package uk.co.compendiumdev.thingifier.api.http.hooks;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.hooks.HookScope;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiHookRegistry;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiResponseHook;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.JsonOutputConfig;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

class ThingifierHttpApiResponseHooksTest {

    JsonThing jsonThing = new JsonThing(new JsonOutputConfig());

    @Test
    void responseHookCanEndResponseProcessing() {

        List<HttpApiResponseHook> responseHooks = new ArrayList<>();
        responseHooks.add(new And404Becomes500Error());

        Thingifier aThingifier = new Thingifier();
        aThingifier.apiConfig().setApiToEnforceAcceptHeaderForResponses(false);
        aThingifier.apiConfig().setApiToEnforceContentTypeForRequests(false);

        aThingifier.defineThing("thing", "things");

        final ThingifierHttpApi api = new ThingifierHttpApi(aThingifier, null, responseHooks);

        final HttpApiResponse response = api.get(new HttpApiRequest("/thing/1234"));
        Assertions.assertEquals(500, response.getStatusCode());
    }

    private class And404Becomes500Error implements HttpApiResponseHook {
        @Override
        public HttpApiResponse run(
                final HttpApiRequest request,
                final HttpApiResponse response,
                ThingifierApiConfig config) {
            if (response.getStatusCode() == 404) {
                return new HttpApiResponse(
                        null, ApiResponse.error(500, "bypassed all processing"), jsonThing, config);
            }
            return null;
        }
    }

    @Test
    void globalResponseHookStillRunsForEveryApiRequest() {
        CountingResponseHook hook = new CountingResponseHook();
        HttpApiHookRegistry hooks = new HttpApiHookRegistry();
        hooks.registerResponseHook(hook);

        ThingifierHttpApi api = new ThingifierHttpApi(thingifier(), hooks);

        api.get(new HttpApiRequest("/todos"));
        api.post(new HttpApiRequest("/todos"));

        Assertions.assertEquals(2, hook.callCount);
    }

    @Test
    void endpointScopedResponseHookRunsOnlyForMatchingPaths() {
        CountingResponseHook hook = new CountingResponseHook();
        HttpApiHookRegistry hooks = new HttpApiHookRegistry();
        hooks.registerResponseHook(HookScope.endpoint("/api/todos"), hook);

        ThingifierHttpApi api = new ThingifierHttpApi(prefixedThingifier(), hooks);

        api.get(new HttpApiRequest("/api/todos"));
        api.get(new HttpApiRequest("/api/projects"));

        Assertions.assertEquals(1, hook.callCount);
    }

    @Test
    void verbScopedResponseHookRunsOnlyForMatchingVerbs() {
        CountingResponseHook hook = new CountingResponseHook();
        HttpApiHookRegistry hooks = new HttpApiHookRegistry();
        hooks.registerResponseHook(HookScope.verbs(RoutingVerb.POST), hook);

        ThingifierHttpApi api = new ThingifierHttpApi(thingifier(), hooks);

        api.get(new HttpApiRequest("/todos"));
        api.post(new HttpApiRequest("/todos"));

        Assertions.assertEquals(1, hook.callCount);
    }

    @Test
    void responseHookEndpointScopeMatchesCurlyBraceParameter() {
        CountingResponseHook hook = new CountingResponseHook();
        HttpApiHookRegistry hooks = new HttpApiHookRegistry();
        hooks.registerResponseHook(HookScope.endpoint("todos/{id}"), hook);

        ThingifierHttpApi api = new ThingifierHttpApi(thingifier(), hooks);

        api.get(new HttpApiRequest("/todos/123"));

        Assertions.assertEquals(1, hook.callCount);
    }

    @Test
    void responseHookEndpointScopeMatchesColonParameter() {
        CountingResponseHook hook = new CountingResponseHook();
        HttpApiHookRegistry hooks = new HttpApiHookRegistry();
        hooks.registerResponseHook(HookScope.endpoint("todos/:id"), hook);

        ThingifierHttpApi api = new ThingifierHttpApi(thingifier(), hooks);

        api.get(new HttpApiRequest("/todos/123"));

        Assertions.assertEquals(1, hook.callCount);
    }

    @Test
    void responseHookVerbScopeUsesEffectiveMethodOverrideVerb() {
        CountingResponseHook hook = new CountingResponseHook();
        HttpApiHookRegistry hooks = new HttpApiHookRegistry();
        hooks.registerResponseHook(HookScope.verbs(RoutingVerb.DELETE), hook);

        ThingifierHttpApi api = new ThingifierHttpApi(thingifier(), hooks);
        HttpApiRequest request =
                new HttpApiRequest("/todos/123")
                        .setVerb(HttpApiRequest.VERB.POST)
                        .addHeader("X-HTTP-Method-Override", "DELETE");

        api.post(request);

        Assertions.assertEquals(1, hook.callCount);
    }

    @Test
    void nonMatchingResponseHookDoesNotShortCircuitResponse() {
        HttpApiHookRegistry hooks = new HttpApiHookRegistry();
        hooks.registerResponseHook(HookScope.endpoint("todos"), new And404Becomes500Error());

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

    private static class CountingResponseHook implements HttpApiResponseHook {
        private int callCount;

        @Override
        public HttpApiResponse run(
                final HttpApiRequest request,
                final HttpApiResponse response,
                final ThingifierApiConfig config) {
            callCount++;
            return null;
        }
    }
}
