package uk.co.compendiumdev.challenge.challengers;

import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.persistence.PersistenceLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Challengers {

    private boolean singlePlayerMode;
    Map<String, ChallengerAuthData> authData;
    public ChallengerAuthData SINGLE_PLAYER;
    public final String SINGLE_PLAYER_GUID="rest-api-challenges-single-player";
    public final ChallengerAuthData DEFAULT_PLAYER_DATA = new ChallengerAuthData();
    PersistenceLayer persistenceLayer;

    public Challengers(){
        authData = new ConcurrentHashMap<>();
        SINGLE_PLAYER = new ChallengerAuthData();
        SINGLE_PLAYER.setGUID(SINGLE_PLAYER_GUID);
        this.singlePlayerMode=true;
    }

    public void setMultiPlayerMode(){
        singlePlayerMode=false;
    }

    public ChallengerAuthData getChallenger(final String challengerGuid) {
        if(singlePlayerMode){
            return SINGLE_PLAYER;
        }

        if(challengerGuid == null || challengerGuid.trim().length()==0) {
            return null;
        }

        return authData.get(challengerGuid);
    }

    public void purgeOldAuthData() {

        if(singlePlayerMode){
            return;
        }

        List<String> deleteMe = new ArrayList();
        final long cutOffTime = System.currentTimeMillis();
        for(ChallengerAuthData data : authData.values()){
            if(data.expiresAt() < cutOffTime ){
                deleteMe.add(data.getXChallenger());
            }
        }

        for(String deleteKey : deleteMe){
            authData.remove(deleteKey);
        }
    }

    public ChallengerAuthData createNewChallenger() {
        ChallengerAuthData newChallenger = new ChallengerAuthData();
        put(newChallenger);
        return newChallenger;
    }

    public void put(final ChallengerAuthData challenger) {
        if(challenger.getXChallenger().contentEquals(SINGLE_PLAYER_GUID)){
            SINGLE_PLAYER = challenger; // we just loaded the single player session
        }else {
            authData.put(challenger.getXChallenger(), challenger);
        }
    }

    public void pass(final ChallengerAuthData challenger, final CHALLENGE challengeId) {
        if(challenger!=null) {
            challenger.pass(challengeId);
            if (persistenceLayer != null) {
                persistenceLayer.saveChallengerStatus(challenger);
            }
        }
    }

    public void setPersistenceLayer(final PersistenceLayer persistenceLayer) {
        this.persistenceLayer = persistenceLayer;
    }
}
