package uk.co.compendiumdev.thingifier.swaggerizer;

import io.swagger.v3.core.util.Json31;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
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
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.ValidationRule;

import java.util.ArrayList;
import java.util.List;

public class Swaggerizer {

    private final ThingifierApiDocumentationDefn apiDefn;
    OpenAPI apiNormal;
    OpenAPI apiPermissive;

    public Swaggerizer(ThingifierApiDocumentationDefn apiDefn){
        this.apiDefn = apiDefn;
    }

    // a swagger configuration allows configuring
    // - field validation on or off - type, min/max etc.
    // - exclude verbs with status 405 (do not add not implemented into swagger)
    // e.g. for Development and Use of API we would want validation on, examples on, only include verbs in definition, exclude verbs with status 405
    // e.g. for testing we would want validation off, examples on, include all verbs

    // TODO: need the field definitions to have descriptions so these can be shown in Swagger

    /*
        Swagger file for normal usage
     */
    public OpenAPI swagger(){
        SwaggerGenerationConfig config = new SwaggerGenerationConfig();

        config.includeMethodNotAllowedEndpoints = false;
        config.includeFieldValidation = true;

        return swagger(config);
    }

    /*
        Swagger file for use in testing
     */
    public OpenAPI swaggerPermissive(){
        SwaggerGenerationConfig config = new SwaggerGenerationConfig();

        config.includeMethodNotAllowedEndpoints = true;
        config.includeFieldValidation = false;

        return swagger(config);
    }

    public OpenAPI swagger(SwaggerGenerationConfig config){

        OpenAPI api = new OpenAPI();

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
        // TODO: this should probably be done in the generate
        for(RoutingDefinition route : routes){
            apiDefn.addAnyGlobalHeaders(route);
        }
        routes.addAll(apiDefn.getAdditionalRoutes());

        List<String> processedAdditionalRoutes = new ArrayList<>();

        Components components = convertEntityDefinitionsToComponents(routingDefinitions);

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
                    paths.addPathItem(prefix + route.urlWithParamFormatter("{", "}"), path);
                    processedAdditionalRoutes.add(route.url());

                    // handle all verbs for this route
                    for (RoutingDefinition subroute : routes) {
                        if (subroute.url().contentEquals(route.url())) {

                            if(!config.includeMethodNotAllowedEndpoints &&
                                subroute.status() !=null &&
                                subroute.status().value() == 405
                            ){
                                // method not allowed so do not add it to the swagger
                                continue;
                            }

                            final Operation operation = new Operation();
                            operation.setDescription(subroute.getDocumentation());
                            operation.setSummary(subroute.getDocumentation());

                            List<Parameter> operationParameters = new ArrayList<>();


                            // TODO: don't like this, possible statuses should be used for all situations
                            if(!subroute.status().isReturnedFromCall()){

                                operation.setResponses(
                                        new ApiResponses().addApiResponse(
                                                String.valueOf(subroute.status().value()),
                                                new ApiResponse().description(
                                                        subroute.status().description())
                                        ));

                            }else {
                                final ApiResponses responses = new ApiResponses();
                                List<RoutingStatus> possibleStatusResponses = subroute.getPossibleStatusReponses();
                                for (RoutingStatus possibleStatus : possibleStatusResponses) {

                                    ApiResponse response = new ApiResponse().description(
                                            possibleStatus.description()
                                    );
                                    if (subroute.hasReturnPayloadFor(possibleStatus.value())) {
                                        // assume that all payloads are setup as components
                                        if (routingDefinitions.hasObjectSchemaNamed(subroute.getReturnPayloadFor(possibleStatus.value()))) {
                                            String ref = "#/components/schemas/" + subroute.getReturnPayloadFor(possibleStatus.value());

                                            Schema<String> object = new Schema<>();
                                            MediaType schema = new MediaType();
                                            schema.setSchema(object);
                                            object.set$ref(ref);

                                            response.setContent(
                                                    new Content().
                                                            addMediaType("application/json", schema).
                                                            addMediaType("application/xml", schema)
                                            );
                                        }
                                    }

                                    responses.addApiResponse(
                                            String.valueOf(possibleStatus.value()),
                                            response
                                    );


                                }

                                if(!possibleStatusResponses.isEmpty()){
                                    operation.setResponses(responses);
                                }
                            }

                            if(subroute.hasRequestPayload()){

                                RequestBody requestBody = new RequestBody();


                                // allow sending empty payloads if no field validation
                                requestBody.setRequired(config.includeFieldValidation);


                                // assume that all payloads are already setup as components
                                String ref = "#/components/schemas/" + subroute.getRequestPayload();

                                Schema<String> object = new Schema<>();
                                MediaType schema = new MediaType();
                                schema.setSchema(object);
                                object.set$ref(ref);

                                requestBody.setContent(
                                        new Content().
                                                addMediaType("application/json", schema).
                                                addMediaType("application/xml", schema)
                                );

                                operation.setRequestBody(requestBody);
                            }

                            if(subroute.isSecuredByBasicAuth()){
                                if(components.getSecuritySchemes() == null  || !components.getSecuritySchemes().containsKey("basicAuth")){
                                    components.addSecuritySchemes(
                                            "basicAuth",
                                            new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic"));
                                }
                                operation.addSecurityItem(
                                        new SecurityRequirement().addList("basicAuth")
                                );
                            }

                            if(subroute.hasRequestUrlParams()) {

                                List<Parameter> urlParameters = new ArrayList<>();

                                // TODO: create a Field to Swaggerizer param method/class
                                List<Field> paramFields = subroute.getRequestUrlParams();
                                for (Field aField : paramFields) {
                                    Parameter param = new Parameter();
                                    param.
                                            in("path").
                                            name(aField.getName()).
                                            example(aField.getRandomExampleValue());

                                    // if it is in path it will always be required
                                    // but we can remove the type validation
                                    if(!config.includeFieldValidation){
                                        param.setAllowEmptyValue(true);
                                    }


                                    Schema<String> schema = new Schema<>();

                                    if(config.includeFieldValidation){
                                        addParamSchemeValidationFromField(schema, aField);


                                    }

                                    param.setSchema(schema);
                                    urlParameters.add(param);
                                }

                                addUrlParametersAtEndpointLevel(path, urlParameters);
                            }

                            addRouteCustomHeaders(subroute, operationParameters);

                            if(!operationParameters.isEmpty()){
                                operation.setParameters(operationParameters);
                            }

                            setOperationVerb(subroute, path, operation);

                        }
                    }
                }
            }
        }


        return api;
    }

    private Components convertEntityDefinitionsToComponents(ApiRoutingDefinition routingDefinitions) {
        Components components = new Components();
        for(EntityDefinition objectSchemaDefinition : routingDefinitions.getObjectSchemas()){

            // add individual entity schema
            ObjectSchema object = asObjectSchema(objectSchemaDefinition);
            components.addSchemas(objectSchemaDefinition.getName(), object);

            // add create schema with ID removed for auto added ids
            ObjectSchema createObject = asCreateObjectSchema(objectSchemaDefinition);
            createObject.title("create " + createObject.getTitle());
            components.addSchemas("create_" + objectSchemaDefinition.getName(), createObject);

            // add list response for entity plural
            ArraySchema arrayObject = asArrayObjectSchema(objectSchemaDefinition);
            components.addSchemas(objectSchemaDefinition.getPlural(), arrayObject);

        }
        return components;
    }

    private void addParamSchemeValidationFromField(Schema<String> schema, Field aField) {
        switch (aField.getType()) {
            case AUTO_INCREMENT:
            case INTEGER:
                schema.addType("integer");
                break;

            case FLOAT:
                schema.addType("number");
                break;
            case BOOLEAN:
                schema.addType("boolean");
                break;
            case AUTO_GUID:
            case STRING:
            case DATE:
            case ENUM: // TODO: properly do Enums
                schema.addType("string");
                break;
            default:
                schema.addType("string");
        }

        // TODO: add min max etc.
    }

    private void addUrlParametersAtEndpointLevel(PathItem path, List<Parameter> urlParameters) {
        for(Parameter param : urlParameters){
            boolean exists = false;
            if(path.getParameters()!=null){
                for(Parameter existingParam : path.getParameters()){
                    if(existingParam.getName().equals(param.getName())){
                        exists = true;
                    }
                }
            }
            if(!exists) {
                path.addParametersItem(param);
            }
        }
    }

    private void setOperationVerb(RoutingDefinition subroute, PathItem path, Operation operation) {
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

    private void addRouteCustomHeaders(RoutingDefinition subroute, List<Parameter> operationParameters) {
        if(subroute.hasCustomHeaders()){
            for(String headerName : subroute.getCustomHeaderNames()){
                String headerType = subroute.getCustomHeaderType(headerName);
                if(headerType != null){

                    Parameter param = new Parameter();
                    param.
                        in("header").
                        name(headerName).
                        required(true);

                    Schema<String> schema = new Schema<>();

                    switch (headerType){
                        case "guid":
                            break;
                        default:
                            schema.addType(headerType);
                    }

                    param.setSchema(schema);
                    operationParameters.add(param);
                }
            }
        }
    }

    private ArraySchema asArrayObjectSchema(EntityDefinition objectSchemaDefinition) {

        ArraySchema arrayObject = new ArraySchema();
        arrayObject.setDescription(objectSchemaDefinition.getPlural());
        arrayObject.setTitle(objectSchemaDefinition.getPlural());
        //arrayObject.setItems(asObjectSchema(objectSchemaDefinition));

        String ref = "#/components/schemas/" + objectSchemaDefinition.getName();

        Schema<String> objectRef = new Schema<>();
        objectRef.set$ref(ref);

        arrayObject.setItems(objectRef);

        XML xml = new XML();
        xml.setWrapped(true);
        arrayObject.setXml(xml);

        return arrayObject;
    }

    private static ObjectSchema asObjectSchema(EntityDefinition objectSchemaDefinition) {
        return asObjectSchema(objectSchemaDefinition, false);
    }

    private static ObjectSchema asCreateObjectSchema(EntityDefinition objectSchemaDefinition) {
        return asObjectSchema(objectSchemaDefinition, true);
    }

    // no auto fields in create
    private static ObjectSchema asObjectSchema(EntityDefinition objectSchemaDefinition, Boolean skipAutos) {
        ObjectSchema object = new ObjectSchema();
        object.setDescription(objectSchemaDefinition.getName());
        object.setTitle(objectSchemaDefinition.getName());

        for(String propertyName : objectSchemaDefinition.getFieldNames()){
            Field propertyDefinition = objectSchemaDefinition.getField(propertyName);
            if(skipAutos &&
                (   propertyDefinition.getType()== FieldType.AUTO_GUID ||
                    propertyDefinition.getType()== FieldType.AUTO_INCREMENT
                )
            ){
            }else {
                Schema<String> propertyItem = new Schema<>();
                propertyItem.setExample(propertyDefinition.getExamples().get(0));

                List<String> description = new ArrayList<>();
                if(propertyDefinition.hasDescription()){
                   description.add(propertyDefinition.getDescription());
                }

                for(ValidationRule validationRule : propertyDefinition.getAllValidationRules()){
                    description.add(validationRule.getExplanation());
                }

                switch (propertyDefinition.getType()) {
                    case AUTO_INCREMENT:
                    case INTEGER:
                        propertyItem.addType("integer");

                        break;

                    case FLOAT:
                        propertyItem.addType("number");
                        break;
                    case BOOLEAN:
                        propertyItem.addType("boolean");
                        break;
                    case AUTO_GUID:
                    case STRING:
                    case DATE:
                    case ENUM: // TODO: properly do Enums
                        propertyItem.addType("string");
                        break;
                    default:
                        propertyItem.addType("string");
                }

                propertyItem.setDescription(joinStrings(description, "."));

                object.addProperties(propertyName, propertyItem);

            }
        }

        XML xml = new XML();
        xml.setWrapped(true);
        xml.setName(objectSchemaDefinition.getName());

        object.setXml(xml);
        return object;
    }

    private static String joinStrings(List<String> description, String postfix) {
        StringBuilder joined = new StringBuilder();
        String prependSpace = "";
        for(String string : description){
            joined.append(prependSpace);
            joined.append(string);
            if(!string.endsWith(postfix)){
                joined.append(postfix);
            }
            prependSpace = " ";
        }
        return joined.toString();
    }


    // TODO: the output from swaggerizer json could be cached

    public String asJson(){
        return asJson(false);
    }

    public String asJson(boolean permissive){
        if(apiNormal==null){
            apiNormal=swagger();
        }
        if(apiPermissive==null){
            apiPermissive=swaggerPermissive();
        }
        if(permissive){
            return Json31.pretty(apiPermissive);
        }else{
            return Json31.pretty(apiNormal);
        }

    }
}
