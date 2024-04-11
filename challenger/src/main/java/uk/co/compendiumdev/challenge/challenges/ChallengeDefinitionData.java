package uk.co.compendiumdev.challenge.challenges;

import java.util.ArrayList;
import java.util.List;

public class ChallengeDefinitionData {

    public final String id;
    public final String name;
    public final String description;
    public Boolean status;
    public List<ChallengeSolutionLink> solutions = new ArrayList<>();
    public List<ChallengeHint> hints = new ArrayList<>();

    public ChallengeDefinitionData(String id, String name, String description){
        this.id = id;
        this.name=name;
        this.description = description;
        this.status = false;
    }

    public void addSolutionLink(final String linkText, final String linkType, final String linkData) {
        solutions.add(new ChallengeSolutionLink(linkText, linkType, linkData));
    }

    public boolean hasSolutionLinks() {
        return solutions.size()>0;
    }

    public void addSolutions(final List<ChallengeSolutionLink> solutions) {
        this.solutions.addAll(solutions);
    }

    public void addHint(final String hintText, final String hintLink) {
        hints.add(new ChallengeHint(hintText, hintLink));
    }

    public boolean hasHints() {
        return hints.size()>0;
    }

    public void addHints(final List<ChallengeHint> hints) {
        this.hints.addAll(hints);
    }

    public void addHint(final String hintText) {
        hints.add(new ChallengeHint(hintText, ""));
    }
}
