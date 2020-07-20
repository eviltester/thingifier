package uk.co.compendiumdev.challenge;

import java.util.UUID;

public class ChallengeAuthData {

    private final String guid;
    private long lastAccessed;
    private String secretNote;

    public ChallengeAuthData(){
        this.guid = UUID.randomUUID().toString();
        touch();
        this.secretNote = "";
    }

    public String getGuid() {
        touch();
        return guid;
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
            secretNote = note.substring(0, maxLen-1);
        }
    }

    public long getLastAccessed() {
        return this.lastAccessed;
    }

    public void touch() {
        lastAccessed = System.currentTimeMillis();
    }
}
