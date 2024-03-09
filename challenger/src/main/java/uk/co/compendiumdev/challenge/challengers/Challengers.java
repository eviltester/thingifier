package uk.co.compendiumdev.challenge.challengers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.ChallengerState;
import uk.co.compendiumdev.challenge.persistence.PersistenceLayer;
import uk.co.compendiumdev.challenge.persistence.PersistenceResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Challengers {

    Logger logger = LoggerFactory.getLogger(Challengers.class);

    private final EntityRelModel erModel;
    private boolean singlePlayerMode;
    Map<String, ChallengerAuthData> authData;
    public ChallengerAuthData SINGLE_PLAYER;
    public static final String SINGLE_PLAYER_GUID="rest-api-challenges-single-player";
    public ChallengerAuthData DEFAULT_PLAYER_DATA;
    PersistenceLayer persistenceLayer;
    private ThingifierApiConfig apiConfig;
    private final Collection<CHALLENGE> definedChallenges;

    public Challengers(EntityRelModel erModel, Collection<CHALLENGE> definedChallenges){
        authData = new ConcurrentHashMap<>();
        this.definedChallenges = definedChallenges;
        SINGLE_PLAYER = new ChallengerAuthData(this.definedChallenges);
        SINGLE_PLAYER.setXChallengerGUID(SINGLE_PLAYER_GUID);
        DEFAULT_PLAYER_DATA = new ChallengerAuthData(this.definedChallenges);
        this.singlePlayerMode=true;
        this.erModel = erModel;
    }

    public Collection<CHALLENGE> getDefinedChallenges() {
        return definedChallenges;
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

    public boolean inMemory(final String challengerGuid){
        if(challengerGuid == null || challengerGuid.trim().isEmpty()) {
            return false;
        }

        ChallengerAuthData challenger;
        if(singlePlayerMode){
            challenger = SINGLE_PLAYER;
        }else{
            challenger = authData.get(challengerGuid);
        }
        return challenger != null;
    }

    public ChallengerAuthData getChallenger(final String challengerGuid) {
        if(singlePlayerMode){
            return SINGLE_PLAYER;
        }

        if(challengerGuid == null || challengerGuid.trim().isEmpty()) {
            return null;
        }

        ChallengerAuthData challenger = authData.get(challengerGuid);

//        if(challengerGuid.equals(SINGLE_PLAYER_GUID)){
//            return SINGLE_PLAYER;
//        }

        if(challenger==null){
            // we don't have challenger in memory, are they available in persistent store?
            if (persistenceLayer != null) {
                final PersistenceResponse response =
                        persistenceLayer.tryToLoadChallenger(this, challengerGuid.trim());
                if(response.isSuccess()){
                    if(authData.containsKey(challengerGuid)) {
                        challenger = authData.get(challengerGuid);
                        challenger.setState(ChallengerState.LOADED_FROM_PERSISTENCE);
                    }
                }
                // todo: surface persistence errors i.e. response.getErrorMessage()
            }
        }

        return challenger;
    }

    public EntityRelModel getErModel(){
        return this.erModel;
    }

    public void purgeOldAuthData() {

        if(singlePlayerMode){
            return;
        }

        List<String> deleteMe = new ArrayList<>();
        final long cutOffTime = System.currentTimeMillis();
        for(ChallengerAuthData data : authData.values()){
            if(data.expiresAt() < cutOffTime ){
                logger.warn("PURGING AUTH: {}", data.getXChallenger());
                deleteMe.add(data.getXChallenger());
            }else{
                logger.info("PURGE: {} expires in {}", data.getXChallenger(), cutOffTime - data.expiresAt());
            }
        }

        for(String deleteKey : deleteMe){
            delete(deleteKey);
            if(erModel!=null){
                if(erModel.getDatabaseNames().contains(deleteKey)){
                    logger.warn("DELETING DATABASE: {}", deleteKey);
                    erModel.deleteInstanceDatabase(deleteKey);
                }
            }
        }
        logger.info("CURRENT Challenger count: {}",authData.values().size());
        if(erModel!=null){
            logger.info("CURRENT database count: {}",erModel.getDatabaseNames().size());
        }
    }

    public ChallengerAuthData createNewChallenger() {
        ChallengerAuthData newChallenger = new ChallengerAuthData(definedChallenges);
        newChallenger.setState(ChallengerState.NEW);
        put(newChallenger);
        return newChallenger;
    }

    public void put(final ChallengerAuthData challenger) {
        // todo: this should really check for single player mode and not just trust the GUID
        if(challenger.getXChallenger().contentEquals(SINGLE_PLAYER_GUID)){
            SINGLE_PLAYER = challenger; // we just loaded the single player session
        }else {
            authData.put(challenger.getXChallenger(), challenger);
        }
    }

    public void persistChallengerState(final ChallengerAuthData challenger){
        if (persistenceLayer != null) {
           String databaseName = challenger.getXChallenger();
            persistenceLayer.saveChallengerStatus(challenger, erModel.getInstanceData(databaseName));
        }
    }

    public void pass(final ChallengerAuthData challenger, final CHALLENGE challengeId) {
        if(challenger!=null) {
            // todo: possibly only update challenge status if not already set
            //if(!challenger.statusOfChallenge(challengeId)) {
                challenger.pass(challengeId);
                persistChallengerState(challenger);
            //}
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

    public void delete(String xChallenger) {
        authData.remove(xChallenger);
    }

    public Set<String> getChallengerGuids() {
        return authData.keySet();
    }
}
