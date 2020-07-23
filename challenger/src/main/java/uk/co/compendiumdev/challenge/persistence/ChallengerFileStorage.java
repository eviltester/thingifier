package uk.co.compendiumdev.challenge.persistence;

import com.google.gson.Gson;
import uk.co.compendiumdev.challenge.ChallengerAuthData;

import java.io.*;
import java.nio.file.Files;

public class ChallengerFileStorage implements PersistenceMechanism {

    @Override
    public void saveChallengerStatus(final ChallengerAuthData data) {

        File file = new File(System.getProperty("User.dir") , getFileNameFor(data.getXChallenger()));
        try(FileOutputStream out = new FileOutputStream(file)) {
            final String dataString = new Gson().toJson(data);
            out.write(dataString.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error writing to file: " + file.getAbsolutePath());
        }
    }

    private String getFileNameFor(final String guid) {
        return guid + ".data.txt";
    }

    @Override
    public ChallengerAuthData loadChallengerStatus(final String guid) {
        File file = new File(System.getProperty("User.dir") , getFileNameFor(guid));
        try {
            final byte[] data = Files.readAllBytes(file.toPath());
            final String dataString = new String(data);
            return new Gson().fromJson(dataString, ChallengerAuthData.class);
        } catch (IOException e) {
            e.getMessage();
            System.out.println("Error Reading Challenge Status From file: " + file.getAbsolutePath());
            return null;
        }
    }
}
