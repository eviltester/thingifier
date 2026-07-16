package uk.co.compendiumdev.thingifier.adapter.javalin;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.staticfiles.Location;
import java.nio.charset.StandardCharsets;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HaltRequestException;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpAfterHandler;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpBeforeHandler;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpRouteDefinition;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpRouteRegistry;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpRouteVerb;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseError;

public final class JavalinHttpServer implements AutoCloseable {
    private final int port;
    private final String staticFilePath;
    private final HttpRouteRegistry registry;
    private Javalin app;

    public JavalinHttpServer(
            final int port, final String staticFilePath, final HttpRouteRegistry registry) {
        this.port = port;
        this.staticFilePath = staticFilePath;
        this.registry = registry;
    }

    public void start() {
        app =
                Javalin.create(
                        config -> {
                            config.router.ignoreTrailingSlashes = false;
                            config.staticFiles.add(
                                    staticFiles -> {
                                        staticFiles.hostedPath = "/";
                                        staticFiles.directory = staticFilePath;
                                        staticFiles.location = Location.CLASSPATH;
                                    });
                            for (HttpBeforeHandler beforeHandler : registry.beforeHandlers()) {
                                config.routes.before(ctx -> runBefore(ctx, beforeHandler));
                            }
                            for (HttpRouteDefinition route : registry.routes()) {
                                config.routes.addHttpHandler(
                                        handlerType(route.verb()),
                                        javalinPath(route.path()),
                                        ctx -> handle(ctx, route));
                            }
                            for (HttpAfterHandler afterHandler : registry.afterHandlers()) {
                                config.routes.after(ctx -> runAfter(ctx, afterHandler));
                            }
                            config.routes.after(this::restoreExactContentType);
                            config.routes.exception(
                                    HaltRequestException.class,
                                    (e, ctx) -> {
                                        ctx.status(e.statusCode());
                                        ctx.result(e.body().getBytes(StandardCharsets.UTF_8));
                                    });
                            config.routes.exception(
                                    RuntimeException.class,
                                    (e, ctx) -> {
                                        ctx.status(400);
                                        ctx.result(
                                                exceptionErrorResponse(e, ctx)
                                                        .getBytes(StandardCharsets.UTF_8));
                                    });
                            config.routes.exception(
                                    Exception.class,
                                    (e, ctx) -> {
                                        ctx.status(500);
                                        ctx.result(
                                                exceptionErrorResponse(e, ctx)
                                                        .getBytes(StandardCharsets.UTF_8));
                                    });
                        });
        app.start(port);
    }

    public void stop() {
        if (app != null) {
            app.stop();
            app = null;
        }
    }

    public int port() {
        return port;
    }

    private void handle(final Context ctx, final HttpRouteDefinition route) throws Exception {
        ctx.attribute(JavalinServerRequest.ROUTE_PATH_ATTRIBUTE, route.path());
        JavalinServerRequest request = new JavalinServerRequest(ctx);
        JavalinServerResponse response = new JavalinServerResponse(ctx);
        String body = route.handler().handle(request, response);
        if (body != null && !response.wasBodySet()) {
            ctx.attribute(JavalinServerResponse.RESPONSE_BODY_ATTRIBUTE, body);
            ctx.result(body.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void runBefore(final Context ctx, final HttpBeforeHandler beforeHandler)
            throws Exception {
        beforeHandler.handle(new JavalinServerRequest(ctx), new JavalinServerResponse(ctx));
    }

    private void runAfter(final Context ctx, final HttpAfterHandler afterHandler) throws Exception {
        afterHandler.handle(new JavalinServerRequest(ctx), new JavalinServerResponse(ctx));
    }

    private void restoreExactContentType(final Context ctx) {
        String contentType = ctx.res().getHeader("Content-Type");
        if (contentType != null && contentType.endsWith(";charset=utf-8")) {
            String exactContentType =
                    contentType.substring(0, contentType.length() - ";charset=utf-8".length());
            ctx.res().setCharacterEncoding(null);
            ctx.res().setContentType(exactContentType);
            ctx.res().setHeader("Content-Type", exactContentType);
        }
    }

    private HandlerType handlerType(final HttpRouteVerb verb) {
        switch (verb) {
            case GET:
                return HandlerType.GET;
            case HEAD:
                return HandlerType.HEAD;
            case OPTIONS:
                return HandlerType.OPTIONS;
            case POST:
                return HandlerType.POST;
            case PUT:
                return HandlerType.PUT;
            case PATCH:
                return HandlerType.PATCH;
            case DELETE:
                return HandlerType.DELETE;
            case TRACE:
                return HandlerType.TRACE;
            default:
                return HandlerType.GET;
        }
    }

    private String javalinPath(final String routePath) {
        if (routePath == null || routePath.isBlank() || "*".equals(routePath)) {
            return "/*";
        }

        String converted = routePath.trim();
        converted = converted.replaceAll(":([A-Za-z][A-Za-z0-9_]*)", "{$1}");
        return converted;
    }

    private String exceptionErrorResponse(final Exception e, final Context ctx) {
        String message = e.getMessage() == null ? e.toString() : e.getMessage();
        return ApiResponseError.asAppropriate(ctx.header("Accept"), message);
    }

    @Override
    public void close() {
        stop();
    }
}
