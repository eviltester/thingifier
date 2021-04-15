package uk.co.compendiumdev.challenge.challenges;

import java.util.ArrayList;
import java.util.List;

public class ChallengeData {

    public final String id;
    public final String name;
    public final String description;
    public Boolean status;

    public ChallengeData(String id, String name, String description){
        this.id = id;
        this.name=name;
        this.description = description;
        this.status = false;
    }

}
