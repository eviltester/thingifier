package uk.co.compendiumdev.thingifier.api.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.flipkart.zjsonpatch.JsonPatch;
import com.flipkart.zjsonpatch.JsonPatchApplicationException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.hooks.ScopedHook;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.DefaultThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.SchemaCatalog;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierSchemaCatalog;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.CollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.InstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipCollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipInstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRouteMapper;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleContext;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleHookRegistry;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiHookRegistry;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiResponseHook;
import uk.co.compendiumdev.thingifier.api.ThingifierRestAPIHandler;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyField;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.JsonBodyValueConverter;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.response.EntityResponseViewResolver;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiRouteRule;
import uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle;
import uk.co.compendiumdev.thingifier.application.schema.RelationshipSpec;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityViewDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

/**
 * HTTP-facing adapter for the generated Thingifier REST API.
 *
 * <p>The adapter coordinates legacy HTTP request/response hooks, lifecycle hooks, request syntax
 * validation, direct API handler calls, response view policy, and final HTTP response rendering.
 */
public final class ThingifierHttpApi {

    // TODO: each 'session' could have its own thingifier to support multiple users
    // TODO: would need the ability to create and delete sessions
    public static final String HTTP_SESSION_HEADER_NAME = "X-THING-HTTP-SESSION-GUID";
    public static final String ACCEPT_QUERY_HEADER = "Accept-Query";
    public static final String QUERY_CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static final String JSONPATH_QUERY_CONTENT_TYPE = "application/jsonpath";
    public static final String STRUCTURED_QUERY_CONTENT_TYPE =
            "application/vnd.thingifier.query+json";
    public static final List<String> SUPPORTED_QUERY_CONTENT_TYPES_LIST =
            List.of(QUERY_CONTENT_TYPE, JSONPATH_QUERY_CONTENT_TYPE, STRUCTURED_QUERY_CONTENT_TYPE);
    public static final String SUPPORTED_QUERY_CONTENT_TYPES =
            String.join(", ", SUPPORTED_QUERY_CONTENT_TYPES_LIST);

    private final Thingifier thingifier;
    private final JsonThing jsonThing;
    private List<HttpApiRequestHook> apiRequestHooks;
    private List<HttpApiResponseHook> apiResponseHooks;
    private final HttpApiHookRegistry apiHookRegistry;
    private final ThingifierApiLifecycleHookRegistry lifecycleHooks;

    /**
     * Verbs understood by the Thingifier HTTP adapter.
     *
     * <p>The adapter keeps this enum separate from {@link RoutingVerb} because HTTP handling also
     * sees methods such as OPTIONS and TRACE which may not be processed by the generated Thingifier
     * API lifecycle.
     */
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

    /**
     * Creates an HTTP API adapter with no legacy or lifecycle hooks.
     *
     * @param aThingifier Thingifier model and configuration
     */
    public ThingifierHttpApi(final Thingifier aThingifier) {
        this(aThingifier, (List<HttpApiRequestHook>) null, (List<HttpApiResponseHook>) null);
    }

    /**
     * Creates an HTTP API adapter using legacy list-based request and response hooks.
     *
     * <p>This constructor is kept for backwards compatibility; scoped hook registries and lifecycle
     * hooks can be supplied through the newer constructors.
     *
     * @param aThingifier Thingifier model and configuration
     * @param apiRequestHooks legacy hooks run before API processing
     * @param apiResponseHooks legacy hooks run after API processing
     */
    public ThingifierHttpApi(
            final Thingifier aThingifier,
            List<HttpApiRequestHook> apiRequestHooks,
            List<HttpApiResponseHook> apiResponseHooks) {
        this(
                aThingifier,
                apiRequestHooks,
                apiResponseHooks,
                new ThingifierApiLifecycleHookRegistry());
    }

    /**
     * Creates an HTTP API adapter using legacy hook lists plus lifecycle hooks.
     *
     * @param aThingifier Thingifier model and configuration
     * @param apiRequestHooks legacy hooks run before API processing
     * @param apiResponseHooks legacy hooks run after API processing
     * @param lifecycleHooks lifecycle hooks run inside Thingifier API processing
     */
    public ThingifierHttpApi(
            final Thingifier aThingifier,
            List<HttpApiRequestHook> apiRequestHooks,
            List<HttpApiResponseHook> apiResponseHooks,
            final ThingifierApiLifecycleHookRegistry lifecycleHooks) {
        this.thingifier = aThingifier;
        this.apiHookRegistry = null;
        this.lifecycleHooks =
                lifecycleHooks == null ? new ThingifierApiLifecycleHookRegistry() : lifecycleHooks;

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

    /**
     * Creates an HTTP API adapter with the scoped legacy hook registry.
     *
     * @param aThingifier Thingifier model and configuration
     * @param apiHookRegistry scoped request/response hook registry
     */
    public ThingifierHttpApi(
            final Thingifier aThingifier, final HttpApiHookRegistry apiHookRegistry) {
        this(aThingifier, apiHookRegistry, new ThingifierApiLifecycleHookRegistry());
    }

    /**
     * Creates an HTTP API adapter with scoped legacy hooks and lifecycle hooks.
     *
     * @param aThingifier Thingifier model and configuration
     * @param apiHookRegistry scoped request/response hook registry
     * @param lifecycleHooks lifecycle hooks run inside Thingifier API processing
     * @return configured HTTP API adapter
     */
    public static ThingifierHttpApi withHookRegistries(
            final Thingifier aThingifier,
            final HttpApiHookRegistry apiHookRegistry,
            final ThingifierApiLifecycleHookRegistry lifecycleHooks) {
        return new ThingifierHttpApi(aThingifier, apiHookRegistry, lifecycleHooks);
    }

    private ThingifierHttpApi(
            final Thingifier aThingifier,
            final HttpApiHookRegistry apiHookRegistry,
            final ThingifierApiLifecycleHookRegistry lifecycleHooks) {
        this.thingifier = aThingifier;
        this.apiRequestHooks = new ArrayList<>();
        this.apiResponseHooks = new ArrayList<>();
        this.apiHookRegistry =
                apiHookRegistry == null ? new HttpApiHookRegistry() : apiHookRegistry;
        this.lifecycleHooks =
                lifecycleHooks == null ? new ThingifierApiLifecycleHookRegistry() : lifecycleHooks;
        jsonThing = new JsonThing(thingifier.apiConfig().jsonOutput());
    }

    /**
     * Processes one HTTP API request through hooks, validation, routing, and response hooks.
     *
     * <p>The order deliberately preserves legacy request hooks first and legacy response hooks
     * last, with lifecycle hooks only applied to generated Thingifier API operations.
     *
     * @param request HTTP API request
     * @param verb original HTTP method before method override handling
     * @return rendered HTTP API response
     */
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

        // any pre-request override processing
        HttpApiResponse httpResponse = runTheHttpApiRequestHooksOn(request, effectiveVerb);

        ThingifierApiLifecycleContext lifecycle = null;
        if (httpResponse == null && supportsLifecycle(effectiveVerb)) {
            lifecycle = lifecycleContextFor(request, effectiveVerb);
            lifecycleHooks.runRouteMatchedHooks(lifecycle);
            if (lifecycle.shouldShortCircuit()) {
                httpResponse = httpResponseFor(request, effectiveVerb, lifecycle.apiResponse());
            }
        }

        if (httpResponse == null && isDisabledByApiSpec(request, effectiveVerb)) {
            httpResponse = disabledRouteResponse(request);
        }

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
            if (lifecycle != null) {
                ApiRequestEnvelope parsedEnvelope =
                        ApiRequestEnvelope.from(
                                request, effectiveVerb, xmlEntityNamesFor(request.getPath()));
                lifecycle.applyParsedEnvelope(parsedEnvelope);
                lifecycleHooks.runBodyParsedHooks(lifecycle);
                if (lifecycle.shouldShortCircuit()) {
                    httpResponse = httpResponseFor(request, effectiveVerb, lifecycle.apiResponse());
                } else {
                    ApiResponse apiResponse = routeAndProcessRequest(lifecycle);
                    httpResponse = httpResponseFor(request, effectiveVerb, apiResponse);
                }
            } else {
                ApiResponse apiResponse = routeAndProcessRequest(request, effectiveVerb);
                httpResponse = httpResponseFor(request, effectiveVerb, apiResponse);
            }
        }

        // run any post processing response hooks
        return runTheHttpApiResponseHooksOn(request, httpResponse, effectiveVerb);
    }

    /**
     * Builds the lifecycle context after the route has been mapped.
     *
     * @param request HTTP API request
     * @param effectiveVerb verb after method override handling
     * @return lifecycle context shared by every lifecycle phase
     */
    private ThingifierApiLifecycleContext lifecycleContextFor(
            final HttpApiRequest request, final HttpVerb effectiveVerb) {
        ThingifierApiRuntime runtime = new DefaultThingifierApiRuntime(thingifier);
        ThingRoute route = new ThingRouteMapper(runtime.schema()).map(request.getPath());
        return new ThingifierApiLifecycleContext(
                runtime,
                request,
                effectiveVerb,
                routingVerbFor(effectiveVerb),
                route,
                thingifier.apiConfig().getApiEndPointPrefix());
    }

    /**
     * Reports whether lifecycle hooks should run for a verb.
     *
     * @param verb effective HTTP API verb
     * @return true for dynamic Thingifier API operations
     */
    private boolean supportsLifecycle(final HttpVerb verb) {
        switch (verb) {
            case GET:
            case HEAD:
            case QUERY:
            case DELETE:
            case POST:
            case PUT:
            case PATCH:
                return true;
            default:
                return false;
        }
    }

    /**
     * Converts an {@link ApiResponse} to an HTTP response, including HEAD body suppression.
     *
     * @param request original HTTP API request
     * @param effectiveVerb verb after method override handling
     * @param apiResponse structured API response
     * @return rendered HTTP API response
     */
    private HttpApiResponse httpResponseFor(
            final HttpApiRequest request,
            final HttpVerb effectiveVerb,
            final ApiResponse apiResponse) {
        HttpApiResponse httpResponse =
                new HttpApiResponse(
                        request.getHeaders(),
                        apiResponse,
                        jsonThing,
                        thingifier.apiConfig(),
                        xmlEntityNamesFor(request.getPath()));

        if (effectiveVerb == HttpVerb.HEAD) {
            final int bodyLength = httpResponse.getBody().getBytes(StandardCharsets.UTF_8).length;
            apiResponse.clearBody();
            apiResponse.setHeader("Content-Length", Integer.toString(bodyLength));
            httpResponse =
                    new HttpApiResponse(
                            request.getHeaders(),
                            apiResponse,
                            jsonThing,
                            thingifier.apiConfig(),
                            xmlEntityNamesFor(request.getPath()));
        }
        return httpResponse;
    }

    /**
     * Reports whether API spec disables the matched generated route.
     *
     * @param request HTTP API request
     * @param verb effective HTTP API verb
     * @return true when a route rule disables this request
     */
    private boolean isDisabledByApiSpec(final HttpApiRequest request, final HttpVerb verb) {
        return routeRuleFor(request, verb).map(ThingifierApiRouteRule::isDisabled).orElse(false);
    }

    /**
     * Finds the API spec route rule matching the request and verb.
     *
     * @param request HTTP API request
     * @param verb effective HTTP API verb
     * @return matching route rule, or empty when the verb is not a generated routing verb
     */
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

    /**
     * Creates the legacy not-found style response used for disabled generated routes.
     *
     * @param request HTTP API request
     * @return HTTP API response representing the disabled route
     */
    private HttpApiResponse disabledRouteResponse(final HttpApiRequest request) {
        return new HttpApiResponse(
                request.getHeaders(),
                ApiResponse.error404("Could not find any instances with " + request.getPath()),
                jsonThing,
                thingifier.apiConfig(),
                xmlEntityNamesFor(request.getPath()));
    }

    /**
     * Validates HTTP request syntax before generated API processing.
     *
     * @param request HTTP API request
     * @param verb effective HTTP API verb
     * @return error response when syntax is invalid, otherwise null
     */
    public HttpApiResponse validateRequestSyntax(
            final HttpApiRequest request, final HttpVerb verb) {

        final HttpApiRequestValidator requestValidator =
                new HttpApiRequestValidator(
                        thingifier.apiConfig(), xmlEntityNamesFor(request.getPath()));

        HttpApiResponse httpResponse = null;

        if (!requestValidator.validateSyntax(request, verb)) {

            httpResponse =
                    new HttpApiResponse(
                            request.getHeaders(),
                            requestValidator.getErrorApiResponse(),
                            jsonThing,
                            thingifier.apiConfig(),
                            xmlEntityNamesFor(request.getPath()));
        }

        return httpResponse;
    }

    /**
     * Routes and processes a request without lifecycle hook state.
     *
     * <p>This path is used by backwards-compatible constructors and direct adapter calls. Response
     * entity view policy is still applied after the direct API handler returns.
     *
     * @param request HTTP API request
     * @param verb effective HTTP API verb
     * @return structured API response
     */
    public ApiResponse routeAndProcessRequest(final HttpApiRequest request, HttpVerb verb) {

        ApiResponse apiResponse = null;
        ApiRequestEnvelope envelope =
                ApiRequestEnvelope.from(request, verb, xmlEntityNamesFor(request.getPath()));

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

    /**
     * Routes and processes a request using an existing lifecycle context.
     *
     * <p>The context carries parsed body data, mapped commands or queries, hook replacements, and
     * the final response through the lifecycle phases.
     *
     * @param lifecycle lifecycle context for the request
     * @return structured API response
     */
    public ApiResponse routeAndProcessRequest(final ThingifierApiLifecycleContext lifecycle) {

        ApiResponse apiResponse = null;
        ApiRequestEnvelope envelope = lifecycle.toEnvelope();
        ThingifierRestAPIHandler api =
                new ThingifierRestAPIHandler(lifecycle.runtime(), lifecycleHooks);

        switch (lifecycle.effectiveVerb()) {
            case GET:
                apiResponse = api.get(envelope, lifecycle);
                break;
            case HEAD:
                apiResponse = api.get(envelope, lifecycle);
                break;
            case QUERY:
                apiResponse = api.query(envelope, lifecycle);
                break;
            case DELETE:
                apiResponse = api.delete(envelope, lifecycle);
                break;
            case POST:
                apiResponse = api.post(envelope, lifecycle);
                break;
            case PUT:
                apiResponse = api.put(envelope, lifecycle);
                break;
            case PATCH:
                apiResponse = api.patch(envelope, lifecycle);
                break;
            default:
                break;
        }

        applyResponseEntityView(lifecycle.request(), lifecycle.effectiveVerb(), apiResponse);
        return apiResponse;
    }

    /**
     * Rejects write input fields that are not allowed by the configured request entity view.
     *
     * <p>Route request views take precedence over entity default request views. This check happens
     * before body-parsed lifecycle hooks because it protects the public API input contract.
     *
     * @param request HTTP API request
     * @param verb effective HTTP API verb
     * @return error response when disallowed input fields are present, otherwise null
     */
    private HttpApiResponse validateEntityViewInput(
            final HttpApiRequest request, final HttpVerb verb) {
        if (verb != HttpVerb.POST && verb != HttpVerb.PUT && verb != HttpVerb.PATCH) {
            return null;
        }

        final EntityDefinition entity = targetEntityFor(request.getPath());
        if (entity == null) {
            return null;
        }

        final Optional<String> configuredViewName =
                thingifier
                        .apiSpec()
                        .requestEntityViewFor(
                                routingVerbFor(verb),
                                request.getPath(),
                                thingifier.apiConfig().getApiEndPointPrefix(),
                                entity);
        if (configuredViewName.isEmpty()) {
            return null;
        }

        final String viewName = configuredViewName.get();
        if (!entity.hasViewNamed(viewName)) {
            return new HttpApiResponse(
                    request.getHeaders(),
                    ApiResponse.error(
                            500,
                            String.format(
                                    "Entity view %s is not defined for %s",
                                    viewName, entity.getName())),
                    jsonThing,
                    thingifier.apiConfig(),
                    xmlEntityNamesFor(request.getPath()));
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
                thingifier.apiConfig(),
                xmlEntityNamesFor(request.getPath()));
    }

    /**
     * Extracts body fields relevant to entity-view input validation.
     *
     * @param request HTTP API request
     * @param verb effective HTTP API verb
     * @param entity target entity definition
     * @return fields that may be checked against the input view
     */
    private List<ApiBodyField> entityViewInputFields(
            final HttpApiRequest request, final HttpVerb verb, final EntityDefinition entity) {
        if (verb == HttpVerb.PATCH) {
            return patchInputFields(request, entity);
        }

        return ApiRequestEnvelope.from(request, verb, xmlEntityNamesFor(request.getPath()))
                .bodyFields()
                .topLevelFields();
    }

    /**
     * Extracts field names affected by a PATCH request.
     *
     * @param request HTTP API request
     * @param entity target entity definition
     * @return fields touched by the patch document
     */
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

    /**
     * Extracts top-level fields from Thingifier's object-style PATCH body.
     *
     * @param rawBody raw request body
     * @return fields present in the object patch document
     */
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

    /**
     * Extracts top-level fields touched by an RFC 6902 JSON Patch document.
     *
     * @param request HTTP API request
     * @param entity target entity definition
     * @return fields referenced or produced by the patch document
     */
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

    /**
     * Adds fields supplied by a JSON Patch operation which replaces the document root.
     *
     * @param inputFields accumulating field list
     * @param operation JSON Patch operation node
     */
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

    /**
     * Converts a JSON object into Thingifier body fields.
     *
     * @param document JSON object node
     * @return top-level body fields
     */
    private List<ApiBodyField> bodyFieldsForObject(final JsonNode document) {
        return ApiBodyFields.fromMap(JsonBodyValueConverter.objectNodeAsMap(document))
                .topLevelFields();
    }

    /**
     * Determines fields that would exist after a root-replacing JSON Patch operation.
     *
     * <p>Root replacement can introduce or remove many fields at once, so the existing instance is
     * patched in memory and the resulting top-level fields are checked against the request view.
     *
     * @param request HTTP API request
     * @param entity target entity definition
     * @param operations JSON Patch operation array
     * @return resulting top-level fields when they can be calculated
     */
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
        } catch (JsonProcessingException
                | JsonPatchApplicationException
                | IllegalArgumentException e) {
            return Optional.empty();
        }

        return Optional.empty();
    }

    /**
     * Reports whether a JSON Patch document contains an operation against the root path.
     *
     * @param operations JSON Patch operation array
     * @return true when a supported operation targets the root document
     */
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

    /**
     * Reports whether a JSON Patch operation can replace the root document.
     *
     * @param operationNode patch operation name node
     * @return true when the operation may produce a new root object
     */
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

    /**
     * Resolves the instance targeted by an instance route.
     *
     * @param request HTTP API request
     * @param entity target entity definition
     * @return matching instance, or null when the route is not an instance route
     */
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

    /**
     * Adds the top-level field name from a JSON Pointer.
     *
     * @param inputFields accumulating field list
     * @param pointerNode JSON Pointer node from a patch operation
     */
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

    /**
     * Extracts the first path segment from a JSON Pointer.
     *
     * @param pointer JSON Pointer text
     * @return unescaped top-level field name, or null for a root/non-pointer value
     */
    private String jsonPointerTopLevelFieldName(final String pointer) {
        if (pointer == null || pointer.isEmpty() || !pointer.startsWith("/")) {
            return null;
        }

        final int nextSeparator = pointer.indexOf("/", 1);
        final String escapedToken =
                nextSeparator == -1 ? pointer.substring(1) : pointer.substring(1, nextSeparator);
        return escapedToken.replace("~1", "/").replace("~0", "~");
    }

    /**
     * Applies route-specific or entity-default response views to a structured response.
     *
     * <p>HTTP adapter responses use the same precedence as direct API responses: explicit route
     * response view first, existing response view next, then entity default resolver.
     *
     * @param request HTTP API request
     * @param verb effective HTTP API verb
     * @param apiResponse structured API response
     */
    private void applyResponseEntityView(
            final HttpApiRequest request, final HttpVerb verb, final ApiResponse apiResponse) {
        if (apiResponse == null
                || apiResponse.isErrorResponse()
                || apiResponse.hasABodyOverride()) {
            return;
        }

        final Optional<ThingifierApiRouteRule> matchingRule = routeRuleFor(request, verb);
        if (apiResponse.getTypeOfThingReturned() == null) {
            return;
        }

        final EntityDefinition entity = apiResponse.getTypeOfThingReturned();
        final String viewName =
                matchingRule
                        .map(rule -> rule.responseEntityViewFor(apiResponse.getStatusCode()))
                        .orElse(null);
        if (viewName != null) {
            if (entity.hasViewNamed(viewName)) {
                apiResponse.usingEntityView(entity.getViewNamed(viewName));
            }
            return;
        }

        if (apiResponse.hasResponseView()) {
            return;
        }

        if (thingifier.apiSpec().defaultResponseEntityViewFor(entity).isPresent()) {
            apiResponse.usingEntityResponseViewResolver(defaultResponseViewResolver());
        }
    }

    /**
     * Creates a resolver for entity-level default response views.
     *
     * @return resolver used when no route-specific view was applied
     */
    private EntityResponseViewResolver defaultResponseViewResolver() {
        return entity -> {
            if (entity == null) {
                return null;
            }
            return thingifier
                    .apiSpec()
                    .defaultResponseEntityViewFor(entity)
                    .filter(entity::hasViewNamed)
                    .map(entity::getViewNamed)
                    .orElse(null);
        };
    }

    /**
     * Resolves the entity targeted by a generated route path.
     *
     * @param path generated API path
     * @return target or related entity, or null when the path does not map to a Thingifier entity
     */
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
        if (route instanceof RelationshipInstanceRoute) {
            final RelationshipInstanceRoute relationship = (RelationshipInstanceRoute) route;
            for (RelationshipSpec spec : relationship.parentEntity().relationships()) {
                if (spec.name().equals(relationship.relationshipName())) {
                    return schema.definitionWithSingularOrPluralNamed(spec.toEntityName());
                }
            }
        }
        return null;
    }

    /**
     * Returns XML entity names that should be accepted or emitted for a path.
     *
     * @param path generated API path
     * @return singular and plural entity names, or an empty list when no entity route matches
     */
    private List<String> xmlEntityNamesFor(final String path) {
        final EntityDefinition entity = targetEntityFor(path);
        if (entity == null) {
            return List.of();
        }
        return List.of(entity.getName(), entity.getPlural());
    }

    /**
     * Handles a GET request.
     *
     * @param request HTTP API request
     * @return HTTP API response
     */
    public HttpApiResponse get(final HttpApiRequest request) {
        return handleRequest(request, HttpVerb.GET);
    }

    /**
     * Handles a HEAD request.
     *
     * @param request HTTP API request
     * @return HTTP API response
     */
    public HttpApiResponse head(final HttpApiRequest request) {
        return handleRequest(request, HttpVerb.HEAD);
    }

    /**
     * Handles a DELETE request.
     *
     * @param request HTTP API request
     * @return HTTP API response
     */
    public HttpApiResponse delete(final HttpApiRequest request) {
        return handleRequest(request, HttpVerb.DELETE);
    }

    /**
     * Handles a POST request.
     *
     * @param request HTTP API request
     * @return HTTP API response
     */
    public HttpApiResponse post(final HttpApiRequest request) {
        return handleRequest(request, HttpVerb.POST);
    }

    /**
     * Handles a PUT request.
     *
     * @param request HTTP API request
     * @return HTTP API response
     */
    public HttpApiResponse put(final HttpApiRequest request) {
        return handleRequest(request, HttpVerb.PUT);
    }

    /**
     * Handles a PATCH request.
     *
     * @param request HTTP API request
     * @return HTTP API response
     */
    public HttpApiResponse patch(final HttpApiRequest request) {
        return handleRequest(request, HttpVerb.PATCH);
    }

    /**
     * Handles a QUERY request.
     *
     * @param request HTTP API request
     * @return HTTP API response
     */
    public HttpApiResponse queryRequest(final HttpApiRequest request) {
        return handleRequest(request, HttpVerb.QUERY);
    }

    /**
     * Handles the legacy query helper by applying a JSONPath-style query to a GET request.
     *
     * @param request HTTP API request
     * @param query query expression
     * @return HTTP API response
     */
    public HttpApiResponse query(final HttpApiRequest request, final String query) {

        HttpApiResponse httpResponse = runTheHttpApiRequestHooksOn(request, HttpVerb.GET);

        if (httpResponse == null) {
            ApiResponse apiResponse =
                    thingifier
                            .api()
                            .get(query, request.getFilterableQueryParams(), request.getHeaders());
            httpResponse =
                    new HttpApiResponse(
                            request.getHeaders(),
                            apiResponse,
                            jsonThing,
                            thingifier.apiConfig(),
                            xmlEntityNamesFor(request.getPath()));
        }

        return runTheHttpApiResponseHooksOn(request, httpResponse, HttpVerb.GET);
    }

    /**
     * Runs legacy HTTP API response hooks in registration order.
     *
     * @param request HTTP API request
     * @param response response produced by generated API processing
     * @param verb effective HTTP API verb
     * @return original or replacement response from hooks
     */
    private HttpApiResponse runTheHttpApiResponseHooksOn(
            final HttpApiRequest request, final HttpApiResponse response, final HttpVerb verb) {
        if (apiHookRegistry != null) {
            final RoutingVerb routingVerb = routingVerbFor(verb);
            for (ScopedHook<HttpApiResponseHook> scopedHook : apiHookRegistry.responseHooks()) {
                if (!scopedHook.matches(
                        request.getPath(),
                        routingVerb,
                        thingifier.apiConfig().getApiEndPointPrefix())) {
                    continue;
                }
                HttpApiResponse returnImmediately =
                        scopedHook.hook().run(request, response, thingifier.apiConfig());
                if (returnImmediately != null) {
                    return returnImmediately;
                }
            }
            return response;
        }

        for (HttpApiResponseHook hook : apiResponseHooks) {
            HttpApiResponse returnImmediately = hook.run(request, response, thingifier.apiConfig());
            if (returnImmediately != null) {
                return returnImmediately;
            }
        }
        return response;
    }

    /**
     * Runs legacy HTTP API request hooks in registration order.
     *
     * @param request HTTP API request
     * @param verb effective HTTP API verb
     * @return short-circuit response from hooks, or null to continue processing
     */
    private HttpApiResponse runTheHttpApiRequestHooksOn(
            final HttpApiRequest request, final HttpVerb verb) {
        if (apiHookRegistry != null) {
            final RoutingVerb routingVerb = routingVerbFor(verb);
            for (ScopedHook<HttpApiRequestHook> scopedHook : apiHookRegistry.requestHooks()) {
                if (!scopedHook.matches(
                        request.getPath(),
                        routingVerb,
                        thingifier.apiConfig().getApiEndPointPrefix())) {
                    continue;
                }
                HttpApiResponse response = scopedHook.hook().run(request, thingifier.apiConfig());
                if (response != null) {
                    return response;
                }
            }
            return null;
        }

        for (HttpApiRequestHook hook : apiRequestHooks) {
            HttpApiResponse response = hook.run(request, thingifier.apiConfig());
            if (response != null) {
                return response;
            }
        }
        return null;
    }

    /**
     * Converts an HTTP adapter verb to the generated routing verb enum.
     *
     * @param verb HTTP adapter verb
     * @return matching routing verb, or null when no generated route verb exists
     */
    private RoutingVerb routingVerbFor(final HttpVerb verb) {
        if (verb == null) {
            return null;
        }
        try {
            return RoutingVerb.valueOf(verb.name());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
