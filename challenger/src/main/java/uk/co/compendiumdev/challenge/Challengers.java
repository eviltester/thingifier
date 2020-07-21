package uk.co.compendiumdev.challenge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Challengers {

    private boolean singlePlayerMode;
    Map<String, ChallengerAuthData> authData;
    public final ChallengerAuthData SINGLE_PLAYER = new ChallengerAuthData();
    public final ChallengerAuthData DEFAULT_PLAYER_DATA = new ChallengerAuthData();

    public Challengers(){
        authData = new ConcurrentHashMap<>();
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
                deleteMe.add(data.getXAuthToken());
            }
        }

        for(String deleteKey : deleteMe){
            authData.remove(deleteKey);
        }
    }

    public ChallengerAuthData createNewChallenger() {
        ChallengerAuthData newChallenger = new ChallengerAuthData();
        authData.put(newChallenger.getXChallenger(), newChallenger);
        return newChallenger;
    }
}
