package uk.co.compendiumdev.challenge;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChallengeAuthDataTest {

    @Test
    void canCreateAuthData(){

        final long timeNow = System.currentTimeMillis();

        ChallengerAuthData authData = new ChallengerAuthData();

        Assertions.assertTrue(authData.getLastAccessed()>=timeNow);
        Assertions.assertTrue(authData.expiresAt()>=timeNow+30000);
        Assertions.assertNotNull(authData.getXChallenger());
        Assertions.assertNotNull(authData.getXAuthToken());
        Assertions.assertNotNull(authData.getNote());
        Assertions.assertEquals("", authData.getNote());
        Assertions.assertFalse(authData.statusOfChallenge(CHALLENGE.GET_CHALLENGES));
    }

    @Test
    void canOverrideGuid(){
        ChallengerAuthData authData = new ChallengerAuthData();
        authData.setXChallengerGUID("bob");
        Assertions.assertEquals("bob", authData.getXChallenger());
    }

    @Test
    void canCreatePassAChallenge(){

        ChallengerAuthData authData = new ChallengerAuthData();

        authData.pass(CHALLENGE.GET_CHALLENGES);

        Assertions.assertTrue(authData.statusOfChallenge(CHALLENGE.GET_CHALLENGES));
    }

    @Test
    void canPassAllChallenges(){

        ChallengerAuthData authData = new ChallengerAuthData();

        for(CHALLENGE challenge : CHALLENGE.values()){
            authData.pass(challenge);
        }

        Assertions.assertTrue(authData.statusOfChallenge(CHALLENGE.GET_CHALLENGES));
        Assertions.assertTrue(authData.statusOfChallenge(CHALLENGE.GET_HEARTBEAT_204));

        for(CHALLENGE challenge : CHALLENGE.values()){
            Assertions.assertTrue(authData.statusOfChallenge(challenge));
        }
    }

    @Test
    void canNotSetNullNote(){
        ChallengerAuthData authData = new ChallengerAuthData();

        Assertions.assertThrows(RuntimeException.class,
                ()-> authData.setNote(null));
    }

    @Test
    void canSetEmptyNote(){
        ChallengerAuthData authData = new ChallengerAuthData();

        authData.setNote("");
        Assertions.assertEquals("", authData.getNote());
    }

    @Test
    void noteTruncatedTo100Chars(){
        ChallengerAuthData authData = new ChallengerAuthData();


        String morethanonehundred = stringOfLength(101);
        Assertions.assertEquals(101, morethanonehundred.length());

        authData.setNote(morethanonehundred);
        Assertions.assertEquals(100, authData.getNote().length());

        authData.setNote(stringOfLength(200));
        Assertions.assertEquals(100, authData.getNote().length());
    }

    private String stringOfLength(final int desiredLength) {
        String ofLength = "";
        while(ofLength.length()<desiredLength){
            ofLength = ofLength + "a";
        }

        return ofLength;
    }

    @Test
    void noteAcceptedAt100CharsOrLess(){
        ChallengerAuthData authData = new ChallengerAuthData();

        authData.setNote(stringOfLength(100));
        Assertions.assertEquals(100, authData.getNote().length());

        authData.setNote(stringOfLength(99));
        Assertions.assertEquals(99, authData.getNote().length());

        authData.setNote(stringOfLength(1));
        Assertions.assertEquals(1, authData.getNote().length());
    }

}
