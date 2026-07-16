package uk.co.compendiumdev.thingifier.crudui;

import java.util.LinkedHashMap;
import java.util.Map;

final class ProjectPathSelection {

    private final boolean available;
    private final boolean selected;
    private final String path;
    private final String message;

    private ProjectPathSelection(
            final boolean available,
            final boolean selected,
            final String path,
            final String message) {
        this.available = available;
        this.selected = selected;
        this.path = path;
        this.message = message;
    }

    static ProjectPathSelection selected(final String path) {
        return new ProjectPathSelection(true, true, path, "Project path selected.");
    }

    static ProjectPathSelection cancelled() {
        return new ProjectPathSelection(true, false, "", "Project browsing cancelled.");
    }

    static ProjectPathSelection unavailable(final String message) {
        return new ProjectPathSelection(false, false, "", message);
    }

    boolean isAvailable() {
        return available;
    }

    String message() {
        return message;
    }

    Map<String, Object> toMap() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("available", available);
        body.put("selected", selected);
        body.put("path", path);
        body.put("message", message);
        return body;
    }
}
