package uk.co.compendiumdev.thingifier.adapter.httpserver;

import java.util.Map;

public interface HttpServerResponse {
    String body();

    void body(String body);

    boolean containsHeader(String name);

    void header(String name, String value);

    Map<String, String> headers();

    void redirect(String location);

    void redirect(String location, int statusCode);

    int status();

    void status(int statusCode);

    String type();

    void type(String contentType);
}
