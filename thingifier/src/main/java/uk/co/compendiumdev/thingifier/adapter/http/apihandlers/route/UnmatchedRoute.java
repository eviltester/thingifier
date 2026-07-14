package uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route;

public final class UnmatchedRoute extends ThingRoute {

    private final String[] parts;

    UnmatchedRoute(final String originalPath, final String[] parts) {
        super(originalPath);
        this.parts = parts.clone();
    }

    public String[] parts() {
        return parts.clone();
    }

    public int partCount() {
        return parts.length;
    }

    public String firstPart() {
        if (parts.length == 0) {
            return "";
        }
        return parts[0];
    }
}
