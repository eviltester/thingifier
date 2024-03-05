package uk.co.compendiumdev.challenge;

import uk.co.compendiumdev.challenge.challengehooks.*;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitions;
import uk.co.compendiumdev.challenge.challengesrouting.*;
import uk.co.compendiumdev.challenge.gui.ChallengerWebGUI;
import uk.co.compendiumdev.challenge.persistence.PersistenceLayer;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ThingifierApiDefn;
import uk.co.compendiumdev.thingifier.application.ThingifierRestServer;
import uk.co.compendiumdev.thingifier.htmlgui.DefaultGUIHTML;


public class ChallengeRouteHandler {
    private final Thingifier thingifier;
    //List<RoutingDefinition> routes;
    ThingifierApiDefn apiDefn;
    ChallengeDefinitions challengeDefinitions;
    Challengers challengers;
    private boolean single_player_mode;
    PersistenceLayer persistenceLayer;
    private boolean guiStayAlive=false; // when set gui makes a call every 5 mins to keep session alive,
                                        // not needed when storing data

    public ChallengeRouteHandler(Thingifier thingifier, ThingifierApiDefn apiDefn, ChallengerConfig config){

        this.apiDefn = apiDefn;
        apiDefn.setThingifier(thingifier);

        apiDefn.addServer("https://apichallenges.herokuapp.com", "heroku hosted version");
        apiDefn.addServer("http://localhost:4567", "local execution");
        apiDefn.setVersion("1.0.0");

        single_player_mode = config.single_player_mode;
        persistenceLayer = config.persistenceLayer;
        guiStayAlive=config.guiStayAlive;

        challengeDefinitions = new ChallengeDefinitions(config);
        this.thingifier = thingifier;
        challengers = new Challengers(thingifier.getERmodel(), challengeDefinitions.getDefinedChallenges());
        challengers.setPersistenceLayer(persistenceLayer);
        if(!single_player_mode){
            challengers.setMultiPlayerMode();
        }

        persistenceLayer.tryToLoadChallenger(challengers, challengers.SINGLE_PLAYER_GUID);

        challengers.setApiConfig(thingifier.apiConfig());

        if(config.isAdminApiEnabled){
            enableAdminApi();
        }


    }

    public boolean isSinglePlayerMode(){
        return single_player_mode;
    }

    public ChallengeRouteHandler configureRoutes() {

        new ChallengerTrackingRoutes().configure(challengers, single_player_mode, apiDefn, persistenceLayer, thingifier, challengeDefinitions);
        new ChallengesRoutes().configure(challengers, single_player_mode, apiDefn, challengeDefinitions);
        new HeartBeatRoutes().configure(apiDefn);
        new AuthRoutes().configure(challengers, apiDefn);
        new MirrorRoutes().configure(apiDefn);
        new SimulationRoutes().configure(apiDefn);

        return this;
    }

    public void addHooks(final ThingifierRestServer restServer) {

        restServer.registerInternalHttpResponseHook(new ChallengerInternalHTTPResponseHook(challengers));
        restServer.registerInternalHttpRequestHook(new ChallengerInternalHTTPRequestHook(challengers));
        restServer.registerHttpApiRequestHook(new ChallengerApiRequestHook(challengers));
        restServer.registerHttpApiResponseHook(new ChallengerApiResponseHook(challengers, thingifier));
    }

    public void setupGui(DefaultGUIHTML guiManagement) {
        new ChallengerWebGUI(guiManagement, guiStayAlive).setup(challengers, challengeDefinitions,
                                                persistenceLayer, single_player_mode);
    }

    public Challengers getChallengers(){
        return challengers;
    }

    public Thingifier getThingifier() {
        return thingifier;
    }

    private void enableAdminApi() {
        thingifier.apiConfig().adminConfig().enableAdminSearch();
        thingifier.apiConfig().adminConfig().enableAdminDataClear();
    }
}
