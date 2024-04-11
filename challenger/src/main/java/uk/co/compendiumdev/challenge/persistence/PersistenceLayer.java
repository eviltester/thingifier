package uk.co.compendiumdev.challenge.persistence;

import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.ChallengerState;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonPopulator;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;

public class PersistenceLayer {

    private StorageType storeOn;

    // TODO: have a database persistence layer e.g. 'save to disk' option for the todos
    // TODO: single player mode should have this switched on by default
    // TODO: allow configuring what is on and what is off for any storage type using constructor rather than environment variables
    // todo: add all active storage mechanisms in a list and store on all - switch it off by removing from list

    ChallengerPersistenceMechanism file = new ChallengerFileStorage();
    DatabaseContentPersistenceMechanism dbfile = (DatabaseContentPersistenceMechanism)file;

    static ChallengerPersistenceMechanism aws;
    boolean allowSaveToS3 = false;
    boolean allowLoadFromS3 = false;

    public PersistenceResponse tryToLoadChallenger(final Challengers challengers,
                                                  final String xChallengerGuid) {

        final PersistenceResponse response = loadChallengerStatus(xChallengerGuid);

        if(response.isSuccess()){
            ChallengerAuthData challenger = new ChallengerAuthData(challengers.getDefinedChallenges()).fromData(response.getAuthData(), challengers.getDefinedChallenges());
            if(xChallengerGuid.equals(Challengers.SINGLE_PLAYER_GUID)){
                challenger.setXChallengerGUID(xChallengerGuid);
            }
            challenger.touch();
            challenger.setState(ChallengerState.LOADED_FROM_PERSISTENCE);// refresh last accessed date
            challengers.put(challenger);

            String databaseName = challenger.getXChallenger();
            challengers.getErModel().createInstanceDatabaseIfNotExisting(databaseName);

            // did we also load the data? if so, populate the database from it
            if(!response.getDatabaseContents().isEmpty()){
                new JsonPopulator(response.getDatabaseContents()).populate(
                        challengers.getErModel().getSchema(),
                        challengers.getErModel().getInstanceData(databaseName)
                );
            }else{
                // set the database to default values
                challengers.getErModel().populateDatabase(databaseName);
            }
        }

        return response;
    }

    public enum StorageType{LOCAL, CLOUD, NONE};

    public PersistenceLayer(StorageType storeWhere){
        this.storeOn = storeWhere;

        if(this.storeOn==StorageType.CLOUD){

            String allow_save = System.getenv("AWS_ALLOW_SAVE");
            if(allow_save!=null && allow_save.toLowerCase().trim().equals("true")){
                allowSaveToS3=false;
            }

            String allow_load = System.getenv("AWS_ALLOW_LOAD");
            if(allow_load!=null && allow_load.toLowerCase().trim().equals("true")){
                allowLoadFromS3=false;
            }

            String bucketName = System.getenv("AWSBUCKET");
            aws= new AwsS3Storage(allowSaveToS3, allowLoadFromS3, bucketName);
        }
    }

    public PersistenceResponse saveChallengerStatus(ChallengerAuthData data, ERInstanceData instanceData){

        if(storeOn== StorageType.LOCAL){
            PersistenceResponse fileStoreChallenger = file.saveChallengerStatus(data);
            PersistenceResponse fileStoreDatabase = dbfile.saveDatabaseContent(data.getXChallenger(), instanceData);
            return new PersistenceResponse().
                    withSuccess(fileStoreChallenger.isSuccess() && fileStoreDatabase.isSuccess()).
                    withErrorMessage(fileStoreChallenger.getErrorMessage() + fileStoreDatabase.getErrorMessage()).
                    withDatabaseContents(fileStoreDatabase.getDatabaseContents()).
                    withChallengerAuthData(fileStoreChallenger.getAuthData());
        }

        if(storeOn==StorageType.CLOUD && aws!=null){
            return aws.saveChallengerStatus(data);
        }

        //if(storeOn==StorageType.NONE){
        return new PersistenceResponse().withSuccess(false).withErrorMessage("No Persistence Configured - store in memory only.");
        //}
    }

    public PersistenceResponse loadChallengerStatus(String guid){

        if(storeOn== StorageType.LOCAL){
            PersistenceResponse fileStoreChallenger = file.loadChallengerStatus(guid);
            PersistenceResponse fileStoreDatabase = dbfile.loadDatabaseContent(guid);
            return new PersistenceResponse().
                    withSuccess(fileStoreChallenger.isSuccess()). // only track challenger success && fileStoreDatabase.isSuccess()).
                    withErrorMessage(fileStoreChallenger.getErrorMessage() + fileStoreDatabase.getErrorMessage()).
                    withDatabaseContents(fileStoreDatabase.getDatabaseContents()).
                    withChallengerAuthData(fileStoreChallenger.getAuthData());
        }

        if(storeOn==StorageType.CLOUD && aws!=null){
            return aws.loadChallengerStatus(guid);
        }

        //if(storeOn==StorageType.NONE){
        return new PersistenceResponse().withSuccess(false).withErrorMessage("No Persistence Configured - store in memory only.");
        //}
    }

    public boolean willAutoSaveChallengerStatusToPersistenceLayer() {

        if (storeOn == StorageType.LOCAL) {
            return true;
        }

        if (storeOn == StorageType.CLOUD && allowSaveToS3) {
            return true;
        }

        return false;
    }

    public boolean willAutoLoadChallengerStatusFromPersistenceLayer() {

        if (storeOn == StorageType.LOCAL) {
            return true;
        }

        if (storeOn == StorageType.CLOUD && allowLoadFromS3) {
            return true;
        }

        return false;
    }
}
