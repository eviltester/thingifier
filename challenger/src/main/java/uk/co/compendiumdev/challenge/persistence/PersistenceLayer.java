package uk.co.compendiumdev.challenge.persistence;

import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;

public class PersistenceLayer {

    private StorageType storeOn;

    // todo: add all active storage mechanisms in a list and store on all - switch it off by removing from list
    PersistenceMechanism file = new ChallengerFileStorage();
    static PersistenceMechanism aws;

    public void setToCloud() {
        storeOn = PersistenceLayer.StorageType.CLOUD;
    }

    public void switchOffPersistence() {
        storeOn=StorageType.NONE;
    }

    public ChallengerAuthData tryToLoadChallenger(final Challengers challengers, final String xChallengerGuid) {
        ChallengerAuthData challenger=null;
        challenger = loadChallengerStatus(xChallengerGuid);
        if(challenger!=null){
            challenger.touch(); // refresh last accessed date
            challengers.put(challenger);
        }
        return challenger;
    }

    public enum StorageType{LOCAL, CLOUD, NONE};

    public PersistenceLayer(StorageType storeWhere){
        this.storeOn = storeWhere;
    }

    public void saveChallengerStatus(ChallengerAuthData data){

        if(storeOn==StorageType.NONE){
            return;
        }

        if(storeOn== StorageType.LOCAL){
            file.saveChallengerStatus(data);
        }else{
            if(aws==null){
                aws=new AwsS3Storage();
            }
            aws.saveChallengerStatus(data);
        }
    }

    public ChallengerAuthData loadChallengerStatus(String guid){

        if(storeOn==StorageType.NONE){
            return null;
        }

        if(storeOn== StorageType.LOCAL){
            return file.loadChallengerStatus(guid);
        }else{
            if(aws==null){
                aws=new AwsS3Storage();
            }
            return aws.loadChallengerStatus(guid);
        }
    }
}
