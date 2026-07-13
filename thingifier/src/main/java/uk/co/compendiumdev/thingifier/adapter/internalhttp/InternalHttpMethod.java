package uk.co.compendiumdev.thingifier.adapter.internalhttp;

import java.util.Locale;

public enum InternalHttpMethod {
    GET,
    HEAD,
    POST,
    PUT,
    DELETE,
    PATCH,
    OPTIONS,
    CONNECT,
    TRACE;

    public static InternalHttpMethod from(final String method) {
        if (method == null || method.trim().isEmpty()) {
            return GET;
        }

        return InternalHttpMethod.valueOf(method.trim().toUpperCase(Locale.ROOT));
    }
}
