package uk.co.compendiumdev.sparkstart;


import spark.Spark;
import uk.co.compendiumdev.challenge.ChallengeMain;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Environment {

    public static boolean SINGLE_PLAYER_MODE = false;

    public static String getEnv(String urlPath){
        return  getBaseUri() + urlPath;
    }

    public static String getBaseUri() {

        // return environment if want to run externally
//        if(true)
//            return "https://apichallenges.herokuapp.com";


        // if not running then start the spark
        if(Port.inUse("localhost", 4567)) {
            return "http://localhost:4567";
        }else{
            //start it up
            Spark.port(4567);
            String [] args;

            if(SINGLE_PLAYER_MODE){
                args = "".split(",");
            }else{
                args = "-multiplayer".split(",");
            };


            ChallengeMain.main(args);

            // wait till running
            int maxtries=10;
            while(!Port.inUse("localhost", 4567)){
                maxtries--;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return "http://localhost:4567";
        }

        // TODO: incorporate browsermob proxy and allow configuration of all
        //  requests through a proxy file to output a HAR file of all requests for later review
    }
}
