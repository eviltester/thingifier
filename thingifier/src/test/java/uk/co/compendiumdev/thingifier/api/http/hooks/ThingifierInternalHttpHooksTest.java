package uk.co.compendiumdev.thingifier.api.http.hooks;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.hooks.HookScope;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpRouteRegistry;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerResponse;
import uk.co.compendiumdev.thingifier.adapter.httpserver.ThingifierHttpApiRoutings;
import uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks.InternalHttpRequestHook;
import uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks.InternalHttpResponseHook;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;

class ThingifierInternalHttpHooksTest {

    @AfterEach
    void clearRegistry() {
        HttpRouteRegistry.clearCurrent();
    }

    @Test
    void unscopedInternalRequestHookStillRuns() throws Exception {
        HttpRouteRegistry registry = new HttpRouteRegistry();
        ThingifierHttpApiRoutings routings = routingsOn(registry);
        CountingInternalRequestHook hook = new CountingInternalRequestHook();
        routings.registerInternalHttpRequestHook(hook);

        registry.beforeHandlers().get(0).handle(request("GET", "/not-api"), new StubResponse());

        Assertions.assertEquals(1, hook.callCount);
    }

    @Test
    void scopedInternalRequestHookRequiresMatchingRoute() throws Exception {
        HttpRouteRegistry registry = new HttpRouteRegistry();
        ThingifierHttpApiRoutings routings = routingsOn(registry);
        CountingInternalRequestHook hook = new CountingInternalRequestHook();
        routings.registerInternalHttpRequestHook(
                HookScope.endpointAndVerbs("/api/heartbeat", RoutingVerb.GET), hook);

        registry.beforeHandlers()
                .get(0)
                .handle(request("GET", "/api/heartbeat"), new StubResponse());
        registry.beforeHandlers()
                .get(0)
                .handle(request("POST", "/api/heartbeat"), new StubResponse());
        registry.beforeHandlers()
                .get(0)
                .handle(request("GET", "/api/challenges"), new StubResponse());

        Assertions.assertEquals(1, hook.callCount);
    }

    @Test
    void scopedInternalResponseHookRequiresMatchingRoute() throws Exception {
        HttpRouteRegistry registry = new HttpRouteRegistry();
        ThingifierHttpApiRoutings routings = routingsOn(registry);
        CountingInternalResponseHook hook = new CountingInternalResponseHook();
        routings.registerInternalHttpResponseHook(
                HookScope.endpointAndVerbs("/api/heartbeat", RoutingVerb.GET), hook);

        registry.afterHandlers().get(0).handle(request("GET", "/api/heartbeat"), response(204));
        registry.afterHandlers().get(0).handle(request("POST", "/api/heartbeat"), response(204));
        registry.afterHandlers().get(0).handle(request("GET", "/api/challenges"), response(200));

        Assertions.assertEquals(1, hook.callCount);
    }

    private ThingifierHttpApiRoutings routingsOn(final HttpRouteRegistry registry) {
        HttpRouteRegistry.use(registry);
        ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.setPathPrefix("/api");
        return new ThingifierHttpApiRoutings(new Thingifier(), apiDefn);
    }

    private StubRequest request(final String method, final String path) {
        return new StubRequest(method, path);
    }

    private StubResponse response(final int statusCode) {
        StubResponse response = new StubResponse();
        response.status(statusCode);
        return response;
    }

    private static class CountingInternalRequestHook implements InternalHttpRequestHook {
        private int callCount;

        @Override
        public InternalHttpResponse run(final InternalHttpRequest request) {
            callCount++;
            return null;
        }
    }

    private static class CountingInternalResponseHook implements InternalHttpResponseHook {
        private int callCount;

        @Override
        public void run(final InternalHttpRequest request, final InternalHttpResponse response) {
            callCount++;
        }
    }

    private static class StubRequest implements HttpServerRequest {
        private final String method;
        private final String path;
        private final Map<String, Object> attributes;
        private final Map<String, String> headers;

        StubRequest(final String method, final String path) {
            this.method = method;
            this.path = path;
            this.attributes = new HashMap<>();
            this.headers = new LinkedHashMap<>();
        }

        @Override
        public Object attribute(final String name) {
            return attributes.get(name);
        }

        @Override
        public void attribute(final String name, final Object value) {
            attributes.put(name, value);
        }

        @Override
        public String body() {
            return "";
        }

        @Override
        public String contentLength() {
            return "0";
        }

        @Override
        public String cookie(final String name) {
            return "";
        }

        @Override
        public String header(final String name) {
            return headers.getOrDefault(name, "");
        }

        @Override
        public Set<String> headerNames() {
            return headers.keySet();
        }

        @Override
        public String host() {
            return "";
        }

        @Override
        public String ip() {
            return "";
        }

        @Override
        public String method() {
            return method;
        }

        @Override
        public String path() {
            return path;
        }

        @Override
        public String pathInfo() {
            return path;
        }

        @Override
        public String protocol() {
            return "";
        }

        @Override
        public String queryParam(final String name) {
            return "";
        }

        @Override
        public Set<String> queryParamNames() {
            return Collections.emptySet();
        }

        @Override
        public List<String> queryParams(final String name) {
            return List.of();
        }

        @Override
        public Map<String, List<String>> queryParamMap() {
            return Map.of();
        }

        @Override
        public String queryString() {
            return "";
        }

        @Override
        public String scheme() {
            return "";
        }

        @Override
        public String splat() {
            return "";
        }

        @Override
        public String[] splatValues() {
            return new String[0];
        }

        @Override
        public String url() {
            return path;
        }

        @Override
        public Map<String, String> urlParams() {
            return Map.of();
        }
    }

    private static class StubResponse implements HttpServerResponse {
        private int statusCode;
        private String body;
        private String type;
        private final Map<String, String> headers;

        StubResponse() {
            statusCode = 200;
            body = "";
            type = "";
            headers = new LinkedHashMap<>();
        }

        @Override
        public String body() {
            return body;
        }

        @Override
        public void body(final String body) {
            this.body = body;
        }

        @Override
        public void forceBody(final String body) {
            this.body = body;
        }

        @Override
        public boolean containsHeader(final String name) {
            return headers.containsKey(name);
        }

        @Override
        public void header(final String name, final String value) {
            headers.put(name, value);
        }

        @Override
        public Map<String, String> headers() {
            return headers;
        }

        @Override
        public void redirect(final String location) {
            headers.put("Location", location);
        }

        @Override
        public void redirect(final String location, final int statusCode) {
            status(statusCode);
            redirect(location);
        }

        @Override
        public int status() {
            return statusCode;
        }

        @Override
        public void status(final int statusCode) {
            this.statusCode = statusCode;
        }

        @Override
        public void suppressContentType() {
            type = "";
        }

        @Override
        public String type() {
            return type;
        }

        @Override
        public void type(final String contentType) {
            type = contentType;
        }
    }
}
