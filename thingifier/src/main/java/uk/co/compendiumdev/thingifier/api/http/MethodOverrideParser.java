package uk.co.compendiumdev.thingifier.api.http;

public final class MethodOverrideParser {

    private MethodOverrideParser() {
    }

    public static ThingifierHttpApi.HttpVerb getEffectiveVerb(final HttpApiRequest request,
                                                              final ThingifierHttpApi.HttpVerb defaultVerb) {
        if (defaultVerb != ThingifierHttpApi.HttpVerb.POST) {
            return defaultVerb;
        }

        final String override = request.getHeader("X-HTTP-Method-Override", "").trim().toUpperCase();
        if (override.isEmpty()) {
            return defaultVerb;
        }

        try {
            return ThingifierHttpApi.HttpVerb.valueOf(override);
        } catch (IllegalArgumentException ignored) {
            return defaultVerb;
        }
    }
}
