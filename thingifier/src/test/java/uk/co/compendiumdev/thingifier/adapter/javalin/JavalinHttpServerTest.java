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
        final String originalValue =
                System.getProperty(JavalinHttpServer.STATIC_CACHE_CONTROL_PROPERTY);
        System.clearProperty(JavalinHttpServer.STATIC_CACHE_CONTROL_PROPERTY);

        try {
            int port = availablePort();
            HttpRouteRegistry registry = new HttpRouteRegistry();

            try (JavalinHttpServer server = new JavalinHttpServer(port, "/public", registry)) {
                server.start();

                HttpResponse<String> response =
                        get("http://localhost:" + port + "/css/default.css");

                Assertions.assertEquals(200, response.statusCode());
                Assertions.assertEquals(
                        "max-age=0", response.headers().firstValue("Cache-Control").orElse(""));
            }
        } finally {
            restoreStaticCacheControlProperty(originalValue);
        }
    }

    @Test
    void classpathStaticAssetsCanUseConfiguredCacheHeader() throws Exception {
        final String originalValue =
                System.getProperty(JavalinHttpServer.STATIC_CACHE_CONTROL_PROPERTY);
        final String cacheControl = "public, max-age=31536000, immutable";
        System.setProperty(JavalinHttpServer.STATIC_CACHE_CONTROL_PROPERTY, cacheControl);

        try {
            int port = availablePort();
            HttpRouteRegistry registry = new HttpRouteRegistry();

            try (JavalinHttpServer server = new JavalinHttpServer(port, "/public", registry)) {
                server.start();

                HttpResponse<String> response =
                        get("http://localhost:" + port + "/css/default.css");

                Assertions.assertEquals(200, response.statusCode());
                Assertions.assertEquals(
                        cacheControl, response.headers().firstValue("Cache-Control").orElse(""));
            }
        } finally {
            restoreStaticCacheControlProperty(originalValue);
        }
    }

    @Test
    void emptyNoContentDoesNotReturnContentTypeHeader() throws Exception {
        int port = availablePort();
        HttpRouteRegistry registry = new HttpRouteRegistry();
        registry.add(
                HttpRouteVerb.GET,
                "/empty-no-content",
                (request, response) -> {
                    response.type("application/json");
                    response.status(204);
                    return "";
                });

        try (JavalinHttpServer server = new JavalinHttpServer(port, "/public", registry)) {
            server.start();

            HttpResponse<String> response = get("http://localhost:" + port + "/empty-no-content");

            Assertions.assertEquals(204, response.statusCode());
            Assertions.assertEquals("", response.body());
            Assertions.assertTrue(response.headers().firstValue("Content-Type").isEmpty());
        }
    }

    @Test
    void keepsSingleLegacyPathParamNamesReadable() {
        Assertions.assertEquals(
                "/challenger/{id}", JavalinHttpServer.javalinPath("/challenger/:id"));
    }

    @Test
    void contentTypeCanBeSuppressedForResponseWithBody() throws Exception {
        int port = availablePort();
        HttpRouteRegistry registry = new HttpRouteRegistry();
        registry.add(
                HttpRouteVerb.GET,
                "/no-content-type",
                (request, response) -> {
                    response.suppressContentType();
                    response.status(200);
                    return "{\"version\":\"6\"}";
                });

        try (JavalinHttpServer server = new JavalinHttpServer(port, "/public", registry)) {
            server.start();

            HttpResponse<String> response = get("http://localhost:" + port + "/no-content-type");

            Assertions.assertEquals(200, response.statusCode());
            Assertions.assertEquals("{\"version\":\"6\"}", response.body());
            Assertions.assertTrue(response.headers().firstValue("Content-Type").isEmpty());
        }
    }

    @Test
    void queryMethodRoutesThroughJavalinAdapter() throws Exception {
        int port = availablePort();
        HttpRouteRegistry registry = new HttpRouteRegistry();
        registry.add(
                HttpRouteVerb.QUERY,
                "/search",
                (request, response) -> {
                    response.type("text/plain");
                    response.status(200);
                    return request.method() + ":" + request.body();
                });

        try (JavalinHttpServer server = new JavalinHttpServer(port, "/public", registry)) {
            server.start();

            String response = rawHttp("QUERY", "/search", port, "title=Task");

            Assertions.assertTrue(response.startsWith("HTTP/1.1 200 OK"));
            Assertions.assertTrue(response.endsWith("QUERY:title=Task"));
        }
    }

    @Test
    void forcedBodyCanBeSentWithNoContentStatus() throws Exception {
        int port = availablePort();
        HttpRouteRegistry registry = new HttpRouteRegistry();
        registry.add(
                HttpRouteVerb.DELETE,
                "/forced-no-content",
                (request, response) -> {
                    response.type("application/json");
                    response.status(204);
                    response.forceBody("{\"message\":\"forced\"}");
                    return "";
                });

        try (JavalinHttpServer server = new JavalinHttpServer(port, "/public", registry)) {
            server.start();

            String response = rawHttp("DELETE", "/forced-no-content", port);

            Assertions.assertTrue(response.startsWith("HTTP/1.1 204 No Content"));
            Assertions.assertTrue(response.contains("Content-Type: application/json"));
            Assertions.assertTrue(response.contains("Content-Length: 20"));
            Assertions.assertTrue(response.endsWith("{\"message\":\"forced\"}"));
        }
    }

    @Test
    void emptyNotFoundDoesNotReturnContentTypeHeader() throws Exception {
        int port = availablePort();
        HttpRouteRegistry registry = new HttpRouteRegistry();
        registry.add(
                HttpRouteVerb.GET,
                "/empty-not-found",
                (request, response) -> {
                    response.type("application/json");
                    response.status(404);
                    return "";
                });

        try (JavalinHttpServer server = new JavalinHttpServer(port, "/public", registry)) {
            server.start();

            HttpResponse<String> response = get("http://localhost:" + port + "/empty-not-found");

            Assertions.assertEquals(404, response.statusCode());
            Assertions.assertEquals("", response.body());
            Assertions.assertTrue(response.headers().firstValue("Content-Type").isEmpty());
        }
    }

    @Test
    void notFoundWithBodyKeepsContentTypeHeader() throws Exception {
        int port = availablePort();
        HttpRouteRegistry registry = new HttpRouteRegistry();
        registry.add(
                HttpRouteVerb.GET,
                "/body-not-found",
                (request, response) -> {
                    response.type("application/json");
                    response.status(404);
                    return "{\"error\":\"missing\"}";
                });

        try (JavalinHttpServer server = new JavalinHttpServer(port, "/public", registry)) {
            server.start();

            HttpResponse<String> response = get("http://localhost:" + port + "/body-not-found");

            Assertions.assertEquals(404, response.statusCode());
            Assertions.assertEquals("{\"error\":\"missing\"}", response.body());
            Assertions.assertEquals(
                    "application/json", response.headers().firstValue("Content-Type").orElse(""));
        }
    }

    private HttpResponse<String> get(final String url) throws Exception {
        return HttpClient.newHttpClient()
                .send(
                        HttpRequest.newBuilder(new URI(url)).GET().build(),
                        HttpResponse.BodyHandlers.ofString());
    }

    private void restoreStaticCacheControlProperty(final String originalValue) {
        if (originalValue == null) {
            System.clearProperty(JavalinHttpServer.STATIC_CACHE_CONTROL_PROPERTY);
        } else {
            System.setProperty(JavalinHttpServer.STATIC_CACHE_CONTROL_PROPERTY, originalValue);
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
        try (Socket socket = new Socket("localhost", port)) {
            socket.setSoTimeout(5000);
            byte[] bodyBytes = body.getBytes(StandardCharsets.ISO_8859_1);
            socket.getOutputStream()
                    .write(
                            (method
                                            + " "
                                            + path
                                            + " HTTP/1.1\r\nHost: localhost:"
                                            + port
                                            + "\r\nContent-Length: "
                                            + bodyBytes.length
                                            + "\r\nConnection: close\r\n\r\n")
                                    .getBytes(StandardCharsets.ISO_8859_1));
            socket.getOutputStream().write(bodyBytes);
            return new String(socket.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
