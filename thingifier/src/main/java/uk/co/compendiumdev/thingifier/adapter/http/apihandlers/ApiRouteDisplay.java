package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

public final class ApiRouteDisplay {

    private static final ApiRouteDisplay EMPTY = new ApiRouteDisplay("");

    private final String originalPath;
    private final String missingInstanceMessage;

    private ApiRouteDisplay(final String originalPath) {
        this(originalPath, "");
    }

    private ApiRouteDisplay(final String originalPath, final String missingInstanceMessage) {
        this.originalPath = originalPath == null ? "" : originalPath;
        this.missingInstanceMessage = missingInstanceMessage == null ? "" : missingInstanceMessage;
    }

    public static ApiRouteDisplay empty() {
        return EMPTY;
    }

    public static ApiRouteDisplay originalPath(final String originalPath) {
        if (originalPath == null || originalPath.isEmpty()) {
            return EMPTY;
        }
        return new ApiRouteDisplay(originalPath);
    }

    public static ApiRouteDisplay missingInstanceMessage(final String message) {
        if (message == null || message.isEmpty()) {
            return EMPTY;
        }
        return new ApiRouteDisplay("", message);
    }

    public String originalPath() {
        return originalPath;
    }

    public boolean hasOriginalPath() {
        return !originalPath.isEmpty();
    }

    public String missingInstanceMessage() {
        return missingInstanceMessage;
    }

    public boolean hasMissingInstanceMessage() {
        return !missingInstanceMessage.isEmpty();
    }
}
