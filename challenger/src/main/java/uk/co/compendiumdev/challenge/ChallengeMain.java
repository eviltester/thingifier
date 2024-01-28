package uk.co.compendiumdev.challenge;

import uk.co.compendiumdev.challenge.apimodel.ChallengeApiModel;
import uk.co.compendiumdev.challenge.challengers.Challengers;
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
            if (arg.toLowerCase().startsWith("-multiplayer")) {
                System.out.println("Running in multiplayer mode");
                challenger.setToMultiPlayerMode();
            }

            if (arg.toLowerCase().startsWith("-cloudstorage")) {
                System.out.println("Setting persistence mechanism to cloud");
                challenger.setToCloudPersistenceMode();
            }

            if(arg.toLowerCase().startsWith("-guikeepalive")){
                System.out.println("Setting GUI to keep session alive through XHR");
                challenger.setGuiToKeepSessionAlive();
            }

            if (arg.toLowerCase().startsWith("-memory")) {
                System.out.println("Setting persistence mechanism to no persistence");
                challenger.setToNoPersistenceMode();
            }

            if (arg.toLowerCase().startsWith("-enableadminapi")) {
                System.out.println("Enabling Admin Api");
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
            System.out.println("Running in Single User Mode");
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
