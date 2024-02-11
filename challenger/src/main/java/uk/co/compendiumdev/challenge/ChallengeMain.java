package uk.co.compendiumdev.challenge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.compendiumdev.challenge.apimodel.ChallengeApiModel;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.MainImplementation;
import uk.co.compendiumdev.thingifier.application.ThingifierRestServer;

public class ChallengeMain {

    static ChallengeRouteHandler challenger;

    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(ChallengeMain.class);
        logger.info("Hello World");

        MainImplementation app = new MainImplementation();
        Thingifier thingifier = new ChallengeApiModel().get();
        app.registerModel("challengeapi", thingifier);

        // add any additional thingifier configurations here if more needed than model has defined
        app.setDefaultsFromArgs(args);

        app.configurePortAndDefaultRoutes();
        app.setupBuiltInConfigurableRoutes();

        // setup routes required for challenges
        challenger = new ChallengeRouteHandler(thingifier, app.getApiDefn());

        for (String arg : args) {
            if (arg.toLowerCase().startsWith("-multiplayer")) {
                logger.info("Running in multiplayer mode");
                challenger.setToMultiPlayerMode();
            }

            if (arg.toLowerCase().startsWith("-cloudstorage")) {
                logger.info("Setting persistence mechanism to cloud");
                challenger.setToCloudPersistenceMode();
            }

            if(arg.toLowerCase().startsWith("-guikeepalive")){
                logger.info("Setting GUI to keep session alive through XHR");
                challenger.setGuiToKeepSessionAlive();
            }

            if (arg.toLowerCase().startsWith("-memory")) {
                logger.info("Setting persistence mechanism to no persistence");
                challenger.setToNoPersistenceMode();
            }

            if (arg.toLowerCase().startsWith("-enableadminapi")) {
                logger.info("Enabling Admin Api");
                challenger.enableAdminApi();
            }
        }

        challenger.configureRoutes();

        app.chooseThingifier();
        // can set profile by adding more configs, or just
        // app.setProfileToUse(aProfile)
        app.configureThingifierWithProfile();

        app.setupDefaultGui();
        challenger.setupGui(app.getGuiManagement());



        if(challenger.isSinglePlayerMode()){
            logger.info("Running in Single User Mode");
            challenger.getThingifier().ensureCreatedAndPopulatedInstanceDatabaseNamed(Challengers.SINGLE_PLAYER_GUID);
        }

        final ThingifierRestServer restServer = app.startRestServer();

        app.addBuiltInArgConfiguredHooks();

        challenger.addHooks(restServer);

    }

    public static ChallengeRouteHandler getChallenger(){
        return challenger;
    }

    public static void stop(){
        challenger = null;
    }
}
