package uk.co.compendiumdev.thingifier.apiconfig;

public class ThingifierApiConfigProfile {
    private final String profileName;
    private final String profileDescription;
    private final ThingifierApiConfig apiConfig;

    public ThingifierApiConfigProfile(final String profileName,
                                      final String profileDescription) {

        this.profileName = profileName;
        this.profileDescription = profileDescription;
        apiConfig = new ThingifierApiConfig();
    }

    public ThingifierApiConfig apiConfig() {
        return apiConfig;
    }

    public String getName() {
        return profileName;
    }

    public String getDescription() {
        return profileDescription;
    }
}
