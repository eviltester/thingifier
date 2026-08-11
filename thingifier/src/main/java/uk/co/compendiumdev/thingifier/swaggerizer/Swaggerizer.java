package uk.co.compendiumdev.thingifier.swaggerizer;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinitionDocGenerator;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityViewDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.ValidationRule;
import uk.co.compendiumdev.thingifier.core.query.PaginationParams;
import uk.co.compendiumdev.thingifier.core.query.SortByFieldName;

public class Swaggerizer {

    private final ThingifierApiDocumentationDefn apiDefn;
    OpenAPI apiNormal;
    OpenAPI apiPermissive;

    public Swaggerizer(ThingifierApiDocumentationDefn apiDefn) {
        this.apiDefn = apiDefn;
    }

    // a swagger configuration allows configuring
    // - field validation on or off - type, min/max etc.
    // - exclude verbs with status 405 (do not add not implemented into swagger)
    // e.g. for Development and Use of API we would want validation on, examples on, only include
    // verbs in definition, exclude verbs with status 405
    // e.g. for testing we would want validation off, examples on, include all verbs

    // TODO: need the field definitions to have descriptions so these can be shown in Swagger

    /*
       Swagger file for normal usage
    */
    public OpenAPI swagger() {
        return swagger(OpenApiSpecificationVersion.OPENAPI_3_1);
    }

    public OpenAPI swagger(final OpenApiSpecificationVersion version) {
        SwaggerGenerationConfig config = new SwaggerGenerationConfig();

        config.includeMethodNotAllowedEndpoints = false;
        config.includeFieldValidation = true;
        config.openApiSpecificationVersion = version.swaggerCoreGenerationVersion();

        return swagger(config);
    }

    /*
       Swagger file for use in testing
    */
    public OpenAPI swaggerPermissive() {
        return swaggerPermissive(OpenApiSpecificationVersion.OPENAPI_3_1);
    }

    public OpenAPI swaggerPermissive(final OpenApiSpecificationVersion version) {
        SwaggerGenerationConfig config = new SwaggerGenerationConfig();

        config.includeMethodNotAllowedEndpoints = true;
        config.includeFieldValidation = false;
        config.openApiSpecificationVersion = version.swaggerCoreGenerationVersion();

        return swagger(config);
    }

    public OpenAPI swagger(SwaggerGenerationConfig config) {

        final OpenApiSpecificationVersion generationVersion =
                config.openApiSpecificationVersion.swaggerCoreGenerationVersion();

        OpenAPI api = new OpenAPI(generationVersion.specVersion());
        api.setOpenapi(generationVersion.documentVersion());

        final Thingifier thingifier = apiDefn.getThingifier();

        final Info info = new Info();

        String titleToUse = thingifier.getTitle();
        if (titleToUse.isEmpty()) {
            titleToUse = apiDefn.getTitle();
        }

        String descriptionToUse = thingifier.getInitialParagraph();
        if (descriptionToUse.isEmpty()) {
            descriptionToUse = apiDefn.getDescription();
        }

        info.setTitle(titleToUse);
        info.setDescription(descriptionToUse);
        info.setVersion(apiDefn.getVersion());

        for (ThingifierApiDocumentationDefn.ApiServer server : apiDefn.getServers()) {
            api.addServersItem(new Server().description(server.description).url(server.url));
        }

        api.setInfo(info);

        ApiRoutingDefinition routingDefinitions =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate(apiDefn.getPathPrefix());
        List<RoutingDefinition> routes = new ArrayList<>(routingDefinitions.definitions());
        // TODO: this should probably be done in the generate
        for (RoutingDefinition route : routes) {
            apiDefn.addAnyGlobalHeaders(route);
        }
        routes.addAll(apiDefn.getAdditionalRoutes());

        List<String> processedAdditionalRoutes = new ArrayList<>();

        Components components = convertEntityDefinitionsToComponents(routingDefinitions);

        api.components(components);

        if (routes != null) {

            api.setPaths(new Paths());
            final Paths paths = api.getPaths();

            for (RoutingDefinition route : routes) {
                if (!processedAdditionalRoutes.contains(route.url())
                        && hasVisibleRouteForUrl(routes, route.url(), config)) {

                    final PathItem path = new PathItem();
                    String prefix = "";
                    if (!route.url().startsWith("/")) {
                        prefix = "/";
                    }
                    paths.addPathItem(prefix + route.urlWithParamFormatter("{", "}"), path);
                    processedAdditionalRoutes.add(route.url());

                    // handle all verbs for this route
                    for (RoutingDefinition subroute : routes) {
                        if (subroute.url().contentEquals(route.url())) {

                            if (!isVisibleRoute(subroute, config)) {
                                continue;
                            }

                            final Operation operation = new Operation();
                            operation.setDescription(subroute.getDocumentation());
                            operation.setSummary(subroute.getDocumentation());

                            List<Parameter> operationParameters = new ArrayList<>();

                            // TODO: don't like this, possible statuses should be used for all
                            // situations
                            if (!subroute.status().isReturnedFromCall()) {

                                ApiResponse response =
                                        new ApiResponse()
                                                .description(subroute.status().description());
                                addRouteResponseHeaders(subroute, response);
                                operation.setResponses(
                                        new ApiResponses()
                                                .addApiResponse(
                                                        String.valueOf(subroute.status().value()),
                                                        response));

                            } else {
                                final ApiResponses responses = new ApiResponses();
                                List<RoutingStatus> possibleStatusResponses =
                                        subroute.getPossibleStatusReponses();
                                for (RoutingStatus possibleStatus : possibleStatusResponses) {

                                    ApiResponse response =
                                            new ApiResponse()
                                                    .description(possibleStatus.description());
                                    addRouteResponseHeaders(subroute, response);
                                    if (subroute.hasReturnPayloadFor(possibleStatus.value())) {
                                        // assume that all payloads are setup as components
                                        if (routingDefinitions.hasObjectSchemaNamed(
                                                subroute.getReturnPayloadFor(
                                                        possibleStatus.value()))) {
                                            String ref =
                                                    "#/components/schemas/"
                                                            + subroute.getReturnPayloadFor(
                                                                    possibleStatus.value());

                                            response.setContent(responseContentWith(ref));
                                        }
                                    }

                                    responses.addApiResponse(
                                            String.valueOf(possibleStatus.value()), response);
                                }

                                if (!possibleStatusResponses.isEmpty()) {
                                    operation.setResponses(responses);
                                }
                            }

                            if (subroute.hasRequestPayload()) {

                                RequestBody requestBody = new RequestBody();

                                // allow sending empty payloads if no field validation
                                requestBody.setRequired(config.includeFieldValidation);

                                // assume that all payloads are already setup as components
                                String ref = "#/components/schemas/" + subroute.getRequestPayload();

                                Schema<String> object = new Schema<>();
                                MediaType schema = new MediaType();
                                schema.setSchema(object);
                                object.set$ref(ref);

                                Content content = new Content();
                                for (String contentType : subroute.getRequestContentTypes()) {
                                    content.addMediaType(contentType, schema);
                                }
                                requestBody.setContent(content);

                                operation.setRequestBody(requestBody);
                            }

                            if (subroute.isSecuredByBasicAuth()) {
                                addHttpSecurityScheme(components, "basicAuth", "basic", null);
                                operation.addSecurityItem(
                                        new SecurityRequirement().addList("basicAuth"));
                            }

                            if (subroute.isSecuredByBearerAuth()) {
                                addHttpSecurityScheme(components, "bearerAuth", "bearer", null);
                                operation.addSecurityItem(
                                        new SecurityRequirement().addList("bearerAuth"));
                            }

                            if (shouldDocumentSortParameter(thingifier, subroute)) {
                                operationParameters.add(
                                        sortByParameter(subroute.getFilterableEntity()));
                            }

                            if (shouldDocumentPagingParameters(thingifier, subroute)) {
                                operationParameters.add(limitParameter(thingifier));
                                operationParameters.add(offsetParameter());
                            }

                            if (subroute.hasRequestUrlParams()) {

                                List<Parameter> urlParameters = new ArrayList<>();

                                for (RoutingDefinition.RequestUrlParameter urlParameter :
                                        subroute.getRequestUrlParameters()) {
                                    Field aField = urlParameter.field();
                                    Parameter param = new Parameter();
                                    param.in("path")
                                            .name(urlParameter.name())
                                            .required(true)
                                            .example(
                                                    openApiExampleValueFor(
                                                            aField,
                                                            aField.getRandomExampleValue()));
                                    if (aField.hasDescription()) {
                                        param.setDescription(aField.getDescription());
                                    }

                                    // if it is in path it will always be required
                                    // but we can remove the type validation
                                    if (!config.includeFieldValidation) {
                                        param.setAllowEmptyValue(true);
                                    }

                                    Schema<String> schema = new Schema<>();

                                    if (config.includeFieldValidation) {
                                        addParamSchemeValidationFromField(schema, aField);
                                    }

                                    param.setSchema(schema);
                                    urlParameters.add(param);
                                }

                                addUrlParametersAtEndpointLevel(path, urlParameters);
                            }

                            addRouteCustomHeaders(subroute, operationParameters);

                            if (!operationParameters.isEmpty()) {
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

    private boolean hasVisibleRouteForUrl(
            final List<RoutingDefinition> routes,
            final String url,
            final SwaggerGenerationConfig config) {
        for (RoutingDefinition route : routes) {
            if (route.url().contentEquals(url) && isVisibleRoute(route, config)) {
                return true;
            }
        }
        return false;
    }

    private boolean isVisibleRoute(
            final RoutingDefinition route, final SwaggerGenerationConfig config) {
        if (route.isHiddenFromDocumentation() || route.isDisabled()) {
            return false;
        }
        return config.includeMethodNotAllowedEndpoints
                || route.status() == null
                || route.status().value() != 405;
    }

    private Components convertEntityDefinitionsToComponents(
            ApiRoutingDefinition routingDefinitions) {
        Components components = new Components();
        for (EntityDefinition objectSchemaDefinition : routingDefinitions.getObjectSchemas()) {

            // add individual entity schema
            ObjectSchema object = asObjectSchema(objectSchemaDefinition);
            components.addSchemas(objectSchemaDefinition.getName(), object);

            // add create schema with ID removed for auto added ids
            ObjectSchema createObject = asCreateObjectSchema(objectSchemaDefinition);
            createObject.title("create " + createObject.getTitle());
            components.addSchemas("create_" + objectSchemaDefinition.getName(), createObject);

            // add list response for entity plural
            ObjectSchema arrayObject = asArrayObjectSchema(objectSchemaDefinition);
            components.addSchemas(objectSchemaDefinition.getPlural(), arrayObject);

            for (EntityViewDefinition view : objectSchemaDefinition.getViews()) {
                ObjectSchema viewObject = asResponseViewObjectSchema(view);
                components.addSchemas(view.getName(), viewObject);

                ObjectSchema createViewObject = asRequestViewObjectSchema(view);
                createViewObject.title("create " + createViewObject.getTitle());
                components.addSchemas("create_" + view.getName(), createViewObject);
            }
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
        for (Parameter param : urlParameters) {
            boolean exists = false;
            if (path.getParameters() != null) {
                for (Parameter existingParam : path.getParameters()) {
                    if (existingParam.getName().equals(param.getName())) {
                        exists = true;
                    }
                }
            }
            if (!exists) {
                path.addParametersItem(param);
            }
        }
    }

    private boolean shouldDocumentSortParameter(
            final Thingifier thingifier, final RoutingDefinition route) {
        if (!route.isFilterable()) {
            return false;
        }
        if (route.verb() == RoutingVerb.QUERY) {
            return true;
        }
        return thingifier.apiConfig().forParams().willAllowFilteringThroughUrlParams();
    }

    private boolean shouldDocumentPagingParameters(
            final Thingifier thingifier, final RoutingDefinition route) {
        return route.isFilterable()
                && thingifier.apiConfig().forParams().willAllowPagingThroughUrlParams();
    }

    private Parameter sortByParameter(final EntityDefinition filterableEntity) {
        Parameter param = new Parameter();
        param.in("query")
                .name(SortByFieldName.PARAMETER_NAME)
                .required(false)
                .description(
                        "Sort collection results by a field. Use +field or field for ascending"
                                + " order, and -field for descending order. Multiple fields can"
                                + " be combined with commas, e.g. +field,-other.")
                .example("+" + sortExampleFieldName(filterableEntity));
        param.setSchema(new StringSchema());
        return param;
    }

    private Parameter limitParameter(final Thingifier thingifier) {
        int defaultLimit = thingifier.apiConfig().forParams().defaultPagingLimit();
        int maxLimit = thingifier.apiConfig().forParams().maxPagingLimit();
        IntegerSchema schema = new IntegerSchema();
        schema.minimum(BigDecimal.ZERO);
        schema.maximum(BigDecimal.valueOf(maxLimit));

        Parameter param = new Parameter();
        param.in("query")
                .name(PaginationParams.LIMIT_PARAMETER_NAME)
                .required(false)
                .description(
                        "Limit collection results. Defaults to "
                                + defaultLimit
                                + " and is capped at "
                                + maxLimit
                                + ".")
                .example(defaultLimit);
        param.setSchema(schema);
        return param;
    }

    private Parameter offsetParameter() {
        IntegerSchema schema = new IntegerSchema();
        schema.minimum(BigDecimal.ZERO);

        Parameter param = new Parameter();
        param.in("query")
                .name(PaginationParams.OFFSET_PARAMETER_NAME)
                .required(false)
                .description("Zero-based number of collection results to skip.")
                .example(0);
        param.setSchema(schema);
        return param;
    }

    private String sortExampleFieldName(final EntityDefinition filterableEntity) {
        if (filterableEntity == null) {
            return "field";
        }
        Field primaryKeyField = filterableEntity.getPrimaryKeyField();
        if (primaryKeyField != null) {
            return primaryKeyField.getName();
        }
        for (String fieldName : filterableEntity.getFieldNames()) {
            return fieldName;
        }
        return "field";
    }

    private void setOperationVerb(RoutingDefinition subroute, PathItem path, Operation operation) {
        switch (subroute.verb()) {
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
            case QUERY:
                operation.addExtension("x-http-method", "QUERY");
                operation.addExtension(
                        "x-query-content-types",
                        ThingifierHttpApi.SUPPORTED_QUERY_CONTENT_TYPES_LIST);
                path.addExtension("x-query-operation", operation);
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
            default:
                break;
        }
    }

    private void addRouteCustomHeaders(
            RoutingDefinition subroute, List<Parameter> operationParameters) {
        if (subroute.hasCustomHeaders()) {
            for (String headerName : subroute.getCustomHeaderNames()) {
                String headerType = subroute.getCustomHeaderType(headerName);
                if (headerType != null) {

                    Parameter param = new Parameter();
                    param.in("header").name(headerName).required(true);

                    Schema<String> schema = new Schema<>();

                    switch (headerType) {
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

    private void addRouteResponseHeaders(
            final RoutingDefinition subroute, final ApiResponse response) {
        if (!subroute.hasResponseHeaders()) {
            return;
        }

        for (String headerName : subroute.getResponseHeaderNames()) {
            Header header = new Header();
            header.setDescription(subroute.getResponseHeaderValue(headerName));
            header.setSchema(new StringSchema());
            response.addHeaderObject(headerName, header);
        }
    }

    private Content responseContentWith(final String ref) {
        Schema<String> object = new Schema<>();
        MediaType schema = new MediaType();
        schema.setSchema(object);
        object.set$ref(ref);

        Content content = new Content();
        for (AcceptHeaderParser.ACCEPT_TYPE responseType :
                AcceptHeaderParser.ACCEPT_TYPE.responseMediaTypes()) {
            if (responseType.usesComponentSchemaInDocumentation()) {
                content.addMediaType(responseType.mediaType(), schema);
            } else {
                MediaType textSchema = new MediaType();
                textSchema.setSchema(new StringSchema());
                content.addMediaType(responseType.mediaType(), textSchema);
            }
        }
        return content;
    }

    private void addHttpSecurityScheme(
            final Components components,
            final String name,
            final String scheme,
            final String bearerFormat) {
        if (components.getSecuritySchemes() != null
                && components.getSecuritySchemes().containsKey(name)) {
            return;
        }

        final SecurityScheme securityScheme =
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme(scheme);
        if (bearerFormat != null) {
            securityScheme.bearerFormat(bearerFormat);
        }
        components.addSecuritySchemes(name, securityScheme);
    }

    private ObjectSchema asArrayObjectSchema(EntityDefinition objectSchemaDefinition) {

        ObjectSchema collectionObject = new ObjectSchema();
        collectionObject.setDescription(objectSchemaDefinition.getPlural());
        collectionObject.setTitle(objectSchemaDefinition.getPlural());

        ArraySchema arrayObject = new ArraySchema();
        arrayObject.setItems(asRequiredResponseObjectSchema(objectSchemaDefinition));

        XML xml = new XML();
        xml.setWrapped(true);
        collectionObject.setXml(xml);
        collectionObject.addProperties(objectSchemaDefinition.getPlural(), arrayObject);
        collectionObject.addRequiredItem(objectSchemaDefinition.getPlural());

        return collectionObject;
    }

    private static ObjectSchema asRequiredResponseObjectSchema(
            EntityDefinition objectSchemaDefinition) {
        ObjectSchema object = asObjectSchema(objectSchemaDefinition);
        if (object.getProperties() != null) {
            for (String propertyName : object.getProperties().keySet()) {
                object.addRequiredItem(propertyName);
            }
        }
        return object;
    }

    private static ObjectSchema asObjectSchema(EntityDefinition objectSchemaDefinition) {
        return asObjectSchema(objectSchemaDefinition, false);
    }

    private static ObjectSchema asCreateObjectSchema(EntityDefinition objectSchemaDefinition) {
        return asObjectSchema(objectSchemaDefinition, true);
    }

    private static ObjectSchema asRequestViewObjectSchema(final EntityViewDefinition view) {
        return asObjectSchema(view.getEntity(), true, view, true);
    }

    private static ObjectSchema asResponseViewObjectSchema(final EntityViewDefinition view) {
        return asObjectSchema(view.getEntity(), false, view, false);
    }

    // no auto fields in create
    private static ObjectSchema asObjectSchema(
            EntityDefinition objectSchemaDefinition, Boolean skipAutos) {
        return asObjectSchema(objectSchemaDefinition, skipAutos, null, false);
    }

    private static ObjectSchema asObjectSchema(
            EntityDefinition objectSchemaDefinition,
            Boolean skipAutos,
            EntityViewDefinition view,
            Boolean requestSchema) {
        ObjectSchema object = new ObjectSchema();
        object.setDescription(schemaDescriptionFor(objectSchemaDefinition));
        object.setTitle(view == null ? objectSchemaDefinition.getName() : view.getName());

        for (String propertyName : objectSchemaDefinition.getFieldNames()) {
            Field propertyDefinition = objectSchemaDefinition.getField(propertyName);
            if (view != null) {
                final boolean visible =
                        requestSchema
                                ? view.isRequestVisible(propertyName)
                                : view.isResponseVisible(propertyName);
                if (!visible) {
                    continue;
                }
            }
            if (skipAutos
                    && (propertyDefinition.getType() == FieldType.AUTO_GUID
                            || propertyDefinition.getType() == FieldType.AUTO_INCREMENT)) {
            } else {
                Schema<String> propertyItem = new Schema<>();
                final List<String> examples = propertyDefinition.getExamples();
                if (!examples.isEmpty()) {
                    propertyItem.setExample(
                            openApiExampleValueFor(propertyDefinition, examples.get(0)));
                }

                List<String> description = new ArrayList<>();
                if (propertyDefinition.hasDescription()) {
                    description.add(propertyDefinition.getDescription());
                }

                for (ValidationRule validationRule : propertyDefinition.getAllValidationRules()) {
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

    private static Object openApiExampleValueFor(final Field field, final String example) {
        if (example == null) {
            return null;
        }

        try {
            switch (field.getType()) {
                case AUTO_INCREMENT:
                case INTEGER:
                    return Integer.valueOf(example);
                case FLOAT:
                    return new BigDecimal(example);
                case BOOLEAN:
                    if ("true".equalsIgnoreCase(example) || "false".equalsIgnoreCase(example)) {
                        return Boolean.valueOf(example);
                    }
                    return example;
                default:
                    return example;
            }
        } catch (NumberFormatException e) {
            return example;
        }
    }

    private static String schemaDescriptionFor(final EntityDefinition objectSchemaDefinition) {
        if (objectSchemaDefinition.hasDescription()) {
            return objectSchemaDefinition.getDescription();
        }
        return objectSchemaDefinition.getName();
    }

    private static String joinStrings(List<String> description, String postfix) {
        StringBuilder joined = new StringBuilder();
        String prependSpace = "";
        for (String string : description) {
            joined.append(prependSpace);
            joined.append(string);
            if (!string.endsWith(postfix)) {
                joined.append(postfix);
            }
            prependSpace = " ";
        }
        return joined.toString();
    }

    // TODO: the output from swaggerizer json could be cached

    public String asJson() {
        return asJson(OpenApiSpecificationVersion.OPENAPI_3_1, false);
    }

    public String asJsonWithPreferredServer(final String preferredServerUrl) {
        return asJsonWithPreferredServer(
                OpenApiSpecificationVersion.OPENAPI_3_1, false, preferredServerUrl);
    }

    public String asJsonWithPreferredServer(
            final boolean permissive, final String preferredServerUrl) {
        return asJsonWithPreferredServer(
                OpenApiSpecificationVersion.OPENAPI_3_1, permissive, preferredServerUrl);
    }

    public String asJsonWithPreferredServer(
            final OpenApiSpecificationVersion version,
            final boolean permissive,
            final String preferredServerUrl) {
        if (version.requiresOpenApi32Finalization()) {
            return new OpenApi32Finalizer()
                    .finalizeJson(
                            asJsonWithPreferredServer(
                                    OpenApiSpecificationVersion.OPENAPI_3_1,
                                    permissive,
                                    preferredServerUrl));
        }

        final OpenAPI api = permissive ? swaggerPermissive(version) : swagger(version);
        preferServer(api, preferredServerUrl);
        return pretty(api, version);
    }

    public String asJson(boolean permissive) {
        return asJson(OpenApiSpecificationVersion.OPENAPI_3_1, permissive);
    }

    public String asJson(final OpenApiSpecificationVersion version) {
        return asJson(version, false);
    }

    public String asJson(final OpenApiSpecificationVersion version, boolean permissive) {
        if (version.requiresOpenApi32Finalization()) {
            return new OpenApi32Finalizer()
                    .finalizeJson(asJson(OpenApiSpecificationVersion.OPENAPI_3_1, permissive));
        }

        if (version != OpenApiSpecificationVersion.OPENAPI_3_1) {
            return pretty(permissive ? swaggerPermissive(version) : swagger(version), version);
        }

        if (apiNormal == null) {
            apiNormal = swagger();
        }
        if (apiPermissive == null) {
            apiPermissive = swaggerPermissive();
        }
        if (permissive) {
            return pretty(apiPermissive, version);
        } else {
            return pretty(apiNormal, version);
        }
    }

    private String pretty(final OpenAPI api, final OpenApiSpecificationVersion version) {
        if (version == OpenApiSpecificationVersion.OPENAPI_3_0) {
            return Json.pretty(api);
        }
        return Json31.pretty(api);
    }

    private void preferServer(final OpenAPI api, final String preferredServerUrl) {
        if (preferredServerUrl == null || preferredServerUrl.trim().isEmpty()) {
            return;
        }

        final String preferredUrl =
                configuredHttpsUrlForSameHost(preferredServerUrl.trim(), api.getServers());
        final List<Server> existingServers = api.getServers();
        final List<Server> reorderedServers = new ArrayList<>();
        Server preferredServer = null;

        if (existingServers != null) {
            for (Server server : existingServers) {
                if (sameServerUrl(preferredUrl, server.getUrl())) {
                    preferredServer = server;
                } else {
                    reorderedServers.add(server);
                }
            }
        }

        if (preferredServer == null) {
            return;
        }
        reorderedServers.add(0, preferredServer);
        api.setServers(reorderedServers);
    }

    private String configuredHttpsUrlForSameHost(
            final String preferredUrl, final List<Server> configuredServers) {
        final URI preferredUri = uriFrom(preferredUrl);
        if (preferredUri == null || !"http".equalsIgnoreCase(preferredUri.getScheme())) {
            return preferredUrl;
        }

        if (configuredServers == null) {
            return preferredUrl;
        }

        for (Server server : configuredServers) {
            if (server.getUrl() == null) {
                continue;
            }

            final String configuredUrl = server.getUrl().trim();
            final URI configuredUri = uriFrom(configuredUrl);
            if (configuredUri == null || !"https".equalsIgnoreCase(configuredUri.getScheme())) {
                continue;
            }

            if (sameHostAndPort(preferredUri, configuredUri)) {
                return configuredUrl;
            }
        }

        return preferredUrl;
    }

    private boolean sameHostAndPort(final URI preferredUri, final URI configuredUri) {
        if (!sameHost(preferredUri, configuredUri)) {
            return false;
        }
        if (preferredUri.getPort() == -1 && configuredUri.getPort() == -1) {
            return true;
        }
        return effectivePort(preferredUri) == effectivePort(configuredUri);
    }

    private boolean sameHost(final URI preferredUri, final URI configuredUri) {
        return preferredUri.getHost() != null
                && configuredUri.getHost() != null
                && preferredUri.getHost().equalsIgnoreCase(configuredUri.getHost());
    }

    private int effectivePort(final URI uri) {
        if (uri.getPort() != -1) {
            return uri.getPort();
        }
        if ("https".equalsIgnoreCase(uri.getScheme())) {
            return 443;
        }
        if ("http".equalsIgnoreCase(uri.getScheme())) {
            return 80;
        }
        return -1;
    }

    private URI uriFrom(final String url) {
        try {
            return URI.create(url);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean sameServerUrl(final String preferredUrl, final String configuredUrl) {
        if (configuredUrl == null) {
            return false;
        }
        return removeTrailingSlash(preferredUrl).equals(removeTrailingSlash(configuredUrl.trim()));
    }

    private String removeTrailingSlash(final String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }
}
