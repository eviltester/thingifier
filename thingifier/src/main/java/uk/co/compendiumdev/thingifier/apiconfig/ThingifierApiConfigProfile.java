package uk.co.compendiumdev.thingifier.apiconfig;

public class ThingifierApiConfigProfile {
    private final String profileName;
    private final String profileDescription;
    private final ThingifierApiConfig apiConfig;

    // todo: allow a profile to inherit from other profiles and only change the
    // parts that they want to change e.g bug fix in the next profile

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
