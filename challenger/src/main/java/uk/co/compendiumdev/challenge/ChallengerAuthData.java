package uk.co.compendiumdev.challenge;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChallengerAuthData {

    private String xAuthToken;
    private final long extratime;
    private String xChallenger;
    private long lastAccessed;
    private long expiresin;
    private String secretNote;
    private Map<CHALLENGE, Boolean> challengeStatus;

    private ChallengerState state;

    public ChallengerAuthData(){
        this.xChallenger = UUID.randomUUID().toString();
        this.xAuthToken = UUID.randomUUID().toString();
        this.expiresin = 600000; // 10 * 60 * 1000; // 10 minutes
        this.extratime = 30000; // 30 * 1000 - extra time on each request
        touch();
        this.secretNote = "";
        resetChallengesStatus();
        this.state = ChallengerState.NEW;
    }

    private void resetChallengesStatus() {
        challengeStatus = new HashMap<>();
        for(CHALLENGE challenge : CHALLENGE.values()){
            challengeStatus.put(challenge, false);
        }
    }

    public String getXAuthToken() {
        touch();
        return xAuthToken;
    }

    public String getNote() {
        touch();
        return secretNote;
    }

    public void setNote(final String note) {
        touch();
        int maxLen = 100;
        if(note == null){
            throw new RuntimeException("No Note Provided");
        }
        if(note.length()<=maxLen){
            secretNote=note;
        }else {
            secretNote = note.substring(0, maxLen);
        }
    }

    public long getLastAccessed() {
        return this.lastAccessed;
    }

    public long expiresAt() {
        return this.lastAccessed + expiresin;
    }

    public void touch() {
        lastAccessed = System.currentTimeMillis();
        expiresin = expiresin + extratime;
        if(expiresin > 600000){
            expiresin = 600000;
        }
    }

    public String getXChallenger() {
        return xChallenger;
    }

    public Boolean statusOfChallenge(final CHALLENGE challenge) {
        Boolean status = challengeStatus.get(challenge);
        if(status==null){
            return false;
        }
        return challengeStatus.get(challenge);
    }

    public void pass(final CHALLENGE id) {
        challengeStatus.put(id, true);
    }

    public void setXChallengerGUID(final String guid) {
        this.xChallenger = guid;
    }

    public void setState(ChallengerState challengerState) {
        this.state = challengerState;
    }

    public ChallengerAuthData fromData(ChallengerAuthData data){
        // set from data but do not fully trust data
        setNote(data.secretNote);

        try {
            setXChallengerGUID(UUID.fromString(data.getXChallenger()).toString());
        }catch(Exception e){
            // could not convert to GUID so use the existing
        }

        try {
            xAuthToken = UUID.fromString(data.getXAuthToken()).toString();;
        }catch(Exception e){
            // could not convert to GUID so use the existing
        }

        state = data.getState();

        resetChallengesStatus();
        for(CHALLENGE challenge : CHALLENGE.values()){
            challengeStatus.put(challenge, data.statusOfChallenge(challenge));
        }

        return this;
    }

    public ChallengerState getState() {
        return this.state;
    }
}
