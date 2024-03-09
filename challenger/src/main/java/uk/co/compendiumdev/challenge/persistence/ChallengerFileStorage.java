package uk.co.compendiumdev.challenge.persistence;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.thingifier.core.domain.instances.ERInstanceData;

import java.io.*;
import java.nio.file.Files;

public class ChallengerFileStorage implements ChallengerPersistenceMechanism, DatabaseContentPersistenceMechanism {

    Logger logger = LoggerFactory.getLogger(ChallengerFileStorage.class);

    public PersistenceResponse saveChallengerStatus(final ChallengerAuthData data) {

        File folder = new File(System.getProperty("User.dir"), "challengersessions");
        folder.mkdirs();

        File file = new File(folder , getFileNameFor(data.getXChallenger()));

        if(!file.exists()){
            logger.warn("Creating new challenger status file: {}", file.getAbsolutePath());
        }

        try(FileOutputStream out = new FileOutputStream(file)) {
            final String dataString = new Gson().toJson(data);
            out.write(dataString.getBytes());
            return new PersistenceResponse().
                    withSuccess(true);
        } catch (IOException e) {
            logger.error("Error writing to file: {}", file.getAbsolutePath(), e);
            return new PersistenceResponse().
                    withSuccess(false).
                    withErrorMessage(e.getMessage());
        }
    }

    private String getFileNameFor(final String guid) {
        return guid + ".data.txt";
    }

    public PersistenceResponse loadChallengerStatus(final String guid) {
        File folder = new File(System.getProperty("User.dir"), "challengersessions");
        File file = new File(folder , getFileNameFor(guid));

        if(!file.exists()){
            String message = "Could not find challenger status file: " + file.getAbsolutePath();
            if(guid.startsWith("rest-api-challenges-single-player")){
                message = message + "\nChallenger status file will be created when a challenge is completed.";
            }
            logger.warn(message);
            return new PersistenceResponse().
                    withSuccess(false).
                    withErrorMessage(message);
        }

        try {
            final byte[] data = Files.readAllBytes(file.toPath());
            final String dataString = new String(data);
            return new PersistenceResponse().
                    withSuccess(true).
                    withChallengerAuthData(
                            new Gson().fromJson(dataString, ChallengerAuthData.class));
        } catch (IOException e) {
            logger.error("Error Reading Challenge Status From file: {}", file.getAbsolutePath(), e);
            return new PersistenceResponse().
                    withSuccess(false).
                    withErrorMessage(e.getMessage());
        }
    }

    public PersistenceResponse saveDatabaseContent(String guid, ERInstanceData todos) {

        File folder = new File(System.getProperty("User.dir"), "challengersessions");
        folder.mkdirs();

        File file = new File(folder , getDatabaseFileNameFor(guid));

        if(!file.exists()){
            logger.warn("Creating new challenger database file: {}", file.getAbsolutePath());
        }

        try(FileOutputStream out = new FileOutputStream(file)) {
            String dataString = "";
            if(todos!=null){
                dataString=todos.asJson();
            }
            out.write(dataString.getBytes());
            return new PersistenceResponse().
                    withSuccess(true);
        } catch (IOException e) {
            logger.error("Error writing to file: {}", file.getAbsolutePath(), e);
            return new PersistenceResponse().
                    withSuccess(false).
                    withErrorMessage(e.getMessage());
        }
    }

    private String getDatabaseFileNameFor(String guid) {
        return guid + ".content.txt";
    }

    public PersistenceResponse loadDatabaseContent(String guid) {
        File folder = new File(System.getProperty("User.dir"), "challengersessions");
        File file = new File(folder , getDatabaseFileNameFor(guid));

        if(!file.exists()){
            String message = "Could not find database contents file: " + file.getAbsolutePath();
            if(guid.startsWith("rest-api-challenges-single-player")){
                message = message + "\nDatabase content file will be created when a challenge is completed.";
            }
            logger.warn(message);
            return new PersistenceResponse().
                    withSuccess(false).
                    withErrorMessage(message);
        }

        try {
            final byte[] data = Files.readAllBytes(file.toPath());
            final String dataString = new String(data);

            return new PersistenceResponse().
                    withSuccess(true).
                    withDatabaseContents(dataString);
        } catch (IOException e) {
            logger.error("Error Reading Database content From file: {}", file.getAbsolutePath(), e);
            return new PersistenceResponse().
                    withSuccess(false).
                    withErrorMessage(e.getMessage());
        }
    }
}
