package uk.co.compendiumdev.challenge.challenges.definitions;

public class ChallengeRenderer {

    public static String renderChallengeNumber(int challengeOrder){
        return String.format("%02d", challengeOrder);
    }
}
