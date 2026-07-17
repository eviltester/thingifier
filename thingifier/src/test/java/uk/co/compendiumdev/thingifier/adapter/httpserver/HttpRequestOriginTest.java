package uk.co.compendiumdev.thingifier.adapter.httpserver;

import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HttpRequestOriginTest {

    @Test
    void usesDirectRequestOriginWhenProxyHeadersAreAbsent() {
        final HttpServerRequest request = request("http", "localhost:4567");

        Assertions.assertEquals("http://localhost:4567", HttpRequestOrigin.from(request));
    }

    @Test
    void usesForwardedProtoAndHostWhenPresent() {
        final HttpServerRequest request =
                request(
                        "http",
                        "internal:4567",
                        "X-Forwarded-Proto",
                        "https",
                        "X-Forwarded-Host",
                        "apichallenges.eviltester.com");

        Assertions.assertEquals(
                "https://apichallenges.eviltester.com", HttpRequestOrigin.from(request));
    }

    @Test
    void usesStandardForwardedHeaderWhenPresent() {
        final HttpServerRequest request =
                request(
                        "http",
                        "internal:4567",
                        "Forwarded",
                        "for=192.0.2.60;proto=https;host=\"apichallenges.eviltester.com\"");

        Assertions.assertEquals(
                "https://apichallenges.eviltester.com", HttpRequestOrigin.from(request));
    }

    private HttpServerRequest request(final String scheme, final String host) {
        return request(scheme, host, "", "");
    }

    private HttpServerRequest request(
            final String scheme,
            final String host,
            final String firstHeaderName,
            final String firstHeaderValue) {
        return request(scheme, host, firstHeaderName, firstHeaderValue, "", "");
    }

    private HttpServerRequest request(
            final String scheme,
            final String host,
            final String firstHeaderName,
            final String firstHeaderValue,
            final String secondHeaderName,
            final String secondHeaderValue) {
        return (HttpServerRequest)
                Proxy.newProxyInstance(
                        getClass().getClassLoader(),
                        new Class<?>[] {HttpServerRequest.class},
                        (proxy, method, args) -> {
                            if ("scheme".equals(method.getName())) {
                                return scheme;
                            }
                            if ("host".equals(method.getName())) {
                                return host;
                            }
                            if ("header".equals(method.getName())) {
                                final String requestedHeader = (String) args[0];
                                if (requestedHeader.equals(firstHeaderName)) {
                                    return firstHeaderValue;
                                }
                                if (requestedHeader.equals(secondHeaderName)) {
                                    return secondHeaderValue;
                                }
                                return null;
                            }
                            throw new UnsupportedOperationException(method.getName());
                        });
    }
}
