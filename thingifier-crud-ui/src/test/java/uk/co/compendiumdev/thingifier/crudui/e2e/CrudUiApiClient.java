package uk.co.compendiumdev.thingifier.crudui.e2e;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

final class CrudUiApiClient {

    private final HttpClient client;
    private final String baseUrl;

    CrudUiApiClient(final String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    ApiResult get(final String path) {
        return send(HttpRequest.newBuilder(uri(path)).GET().build());
    }

    ApiResult delete(final String path) {
        return send(HttpRequest.newBuilder(uri(path)).DELETE().build());
    }

    ApiResult postJson(final String path, final String json) {
        return send(
                HttpRequest.newBuilder(uri(path))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build());
    }

    ApiResult putJson(final String path, final String json) {
        return send(
                HttpRequest.newBuilder(uri(path))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json))
                        .build());
    }

    ApiResult postText(final String path, final String text) {
        return send(
                HttpRequest.newBuilder(uri(path))
                        .header("Content-Type", "text/plain")
                        .header("Accept", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(text))
                        .build());
    }

    String absoluteUrl(final String path) {
        return baseUrl + path;
    }

    String encoded(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private ApiResult send(final HttpRequest request) {
        try {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            return new ApiResult(response.statusCode(), response.body(), response.headers().map());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private URI uri(final String path) {
        return URI.create(baseUrl + path);
    }

    static final class ApiResult {

        private final int statusCode;
        private final String body;
        private final Map<String, java.util.List<String>> headers;

        ApiResult(
                final int statusCode,
                final String body,
                final Map<String, java.util.List<String>> headers) {
            this.statusCode = statusCode;
            this.body = body;
            this.headers = headers;
        }

        int statusCode() {
            return statusCode;
        }

        String body() {
            return body;
        }

        JsonObject jsonObject() {
            JsonElement parsed = JsonParser.parseString(body);
            return parsed.getAsJsonObject();
        }

        String header(final String name) {
            return headers.getOrDefault(name.toLowerCase(), java.util.List.of()).stream()
                    .findFirst()
                    .orElse("");
        }
    }
}
