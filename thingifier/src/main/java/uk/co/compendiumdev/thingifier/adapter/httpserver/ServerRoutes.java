package uk.co.compendiumdev.thingifier.adapter.httpserver;

public final class ServerRoutes {
    public static final Redirect redirect = new Redirect();

    private ServerRoutes() {}

    public static void get(final String path, final HttpRouteHandler handler) {
        route(HttpRouteVerb.GET, path, handler);
    }

    public static void head(final String path, final HttpRouteHandler handler) {
        route(HttpRouteVerb.HEAD, path, handler);
    }

    public static void options(final String path, final HttpRouteHandler handler) {
        route(HttpRouteVerb.OPTIONS, path, handler);
    }

    public static void post(final String path, final HttpRouteHandler handler) {
        route(HttpRouteVerb.POST, path, handler);
    }

    public static void put(final String path, final HttpRouteHandler handler) {
        route(HttpRouteVerb.PUT, path, handler);
    }

    public static void patch(final String path, final HttpRouteHandler handler) {
        route(HttpRouteVerb.PATCH, path, handler);
    }

    public static void delete(final String path, final HttpRouteHandler handler) {
        route(HttpRouteVerb.DELETE, path, handler);
    }

    public static void trace(final String path, final HttpRouteHandler handler) {
        route(HttpRouteVerb.TRACE, path, handler);
    }

    public static void after(final HttpAfterHandler handler) {
        HttpRouteRegistry.current().after(handler);
    }

    public static void before(final HttpBeforeHandler handler) {
        HttpRouteRegistry.current().before(handler);
    }

    public static void halt(final int statusCode, final String body) {
        throw new HaltRequestException(statusCode, body);
    }

    public static void route(
            final HttpRouteVerb verb, final String path, final HttpRouteHandler handler) {
        HttpRouteRegistry.current().add(verb, path, handler);
    }

    public static final class Redirect {
        public void get(final String fromPath, final String toPath) {
            ServerRoutes.get(
                    fromPath,
                    (request, response) -> {
                        response.redirect(toPath);
                        return "";
                    });
        }
    }
}
