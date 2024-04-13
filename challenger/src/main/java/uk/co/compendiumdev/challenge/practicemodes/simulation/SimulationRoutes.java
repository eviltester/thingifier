package uk.co.compendiumdev.challenge.practicemodes.simulation;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.restapihandlers.RestApiGetHandler;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.httprouting.ThingifierAutoDocGenRouting;
import uk.co.compendiumdev.thingifier.application.routehandlers.HttpApiRequestHandler;
import uk.co.compendiumdev.thingifier.application.routehandlers.SparkApiRequestResponseHandler;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceCollection;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.MaximumLengthValidationRule;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.htmlgui.htmlgen.DefaultGUIHTML;
import uk.co.compendiumdev.thingifier.spark.SimpleSparkRouteCreator;
import uk.co.compendiumdev.thingifier.swaggerizer.Swaggerizer;

import java.util.ArrayList;
import java.util.List;

import static spark.Spark.*;

public class SimulationRoutes {

    private ThingifierHttpApi httpApi;
    private JsonThing jsonThing;
    public Thingifier simulation;
    public EntityDefinition entityDefn;
    private EntityInstanceCollection entityStorage;

    private ThingifierApiDocumentationDefn apiDocDefn;
    private ThingifierAutoDocGenRouting simulatorDocsRouting;
    private DefaultGUIHTML guiTemplates;

    public SimulationRoutes(DefaultGUIHTML guiTemplates){
        this.guiTemplates=guiTemplates;
    }

    public void setUpData(){
        // fake the data storage
        this.simulation = new Thingifier();

        simulation.setDocumentation("Simulation Mode", "A simulated API, each request generates a new set of data but responses are processed by an API handler.");
        this.entityDefn = this.simulation.defineThing("entity", "entities");

        this.entityDefn.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        this.entityDefn.addFields(
                Field.is("name", FieldType.STRING).
                        makeMandatory().
                        withValidation(new MaximumLengthValidationRule(50)).
                        withDefaultValue("unnamed"),
                Field.is("description", FieldType.STRING).
                        withDefaultValue("").
                        withValidation(new MaximumLengthValidationRule(200))
        );

        this.entityStorage = this.simulation.getThingInstancesNamed("entity", EntityRelModel.DEFAULT_DATABASE_NAME);

        for(int id=1; id<=10; id++){

            this.entityStorage.createManagedInstance().
                        //setValue("id", String.valueOf(id)).
                        setValue("name", "entity number " +id);
        }

        this.entityStorage.createManagedInstance().
                //setValue("id", String.valueOf(id)).
                        setValue("name", "bob");

        // this gives us access to the common http processing functions
        this.httpApi = new ThingifierHttpApi(this.simulation);
        this.jsonThing = new JsonThing(this.simulation.apiConfig().jsonOutput());

        apiDocDefn = new ThingifierApiDocumentationDefn();
        apiDocDefn.setThingifier(simulation);
        apiDocDefn.setPathPrefix("/sim"); // where can the API endpoints be found

        ThingifierApiConfig customApiconfig = new ThingifierApiConfig("/sim");
        simulation.apiConfig().setFrom(customApiconfig);

        simulation.apidocsconfig().setHeaderSectionOverride("""
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
                """.stripIndent());

        simulatorDocsRouting = new ThingifierAutoDocGenRouting(
                                            simulation,
                                            apiDocDefn,
                                            guiTemplates
        );

    }

    public void configure() {

        setUpData();

        // /sim should be the GUI
        String apiEndpoint = "/sim/entities";

        // redirect a GET to "/fromPath" to "/toPath" for GUI
        redirect.get("/sim", "/practice-modes/simulation");

        options(apiEndpoint, (request, result) -> {
            result.status(204);
            result.header("Allow", "GET, POST, PUT, HEAD, OPTIONS");
            return "";
        });

        new SimpleSparkRouteCreator(apiEndpoint).status(501, List.of("patch", "trace"));
        new SimpleSparkRouteCreator(apiEndpoint).status(405, List.of("delete"));

        new SimpleSparkRouteCreator(apiEndpoint + "/*").status(501, List.of("patch", "trace"));

        options(apiEndpoint + "/*", (request, result) -> {
            result.status(204);
            result.header("x-robots-tag", "noindex");
            result.header("Allow", "GET, POST, PUT, DELETE, HEAD, OPTIONS");
            return "";
        });

        HttpApiRequestHandler getEntitiesHandler = (HttpApiRequest anHttpApiRequest) -> {
            // remove id 11 because that is a POST so not available in the list
            List<Integer> idsToRemove = new ArrayList<>();
            idsToRemove.add(11);

            // process it because the request validated
            List<EntityInstance> instances = new ArrayList();
            for (EntityInstance possible : this.entityStorage.getInstances()) {
                if (!idsToRemove.contains(
                        possible.getFieldValue("id").asInteger())) {
                    instances.add(possible);
                }
            }

            Thingifier cloned = this.simulation.cloneWithDifferentData(instances);
            return new RestApiGetHandler(cloned).handle("entities", anHttpApiRequest.getFilterableQueryParams(), anHttpApiRequest.getHeaders());

        };

        get(apiEndpoint, (request, result) -> {
            return new SparkApiRequestResponseHandler(request, result, simulation).
                    usingHandler(getEntitiesHandler).handle();
        });

        head(apiEndpoint, (request, result) -> {

            new SparkApiRequestResponseHandler(request, result, simulation).
                    usingHandler(getEntitiesHandler).handle();
            return "";
        });

        HttpApiRequestHandler getEntityHandler = (HttpApiRequest anHttpApiRequest) -> {

            ApiResponse response = null;

            // process it because the request validated
            String id = anHttpApiRequest.getUrlParam(":id");
            EntityInstance instance = this.entityStorage.findInstanceByPrimaryKey(id);
            if (instance == null) {
                response = ApiResponse.error404("Could not find Entity with ID " + id);
            } else {
                response = ApiResponse.success().returnSingleInstance(instance);
            }

            if (id.equals("10")) {
                // 10 is the entity we amend to name:eris
                EntityInstance fake = new EntityInstance(entityDefn).
                        overrideValue("id", "10").setValue("name", "eris");
                instance = fake;
                response = ApiResponse.success().returnSingleInstance(instance);
            }

            if (id.equals("9")) {
                // 9 is the entity we delete
                response = ApiResponse.error404("Could not find Entity with ID 9");
            }

            return response;
        };

        // get a specific entity
        get(apiEndpoint + "/:id", (request, result) -> {

            return new SparkApiRequestResponseHandler(request, result, simulation).
                    usingHandler(getEntityHandler)
                    .handle();
        });

        head(apiEndpoint + "/:id", (request, result) -> {

            new SparkApiRequestResponseHandler(request, result, simulation).
                    usingHandler(getEntityHandler)
                    .handle();

            return "";
        });

        // post create new - will create as 11 {"name":"bob"}
        post(apiEndpoint, (request, result) -> {

            return new SparkApiRequestResponseHandler(request, result, simulation).
                    usingHandler((anHttpApiRequest) -> {
                        return ApiResponse.created(this.entityStorage.
                                        findInstanceByPrimaryKey("11"),
                                this.simulation.apiConfig());
                    }).handle();
        });

        HttpApiRequestHandler putAndPostEntityHandler = (HttpApiRequest anHttpApiRequest) -> {
            // process it because the request validated
            ApiResponse response = null;
            String id = anHttpApiRequest.getUrlParam(":id");
            if (id.equals("11")) {
                // we can create id 11
                response = ApiResponse.created(
                        this.entityStorage.findInstanceByPrimaryKey("11"),
                        this.simulation.apiConfig());
            } else {
                if (id.equals("10")) {
                    // 10 is the entity we amend to name:eris
                    EntityInstance fake = new EntityInstance(entityDefn).
                            overrideValue("id", "10").setValue("name", "eris");
                    response = ApiResponse.success().returnSingleInstance(fake);
                } else {
                    final EntityInstance instance = this.entityStorage.findInstanceByPrimaryKey(id);
                    if (instance == null) {
                        if (anHttpApiRequest.getVerb() == HttpApiRequest.VERB.POST) {
                            response = ApiResponse.error404("Could not find Entity with ID " + id);
                        } else { // must be a PUT
                            response = ApiResponse.error(403, "Not authorised to create that entity");
                        }
                    } else {
                        response = ApiResponse.error(403, "Not authorised to amend that entity");
                    }
                }
            }
            return response;
        };

        // post amend 10
        // post create - 11
        post(apiEndpoint + "/:id", (request, result) -> {
            return new SparkApiRequestResponseHandler(request, result, simulation).
                    usingHandler(putAndPostEntityHandler).handle();
        });

        // put specific id will create (11),
        //  and can amend with put (10)
        put(apiEndpoint + "/:id", (request, result) -> {
            return new SparkApiRequestResponseHandler(request, result, simulation).
                    usingHandler(putAndPostEntityHandler).handle();
        });

        delete(apiEndpoint + "/:id", (request, result) -> {

            return new SparkApiRequestResponseHandler(request, result, simulation).
                    usingHandler((anHttpApiRequest) -> {
                        ApiResponse response = null;
                        String id = anHttpApiRequest.getUrlParam(":id");
                        if (id.equals("9")) {
                            // we can delete id 9
                            response = new ApiResponse(204);
                        } else {
                            final EntityInstance instance = this.entityStorage.findInstanceByPrimaryKey(id);
                            if (instance == null) {
                                response = ApiResponse.error404("Could not find Entity with ID " + id);
                            } else {
                                response = ApiResponse.error(403, "Not authorised to delete that entity");
                            }
                        }
                        return response;
                    }).handle();
        });
    }
}
