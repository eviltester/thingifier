package uk.co.compendiumdev.challenge.practicemodes.simulation;

import static spark.Spark.*;

import java.util.List;
import uk.co.compendiumdev.challenge.ChallengerConfig;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.ThingifierAutoDocGenRouting;
import uk.co.compendiumdev.thingifier.adapter.http.routehandlers.HttpApiRequestHandler;
import uk.co.compendiumdev.thingifier.adapter.http.routehandlers.SparkApiRequestResponseHandler;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.MaximumLengthValidationRule;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.query.FilterBy;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.repository.EntityInstanceQuery;
import uk.co.compendiumdev.thingifier.core.repository.EntityInstanceRepository;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreProviderConfig;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;
import uk.co.compendiumdev.thingifier.spark.SimpleSparkRouteCreator;

public class SimulationRoutes {

    public Thingifier simulation;
    public EntityDefinition entityDefn;
    private EntityInstanceRepository entityRepository;
    private EntityInstanceQuery entityQuery;

    private ThingifierApiDocumentationDefn apiDocDefn;
    private DefaultGUIHTML guiTemplates;
    private final ThingStoreProviderConfig simulationRepositoryConfig;

    public SimulationRoutes(DefaultGUIHTML guiTemplates) {
        this(guiTemplates, ChallengerConfig.defaultSimulationRepositoryConfig());
    }

    public SimulationRoutes(
            final DefaultGUIHTML guiTemplates,
            final ThingStoreProviderConfig simulationRepositoryConfig) {
        this.guiTemplates = guiTemplates;
        this.simulationRepositoryConfig = simulationRepositoryConfig;
    }

    public void setUpData() {
        setUpRepositoryBackedData();
        setUpDocumentation();
    }

    void setUpRepositoryBackedData() {
        // fake the data storage
        simulation =
                new Thingifier(new EntityRelModel(simulationRepositoryConfig.createProvider()));

        simulation.setDocumentation(
                "Simulation Mode",
                "A simulated API, each request generates a new set of data but responses are processed by an API handler.");
        entityDefn = simulation.defineThing("entity", "entities");

        entityDefn.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        entityDefn.addFields(
                Field.is("name", FieldType.STRING)
                        .makeMandatory()
                        .withValidation(new MaximumLengthValidationRule(50))
                        .withDefaultValue("unnamed"),
                Field.is("description", FieldType.STRING)
                        .withDefaultValue("")
                        .withValidation(new MaximumLengthValidationRule(200)));

        ThingStore store = simulation.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
        entityRepository = store.entities();
        entityQuery = store.entityQueries();

        for (int id = 1; id <= 10; id++) {

            createManagedEntityNamed("entity number " + id);
        }

        createManagedEntityNamed("bob");

        ThingifierApiConfig customApiconfig = new ThingifierApiConfig("/sim");
        simulation.apiConfig().setFrom(customApiconfig);
    }

    private void setUpDocumentation() {
        apiDocDefn = new ThingifierApiDocumentationDefn();
        apiDocDefn.addServer("https://apichallenges.eviltester.com", "cloud hosted version");
        apiDocDefn.addServer("http://localhost:4567", "local execution");
        apiDocDefn.setVersion("1.0.0");
        apiDocDefn.setThingifier(simulation);
        apiDocDefn.setPathPrefix("/sim"); // where can the API endpoints be found
        apiDocDefn.setSeoTitle("Simulation Mode API Documentation | API Challenges");
        apiDocDefn.setSeoDescription(
                "Review Simulation Mode API documentation for deterministic request behavior, payload formats, and response handling used in guided practice.");
        apiDocDefn.setMetaRobots("noindex,follow");
        apiDocDefn.setOgType("website");
        apiDocDefn.setTwitterCard("summary_large_image");

        simulation
                .apidocsconfig()
                .setHeaderSectionOverride(
                        """
                <p>A simulated API, where each request is run against a new generated set of data but
                 responses are processed by an API handler.
                 </p>
                 <p>
                 No data is stored on the server.
                 </p>
                 <p>
                 The simulator is designed to be used by following along with the instructions
                 otherwise sequential requests will not make sense e.g. if you DELETE an item
                 and then GET it, then it will be returned because the simulator expects you to
                 DELETE a specific item.
                 </p>
                """
                                .stripIndent());

        new ThingifierAutoDocGenRouting(simulation, apiDocDefn, guiTemplates);
    }

    private EntityInstance createManagedEntityNamed(final String name) {
        return entityRepository.create(
                EntityInstanceDraft.forEntity(entityDefn).withField("name", name));
    }

    public void configure() {

        setUpData();

        // /sim should be the GUI
        String apiEndpoint = "/sim/entities";

        // redirect a GET to "/fromPath" to "/toPath" for GUI
        redirect.get("/sim", "/practice-modes/simulation");

        options(
                apiEndpoint,
                (request, result) -> {
                    result.status(204);
                    result.header("Allow", "GET, POST, PUT, HEAD, OPTIONS");
                    return "";
                });

        new SimpleSparkRouteCreator(apiEndpoint).status(501, true, List.of("patch", "trace"));
        new SimpleSparkRouteCreator(apiEndpoint).status(405, true, List.of("delete"));

        new SimpleSparkRouteCreator(apiEndpoint + "/*")
                .status(501, true, List.of("patch", "trace"));

        options(
                apiEndpoint + "/*",
                (request, result) -> {
                    result.status(204);
                    result.header("x-robots-tag", "noindex");
                    result.header("Allow", "GET, POST, PUT, DELETE, HEAD, OPTIONS");
                    return "";
                });

        HttpApiRequestHandler getEntitiesHandler =
                (HttpApiRequest anHttpApiRequest) -> {
                    QueryFilterParams queryParams = anHttpApiRequest.getFilterableQueryParams();
                    // id 11 is the special POST-created entity and is not visible in collection
                    // GET.
                    queryParams.add(new FilterBy("id", "!=11"));

                    List<EntityInstance> instances = entityQuery.list(entityDefn, queryParams);
                    return ApiResponse.success()
                            .returnInstanceCollection(instances)
                            .resultContainsType(entityDefn);
                };

        get(
                apiEndpoint,
                (request, result) -> {
                    return new SparkApiRequestResponseHandler(request, result, simulation)
                            .usingHandler(getEntitiesHandler)
                            .handle();
                });

        head(
                apiEndpoint,
                (request, result) -> {
                    new SparkApiRequestResponseHandler(request, result, simulation)
                            .usingHandler(getEntitiesHandler)
                            .handle();
                    return "";
                });

        HttpApiRequestHandler getEntityHandler =
                (HttpApiRequest anHttpApiRequest) -> {
                    ApiResponse response;

                    // process it because the request validated
                    String id = anHttpApiRequest.getUrlParam(":id");
                    EntityInstance instance = entityQuery.findByPrimaryKey(entityDefn, id);
                    if (instance == null) {
                        response = ApiResponse.error404("Could not find Entity with ID " + id);
                    } else {
                        response = ApiResponse.success().returnSingleInstance(instance);
                    }

                    if (id.equals("10")) {
                        // 10 is the entity we amend to name:eris
                        response =
                                ApiResponse.success()
                                        .returnSingleDraft(
                                                EntityInstanceDraft.forEntity(entityDefn)
                                                        .withProtectedField("id", "10")
                                                        .withField("name", "eris"));
                    }

                    if (id.equals("9")) {
                        // 9 is the entity we delete
                        response = ApiResponse.error404("Could not find Entity with ID 9");
                    }

                    return response;
                };

        // get a specific entity
        get(
                apiEndpoint + "/:id",
                (request, result) -> {
                    return new SparkApiRequestResponseHandler(request, result, simulation)
                            .usingHandler(getEntityHandler)
                            .handle();
                });

        head(
                apiEndpoint + "/:id",
                (request, result) -> {
                    new SparkApiRequestResponseHandler(request, result, simulation)
                            .usingHandler(getEntityHandler)
                            .handle();

                    return "";
                });

        // post create new - will create as 11 {"name":"bob"}
        post(
                apiEndpoint,
                (request, result) -> {
                    return new SparkApiRequestResponseHandler(request, result, simulation)
                            .usingHandler(
                                    (anHttpApiRequest) -> {
                                        return ApiResponse.created(
                                                entityQuery.findByPrimaryKey(entityDefn, "11"),
                                                simulation.apiConfig());
                                    })
                            .handle();
                });

        HttpApiRequestHandler putAndPostEntityHandler =
                (HttpApiRequest anHttpApiRequest) -> {
                    // process it because the request validated
                    ApiResponse response;
                    String id = anHttpApiRequest.getUrlParam(":id");
                    if (id.equals("11")) {
                        // we can create id 11
                        response =
                                ApiResponse.created(
                                        entityQuery.findByPrimaryKey(entityDefn, "11"),
                                        simulation.apiConfig());
                    } else {
                        if (id.equals("10")) {
                            // 10 is the entity we amend to name:eris
                            response =
                                    ApiResponse.success()
                                            .returnSingleDraft(
                                                    EntityInstanceDraft.forEntity(entityDefn)
                                                            .withProtectedField("id", "10")
                                                            .withField("name", "eris"));
                        } else {
                            final EntityInstance instance =
                                    entityQuery.findByPrimaryKey(entityDefn, id);
                            if (instance == null) {
                                if (anHttpApiRequest.getVerb() == HttpApiRequest.VERB.POST) {
                                    response =
                                            ApiResponse.error404(
                                                    "Could not find Entity with ID " + id);
                                } else { // must be a PUT
                                    response =
                                            ApiResponse.error(
                                                    403, "Not authorised to create that entity");
                                }
                            } else {
                                response =
                                        ApiResponse.error(
                                                403, "Not authorised to amend that entity");
                            }
                        }
                    }
                    return response;
                };

        // post amend 10
        // post create - 11
        post(
                apiEndpoint + "/:id",
                (request, result) -> {
                    return new SparkApiRequestResponseHandler(request, result, simulation)
                            .usingHandler(putAndPostEntityHandler)
                            .handle();
                });

        // put specific id will create (11),
        //  and can amend with put (10)
        put(
                apiEndpoint + "/:id",
                (request, result) -> {
                    return new SparkApiRequestResponseHandler(request, result, simulation)
                            .usingHandler(putAndPostEntityHandler)
                            .handle();
                });

        delete(
                apiEndpoint + "/:id",
                (request, result) -> {
                    return new SparkApiRequestResponseHandler(request, result, simulation)
                            .usingHandler(
                                    (anHttpApiRequest) -> {
                                        ApiResponse response;
                                        String id = anHttpApiRequest.getUrlParam(":id");
                                        if (id.equals("9")) {
                                            // we can delete id 9
                                            response = new ApiResponse(204);
                                        } else {
                                            final EntityInstance instance =
                                                    entityQuery.findByPrimaryKey(entityDefn, id);
                                            if (instance == null) {
                                                response =
                                                        ApiResponse.error404(
                                                                "Could not find Entity with ID "
                                                                        + id);
                                            } else {
                                                response =
                                                        ApiResponse.error(
                                                                403,
                                                                "Not authorised to delete that entity");
                                            }
                                        }
                                        return response;
                                    })
                            .handle();
                });
    }

    public void close() {
        if (simulation != null) {
            simulation.close();
        }
    }
}
