package uk.co.compendiumdev.thingifier.adapter.javalin;

import io.javalin.http.Context;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpServerRequest;

final class JavalinServerRequest implements HttpServerRequest {
    static final String ROUTE_PATH_ATTRIBUTE = "thingifier.route.path";

    private final Context context;

    JavalinServerRequest(final Context context) {
        this.context = context;
    }

    @Override
    public Object attribute(final String name) {
        return context.attribute(name);
    }

    @Override
    public void attribute(final String name, final Object value) {
        context.attribute(name, value);
    }

    @Override
    public String body() {
        return context.body();
    }

    @Override
    public String contentLength() {
        return context.header("Content-Length");
    }

    @Override
    public String cookie(final String name) {
        return context.cookie(name);
    }

    @Override
    public String header(final String name) {
        return context.header(name);
    }

    @Override
    public Set<String> headerNames() {
        return new LinkedHashSet<>(context.headerMap().keySet());
    }

    @Override
    public String host() {
        return context.host();
    }

    @Override
    public String ip() {
        return context.ip();
    }

    @Override
    public String method() {
        return context.method().name();
    }

    @Override
    public String path() {
        return context.path();
    }

    @Override
    public String pathInfo() {
        return context.path();
    }

    @Override
    public String protocol() {
        return context.protocol();
    }

    @Override
    public String queryParam(final String name) {
        return context.queryParam(name);
    }

    @Override
    public Set<String> queryParamNames() {
        return new LinkedHashSet<>(context.queryParamMap().keySet());
    }

    @Override
    public List<String> queryParams(final String name) {
        return context.queryParams(name);
    }

    @Override
    public Map<String, List<String>> queryParamMap() {
        return new LinkedHashMap<>(context.queryParamMap());
    }

    @Override
    public String queryString() {
        return context.queryString();
    }

    @Override
    public String scheme() {
        return context.scheme();
    }

    @Override
    public String splat() {
        String routePath = context.attribute(ROUTE_PATH_ATTRIBUTE);
        if (routePath == null || !routePath.endsWith("/*")) {
            return "";
        }

        String prefix = routePath.substring(0, routePath.length() - 1);
        String path = context.path();
        if (path.startsWith(prefix)) {
            return path.substring(prefix.length());
        }
        return "";
    }

    @Override
    public String[] splatValues() {
        return new String[] {splat()};
    }

    @Override
    public String url() {
        return context.url();
    }

    @Override
    public Map<String, String> urlParams() {
        String routePath = context.attribute(ROUTE_PATH_ATTRIBUTE);
        if (routePath != null) {
            return urlParamsFromRoute(routePath, context.path());
        }

        Map<String, String> params = new LinkedHashMap<>();
        for (Map.Entry<String, String> param : context.pathParamMap().entrySet()) {
            params.put(param.getKey(), param.getValue());
            params.put(":" + param.getKey(), param.getValue());
        }
        return params;
    }

    private Map<String, String> urlParamsFromRoute(final String routePath, final String path) {
        Map<String, String> params = new LinkedHashMap<>();
        String[] routeParts = stripLeadingSlash(routePath).split("/", -1);
        String[] pathParts = stripLeadingSlash(path).split("/", -1);
        int max = Math.min(routeParts.length, pathParts.length);

        for (int index = 0; index < max; index++) {
            String routePart = routeParts[index];
            if (routePart.matches(":[A-Za-z][A-Za-z0-9_]*")) {
                String name = routePart.substring(1);
                params.put(name, pathParts[index]);
                params.put(":" + name, pathParts[index]);
            }
        }

        return params;
    }

    private String stripLeadingSlash(final String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.startsWith("/") ? value.substring(1) : value;
    }
}
