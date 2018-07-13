package uk.co.compendiumdev.thingifier.generic;

import java.util.UUID;

public class GUID {
    public static String create() {
        //String alphabet = "abcdefghijklmnopqrstuvwxyz";
        //String prefix = "";
        String guid = "";

        //guid = prefix + System.currentTimeMillis() + System.nanoTime();

        guid = UUID.randomUUID().toString();

        return guid;
    }
}
