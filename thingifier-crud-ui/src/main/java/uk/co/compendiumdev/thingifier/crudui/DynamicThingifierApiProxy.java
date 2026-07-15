package uk.co.compendiumdev.thingifier.crudui;

import java.util.Map;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpMethod;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.conversion.ThingifierHttpApiBridge;

public final class DynamicThingifierApiProxy {

    private final ActiveThingifierWorkspace workspace;

    public DynamicThingifierApiProxy(final ActiveThingifierWorkspace workspace) {
        this.workspace = workspace;
    }

    public UiHttpResponse forward(final InternalHttpRequest request) {
        request.setPath(stripApiPrefix(request.getPath()));
        ThingifierHttpApiBridge bridge =
                new ThingifierHttpApiBridge(workspace.snapshot().thingifier());
        InternalHttpResponse response = invoke(bridge, request);
        return new UiHttpResponse(
                response.getStatusCode(),
                response.getType(),
                response.getBody(),
                response.getHeaders().asMap());
    }

    public UiHttpResponse getJson(final String path) {
        return forward(jsonRequest(InternalHttpMethod.GET, path, ""));
    }

    public UiHttpResponse postJson(final String path, final String body) {
        return forward(jsonRequest(InternalHttpMethod.POST, path, body));
    }

    public UiHttpResponse deleteJson(final String path) {
        return forward(jsonRequest(InternalHttpMethod.DELETE, path, ""));
    }

    private InternalHttpResponse invoke(
            final ThingifierHttpApiBridge bridge, final InternalHttpRequest request) {
        switch (request.getMethod()) {
            case GET:
                return bridge.get(request);
            case HEAD:
                return bridge.head(request);
            case POST:
                return bridge.post(request);
            case PUT:
                return bridge.put(request);
            case DELETE:
                return bridge.delete(request);
            default:
                return new InternalHttpResponse()
                        .setStatus(405)
                        .setType("application/json")
                        .setBody("{\"errorMessages\":[\"Method not allowed\"]}");
        }
    }

    private InternalHttpRequest jsonRequest(
            final InternalHttpMethod method, final String path, final String body) {
        return new InternalHttpRequest("/api/" + path)
                .setVerb(method)
                .setBody(body)
                .setHeaders(
                        Map.of("Accept", "application/json", "Content-Type", "application/json"));
    }

    private String stripApiPrefix(final String path) {
        String normalized = path == null ? "" : path;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if ("api".equals(normalized)) {
            return "";
        }
        if (normalized.startsWith("api/")) {
            return normalized.substring("api/".length());
        }
        return normalized;
    }
}
