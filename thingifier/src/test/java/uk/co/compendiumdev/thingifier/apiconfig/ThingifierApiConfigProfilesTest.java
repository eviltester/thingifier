package uk.co.compendiumdev.thingifier.apiconfig;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ThingifierApiConfigProfilesTest {

    @Test
    public void initiallyThereAreNoProfiles(){

        ThingifierApiConfigProfiles profiles = new ThingifierApiConfigProfiles();
        Assertions.assertEquals(0, profiles.countOfProfiles());
    }

    @Test
    public void canCreateAProfile(){

        ThingifierApiConfigProfiles profiles = new ThingifierApiConfigProfiles();
        final ThingifierApiConfigProfile profile = profiles.create(
                                                        "aProfile",
                                                    "this profile desc");

        Assertions.assertEquals("aProfile", profile.getName());
        Assertions.assertEquals("this profile desc", profile.getDescription());
        Assertions.assertEquals(1, profiles.countOfProfiles());

        Assertions.assertNotNull(profile.apiConfig());
    }

    @Test
    public void canNotAddAProfileUsingTheList(){
        ThingifierApiConfigProfiles profiles = new ThingifierApiConfigProfiles();
        final List<ThingifierApiConfigProfile> profileList = profiles.getProfiles();

        profileList.add(new ThingifierApiConfigProfile("name", "desc"));

        Assertions.assertEquals(0, profiles.countOfProfiles());

        // TODO: create an addProfile method to allow adding extra profiles
    }

    @Test
    public void theLastProfileAddedIsTheDefaultProfile(){
        ThingifierApiConfigProfiles profiles = new ThingifierApiConfigProfiles();

        Assertions.assertNull(profiles.getDefault(),
                    "no profile setup, expected null");

        profiles.create("profile1", "1");
        profiles.create("profile2", "2");
        profiles.create("default", "3");
        Assertions.assertEquals(3, profiles.countOfProfiles());

        Assertions.assertEquals("default",
                    profiles.getDefault().getName());
    }

    @Test
    public void canConfigureAProfileAsDefaultWhenAdding(){
        ThingifierApiConfigProfiles profiles = new ThingifierApiConfigProfiles();

        profiles.create("profile1", "1");
        profiles.createDefaultProfile("profile2", "2");
        profiles.create("profile3", "3");
        Assertions.assertEquals(3, profiles.countOfProfiles());

        Assertions.assertEquals("profile2",
                profiles.getDefault().getName());
    }

    @Test
    public void canOverrideAsDefaultWhenAdding(){
        ThingifierApiConfigProfiles profiles = new ThingifierApiConfigProfiles();

        profiles.create("profile1", "1");
        profiles.createDefaultProfile("profile2", "2");
        profiles.createDefaultProfile("default", "3");
        Assertions.assertEquals(3, profiles.countOfProfiles());

        Assertions.assertEquals("default",
                profiles.getDefault().getName());
    }

    @Test
    public void profilesAreReturnedInTheOrderAdded(){
        ThingifierApiConfigProfiles profiles = new ThingifierApiConfigProfiles();

        profiles.create("1", "1");
        profiles.create("2", "2");
        profiles.create("3", "3");
        Assertions.assertEquals(3, profiles.getProfiles().size());

        int expectedProfileName = 1;
        for(ThingifierApiConfigProfile profile : profiles.getProfiles()){
            Assertions.assertEquals(
                    String.valueOf(expectedProfileName),
                    profile.getName());
            expectedProfileName++;
        }

        Assertions.assertEquals(4, expectedProfileName);
    }

    @Test
    public void canGetProfileUsingName(){
        ThingifierApiConfigProfiles profiles = new ThingifierApiConfigProfiles();

        final ThingifierApiConfigProfile v1 = profiles.create("1", "1");
        final ThingifierApiConfigProfile v2 = profiles.create("2", "2");
        profiles.create("3", "3");

        Assertions.assertEquals(v1, profiles.getProfile("1"));
        Assertions.assertEquals(v2, profiles.getProfile("2"));

        Assertions.assertNull(profiles.getProfile("doesnotexist"));
    }
}
