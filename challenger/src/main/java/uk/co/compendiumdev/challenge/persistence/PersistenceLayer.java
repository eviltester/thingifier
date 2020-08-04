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

    public PersistenceResponse tryToLoadChallenger(final Challengers challengers,
                                                  final String xChallengerGuid) {

        final PersistenceResponse response = loadChallengerStatus(xChallengerGuid);

        if(response.isSuccess()){
            ChallengerAuthData challenger = response.getAuthData();
            challenger.touch(); // refresh last accessed date
            challengers.put(challenger);
        }

        return response;
    }

    public enum StorageType{LOCAL, CLOUD, NONE};

    public PersistenceLayer(StorageType storeWhere){
        this.storeOn = storeWhere;
    }

    public PersistenceResponse saveChallengerStatus(ChallengerAuthData data){

        if(storeOn==StorageType.NONE){
            return new PersistenceResponse().withSuccess(true);
        }

        if(storeOn== StorageType.LOCAL){
            return file.saveChallengerStatus(data);
        }else{
            if(aws==null){
                aws=new AwsS3Storage();
            }
            return aws.saveChallengerStatus(data);
        }
    }

    public PersistenceResponse loadChallengerStatus(String guid){

        if(storeOn==StorageType.NONE){
            return new PersistenceResponse().withSuccess(true);
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
