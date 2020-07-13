package uk.co.compendiumdev.thingifier.apiconfig;

import java.util.ArrayList;
import java.util.List;

public class ThingifierApiConfigProfiles {

    List<ThingifierApiConfigProfile> profiles;
    ThingifierApiConfigProfile defaultProfile;

    public ThingifierApiConfigProfiles(){
        profiles = new ArrayList<>();
    }

    public ThingifierApiConfigProfile create(final String profileName,
                                             final String profileDescription) {

        final ThingifierApiConfigProfile profile = new ThingifierApiConfigProfile(
                                                            profileName, profileDescription);
        profiles.add(profile);
        return profile;
    }

    public List<ThingifierApiConfigProfile> getProfiles() {
        List<ThingifierApiConfigProfile> profilesCopy = new ArrayList<>();
        profilesCopy.addAll(profiles);
        return profilesCopy;
    }

    public ThingifierApiConfigProfile getDefault() {

        ThingifierApiConfigProfile profile=null;

        if(defaultProfile==null){
            if(profiles.size()>0) {
                profile = profiles.get(profiles.size() - 1);
            }
        }else{
            profile=defaultProfile;
        }

        return profile;
    }

    public ThingifierApiConfigProfile createDefaultProfile(final String name, final String description) {
        final ThingifierApiConfigProfile profile = create(name, description);
        if(defaultProfile!=null){
            System.out.println(
                String.format(
                    "WARNING: overriding default profile from %s to %s",
                    defaultProfile.getName(), name
                ));
        }
        defaultProfile = profile;
        return profile;
    }

    public int countOfProfiles() {
        return getProfiles().size();
    }

    public ThingifierApiConfigProfile getProfile(final String name) {
        for(ThingifierApiConfigProfile profile : profiles){
            if(profile.getName().equals(name)){
                return profile;
            }
        }
        return null;
    }
}
