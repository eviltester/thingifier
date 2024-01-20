package uk.co.compendiumdev.challenge.challengers;

import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.persistence.PersistenceLayer;
import uk.co.compendiumdev.challenge.persistence.PersistenceResponse;
import uk.co.compendiumdev.thingifier.api.ThingifierApiDefn;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Challengers {

    private boolean singlePlayerMode;
    Map<String, ChallengerAuthData> authData;
    public ChallengerAuthData SINGLE_PLAYER;
    public static final String SINGLE_PLAYER_GUID="rest-api-challenges-single-player";
    public final ChallengerAuthData DEFAULT_PLAYER_DATA = new ChallengerAuthData();
    PersistenceLayer persistenceLayer;
    private ThingifierApiConfig apiConfig;

    public Challengers(){
        authData = new ConcurrentHashMap<>();
        SINGLE_PLAYER = new ChallengerAuthData();
        SINGLE_PLAYER.setXChallengerGUID(SINGLE_PLAYER_GUID);
        this.singlePlayerMode=true;
    }

    public void setMultiPlayerMode(){
        singlePlayerMode=false;
    }

    public boolean isMultiPlayerMode(){
        return !singlePlayerMode;
    }

    public boolean isSinglePlayerMode(){
        return singlePlayerMode;
    }

    public ChallengerAuthData getChallenger(final String challengerGuid) {
        if(singlePlayerMode){
            return SINGLE_PLAYER;
        }

        if(challengerGuid == null || challengerGuid.trim().length()==0) {
            return null;
        }

        ChallengerAuthData challenger = authData.get(challengerGuid);

        if(challenger==null){
            // we don't have challenger in memory, are they available in persistent store?
            if (persistenceLayer != null) {
                final PersistenceResponse response =
                        persistenceLayer.tryToLoadChallenger(this, challengerGuid.trim());
                if(response.isSuccess()){
                    challenger = authData.get(challengerGuid);
                }
                // todo: surface persistence errors i.e. response.getErrorMessage()

            }
        }

        return challenger;
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

    public void setApiConfig(ThingifierApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    public ThingifierApiConfig getApiConfig() {
        return this.apiConfig;
    }
}
