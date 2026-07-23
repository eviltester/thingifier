package uk.co.compendiumdev.thingifier.adapter.javalin;

import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    void keepsSingleLegacyPathParamNamesReadable() {
        Assertions.assertEquals(
                "/challenger/{id}", JavalinHttpServer.javalinPath("/challenger/:id"));
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

    private int availablePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
