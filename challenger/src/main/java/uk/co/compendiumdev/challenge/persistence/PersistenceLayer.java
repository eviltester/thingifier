package uk.co.compendiumdev.challenge.persistence;

import uk.co.compendiumdev.challenge.ChallengerAuthData;

public class PersistenceLayer {

    private StorageType storeOn;
    PersistenceMechanism file = new ChallengerFileStorage();
    static PersistenceMechanism aws;

    public void setToCloud() {
        storeOn = PersistenceLayer.StorageType.CLOUD;
    }

    public enum StorageType{LOCAL, CLOUD};

    public PersistenceLayer(StorageType storeWhere){
        this.storeOn = storeWhere;
    }

    public void saveChallengerStatus(ChallengerAuthData data){

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
