package uk.co.compendiumdev.challenge;

import uk.co.compendiumdev.challenge.challengehooks.*;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitions;
import uk.co.compendiumdev.challenge.challengesrouting.AuthRoutes;
import uk.co.compendiumdev.challenge.challengesrouting.ChallengerTrackingRoutes;
import uk.co.compendiumdev.challenge.challengesrouting.ChallengesRoutes;
import uk.co.compendiumdev.challenge.challengesrouting.HeartBeatRoutes;
import uk.co.compendiumdev.challenge.gui.ChallengerWebGUI;
import uk.co.compendiumdev.challenge.persistence.PersistenceLayer;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ThingifierApiDefn;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.application.ThingifierRestServer;
import uk.co.compendiumdev.thingifier.htmlgui.DefaultGUIHTML;

import java.util.*;


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

    public ChallengeRouteHandler(Thingifier thingifier, ThingifierApiDefn apiDefn){

        this.apiDefn = apiDefn;
        apiDefn.setThingifier(thingifier);

        apiDefn.addServer("https://apichallenges.herokuapp.com", "heroku hosted version");
        apiDefn.addServer("http://localhost:4567", "local execution");
        apiDefn.setVersion("1.0.0");

        challengeDefinitions = new ChallengeDefinitions();
        this.thingifier = thingifier;
        challengers = new Challengers();
        single_player_mode = true;
        persistenceLayer = new PersistenceLayer(PersistenceLayer.StorageType.LOCAL);
        challengers.setPersistenceLayer(persistenceLayer);
        persistenceLayer.tryToLoadChallenger(challengers, challengers.SINGLE_PLAYER_GUID);
    }

    public void setGuiToKeepSessionAlive(){
        guiStayAlive=true;
    }

    public void setToMultiPlayerMode(){
        single_player_mode = false;
        challengers.setMultiPlayerMode();
    }

    public void setToCloudPersistenceMode(){
        persistenceLayer.setToCloud();
    }

    public void setToNoPersistenceMode() {
        persistenceLayer.switchOffPersistence();
    }


    public List<RoutingDefinition> getRoutes(){
        return apiDefn.getAdditionalRoutes();
    }

    public ChallengeRouteHandler configureRoutes() {

        new ChallengerTrackingRoutes().configure(challengers, single_player_mode, apiDefn, persistenceLayer);
        new ChallengesRoutes().configure(challengers, single_player_mode, apiDefn, challengeDefinitions);
        new HeartBeatRoutes().configure(apiDefn);
        new AuthRoutes().configure(challengers, apiDefn);

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
}
