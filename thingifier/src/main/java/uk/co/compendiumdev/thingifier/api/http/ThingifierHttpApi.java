package uk.co.compendiumdev.thingifier.api.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.flipkart.zjsonpatch.JsonPatch;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.SchemaCatalog;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierSchemaCatalog;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.CollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.InstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipCollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRouteMapper;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiResponseHook;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyField;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.JsonBodyValueConverter;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiRouteRule;
import uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle;
import uk.co.compendiumdev.thingifier.application.schema.RelationshipSpec;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityViewDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public final class ThingifierHttpApi {

    // TODO: each 'session' could have its own thingifier to support multiple users
    // TODO: would need the ability to create and delete sessions
    public static final String HTTP_SESSION_HEADER_NAME = "X-THING-HTTP-SESSION-GUID";
    public static final String ACCEPT_QUERY_HEADER = "Accept-Query";
    public static final String QUERY_CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static final String JSONPATH_QUERY_CONTENT_TYPE = "application/jsonpath";
    public static final List<String> SUPPORTED_QUERY_CONTENT_TYPES_LIST =
            List.of(QUERY_CONTENT_TYPE, JSONPATH_QUERY_CONTENT_TYPE);
    public static final String SUPPORTED_QUERY_CONTENT_TYPES =
            String.join(", ", SUPPORTED_QUERY_CONTENT_TYPES_LIST);

    private final Thingifier thingifier;
    private final JsonThing jsonThing;
    private List<HttpApiRequestHook> apiRequestHooks;
    private List<HttpApiResponseHook> apiResponseHooks;

    public enum HttpVerb {
        GET,
        DELETE,
        POST,
        PUT,
        HEAD,
        QUERY,
        // NOT Handled
        OPTIONS,
        PATCH,
        TRACE
    };

    public ThingifierHttpApi(final Thingifier aThingifier) {
        this(aThingifier, null, null);
    }

    public ThingifierHttpApi(
            final Thingifier aThingifier,
            List<HttpApiRequestHook> apiRequestHooks,
            List<HttpApiResponseHook> apiResponseHooks) {
        this.thingifier = aThingifier;

        // request hooks are used to do initial processing and possibly prevent processing
        if (apiRequestHooks == null) {
            this.apiRequestHooks = new ArrayList<>();
        } else {
            this.apiRequestHooks = apiRequestHooks;
        }

        // response hooks are used after the main API processing and possibly override values
        if (apiResponseHooks == null) {
            this.apiResponseHooks = new ArrayList<>();
        } else {
            this.apiResponseHooks = apiResponseHooks;
        }

        jsonThing = new JsonThing(thingifier.apiConfig().jsonOutput());
    }

    private HttpApiResponse handleRequest(final HttpApiRequest request, HttpVerb verb) {

        // if the request.url has the 'prefix' then remove the prefix and process the request
        // if(request.getPath())

        String prefix = thingifier.apiConfig().getApiEndPointPrefix();
        if (prefix != null && !prefix.isEmpty()) {
            if (prefix.startsWith("/")) {
                prefix = prefix.substring(1);
            }
            request.removePrefixFromPath(prefix);
        }

        final HttpVerb effectiveVerb = MethodOverrideParser.getEffectiveVerb(request, verb);
        if (isDisabledByApiSpec(request, effectiveVerb)) {
            return disabledRouteResponse(request);
        }

        // any pre-request override processing
        HttpApiResponse httpResponse = runTheHttpApiRequestHooksOn(request);

        // TODO: consider 'validation' hooks which can be used to override/augment validation

        // validate request syntax
        if (httpResponse == null) {
            httpResponse = validateRequestSyntax(request, effectiveVerb);
        }

        if (httpResponse == null) {
            httpResponse = validateEntityViewInput(request, effectiveVerb);
        }

        // TODO: consider 'processing' hooks which can be used to override the generic processing

        // no httpResponse generated after validation so it is not in error
        if (httpResponse == null) {
            ApiResponse apiResponse = routeAndProcessRequest(request, effectiveVerb);

            httpResponse =
                    new HttpApiResponse(
                            request.getHeaders(), apiResponse, jsonThing, thingifier.apiConfig());

            if (effectiveVerb == HttpVerb.HEAD) {
                final int bodyLength =
                        httpResponse.getBody().getBytes(StandardCharsets.UTF_8).length;
                apiResponse.clearBody();
                apiResponse.setHeader("Content-Length", Integer.toString(bodyLength));
                httpResponse =
                        new HttpApiResponse(
                                request.getHeaders(),
                                apiResponse,
                                jsonThing,
                                thingifier.apiConfig());
            }
        }

        // run any post processing response hooks
        return runTheHttpApiResponseHooksOn(request, httpResponse);
    }

    private boolean isDisabledByApiSpec(final HttpApiRequest request, final HttpVerb verb) {
        return routeRuleFor(request, verb).map(ThingifierApiRouteRule::isDisabled).orElse(false);
    }

    private Optional<ThingifierApiRouteRule> routeRuleFor(
            final HttpApiRequest request, final HttpVerb verb) {
        try {
            return thingifier
                    .apiSpec()
                    .ruleFor(
                            RoutingVerb.valueOf(verb.name()),
                            request.getPath(),
                            thingifier.apiConfig().getApiEndPointPrefix());
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private HttpApiResponse disabledRouteResponse(final HttpApiRequest request) {
        return new HttpApiResponse(
                request.getHeaders(),
                ApiResponse.error404("Could not find any instances with " + request.getPath()),
                jsonThing,
                thingifier.apiConfig());
    }

    /** return an error response if the request is invalid, null if valid */
    public HttpApiResponse validateRequestSyntax(
            final HttpApiRequest request, final HttpVerb verb) {

        final HttpApiRequestValidator requestValidator =
                new HttpApiRequestValidator(thingifier.apiConfig());

        HttpApiResponse httpResponse = null;

        if (!requestValidator.validateSyntax(request, verb)) {

            httpResponse =
                    new HttpApiResponse(
                            request.getHeaders(),
                            requestValidator.getErrorApiResponse(),
                            jsonThing,
                            thingifier.apiConfig());
        }

        return httpResponse;
    }

    public ApiResponse routeAndProcessRequest(final HttpApiRequest request, HttpVerb verb) {

        ApiResponse apiResponse = null;
        ApiRequestEnvelope envelope =
                ApiRequestEnvelope.from(request, verb, thingifier.getThingNames());

        switch (verb) {
            case GET:
                apiResponse = thingifier.api().get(envelope);
                break;
            case HEAD:
                apiResponse = thingifier.api().get(envelope);
                break;
            case QUERY:
                apiResponse = thingifier.api().query(envelope);
                break;
            case DELETE:
                apiResponse = thingifier.api().delete(envelope);
                break;
            case POST:
                apiResponse = thingifier.api().post(envelope);
                break;
            case PUT:
                apiResponse = thingifier.api().put(envelope);
                break;
            case PATCH:
                apiResponse = thingifier.api().patch(envelope);
                break;
            default:
                break;
        }

        applyResponseEntityView(request, verb, apiResponse);
        return apiResponse;
    }

    private HttpApiResponse validateEntityViewInput(
            final HttpApiRequest request, final HttpVerb verb) {
        if (verb != HttpVerb.POST && verb != HttpVerb.PUT && verb != HttpVerb.PATCH) {
            return null;
        }

        final Optional<ThingifierApiRouteRule> matchingRule = routeRuleFor(request, verb);
        if (matchingRule.isEmpty() || !matchingRule.get().hasRequestEntityView()) {
            return null;
        }

        final EntityDefinition entity = targetEntityFor(request.getPath());
        if (entity == null) {
            return null;
        }

        final String viewName = matchingRule.get().getRequestEntityView();
        if (!entity.hasViewNamed(viewName)) {
            return new HttpApiResponse(
                    request.getHeaders(),
                    ApiResponse.error(
                            500,
                            String.format(
                                    "Entity view %s is not defined for %s",
                                    viewName, entity.getName())),
                    jsonThing,
                    thingifier.apiConfig());
        }

        final EntityViewDefinition view = entity.getViewNamed(viewName);
        final List<String> disallowedFields = new ArrayList<>();
        for (ApiBodyField field : entityViewInputFields(request, verb, entity)) {
            if (entity.hasFieldNameDefined(field.name()) && !view.isInputAllowed(field.name())) {
                disallowedFields.add(field.name());
            }
        }

        if (disallowedFields.isEmpty()) {
            return null;
        }

        return new HttpApiResponse(
                request.getHeaders(),
                ApiResponse.error(
                        422,
                        String.format(
                                "Fields are not allowed by %s: %s",
                                viewName, String.join(", ", disallowedFields))),
                jsonThing,
                thingifier.apiConfig());
    }

    private List<ApiBodyField> entityViewInputFields(
            final HttpApiRequest request, final HttpVerb verb, final EntityDefinition entity) {
        if (verb == HttpVerb.PATCH) {
            return patchInputFields(request, entity);
        }

        return ApiRequestEnvelope.from(request, verb, thingifier.getThingNames())
                .bodyFields()
                .topLevelFields();
    }

    private List<ApiBodyField> patchInputFields(
            final HttpApiRequest request, final EntityDefinition entity) {
        final Optional<EntityPatchUpdateStyle> style =
                EntityPatchUpdateStyle.fromContentType(request.getContentTypeHeader());
        if (style.isEmpty()) {
            return List.of();
        }

        if (style.get() == EntityPatchUpdateStyle.JSON_PATCH_RFC6902) {
            return jsonPatchInputFields(request, entity);
        }

        return objectPatchInputFields(request.getBody());
    }

    private List<ApiBodyField> objectPatchInputFields(final String rawBody) {
        try {
            final JsonNode document = JsonBodyValueConverter.readTree(rawBody);
            if (document == null || !document.isObject()) {
                return List.of();
            }
            return bodyFieldsForObject(document);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            return List.of();
        }
    }

    private List<ApiBodyField> jsonPatchInputFields(
            final HttpApiRequest request, final EntityDefinition entity) {
        final List<ApiBodyField> inputFields = new ArrayList<>();
        final JsonNode operations;
        try {
            operations = JsonBodyValueConverter.readTree(request.getBody());
        } catch (JsonProcessingException | IllegalArgumentException e) {
            return inputFields;
        }

        if (operations == null || !operations.isArray()) {
            return inputFields;
        }

        for (JsonNode operation : operations) {
            if (!operation.isObject()) {
                continue;
            }

            addJsonPointerTopLevelField(inputFields, operation.get("path"));
            addJsonPointerTopLevelField(inputFields, operation.get("from"));
            addRootReplacementFields(inputFields, operation);
        }

        rootResultFieldsForJsonPatch(request, entity, operations).ifPresent(inputFields::addAll);

        return inputFields;
    }

    private void addRootReplacementFields(
            final List<ApiBodyField> inputFields, final JsonNode operation) {
        final JsonNode path = operation.get("path");
        final JsonNode value = operation.get("value");
        if (path != null
                && path.isTextual()
                && path.asText().isEmpty()
                && value != null
                && value.isObject()) {
            inputFields.addAll(bodyFieldsForObject(value));
        }
    }

    private List<ApiBodyField> bodyFieldsForObject(final JsonNode document) {
        return ApiBodyFields.fromMap(JsonBodyValueConverter.objectNodeAsMap(document))
                .topLevelFields();
    }

    private Optional<List<ApiBodyField>> rootResultFieldsForJsonPatch(
            final HttpApiRequest request,
            final EntityDefinition entity,
            final JsonNode operations) {
        if (!hasRootReplacementOperation(operations)) {
            return Optional.empty();
        }

        final EntityInstance instance = targetInstanceFor(request, entity);
        if (instance == null) {
            return Optional.empty();
        }

        try {
            final JsonNode currentDocument =
                    JsonBodyValueConverter.readTree(jsonThing.asJsonObject(instance).toString());
            final JsonNode patchedDocument = JsonPatch.apply(operations, currentDocument);
            if (patchedDocument != null && patchedDocument.isObject()) {
                return Optional.of(bodyFieldsForObject(patchedDocument));
            }
        } catch (JsonProcessingException | RuntimeException e) {
            return Optional.empty();
        }

        return Optional.empty();
    }

    private boolean hasRootReplacementOperation(final JsonNode operations) {
        for (JsonNode operation : operations) {
            if (!operation.isObject()) {
                continue;
            }

            final JsonNode path = operation.get("path");
            if (path != null
                    && path.isTextual()
                    && path.asText().isEmpty()
                    && rootReplacementOperation(operation.get("op"))) {
                return true;
            }
        }
        return false;
    }

    private boolean rootReplacementOperation(final JsonNode operationNode) {
        if (operationNode == null || !operationNode.isTextual()) {
            return false;
        }

        switch (operationNode.asText()) {
            case "add":
            case "replace":
            case "copy":
            case "move":
                return true;
            default:
                return false;
        }
    }

    private EntityInstance targetInstanceFor(
            final HttpApiRequest request, final EntityDefinition entity) {
        final SchemaCatalog schema = new ThingifierSchemaCatalog(thingifier);
        final ThingRoute route = new ThingRouteMapper(schema).map(request.getPath());
        if (!(route instanceof InstanceRoute)) {
            return null;
        }

        return ThingifierRequestContext.from(thingifier, request.getHeaders())
                .store()
                .entityQueries()
                .findByQueryIdentifier(entity, ((InstanceRoute) route).identifier());
    }

    private void addJsonPointerTopLevelField(
            final List<ApiBodyField> inputFields, final JsonNode pointerNode) {
        if (pointerNode == null || !pointerNode.isTextual()) {
            return;
        }

        final String fieldName = jsonPointerTopLevelFieldName(pointerNode.asText());
        if (fieldName != null) {
            inputFields.add(new ApiBodyField(fieldName, ""));
        }
    }

    private String jsonPointerTopLevelFieldName(final String pointer) {
        if (pointer == null || pointer.isEmpty() || !pointer.startsWith("/")) {
            return null;
        }

        final int nextSeparator = pointer.indexOf("/", 1);
        final String escapedToken =
                nextSeparator == -1 ? pointer.substring(1) : pointer.substring(1, nextSeparator);
        return escapedToken.replace("~1", "/").replace("~0", "~");
    }

    private void applyResponseEntityView(
            final HttpApiRequest request, final HttpVerb verb, final ApiResponse apiResponse) {
        if (apiResponse == null
                || apiResponse.isErrorResponse()
                || apiResponse.hasABodyOverride()) {
            return;
        }

        final Optional<ThingifierApiRouteRule> matchingRule = routeRuleFor(request, verb);
        if (matchingRule.isEmpty()) {
            return;
        }

        final String viewName =
                matchingRule.get().responseEntityViewFor(apiResponse.getStatusCode());
        if (viewName == null || apiResponse.getTypeOfThingReturned() == null) {
            return;
        }

        final EntityDefinition entity = apiResponse.getTypeOfThingReturned();
        if (entity.hasViewNamed(viewName)) {
            apiResponse.usingEntityView(entity.getViewNamed(viewName));
        }
    }

    private EntityDefinition targetEntityFor(final String path) {
        final SchemaCatalog schema = new ThingifierSchemaCatalog(thingifier);
        final ThingRoute route = new ThingRouteMapper(schema).map(path);
        if (route instanceof CollectionRoute) {
            return schema.definitionWithSingularOrPluralNamed(
                    ((CollectionRoute) route).entity().name());
        }
        if (route instanceof InstanceRoute) {
            return schema.definitionWithSingularOrPluralNamed(
                    ((InstanceRoute) route).entity().name());
        }
        if (route instanceof RelationshipCollectionRoute) {
            final RelationshipCollectionRoute relationship = (RelationshipCollectionRoute) route;
            for (RelationshipSpec spec : relationship.parentEntity().relationships()) {
                if (spec.name().equals(relationship.relationshipName())) {
                    return schema.definitionWithSingularOrPluralNamed(spec.toEntityName());
                }
            }
        }
        return null;
    }

    public HttpApiResponse get(final HttpApiRequest request) {
        return handleRequest(request, HttpVerb.GET);
    }

    public HttpApiResponse head(final HttpApiRequest request) {
        return handleRequest(request, HttpVerb.HEAD);
    }

    public HttpApiResponse delete(final HttpApiRequest request) {
        return handleRequest(request, HttpVerb.DELETE);
    }

    public HttpApiResponse post(final HttpApiRequest request) {
        return handleRequest(request, HttpVerb.POST);
    }

    public HttpApiResponse put(final HttpApiRequest request) {
        return handleRequest(request, HttpVerb.PUT);
    }

    public HttpApiResponse patch(final HttpApiRequest request) {
        return handleRequest(request, HttpVerb.PATCH);
    }

    public HttpApiResponse queryRequest(final HttpApiRequest request) {
        return handleRequest(request, HttpVerb.QUERY);
    }

    public HttpApiResponse query(final HttpApiRequest request, final String query) {

        HttpApiResponse httpResponse = runTheHttpApiRequestHooksOn(request);

        if (httpResponse == null) {
            ApiResponse apiResponse =
                    thingifier
                            .api()
                            .get(query, request.getFilterableQueryParams(), request.getHeaders());
            httpResponse =
                    new HttpApiResponse(
                            request.getHeaders(), apiResponse, jsonThing, thingifier.apiConfig());
        }

        return runTheHttpApiResponseHooksOn(request, httpResponse);
    }

    private HttpApiResponse runTheHttpApiResponseHooksOn(
            final HttpApiRequest request, final HttpApiResponse response) {
        for (HttpApiResponseHook hook : apiResponseHooks) {
            HttpApiResponse returnImmediately = hook.run(request, response, thingifier.apiConfig());
            if (returnImmediately != null) {
                return returnImmediately;
            }
        }
        return response;
    }

    private HttpApiResponse runTheHttpApiRequestHooksOn(final HttpApiRequest request) {
        for (HttpApiRequestHook hook : apiRequestHooks) {
            HttpApiResponse response = hook.run(request, thingifier.apiConfig());
            if (response != null) {
                return response;
            }
        }
        return null;
    }
}
