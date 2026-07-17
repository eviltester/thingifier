package uk.co.compendiumdev.thingifier.adapter.httpserver;

final class HttpRequestOrigin {

    private HttpRequestOrigin() {}

    static String from(final HttpServerRequest request) {
        return "%s://%s".formatted(schemeFrom(request), hostFrom(request));
    }

    private static String schemeFrom(final HttpServerRequest request) {
        final String forwardedProto = forwardedHeaderValue(request.header("Forwarded"), "proto");
        if (hasText(forwardedProto)) {
            return forwardedProto;
        }

        final String proxyProto = firstHeaderValue(request.header("X-Forwarded-Proto"));
        if (hasText(proxyProto)) {
            return proxyProto;
        }

        return request.scheme();
    }

    private static String hostFrom(final HttpServerRequest request) {
        final String forwardedHost = forwardedHeaderValue(request.header("Forwarded"), "host");
        if (hasText(forwardedHost)) {
            return forwardedHost;
        }

        final String proxyHost = firstHeaderValue(request.header("X-Forwarded-Host"));
        if (hasText(proxyHost)) {
            return proxyHost;
        }

        return request.host();
    }

    private static String forwardedHeaderValue(final String header, final String key) {
        final String firstValue = firstHeaderValue(header);
        if (!hasText(firstValue)) {
            return "";
        }

        final String prefix = "%s=".formatted(key);
        final String[] parts = firstValue.split(";");
        for (final String part : parts) {
            final String trimmed = part.trim();
            if (trimmed.toLowerCase().startsWith(prefix)) {
                return unquote(trimmed.substring(prefix.length()).trim());
            }
        }

        return "";
    }

    private static String firstHeaderValue(final String header) {
        if (!hasText(header)) {
            return "";
        }
        return header.split(",", 2)[0].trim();
    }

    private static String unquote(final String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static boolean hasText(final String value) {
        return value != null && !value.trim().isEmpty();
    }
}
