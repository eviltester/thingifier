package uk.co.compendiumdev.challenge.persistence;

import com.google.gson.Gson;
import uk.co.compendiumdev.challenge.ChallengerAuthData;

import java.io.*;
import java.nio.file.Files;

public class ChallengerFileStorage implements PersistenceMechanism {

    @Override
    public PersistenceResponse saveChallengerStatus(final ChallengerAuthData data) {

        File file = new File(System.getProperty("User.dir") , getFileNameFor(data.getXChallenger()));

        if(!file.exists()){
            String message = "Creating new challenger status file: " + file.getAbsolutePath();
            System.out.println(message);
        }

        try(FileOutputStream out = new FileOutputStream(file)) {
            final String dataString = new Gson().toJson(data);
            out.write(dataString.getBytes());
            return new PersistenceResponse().
                    withSuccess(true);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error writing to file: " + file.getAbsolutePath());
            return new PersistenceResponse().
                    withSuccess(false).
                    withErrorMessage(e.getMessage());
        }
    }

    private String getFileNameFor(final String guid) {
        return guid + ".data.txt";
    }

    @Override
    public PersistenceResponse loadChallengerStatus(final String guid) {
        File file = new File(System.getProperty("User.dir") , getFileNameFor(guid));

        if(!file.exists()){
            String message = "Could not find challenger status file: " + file.getAbsolutePath();
            if(guid.startsWith("rest-api-challenges-single-player")){
                message = message + "\nChallenger status file will be created when a challenge is completed.";
            }
            System.out.println(message);
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
            e.getMessage();
            System.out.println("Error Reading Challenge Status From file: " + file.getAbsolutePath());
            return new PersistenceResponse().
                    withSuccess(false).
                    withErrorMessage(e.getMessage());
        }
    }
}
