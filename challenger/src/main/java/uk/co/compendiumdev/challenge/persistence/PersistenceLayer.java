package uk.co.compendiumdev.challenge.persistence;

import uk.co.compendiumdev.challenge.ChallengerAuthData;

public class PersistenceLayer {

    private final StorageType storeOn;
    PersistenceMechanism file = new ChallengerFileStorage();

    public enum StorageType{LOCAL, CLOUD};

    public PersistenceLayer(StorageType storeWhere){
        this.storeOn = storeWhere;
    }

    public void saveChallengerStatus(ChallengerAuthData data){

        if(storeOn== StorageType.LOCAL){
            file.saveChallengerStatus(data);
        }else{
            System.out.println("Not implemented cloud storage");
        }
    }

    public ChallengerAuthData loadChallengerStatus(String guid){
        if(storeOn== StorageType.LOCAL){
            return file.loadChallengerStatus(guid);
        }else{
            System.out.println("Not implemented cloud storage");
            return null;
        }
    }
}
