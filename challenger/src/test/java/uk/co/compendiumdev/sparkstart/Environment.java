package uk.co.compendiumdev.sparkstart;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;
import uk.co.compendiumdev.challenge.ChallengeMain;
import uk.co.compendiumdev.challenge.ChallengerAuthData;

import java.util.ArrayList;
import java.util.List;

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
        return Environment.getBaseUri(isSinglePlayerMode, false);
    }

    public static String getBaseUri(boolean isSinglePlayerMode, boolean writeFiles) {

        // return environment if want to run externally
//        if(true)
//            return "https://apichallenges.eviltester.com";

        logger.info("Checking app running");
        // if not running then start the spark
        if(ChallengeMain.getChallenger()==null || !Port.inUse("localhost", 4567)) {

            logger.info("App not running starting with single player mode == " + isSinglePlayerMode);
            //start it up
            Spark.port(4567);
            List<String> args = new ArrayList<>();

            if (isSinglePlayerMode) {
                // setup an single player mode args here
            } else {
                // setup an multi player mode args here
                args.add("-multiplayer");
            }

            if(!writeFiles){
               args.add("-nostorage");
            }

            ChallengeMain.main(args.toArray(new String[0]));
            waitTillRunningStatus(true);

            logger.info("App running started with single player mode == " + ChallengeMain.getChallenger().isSinglePlayerMode());
        }

        return "http://localhost:4567";

        // TODO: incorporate browsermob proxy and allow configuration of all
        //  requests through a proxy file to output a HAR file of all requests for later review
    }

    public static void waitTillRunningStatus(final boolean running) {
        // wait till running
        int maxtries = 10;
        logger.info("Waiting for server to be in state " + running);
        while (Port.inUse("localhost", 4567) != running) {
            maxtries--;
            try {
                logger.info(String.format("Waiting for server %d", maxtries));
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.error("Interruption during running check ",e);
            }
            if(maxtries<=0){
                logger.info("Max retries in running status finished.");
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
