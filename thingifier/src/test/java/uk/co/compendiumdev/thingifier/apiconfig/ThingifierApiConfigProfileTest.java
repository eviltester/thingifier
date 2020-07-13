package uk.co.compendiumdev.thingifier.apiconfig;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ThingifierApiConfigProfileTest {

    @Test
    public void canCreateAProfile(){

        final ThingifierApiConfigProfile profile =
                    new ThingifierApiConfigProfile(
                            "name", "desc");

        Assertions.assertEquals("name", profile.getName());
        Assertions.assertEquals("desc", profile.getDescription());
        Assertions.assertNotNull(profile.apiConfig());
    }
}
