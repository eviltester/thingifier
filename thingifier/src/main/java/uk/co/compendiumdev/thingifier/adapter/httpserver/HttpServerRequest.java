package uk.co.compendiumdev.thingifier.adapter.httpserver;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface HttpServerRequest {
    Object attribute(String name);

    void attribute(String name, Object value);

    String body();

    String contentLength();

    String cookie(String name);

    String header(String name);

    Set<String> headerNames();

    String host();

    String ip();

    String method();

    String path();

    String pathInfo();

    default String params(final String name) {
        return urlParams().get(name);
    }

    String protocol();

    String queryParam(String name);

    Set<String> queryParamNames();

    List<String> queryParams(String name);

    Map<String, List<String>> queryParamMap();

    String queryString();

    String scheme();

    String splat();

    String[] splatValues();

    String url();

    Map<String, String> urlParams();
}
