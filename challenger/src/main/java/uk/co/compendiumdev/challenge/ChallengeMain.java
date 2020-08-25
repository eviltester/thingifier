package uk.co.compendiumdev.challenge;

import uk.co.compendiumdev.challenge.apimodel.ChallengeApiModel;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.application.MainImplementation;
import uk.co.compendiumdev.thingifier.application.ThingifierRestServer;

public class ChallengeMain {

    static ChallengeRouteHandler challenger;

    public static void main(String[] args) {

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
            if (arg.startsWith("-multiplayer")) {
                System.out.println("Running in multiplayer mode");
                challenger.setToMultiPlayerMode();
            }

            if (arg.startsWith("-cloudstorage")) {
                System.out.println("Setting persistence mechanism to cloud");
                challenger.setToCloudPersistenceMode();
            }

            if(arg.startsWith("-guikeepalive")){
                System.out.println("Setting GUI to keep session alive through XHR");
                challenger.setGuiToKeepSessionAlive();
            }

            if (arg.startsWith("-memory")) {
                System.out.println("Setting persistence mechanism to no persistence");
                challenger.setToNoPersistenceMode();
            }
        }


        challenger.configureRoutes();

        app.chooseThingifier();
        // can set profile by adding more configs, or just
        // app.setProfileToUse(aProfile)
        app.configureThingifierWithProfile();

        app.setupDefaultGui();
        challenger.setupGui(app.getGuiManagement());

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
