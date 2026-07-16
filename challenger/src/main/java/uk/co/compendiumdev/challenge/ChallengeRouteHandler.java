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
import uk.co.compendiumdev.thingifier.adapter.httpserver.ThingifierHttpApiRoutings;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;

public class ChallengeRouteHandler {
    private final Thingifier thingifier;
    // List<RoutingDefinition> routes;

    ThingifierApiDocumentationDefn apiChallengesDocumentationDefn;
    ThingifierApiDocumentationDefn mirrorModeDocumentationDefn;

    ChallengeDefinitions challengeDefinitions;
    Challengers challengers;
    private boolean single_player_mode;
    private final ChallengerConfig config;
    PersistenceLayer persistenceLayer;
    // when set gui makes a call every 5 mins to keep session alive
    private boolean guiStayAlive;
    private DefaultGUIHTML guiTemplates;
    private SimulationRoutes simulationRoutes;

    // not needed when storing data

    public ChallengeRouteHandler(
            Thingifier thingifier,
            ThingifierApiDocumentationDefn apiDefn,
            ChallengerConfig config) {

        this.config = config;
        this.apiChallengesDocumentationDefn = apiDefn;
        apiDefn.setThingifier(thingifier);
        apiDefn.setSeoTitle("API Challenges API Documentation | API Challenges");
        apiDefn.setSeoDescription(
                "Explore API Challenges endpoint documentation with request formats, payload examples, and expected responses for practical API testing.");
        apiDefn.setMetaRobots("index,follow");
        apiDefn.setOgType("website");
        apiDefn.setTwitterCard("summary_large_image");

        this.mirrorModeDocumentationDefn = new ThingifierApiDocumentationDefn();
        mirrorModeDocumentationDefn.setTitle("Mirror Mode");
        mirrorModeDocumentationDefn.setDescription("Mirror HTTP Requests");
        mirrorModeDocumentationDefn.setSeoTitle("Mirror Mode API Documentation | API Challenges");
        mirrorModeDocumentationDefn.setSeoDescription(
                "Review Mirror Mode endpoint documentation to inspect reflected HTTP requests, headers, and payload behavior for debugging and learning.");
        mirrorModeDocumentationDefn.setMetaRobots("noindex,follow");
        mirrorModeDocumentationDefn.setOgType("website");
        mirrorModeDocumentationDefn.setTwitterCard("summary_large_image");
        mirrorModeDocumentationDefn.addServer(
                "https://apichallenges.eviltester.com", "cloud hosted version");
        mirrorModeDocumentationDefn.addServer("http://localhost:4567", "local execution");
        mirrorModeDocumentationDefn.setVersion("1.0.0");

        apiDefn.addServer("https://apichallenges.eviltester.com", "cloud hosted version");
        apiDefn.addServer("http://localhost:4567", "local execution");
        apiDefn.setVersion("1.0.0");

        single_player_mode = config.single_player_mode;
        persistenceLayer = config.persistenceLayer;
        guiStayAlive = config.guiStayAlive;

        challengeDefinitions = new ChallengeDefinitions(config);
        this.thingifier = thingifier;
        challengers =
                new Challengers(
                        thingifier.getERmodel(), challengeDefinitions.getDefinedChallenges());
        challengers.setPersistenceLayer(persistenceLayer);
        if (!single_player_mode) {
            challengers.setMultiPlayerMode();
        }

        if (single_player_mode) {
            // auto load any single player challenger details in single player mode
            persistenceLayer.tryToLoadChallenger(challengers, challengers.SINGLE_PLAYER_GUID);
        }

        challengers.setApiConfig(thingifier.apiConfig());

        if (config.isAdminApiEnabled) {
            enableAdminApi();
        }

        this.guiTemplates = new DefaultGUIHTML();
    }

    public boolean isSinglePlayerMode() {
        return single_player_mode;
    }

    public ChallengeRouteHandler configureRoutes() {

        new ChallengerTrackingRoutes()
                .configure(
                        challengers,
                        single_player_mode,
                        apiChallengesDocumentationDefn,
                        persistenceLayer,
                        thingifier,
                        challengeDefinitions);
        new ChallengesRoutes()
                .configure(
                        challengers,
                        single_player_mode,
                        apiChallengesDocumentationDefn,
                        challengeDefinitions);
        new HeartBeatRoutes().configure(apiChallengesDocumentationDefn);
        new AuthRoutes().configure(challengers, apiChallengesDocumentationDefn);

        // Mirror routes should not show up in the apichallenges apiDefn
        new MirrorRoutes().configure(mirrorModeDocumentationDefn, guiTemplates);

        // Simulation routes should not show
        simulationRoutes =
                new SimulationRoutes(guiTemplates, config.getSimulationRepositoryConfig());
        simulationRoutes.configure();

        new SimpleApiRoutes(guiTemplates).configure();

        return this;
    }

    public void addHooks(final ThingifierHttpApiRoutings apiRoutings) {

        // TODO: these internal HTTP hooks are registered through server-level before/after hooks.
        // They can run for every HTTP request handled by the server, not just requests for this
        // Thingifier API routing. Prefer route-scoped internal HTTP hooks or move this behavior
        // into the API bridge boundary when it only applies to this Thingifier.
        apiRoutings.registerInternalHttpResponseHook(
                new ChallengerInternalHTTPResponseHook(challengers));
        apiRoutings.registerInternalHttpRequestHook(
                new ChallengerInternalHTTPRequestHook(challengers));

        // These hooks run inside the Thingifier API bridge for this routing instance, so they are
        // scoped to this Thingifier's API request/response processing.
        apiRoutings.registerHttpApiRequestHook(new ChallengerApiRequestHook(challengers));
        apiRoutings.registerHttpApiResponseHook(
                new ChallengerApiResponseHook(challengers, thingifier));
    }

    public void setupGui(DefaultGUIHTML guiManagement) {
        this.guiTemplates = guiManagement;
        new ChallengerWebGUI(guiManagement, guiStayAlive)
                .setup(challengers, challengeDefinitions, persistenceLayer, single_player_mode);
    }

    public Challengers getChallengers() {
        return challengers;
    }

    public Thingifier getThingifier() {
        return thingifier;
    }

    public void close() {
        if (simulationRoutes != null) {
            simulationRoutes.close();
        }
        thingifier.close();
    }

    private void enableAdminApi() {
        thingifier.apiConfig().adminConfig().enableAdminSearch();
        thingifier.apiConfig().adminConfig().enableAdminDataClear();
    }
}
