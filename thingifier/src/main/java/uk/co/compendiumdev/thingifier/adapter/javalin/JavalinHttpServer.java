package uk.co.compendiumdev.thingifier.adapter.javalin;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.staticfiles.Location;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HaltRequestException;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpAfterHandler;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpBeforeHandler;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpRouteDefinition;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpRouteRegistry;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpRouteVerb;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseError;

public final class JavalinHttpServer implements AutoCloseable {
    private static final String[] STATIC_ASSET_PREFIXES = {
        "/css/", "/js/", "/favicon/", "/images/"
    };
    private static final String[] STATIC_ASSET_FILES = {"/robots.txt", "/sitemap.bak"};

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
                            config.routes.before(this::serveClasspathStaticAsset);
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
                            config.routes.after(this::removeContentTypeFromEmptyNotFound);
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

    private void serveClasspathStaticAsset(final Context ctx) throws Exception {
        if (ctx.method() != HandlerType.GET && ctx.method() != HandlerType.HEAD) {
            return;
        }

        final String path = ctx.path();
        if (!isStaticAssetPath(path)) {
            return;
        }

        final InputStream resource =
                Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream(classpathStaticResource(path));
        if (resource == null) {
            return;
        }

        ctx.status(200);
        ctx.header("Cache-Control", "max-age=0");
        String contentType = contentTypeFor(path);
        if (contentType != null) {
            ctx.contentType(contentType);
        }
        if (ctx.method() == HandlerType.HEAD) {
            resource.close();
            ctx.result(new byte[0]);
        } else {
            ctx.result(resource);
        }
        ctx.skipRemainingHandlers();
    }

    private boolean isStaticAssetPath(final String path) {
        for (String prefix : STATIC_ASSET_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        for (String file : STATIC_ASSET_FILES) {
            if (path.equals(file)) {
                return true;
            }
        }
        return false;
    }

    private String classpathStaticResource(final String path) {
        String base = staticFilePath == null ? "" : staticFilePath.trim();
        while (base.startsWith("/")) {
            base = base.substring(1);
        }
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String resourcePath = path.startsWith("/") ? path.substring(1) : path;
        if (base.isEmpty()) {
            return resourcePath;
        }
        return base + "/" + resourcePath;
    }

    private String contentTypeFor(final String path) {
        if (path.endsWith(".css")) {
            return "text/css";
        }
        if (path.endsWith(".js")) {
            return "application/javascript";
        }
        if (path.endsWith(".webmanifest")) {
            return "application/manifest+json";
        }
        if (path.endsWith(".png")) {
            return "image/png";
        }
        if (path.endsWith(".ico")) {
            return "image/x-icon";
        }
        if (path.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (path.endsWith(".txt")) {
            return "text/plain";
        }
        return URLConnection.guessContentTypeFromName(path);
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

    private void removeContentTypeFromEmptyNotFound(final Context ctx) {
        if (ctx.statusCode() != 404) {
            return;
        }

        if (!hasEmptyBody(ctx)) {
            return;
        }

        ctx.res().setCharacterEncoding(null);
        ctx.res().setContentType(null);
        ctx.res().setHeader("Content-Type", null);
    }

    private boolean hasEmptyBody(final Context ctx) {
        String body = ctx.attribute(JavalinServerResponse.RESPONSE_BODY_ATTRIBUTE);
        if (body != null) {
            return body.isEmpty();
        }

        String result = ctx.result();
        return result == null || result.isEmpty();
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

    static String javalinPath(final String routePath) {
        if (routePath == null || routePath.isBlank() || "*".equals(routePath)) {
            return "/*";
        }

        String converted = routePath.trim();
        String[] parts = converted.split("/", -1);
        Map<String, Integer> nameCounts = new HashMap<>();
        for (int index = 0; index < parts.length; index++) {
            String part = parts[index];
            if (part.matches(":[A-Za-z][A-Za-z0-9_]*")) {
                String name = part.substring(1);
                int count = nameCounts.merge(name, 1, Integer::sum);
                String javalinName = count == 1 ? name : name + "__" + count;
                parts[index] = "{" + javalinName + "}";
            }
        }
        return String.join("/", parts);
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
