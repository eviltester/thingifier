package uk.co.compendiumdev.challenge;

public class ChallengeData {

    public final String name;
    public final String description;
    public boolean status;

    public ChallengeData(String name, String description){
        this.name=name;
        this.description = description;
        this.status = false;
    }
}
