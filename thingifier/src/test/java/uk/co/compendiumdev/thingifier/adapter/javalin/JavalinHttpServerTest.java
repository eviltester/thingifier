package uk.co.compendiumdev.thingifier.adapter.javalin;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpRouteRegistry;
import uk.co.compendiumdev.thingifier.adapter.httpserver.HttpRouteVerb;

class JavalinHttpServerTest {

    @Test
    void duplicateLegacyPathParamNamesAreMadeUniqueForJavalin() {
        Assertions.assertEquals(
                "todos/{id}/tasksof/{id__2}",
                JavalinHttpServer.javalinPath("todos/:id/tasksof/:id"));
    }

    @Test
    void classpathStaticAssetsUseDefaultNoCacheHeader() throws Exception {
        withStaticCacheControlProperty(
                null,
                () ->
                        withStartedServer(
                                port -> {
                                    HttpResponse<String> response =
                                            get("http://localhost:" + port + "/css/default.css");

                                    Assertions.assertEquals(200, response.statusCode());
                                    Assertions.assertEquals(
                                            "max-age=0",
                                            response.headers()
                                                    .firstValue("Cache-Control")
                                                    .orElse(""));
                                }));
    }

    @Test
    void classpathStaticAssetsCanUseConfiguredCacheHeader() throws Exception {
        final String cacheControl = "public, max-age=31536000, immutable";

        withStaticCacheControlProperty(
                cacheControl,
                () ->
                        withStartedServer(
                                port -> {
                                    HttpResponse<String> response =
                                            get("http://localhost:" + port + "/css/default.css");

                                    Assertions.assertEquals(200, response.statusCode());
                                    Assertions.assertEquals(
                                            cacheControl,
                                            response.headers()
                                                    .firstValue("Cache-Control")
                                                    .orElse(""));
                                }));
    }

    @Test
    void requestHeaderSizeUsesBrowserFriendlyDefault() {
        withRequestHeaderSizeProperty(
                null, () -> Assertions.assertEquals(32768, JavalinHttpServer.requestHeaderSize()));
    }

    @Test
    void requestHeaderSizeCanUseConfiguredSystemProperty() {
        withRequestHeaderSizeProperty(
                "49152",
                () -> Assertions.assertEquals(49152, JavalinHttpServer.requestHeaderSize()));
    }

    @Test
    void requestHeaderSizeIgnoresInvalidSystemProperty() {
        withRequestHeaderSizeProperty(
                "not-a-number",
                () -> Assertions.assertEquals(32768, JavalinHttpServer.requestHeaderSize()));
    }

    @Test
    void acceptsLargeBrowserCookieHeadersUpToConfiguredRequestHeaderSize() throws Exception {
        withStartedServer(
                port -> {
                    String oversizedCookie = "oversized=" + "x".repeat(12000);
                    String response =
                            rawHttp(
                                    "GET",
                                    "/css/default.css",
                                    port,
                                    "",
                                    "Cookie: " + oversizedCookie);

                    Assertions.assertTrue(response.startsWith("HTTP/1.1 200 OK"), response);
                });
    }

    @Test
    void emptyNoContentDoesNotReturnContentTypeHeader() throws Exception {
        withStartedServer(
                registry ->
                        registry.add(
                                HttpRouteVerb.GET,
                                "/empty-no-content",
                                (request, response) -> {
                                    response.type("application/json");
                                    response.status(204);
                                    return "";
                                }),
                port -> {
                    HttpResponse<String> response =
                            get("http://localhost:" + port + "/empty-no-content");

                    Assertions.assertEquals(204, response.statusCode());
                    Assertions.assertEquals("", response.body());
                    Assertions.assertTrue(response.headers().firstValue("Content-Type").isEmpty());
                });
    }

    @Test
    void keepsSingleLegacyPathParamNamesReadable() {
        Assertions.assertEquals(
                "/challenger/{id}", JavalinHttpServer.javalinPath("/challenger/:id"));
    }

    @Test
    void contentTypeCanBeSuppressedForResponseWithBody() throws Exception {
        withStartedServer(
                registry ->
                        registry.add(
                                HttpRouteVerb.GET,
                                "/no-content-type",
                                (request, response) -> {
                                    response.suppressContentType();
                                    response.status(200);
                                    return "{\"version\":\"6\"}";
                                }),
                port -> {
                    HttpResponse<String> response =
                            get("http://localhost:" + port + "/no-content-type");

                    Assertions.assertEquals(200, response.statusCode());
                    Assertions.assertEquals("{\"version\":\"6\"}", response.body());
                    Assertions.assertTrue(response.headers().firstValue("Content-Type").isEmpty());
                });
    }

    @Test
    void queryMethodRoutesThroughJavalinAdapter() throws Exception {
        withStartedServer(
                registry ->
                        registry.add(
                                HttpRouteVerb.QUERY,
                                "/search",
                                (request, response) -> {
                                    response.type("text/plain");
                                    response.status(200);
                                    return request.method() + ":" + request.body();
                                }),
                port -> {
                    String response = rawHttp("QUERY", "/search", port, "title=Task");

                    Assertions.assertTrue(response.startsWith("HTTP/1.1 200 OK"));
                    Assertions.assertTrue(response.endsWith("QUERY:title=Task"));
                });
    }

    @Test
    void forcedBodyCanBeSentWithNoContentStatus() throws Exception {
        withStartedServer(
                registry ->
                        registry.add(
                                HttpRouteVerb.DELETE,
                                "/forced-no-content",
                                (request, response) -> {
                                    response.type("application/json");
                                    response.status(204);
                                    response.forceBody("{\"message\":\"forced\"}");
                                    return "";
                                }),
                port -> {
                    String response = rawHttp("DELETE", "/forced-no-content", port);

                    Assertions.assertTrue(response.startsWith("HTTP/1.1 204 No Content"));
                    Assertions.assertTrue(response.contains("Content-Type: application/json"));
                    Assertions.assertTrue(response.contains("Content-Length: 20"));
                    Assertions.assertTrue(response.endsWith("{\"message\":\"forced\"}"));
                });
    }

    @Test
    void emptyNotFoundDoesNotReturnContentTypeHeader() throws Exception {
        withStartedServer(
                registry ->
                        registry.add(
                                HttpRouteVerb.GET,
                                "/empty-not-found",
                                (request, response) -> {
                                    response.type("application/json");
                                    response.status(404);
                                    return "";
                                }),
                port -> {
                    HttpResponse<String> response =
                            get("http://localhost:" + port + "/empty-not-found");

                    Assertions.assertEquals(404, response.statusCode());
                    Assertions.assertEquals("", response.body());
                    Assertions.assertTrue(response.headers().firstValue("Content-Type").isEmpty());
                });
    }

    @Test
    void notFoundWithBodyKeepsContentTypeHeader() throws Exception {
        withStartedServer(
                registry ->
                        registry.add(
                                HttpRouteVerb.GET,
                                "/body-not-found",
                                (request, response) -> {
                                    response.type("application/json");
                                    response.status(404);
                                    return "{\"error\":\"missing\"}";
                                }),
                port -> {
                    HttpResponse<String> response =
                            get("http://localhost:" + port + "/body-not-found");

                    Assertions.assertEquals(404, response.statusCode());
                    Assertions.assertEquals("{\"error\":\"missing\"}", response.body());
                    Assertions.assertEquals(
                            "application/json",
                            response.headers().firstValue("Content-Type").orElse(""));
                });
    }

    private HttpResponse<String> get(final String url) throws Exception {
        return HttpClient.newHttpClient()
                .send(
                        HttpRequest.newBuilder(new URI(url)).GET().build(),
                        HttpResponse.BodyHandlers.ofString());
    }

    private void withStartedServer(final ServerRequest request) throws Exception {
        withStartedServer(registry -> {}, request);
    }

    private void withStartedServer(final RouteConfigurer routes, final ServerRequest request)
            throws Exception {
        int port = availablePort();
        HttpRouteRegistry registry = new HttpRouteRegistry();
        routes.configure(registry);

        try (JavalinHttpServer server = new JavalinHttpServer(port, "/public", registry)) {
            server.start();
            request.run(port);
        }
    }

    private void withStaticCacheControlProperty(
            final String configuredValue, final CheckedRunnable request) throws Exception {
        final String originalValue =
                System.getProperty(JavalinHttpServer.STATIC_CACHE_CONTROL_PROPERTY);
        if (configuredValue == null) {
            System.clearProperty(JavalinHttpServer.STATIC_CACHE_CONTROL_PROPERTY);
        } else {
            System.setProperty(JavalinHttpServer.STATIC_CACHE_CONTROL_PROPERTY, configuredValue);
        }

        try {
            request.run();
        } finally {
            restoreStaticCacheControlProperty(originalValue);
        }
    }

    private void restoreStaticCacheControlProperty(final String originalValue) {
        if (originalValue == null) {
            System.clearProperty(JavalinHttpServer.STATIC_CACHE_CONTROL_PROPERTY);
        } else {
            System.setProperty(JavalinHttpServer.STATIC_CACHE_CONTROL_PROPERTY, originalValue);
        }
    }

    private void withRequestHeaderSizeProperty(
            final String configuredValue, final CheckedRunnable request) {
        final String originalValue =
                System.getProperty(JavalinHttpServer.REQUEST_HEADER_SIZE_PROPERTY);
        if (configuredValue == null) {
            System.clearProperty(JavalinHttpServer.REQUEST_HEADER_SIZE_PROPERTY);
        } else {
            System.setProperty(JavalinHttpServer.REQUEST_HEADER_SIZE_PROPERTY, configuredValue);
        }

        try {
            request.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            restoreRequestHeaderSizeProperty(originalValue);
        }
    }

    private void restoreRequestHeaderSizeProperty(final String originalValue) {
        if (originalValue == null) {
            System.clearProperty(JavalinHttpServer.REQUEST_HEADER_SIZE_PROPERTY);
        } else {
            System.setProperty(JavalinHttpServer.REQUEST_HEADER_SIZE_PROPERTY, originalValue);
        }
    }

    private int availablePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private String rawHttp(final String method, final String path, final int port)
            throws Exception {
        return rawHttp(method, path, port, "");
    }

    private String rawHttp(
            final String method, final String path, final int port, final String body)
            throws Exception {
        return rawHttp(method, path, port, body, new String[0]);
    }

    private String rawHttp(
            final String method,
            final String path,
            final int port,
            final String body,
            final String... headers)
            throws Exception {
        try (Socket socket = new Socket("localhost", port)) {
            socket.setSoTimeout(5000);
            byte[] bodyBytes = body.getBytes(StandardCharsets.ISO_8859_1);
            StringBuilder rawRequest = new StringBuilder();
            rawRequest
                    .append(method)
                    .append(" ")
                    .append(path)
                    .append(" HTTP/1.1\r\nHost: localhost:")
                    .append(port)
                    .append("\r\nContent-Length: ")
                    .append(bodyBytes.length)
                    .append("\r\n");
            for (String header : headers) {
                rawRequest.append(header).append("\r\n");
            }
            rawRequest.append("Connection: close\r\n\r\n");
            socket.getOutputStream()
                    .write(rawRequest.toString().getBytes(StandardCharsets.ISO_8859_1));
            socket.getOutputStream().write(bodyBytes);
            return new String(socket.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @FunctionalInterface
    private interface RouteConfigurer {
        void configure(HttpRouteRegistry registry);
    }

    @FunctionalInterface
    private interface ServerRequest {
        void run(int port) throws Exception;
    }

    @FunctionalInterface
    private interface CheckedRunnable {
        void run() throws Exception;
    }
}
