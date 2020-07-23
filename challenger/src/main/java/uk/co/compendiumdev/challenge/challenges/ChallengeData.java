package uk.co.compendiumdev.challenge.challenges;

public class ChallengeData {

    public final String name;
    public final String description;
    public Boolean status;

    public ChallengeData(String name, String description){
        this.name=name;
        this.description = description;
        this.status = false;
    }
}
