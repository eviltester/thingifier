package uk.co.compendiumdev.thingifier.api;

public enum RoutingVerb {
    GET("GET"), HEAD("HEAD"), DELETE("DELETE"), PATCH("PATCH"), PUT("PUT"), OPTIONS("OPTIONS"), POST("POST");

    private final String verbtext;

    RoutingVerb(String verbtext) {
        this.verbtext = verbtext;
    }

    @Override
    public String toString() {
        return this.verbtext;
    }
}
