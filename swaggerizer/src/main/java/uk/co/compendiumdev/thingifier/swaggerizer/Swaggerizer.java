package uk.co.compendiumdev.thingifier.swaggerizer;

import com.google.gson.GsonBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ThingifierApiDefn;
import uk.co.compendiumdev.thingifier.api.routings.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.ApiRoutingDefinitionGenerator;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingStatus;

import java.util.ArrayList;
import java.util.List;

public class Swaggerizer {

    private final ThingifierApiDefn apiDefn;
    OpenAPI api;

    public Swaggerizer(ThingifierApiDefn apiDefn){
        this.apiDefn = apiDefn;
    }

    public OpenAPI swagger(){

        api = new OpenAPI();

        final Thingifier thingifier = apiDefn.getThingifier();

        final Info info = new Info();
        info.setTitle(thingifier.getTitle());
        info.setDescription(thingifier.getInitialParagraph());
        info.setVersion(apiDefn.getVersion());

        for(ThingifierApiDefn.ApiServer server : apiDefn.getServers()){
            api.addServersItem(
                    new Server().description(server.description).
                                url(server.url));
        }

        api.setInfo(info);

        ApiRoutingDefinition routingDefinitions = new ApiRoutingDefinitionGenerator(thingifier).generate();

        List<RoutingDefinition> routes = new ArrayList<>(routingDefinitions.definitions());
        routes.addAll(apiDefn.getAdditionalRoutes());

        List<String> processedAdditionalRoutes = new ArrayList<>();

        if(routes!=null) {

            api.setPaths(new Paths());
            final Paths paths = api.getPaths();

            for (RoutingDefinition route : routes) {
                if (!processedAdditionalRoutes.contains(route.url())){

                    final PathItem path = new PathItem();
                    String prefix="";
                    if(!route.url().startsWith("/")){
                        prefix = "/";
                    }
                    paths.addPathItem(prefix + route.url(), path);
                    processedAdditionalRoutes.add(route.url());

                    // handle all verbs for this route
                    for (RoutingDefinition subroute : routes) {
                        if (subroute.url().contentEquals(route.url())) {

                            final Operation operation = new Operation();
                            operation.setDescription(subroute.getDocumentation());
                            // TODO: need to build up examples and status in the automated route generation
                            if(!subroute.status().isReturnedFromCall()){
                                operation.setResponses(
                                        new ApiResponses().addApiResponse(
                                                String.valueOf(subroute.status().value()),
                                                new ApiResponse().description(
                                                        subroute.status().description())
                                        ));
                            }else{
                                final ApiResponses responses = new ApiResponses();
                                final List<RoutingStatus> possibleStatusResponses = subroute.getPossibleStatusReponses();
                                for(RoutingStatus possibleStatus : possibleStatusResponses){
                                    responses.addApiResponse(
                                            String.valueOf(possibleStatus.value()),
                                            new ApiResponse().description(
                                                    possibleStatus.description())
                                    );
                                }
                                if(possibleStatusResponses.size()>0){
                                    operation.setResponses(responses);
                                }
                            }

                            switch(subroute.verb()){
                                case GET:
                                    path.setGet(operation);
                                    break;
                                case POST:
                                    path.setPost(operation);
                                    break;
                                case PUT:
                                    path.setPut(operation);
                                    break;
                                case HEAD:
                                    path.setHead(operation);
                                    break;
                                case PATCH:
                                    path.setPatch(operation);
                                    break;
                                case DELETE:
                                    path.setDelete(operation);
                                    break;
                                case OPTIONS:
                                    path.setOptions(operation);
                                    break;
                            }
                        }
                    }
                }
            }
        }


        return api;
    }

    public String asJson(){
        if(api==null){
            swagger();
        }
        return new GsonBuilder().setPrettyPrinting().
                create().toJson(api);
    }
}
