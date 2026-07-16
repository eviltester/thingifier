package uk.co.compendiumdev.thingifier.adapter.httpserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HttpRouteRegistry {
    private static HttpRouteRegistry current;

    private final List<HttpRouteDefinition> routes;
    private final List<HttpBeforeHandler> beforeHandlers;
    private final List<HttpAfterHandler> afterHandlers;

    public HttpRouteRegistry() {
        this.routes = new ArrayList<>();
        this.beforeHandlers = new ArrayList<>();
        this.afterHandlers = new ArrayList<>();
    }

    public static HttpRouteRegistry current() {
        if (current == null) {
            current = new HttpRouteRegistry();
        }
        return current;
    }

    public static void use(final HttpRouteRegistry registry) {
        current = registry;
    }

    public static void clearCurrent() {
        current = null;
    }

    public void add(final HttpRouteVerb verb, final String path, final HttpRouteHandler handler) {
        routes.add(new HttpRouteDefinition(verb, path, handler));
    }

    public void before(final HttpBeforeHandler handler) {
        beforeHandlers.add(handler);
    }

    public void after(final HttpAfterHandler handler) {
        afterHandlers.add(handler);
    }

    public List<HttpRouteDefinition> routes() {
        return Collections.unmodifiableList(routes);
    }

    public List<HttpBeforeHandler> beforeHandlers() {
        return Collections.unmodifiableList(beforeHandlers);
    }

    public List<HttpAfterHandler> afterHandlers() {
        return Collections.unmodifiableList(afterHandlers);
    }
}
