package uk.co.compendiumdev.challenge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.compendiumdev.challenge.apimodel.ChallengeApiModel;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.MainImplementation;
import uk.co.compendiumdev.thingifier.application.httprouting.ThingifierHttpApiRoutings;

public class ChallengeMain {

    static ChallengeRouteHandler challenger;

    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(ChallengeMain.class);

        logger.info("Starting Challenger");

        MainImplementation app = new MainImplementation();
        Thingifier thingifier = new ChallengeApiModel().get();
        app.registerModel("challengeapi", thingifier);

        // add any additional thingifier configurations here if more needed than model has defined
        app.setDefaultsFromArgs(args);

        app.configurePortAndDefaultRoutes();
        app.setupBuiltInConfigurableRoutes();

        // create a default configuration class which we pass into the ChallengeRouteHandler and configure via args
        ChallengerConfig config = new ChallengerConfig();

        for (String arg : args) {
            if (arg.toLowerCase().startsWith("-multiplayer") ||
                arg.toLowerCase().startsWith("-multiuser")) {
                logger.info("Running in multiplayer mode");
                config.setToMultiPlayerMode();
            }

            if (arg.toLowerCase().startsWith("-cloudstorage")) {
                logger.info("Setting persistence mechanism to cloud");
                config.setToCloudPersistenceMode();
            }

            if(arg.toLowerCase().startsWith("-guikeepalive")){
                logger.info("Setting GUI to keep session alive through XHR");
                config.setGuiToKeepSessionAlive();
            }

            if (arg.toLowerCase().startsWith("-memory") ||
                arg.toLowerCase().startsWith("-nostorage")
            ) {
                logger.info("Setting persistence mechanism to no persistence");
                config.setToNoPersistenceMode();
            }

            if (arg.toLowerCase().startsWith("-enableadminapi")) {
                logger.info("Enabling Admin Api");
                config.enableAdminApi();
            }

            if(arg.toLowerCase().startsWith("-unlimitedtodos")){
                // remove the limit on number of todos
                logger.info("Enabling Unlimited TODO Instances");
                thingifier.getDefinitionNamed("todo").setNoMaxInstanceLimit();
            }
        }

        thingifier.apiConfig().setApiToAllowRobotsIndexingResponses(false);
        thingifier.apiConfig().setSupportsMultipleDatabases(true);

        // setup routes required for challenges
        challenger = new ChallengeRouteHandler(thingifier, app.getApiDefn(), config);


        app.chooseThingifier();
        // can set profile by adding more configs, or just
        // app.setProfileToUse(aProfile)
        app.configureThingifierWithProfile();

        app.setupDefaultGui();
        app.getGuiManagement().setCanonicalHost("https://apichallenges.eviltester.com");
        app.getGuiManagement().appendToCustomHeadContent(
"""
        <link rel="apple-touch-icon" sizes="180x180" href="/favicon/apple-touch-icon.png">
        <link rel="icon" type="image/png" sizes="32x32" href="/favicon/favicon-32x32.png">
        <link rel="icon" type="image/png" sizes="16x16" href="/favicon/favicon-16x16.png">
        <link rel="manifest" href="/favicon/site.webmanifest">
        <link rel="stylesheet" href="/css/toc.css">
        <link rel="stylesheet" href="/css/content.css">
            """
        );

        challenger.setupGui(app.getGuiManagement());
        challenger.configureRoutes();

        if(challenger.isSinglePlayerMode()){
            logger.info("Running in Single User Mode");
            challenger.getThingifier().ensureCreatedAndPopulatedInstanceDatabaseNamed(Challengers.SINGLE_PLAYER_GUID);
        }

        final ThingifierHttpApiRoutings restServer = app.startRestServer();

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
