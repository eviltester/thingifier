package uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route;

public abstract class ThingRoute {

    private final String originalPath;

    ThingRoute(final String originalPath) {
        this.originalPath = originalPath;
    }

    public String originalPath() {
        return originalPath;
    }
}
