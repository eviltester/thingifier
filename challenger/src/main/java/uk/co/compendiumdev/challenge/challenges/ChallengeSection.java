package uk.co.compendiumdev.challenge.challenges;

import java.util.ArrayList;
import java.util.List;

public class ChallengeSection {
    private final String title;
    private final String description;
    private final ArrayList<ChallengeData> challenges;

    public ChallengeSection(final String title, final String description) {
        this.title = title;
        this.description = description;
        this.challenges = new ArrayList<ChallengeData>();
    }

    public void addChallenge(final ChallengeData challenge) {
        challenges.add(challenge);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<ChallengeData> getChallenges() {
        return this.challenges;
    }
}
