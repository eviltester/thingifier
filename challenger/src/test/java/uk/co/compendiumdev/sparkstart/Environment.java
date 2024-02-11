package uk.co.compendiumdev.sparkstart;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;
import uk.co.compendiumdev.challenge.ChallengeMain;
import uk.co.compendiumdev.challenge.ChallengerAuthData;

public class Environment {

    static Logger logger = LoggerFactory.getLogger(Port.class);

    // these unit tests only work in multi-player mode
    public static boolean SINGLE_PLAYER_MODE = false;

    public static String getEnv(String urlPath){
        return  getBaseUri() + urlPath;
    }

    public static String getBaseUri() {
        return Environment.getBaseUri(SINGLE_PLAYER_MODE);
    }

    public static String getBaseUri(boolean isSinglePlayerMode) {

        // return environment if want to run externally
//        if(true)
//            return "https://apichallenges.herokuapp.com";


        // if not running then start the spark
        if(ChallengeMain.getChallenger()==null || !Port.inUse("localhost", 4567)) {
            //start it up
            Spark.port(4567);
            String[] args;

            if (isSinglePlayerMode) {
                args = "".split(",");
            } else {
                args = "-multiplayer".split(",");
            }


            ChallengeMain.main(args);
            waitTillRunningStatus(true);
        }

        return "http://localhost:4567";

        // TODO: incorporate browsermob proxy and allow configuration of all
        //  requests through a proxy file to output a HAR file of all requests for later review
    }

    public static void waitTillRunningStatus(final boolean running) {
        // wait till running
        int maxtries = 10;
        while (Port.inUse("localhost", 4567) != running) {
            maxtries--;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.error("Interruption during running check ",e);
            }
            if(maxtries<=0){
                return;
            }
        }
    }

    public static void stop(){
        Spark.stop();
        Spark.awaitStop();
        ChallengeMain.stop();
        waitTillRunningStatus(false);
    }
}
