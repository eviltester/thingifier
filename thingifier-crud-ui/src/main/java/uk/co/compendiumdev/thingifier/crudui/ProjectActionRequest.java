package uk.co.compendiumdev.thingifier.crudui;

import java.util.Map;

final class ProjectActionRequest {

    static final String ACTION_LOAD = "load";
    static final String ACTION_SAVE = "save";

    private final String action;
    private final String path;

    private ProjectActionRequest(final String action, final String path) {
        this.action = action;
        this.path = path;
    }

    static ProjectActionRequest fromJson(final String requestJson, final boolean pathRequired) {
        Map<?, ?> request =
                JsonSupport.fromJsonMap(
                        requestJson,
                        "Project request must contain a JSON object",
                        "Could not parse project request JSON");
        String action = stringValue(request.get("action")).trim();
        if (action.isEmpty()) {
            throw new CrudUiException(400, "Project request must contain action");
        }
        if (!ACTION_LOAD.equals(action) && !ACTION_SAVE.equals(action)) {
            throw new CrudUiException(400, "Project action must be save or load");
        }
        String path = stringValue(request.get("path")).trim();
        if (pathRequired && path.isEmpty()) {
            throw new CrudUiException(400, "Project request must contain path");
        }
        return new ProjectActionRequest(action, path);
    }

    String action() {
        return action;
    }

    String path() {
        return path;
    }

    boolean isSave() {
        return ACTION_SAVE.equals(action);
    }

    boolean isLoad() {
        return ACTION_LOAD.equals(action);
    }

    private static String stringValue(final Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
