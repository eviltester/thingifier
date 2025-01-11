package uk.co.compendiumdev.thingifier.swaggerizer;

import io.swagger.v3.core.util.Json31;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinitionDocGenerator;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;

import java.util.ArrayList;
import java.util.List;

public class Swaggerizer {

    private final ThingifierApiDocumentationDefn apiDefn;
    OpenAPI api;

    public Swaggerizer(ThingifierApiDocumentationDefn apiDefn){
        this.apiDefn = apiDefn;
    }

    public OpenAPI swagger(){

        api = new OpenAPI();

        final Thingifier thingifier = apiDefn.getThingifier();

        final Info info = new Info();

        String titleToUse = thingifier.getTitle();
        if(titleToUse.isEmpty()){
            titleToUse = apiDefn.getTitle();
        }

        String descriptionToUse = thingifier.getInitialParagraph();
        if(descriptionToUse.isEmpty()){
            descriptionToUse = apiDefn.getDescription();
        }

        info.setTitle(titleToUse);
        info.setDescription(descriptionToUse);
        info.setVersion(apiDefn.getVersion());

        for(ThingifierApiDocumentationDefn.ApiServer server : apiDefn.getServers()){
            api.addServersItem(
                    new Server().description(server.description).
                                url(server.url));
        }

        api.setInfo(info);

        ApiRoutingDefinition routingDefinitions = new ApiRoutingDefinitionDocGenerator(thingifier).generate(apiDefn.getPathPrefix());
        List<RoutingDefinition> routes = new ArrayList<>(routingDefinitions.definitions());
        routes.addAll(apiDefn.getAdditionalRoutes());

        List<String> processedAdditionalRoutes = new ArrayList<>();

        Components components = new Components();
        for(EntityDefinition objectSchemaDefinition : routingDefinitions.getObjectSchemas()){

            ObjectSchema object = new ObjectSchema();
            object.setDescription(objectSchemaDefinition.getName());
            object.setTitle(objectSchemaDefinition.getName());

            //object.name(objectSchemaDefinition.getName());
            for(String propertyName : objectSchemaDefinition.getFieldNames()){
                Field propertyDefinition = objectSchemaDefinition.getField(propertyName);
                Schema<String> propertyItem = new Schema<String>();
                propertyItem.setExample(propertyDefinition.getExamples().get(0));
                object.addProperties(propertyName, propertyItem);
            }

            components.addSchemas(objectSchemaDefinition.getName(), object);
        }

        api.components(components);

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

                                    ApiResponse response = new ApiResponse().description(
                                            possibleStatus.description()
                                    );
                                    if(route.hasReturnPayloadFor(possibleStatus.value())){
                                        if(routingDefinitions.hasObjectSchemaNamed(route.getReturnPayloadFor(possibleStatus.value()))){
                                            String ref = "#/components/schemas/" + route.getReturnPayloadFor(possibleStatus.value());

                                            Schema<String> object = new Schema<>();
                                            MediaType schema = new MediaType();
                                            schema.setSchema(object);
                                            object.set$ref(ref);
                                            response.setContent(
                                                    new Content().addMediaType("application/json", schema));
                                        }
                                    }
                                    responses.addApiResponse(
                                        String.valueOf(possibleStatus.value()),
                                        response
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

    // TODO: the output from swaggerizer json could be cached
    public String asJson(){
        if(api==null){
            swagger();
        }
        return Json31.pretty(api);
    }
}
