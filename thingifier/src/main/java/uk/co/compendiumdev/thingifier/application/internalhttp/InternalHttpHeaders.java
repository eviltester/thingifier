package uk.co.compendiumdev.thingifier.application.internalhttp;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class InternalHttpHeaders {

    private final Map<String, String> headers;

    public InternalHttpHeaders() {
        headers = new LinkedHashMap<>();
    }

    public void put(final String headerName, final String value) {
        if (headerName == null) {
            return;
        }

        String valueToAdd = value == null ? "" : value;
        headers.put(normalise(headerName), valueToAdd);
    }

    public void putAll(final Map<String, String> headersToAdd) {
        if (headersToAdd == null) {
            return;
        }

        for (Map.Entry<String, String> entry : headersToAdd.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public void putAll(final InternalHttpHeaders headersToAdd) {
        if (headersToAdd == null) {
            return;
        }

        putAll(headersToAdd.asMap());
    }

    public String get(final String headerName) {
        if (headerName == null) {
            return "";
        }

        if (!headers.containsKey(normalise(headerName))) {
            return "";
        }

        return headers.get(normalise(headerName));
    }

    public Map<String, String> asMap() {
        return new LinkedHashMap<>(headers);
    }

    public int size() {
        return headers.size();
    }

    public boolean headerExists(final String headerName) {
        if (headerName == null) {
            return false;
        }

        return headers.containsKey(normalise(headerName));
    }

    private String normalise(final String headerName) {
        return headerName.trim().toLowerCase(Locale.ROOT);
    }
}
