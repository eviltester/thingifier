package uk.co.compendiumdev.challenge;

import uk.co.compendiumdev.challenge.challengehooks.*;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.challenge.challenges.ChallengeDefinitions;
import uk.co.compendiumdev.challenge.challengesrouting.*;
import uk.co.compendiumdev.challenge.gui.ChallengerWebGUI;
import uk.co.compendiumdev.challenge.persistence.PersistenceLayer;
import uk.co.compendiumdev.challenge.practicemodes.mirror.MirrorRoutes;
import uk.co.compendiumdev.challenge.practicemodes.simpleapi.SimpleApiRoutes;
import uk.co.compendiumdev.challenge.practicemodes.simulation.SimulationRoutes;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.application.httprouting.ThingifierHttpApiRoutings;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;


public class ChallengeRouteHandler {
    private final Thingifier thingifier;
    //List<RoutingDefinition> routes;

    ThingifierApiDocumentationDefn apiChallengesDocumentationDefn;
    ThingifierApiDocumentationDefn mirrorModeDocumentationDefn;

    ChallengeDefinitions challengeDefinitions;
    Challengers challengers;
    private boolean single_player_mode;
    PersistenceLayer persistenceLayer;
    private boolean guiStayAlive=false; // when set gui makes a call every 5 mins to keep session alive,
    private DefaultGUIHTML guiTemplates;
    // not needed when storing data

    public ChallengeRouteHandler(Thingifier thingifier, ThingifierApiDocumentationDefn apiDefn, ChallengerConfig config){

        this.apiChallengesDocumentationDefn = apiDefn;
        apiDefn.setThingifier(thingifier);

        this.mirrorModeDocumentationDefn = new ThingifierApiDocumentationDefn();
        mirrorModeDocumentationDefn.setTitle("Mirror Mode");
        mirrorModeDocumentationDefn.setDescription("Mirror HTTP Requests");

        apiDefn.addServer("https://apichallenges.eviltester.com", "heroku hosted version");
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

        if(single_player_mode) {
            // auto load any single player challenger details in single player mode
            persistenceLayer.tryToLoadChallenger(challengers, challengers.SINGLE_PLAYER_GUID);
        }

        challengers.setApiConfig(thingifier.apiConfig());

        if(config.isAdminApiEnabled){
            enableAdminApi();
        }

        this.guiTemplates = new DefaultGUIHTML();


    }

    public boolean isSinglePlayerMode(){
        return single_player_mode;
    }

    public ChallengeRouteHandler configureRoutes() {

        new ChallengerTrackingRoutes().configure(challengers, single_player_mode, apiChallengesDocumentationDefn, persistenceLayer, thingifier, challengeDefinitions);
        new ChallengesRoutes().configure(challengers, single_player_mode, apiChallengesDocumentationDefn, challengeDefinitions);
        new HeartBeatRoutes().configure(apiChallengesDocumentationDefn);
        new AuthRoutes().configure(challengers, apiChallengesDocumentationDefn);

        // Mirror routes should not show up in the apichallenges apiDefn
        new MirrorRoutes().configure(mirrorModeDocumentationDefn, guiTemplates);

        // Simulation routes should not show
        new SimulationRoutes(guiTemplates).configure();

        new SimpleApiRoutes(guiTemplates).configure();

        return this;
    }

    public void addHooks(final ThingifierHttpApiRoutings restServer) {

        // TODO: this is wrong - rethink this - we need SparkLevel, InternalHttp level (pre-post
        // these hooks are registered at a spark before and after level so run on every request,
        // regardless of thingifier used - this is wrong
        restServer.registerInternalHttpResponseHook(new ChallengerInternalHTTPResponseHook(challengers));
        restServer.registerInternalHttpRequestHook(new ChallengerInternalHTTPRequestHook(challengers));

        // add hooks at the API bridge pre and post level so they only work in the specific thingifier
        restServer.registerHttpApiRequestHook(new ChallengerApiRequestHook(challengers));
        restServer.registerHttpApiResponseHook(new ChallengerApiResponseHook(challengers, thingifier));
    }

    public void setupGui(DefaultGUIHTML guiManagement) {
        this.guiTemplates = guiManagement;
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
