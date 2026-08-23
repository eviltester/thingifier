package uk.co.compendiumdev.thingifier.api.spec;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import uk.co.compendiumdev.thingifier.api.callbacks.ThingifierApiOperationCallback;
import uk.co.compendiumdev.thingifier.api.callbacks.ThingifierApiOperationCallbackDefinition;
import uk.co.compendiumdev.thingifier.api.callbacks.ThingifierApiOperationCallbackDefinition.Outcome;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.response.RouteApiResponsePolicy;
import uk.co.compendiumdev.thingifier.api.security.SecuritySchemeNames;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthorizer;
import uk.co.compendiumdev.thingifier.api.validation.ApiOperationValidator;
import uk.co.compendiumdev.thingifier.api.validation.ApiOperationValidatorDefinition;
import uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle;
import uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation;
import uk.co.compendiumdev.thingifier.apiconfig.RelationshipWriteOperation;

/**
 * Configures the generated API behaviour for one verb and path pattern.
 *
 * <p>Route rules are deliberately narrow overrides for generated Thingifier routes. They let a
 * model keep its generated route set while shaping the public API with documentation, visibility,
 * authentication, allowed write semantics, and entity views.
 */
public final class ThingifierApiRouteRule {

    private final RoutingVerb verb;
    private final String pathPattern;
    private boolean hidden;
    private boolean disabled;
    private boolean methodNotAllowed;
    private boolean usesBasicAuth;
    private String basicAuthSchemeName;
    private String enforcedBasicAuthSchemeName;
    private boolean usesBearerAuth;
    private String bearerAuthSchemeName;
    private String enforcedBearerAuthSchemeName;
    private boolean usesApiKeyAuth;
    private String apiKeyAuthSchemeName;
    private String enforcedApiKeyAuthSchemeName;
    private final List<String> authEnforcementSchemeNames;
    private final List<ThingifierApiAuthorizer> authorizers;
    private final List<ApiOperationValidatorDefinition> apiOperationValidators;
    private final List<ThingifierApiOperationCallbackDefinition> operationCallbacks;
    private RouteApiResponsePolicy successResponsePolicy;
    private final Map<Integer, RouteApiResponsePolicy> errorResponsePolicies;
    private final Map<Integer, List<RouteApiResponsePolicy>> conditionalErrorResponsePolicies;
    private RouteApiResponsePolicy validationErrorResponsePolicy;
    private String documentation;
    private String requestPayload;
    private String requestEntityView;
    private String defaultEntityView;
    private Map<Integer, String> responseEntityViews;
    private ResponseShape responseShape;
    private String mappedEntityName;
    private String fixedIdentifier;
    private FixedResourcePolicy fixedResourcePolicy;
    private EnumSet<EntityWriteOperation> entityWriteOperations;
    private EnumSet<EntityPatchUpdateStyle> entityPatchUpdateStyles;
    private EnumSet<RelationshipWriteOperation> relationshipWriteOperations;

    ThingifierApiRouteRule(final RoutingVerb verb, final String pathPattern) {
        this.verb = verb;
        this.pathPattern = pathPattern == null ? "" : pathPattern;
        this.hidden = false;
        this.disabled = false;
        this.methodNotAllowed = false;
        this.usesBasicAuth = false;
        this.basicAuthSchemeName = SecuritySchemeNames.DEFAULT_BASIC_AUTH_SCHEME;
        this.enforcedBasicAuthSchemeName = null;
        this.usesBearerAuth = false;
        this.bearerAuthSchemeName = SecuritySchemeNames.DEFAULT_BEARER_AUTH_SCHEME;
        this.enforcedBearerAuthSchemeName = null;
        this.usesApiKeyAuth = false;
        this.apiKeyAuthSchemeName = SecuritySchemeNames.DEFAULT_API_KEY_AUTH_SCHEME;
        this.enforcedApiKeyAuthSchemeName = null;
        this.authEnforcementSchemeNames = new java.util.ArrayList<>();
        this.authorizers = new java.util.ArrayList<>();
        this.apiOperationValidators = new java.util.ArrayList<>();
        this.operationCallbacks = new java.util.ArrayList<>();
        this.successResponsePolicy = null;
        this.errorResponsePolicies = new HashMap<>();
        this.conditionalErrorResponsePolicies = new HashMap<>();
        this.validationErrorResponsePolicy = null;
        this.documentation = null;
        this.requestPayload = null;
        this.requestEntityView = null;
        this.defaultEntityView = null;
        this.responseEntityViews = new HashMap<>();
        this.responseShape = ResponseShape.DEFAULT;
        this.mappedEntityName = null;
        this.fixedIdentifier = null;
        this.fixedResourcePolicy = FixedResourcePolicy.RETURN_404;
        this.entityWriteOperations = null;
        this.entityPatchUpdateStyles = null;
        this.relationshipWriteOperations = null;
    }

    /**
     * Returns the HTTP-style verb this rule applies to.
     *
     * @return the configured routing verb
     */
    public RoutingVerb verb() {
        return verb;
    }

    /**
     * Returns the route path pattern this rule applies to.
     *
     * <p>Patterns use the same parameter forms as generated routes, including {@code {id}} and
     * {@code :id}.
     *
     * @return configured route path pattern
     */
    public String pathPattern() {
        return pathPattern;
    }

    /**
     * Hides the generated route from API documentation while leaving runtime behaviour available.
     *
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule hide() {
        hidden = true;
        return this;
    }

    /**
     * Hides the generated route from API documentation.
     *
     * <p>This alias keeps older configuration readable while making the intent explicit.
     *
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule hideFromDocs() {
        return hide();
    }

    /**
     * Disables the generated route so it behaves like it is not part of the API.
     *
     * <p>Disabled routes are hidden from documentation and return the existing not-found style
     * response instead of a 405.
     *
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule disable() {
        disabled = true;
        return this;
    }

    /**
     * Marks the generated route as present but unavailable for this method.
     *
     * <p>This is for public API surface decisions where clients should receive HTTP 405 Method Not
     * Allowed and an Allow header rather than a not-found response.
     *
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule methodNotAllowed() {
        methodNotAllowed = true;
        return this;
    }

    /**
     * Reports whether this route should be hidden from documentation.
     *
     * @return true when the route is documentation-hidden
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Reports whether this route should behave as disabled.
     *
     * @return true when the route should be treated as absent at runtime
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Reports whether this route should return 405 Method Not Allowed.
     *
     * @return true when the method is publicly unavailable but still a known route
     */
    public boolean isMethodNotAllowed() {
        return methodNotAllowed;
    }

    /**
     * Marks the route as requiring Basic authentication in generated documentation.
     *
     * <p>This historical form is documentation-only. Use {@link #secureWithBasicAuth(String)} when
     * Thingifier should also enforce a named Basic policy at runtime.
     *
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule secureWithBasicAuth() {
        usesBasicAuth = true;
        basicAuthSchemeName = SecuritySchemeNames.DEFAULT_BASIC_AUTH_SCHEME;
        return this;
    }

    /**
     * Marks the route as requiring a named Basic authentication scheme and runtime enforcement.
     *
     * <p>The scheme name is used in generated documentation, authenticator lookup, and the
     * authenticated-principal slot on the request context. The Basic realm is configured on {@link
     * uk.co.compendiumdev.thingifier.api.security.ThingifierApiSecuritySpec#basic(String, String)}.
     * This single-scheme convenience form replaces any previously configured runtime auth
     * alternatives.
     *
     * @param schemeName named Basic scheme, for example {@code adminPassword}
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule secureWithBasicAuth(final String schemeName) {
        final String normalizedSchemeName = SecuritySchemeNames.requireValid(schemeName);
        usesBasicAuth = true;
        usesBearerAuth = false;
        usesApiKeyAuth = false;
        basicAuthSchemeName = normalizedSchemeName;
        enforcedBasicAuthSchemeName = normalizedSchemeName;
        enforcedBearerAuthSchemeName = null;
        enforcedApiKeyAuthSchemeName = null;
        replaceAuthEnforcementWith(normalizedSchemeName);
        return this;
    }

    /**
     * Marks the route as requiring Bearer authentication in generated documentation.
     *
     * <p>This historical form is documentation-only. Use {@link #secureWithBearerAuth(String)} when
     * Thingifier should also enforce a named bearer policy at runtime.
     *
     * @return this rule so route API configuration can be chained
     */
    @Deprecated
    public ThingifierApiRouteRule secureWithBearerAuth() {
        usesBearerAuth = true;
        bearerAuthSchemeName = SecuritySchemeNames.DEFAULT_BEARER_AUTH_SCHEME;
        return this;
    }

    /**
     * Marks the route as requiring a named Bearer authentication scheme and runtime enforcement.
     *
     * <p>The scheme name is used in generated documentation, authenticator lookup, and the
     * authenticated-principal slot on the request context. Applications supply the authenticator
     * through {@link ThingifierApiSpec#authenticator(String,
     * uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthenticator)}.
     *
     * <p>This single-scheme convenience form replaces any previously configured runtime auth
     * alternatives.
     *
     * @param schemeName named bearer scheme, for example {@code cartToken}
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule secureWithBearerAuth(final String schemeName) {
        final String normalizedSchemeName = SecuritySchemeNames.requireValid(schemeName);
        usesBasicAuth = false;
        usesBearerAuth = true;
        usesApiKeyAuth = false;
        bearerAuthSchemeName = normalizedSchemeName;
        enforcedBasicAuthSchemeName = null;
        enforcedBearerAuthSchemeName = normalizedSchemeName;
        enforcedApiKeyAuthSchemeName = null;
        replaceAuthEnforcementWith(normalizedSchemeName);
        return this;
    }

    /**
     * Marks the route as requiring a named API key authentication scheme.
     *
     * <p>API key auth is for public token headers such as {@code X-API-KEY} or {@code
     * X-AUTH-TOKEN}. The scheme name is used for documentation, authenticator lookup, and the
     * authenticated-principal slot on the request context. The header name is configured on {@link
     * uk.co.compendiumdev.thingifier.api.security.ThingifierApiSecuritySpec#apiKey(String,
     * String)}. This single-scheme convenience form replaces any previously configured runtime auth
     * alternatives.
     *
     * @param schemeName named API key scheme, for example {@code authToken}
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule secureWithApiKey(final String schemeName) {
        final String normalizedSchemeName = SecuritySchemeNames.requireValid(schemeName);
        usesBasicAuth = false;
        usesBearerAuth = false;
        usesApiKeyAuth = true;
        apiKeyAuthSchemeName = normalizedSchemeName;
        enforcedBasicAuthSchemeName = null;
        enforcedBearerAuthSchemeName = null;
        enforcedApiKeyAuthSchemeName = normalizedSchemeName;
        replaceAuthEnforcementWith(normalizedSchemeName);
        return this;
    }

    /**
     * Requires one of several named authentication schemes, tried in declaration order.
     *
     * <p>The named schemes must be declared on {@link
     * uk.co.compendiumdev.thingifier.api.security.ThingifierApiSecuritySpec} so Thingifier knows
     * whether each credential is Basic, Bearer, or API key. Runtime auth selects the first declared
     * scheme whose credential source is present. If that credential is malformed or rejected,
     * Thingifier stops immediately rather than falling through to later alternatives.
     *
     * @param schemeNames ordered security scheme names accepted by this route
     * @return this rule so route API configuration can be chained
     * @throws IllegalArgumentException when no scheme names are supplied or a name is blank
     */
    public ThingifierApiRouteRule secureWithAnyOf(final String... schemeNames) {
        final List<String> normalizedSchemeNames = normalizedSchemeNames(schemeNames);
        usesBasicAuth = false;
        usesBearerAuth = false;
        usesApiKeyAuth = false;
        basicAuthSchemeName = SecuritySchemeNames.DEFAULT_BASIC_AUTH_SCHEME;
        bearerAuthSchemeName = SecuritySchemeNames.DEFAULT_BEARER_AUTH_SCHEME;
        apiKeyAuthSchemeName = SecuritySchemeNames.DEFAULT_API_KEY_AUTH_SCHEME;
        enforcedBasicAuthSchemeName = null;
        enforcedBearerAuthSchemeName = null;
        enforcedApiKeyAuthSchemeName = null;
        authEnforcementSchemeNames.clear();
        authEnforcementSchemeNames.addAll(normalizedSchemeNames);
        return this;
    }

    /**
     * Adds a route-specific authorization callback.
     *
     * <p>Authorizers run only after the named authenticator accepts the route's credential.
     * Multiple authorizers are evaluated in registration order and the first rejection stops the
     * request.
     *
     * @param authorizer authorization callback
     * @return this rule so route API configuration can be chained
     * @throws IllegalArgumentException when the authorizer is null
     */
    public ThingifierApiRouteRule authorizeWith(final ThingifierApiAuthorizer authorizer) {
        if (authorizer == null) {
            throw new IllegalArgumentException("authorizer is required");
        }
        authorizers.add(authorizer);
        return this;
    }

    /**
     * Reports whether this route is documented as bearer secured.
     *
     * @return true when bearer auth should appear in generated documentation
     */
    public boolean isSecuredByBearerAuth() {
        return usesBearerAuth;
    }

    /**
     * Returns the bearer scheme name used in generated documentation.
     *
     * @return bearer auth scheme name
     */
    public String bearerAuthSchemeName() {
        return bearerAuthSchemeName;
    }

    /**
     * Reports whether this route should enforce bearer auth at runtime.
     *
     * @return true when the route has a named bearer enforcement scheme
     */
    public boolean hasBearerAuthEnforcement() {
        return enforcedBearerAuthSchemeName != null;
    }

    /**
     * Returns the bearer scheme name used for runtime enforcement.
     *
     * @return bearer enforcement scheme name, or null for documentation-only bearer routes
     */
    public String bearerAuthEnforcementSchemeName() {
        return enforcedBearerAuthSchemeName;
    }

    /**
     * Reports whether this route is documented as API-key secured.
     *
     * @return true when API key auth should appear in generated documentation
     */
    public boolean isSecuredByApiKeyAuth() {
        return usesApiKeyAuth;
    }

    /**
     * Returns the API key scheme name used in generated documentation.
     *
     * @return API key auth scheme name
     */
    public String apiKeyAuthSchemeName() {
        return apiKeyAuthSchemeName;
    }

    /**
     * Reports whether this route should enforce API key auth at runtime.
     *
     * @return true when the route has a named API key enforcement scheme
     */
    public boolean hasApiKeyAuthEnforcement() {
        return enforcedApiKeyAuthSchemeName != null;
    }

    /**
     * Returns the API key scheme name used for runtime enforcement.
     *
     * @return API key enforcement scheme name, or null when API key auth is not enforced
     */
    public String apiKeyAuthEnforcementSchemeName() {
        return enforcedApiKeyAuthSchemeName;
    }

    /**
     * Reports whether this route has named runtime authentication enforcement.
     *
     * @return true when one or more auth schemes are enforced
     */
    public boolean hasAuthEnforcement() {
        return !authEnforcementSchemeNames.isEmpty();
    }

    /**
     * Returns runtime auth schemes in the order Thingifier should try them.
     *
     * <p>Single-scheme convenience methods return a one-item list. {@link
     * #secureWithAnyOf(String...)} returns all configured alternatives in declaration order.
     *
     * @return immutable ordered auth scheme names
     */
    public List<String> authEnforcementSchemeNames() {
        return List.copyOf(authEnforcementSchemeNames);
    }

    /**
     * Returns route-specific authorizers in registration order.
     *
     * @return immutable authorizer list
     */
    public List<ThingifierApiAuthorizer> authorizers() {
        return List.copyOf(authorizers);
    }

    /**
     * Reports whether this route is documented as Basic secured.
     *
     * @return true when Basic auth should appear in generated documentation
     */
    public boolean isSecuredByBasicAuth() {
        return usesBasicAuth;
    }

    /**
     * Returns the Basic scheme name used in generated documentation.
     *
     * @return Basic auth scheme name
     */
    public String basicAuthSchemeName() {
        return basicAuthSchemeName;
    }

    /**
     * Reports whether this route should enforce Basic auth at runtime.
     *
     * @return true when the route has a named Basic enforcement scheme
     */
    public boolean hasBasicAuthEnforcement() {
        return enforcedBasicAuthSchemeName != null;
    }

    /**
     * Returns the Basic scheme name used for runtime enforcement.
     *
     * @return Basic enforcement scheme name, or null for documentation-only Basic routes
     */
    public String basicAuthEnforcementSchemeName() {
        return enforcedBasicAuthSchemeName;
    }

    /**
     * Adds route-specific documentation text to the generated API definition.
     *
     * @param documentation documentation text to append to the route definition
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule addDocumentation(final String documentation) {
        this.documentation = documentation;
        return this;
    }

    /**
     * Overrides the named request payload schema advertised for this route.
     *
     * @param requestPayload object schema name to use as the request payload
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule requestPayload(final String requestPayload) {
        this.requestPayload = requestPayload;
        return this;
    }

    /**
     * Sets the entity view used to validate accepted request fields for this route.
     *
     * <p>Route-specific request views override entity-level defaults because endpoint contracts can
     * be narrower or broader than the normal entity contract.
     *
     * @param viewName entity view name used for request validation
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule requestEntityView(final String viewName) {
        this.requestEntityView = viewName;
        return this;
    }

    /**
     * Sets the response entity view for a specific successful or error status code.
     *
     * @param statusCode status code whose response payload should use the view
     * @param viewName entity view name used when rendering the response body
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule responseEntityView(final int statusCode, final String viewName) {
        this.responseEntityViews.put(statusCode, viewName);
        return this;
    }

    /**
     * Sets the default response entity view for successful responses on this route.
     *
     * <p>This is the response-only counterpart to {@link #entityView(String)}. It is useful for
     * fixed instance routes where the public response shape should be constrained but writes may
     * still accept a different request view.
     *
     * @param viewName entity view name used for successful responses
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule defaultEntityView(final String viewName) {
        this.defaultEntityView = viewName;
        return this;
    }

    /**
     * Sets the successful entity response shape for this route.
     *
     * <p>Use {@link ResponseShape#SINGLE_INSTANCE} for fixed-resource routes where the public URL
     * represents one known entity instance and must render as one object, even when legacy global
     * configuration would normally wrap instance reads in a collection response.
     *
     * @param shape route response shape, or {@link ResponseShape#DEFAULT} when null
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule responseShape(final ResponseShape shape) {
        this.responseShape = shape == null ? ResponseShape.DEFAULT : shape;
        return this;
    }

    /**
     * Requires successful responses on this route to render as one persisted instance.
     *
     * <p>This is a convenience alias for {@code responseShape(ResponseShape.SINGLE_INSTANCE)}. It
     * is intended for fixed-resource routes such as {@code /secret/note} where the fixed identifier
     * is declared in server code rather than supplied by the URL.
     *
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule respondWithSingleInstance() {
        return responseShape(ResponseShape.SINGLE_INSTANCE);
    }

    /**
     * Declares the model entity that a non-generated public route should target.
     *
     * <p>Use this with {@link #withFixedIdentifier(String)} when a route such as {@code
     * /secret/note} should be processed as a Thingifier-managed entity instance without putting the
     * identifier in the public URL.
     *
     * @param entityName singular or plural model entity name
     * @return this rule so route API configuration can be chained
     * @throws IllegalArgumentException when the entity name is blank
     */
    public ThingifierApiRouteRule mapsToEntity(final String entityName) {
        this.mappedEntityName = requireText(entityName, "entity name");
        return this;
    }

    /**
     * Maps this public route to one fixed entity identifier.
     *
     * <p>The fixed identifier is a trusted server-side route decision, not a value read from the
     * client URL. Runtime processing resolves the public route to an internal instance route while
     * keeping the public path available for documentation, hooks, and error messages.
     *
     * @param identifier identifier of the target entity instance
     * @return this rule so route API configuration can be chained
     * @throws IllegalArgumentException when the identifier is blank or the route path has URL
     *     parameters
     */
    public ThingifierApiRouteRule withFixedIdentifier(final String identifier) {
        return withFixedIdentifier(identifier, FixedResourcePolicy.RETURN_404);
    }

    /**
     * Maps this public route to one fixed entity identifier with explicit missing-instance policy.
     *
     * @param identifier identifier of the target entity instance
     * @param policy behaviour when the target instance is missing
     * @return this rule so route API configuration can be chained
     * @throws IllegalArgumentException when the identifier is blank, the policy is null, or the
     *     route path has URL parameters
     */
    public ThingifierApiRouteRule withFixedIdentifier(
            final String identifier, final FixedResourcePolicy policy) {
        if (hasPathParameter(pathPattern)) {
            throw new IllegalArgumentException(
                    "fixed identifier routes must not contain path parameters");
        }
        this.fixedIdentifier = requireText(identifier, "fixed identifier");
        if (policy == null) {
            throw new IllegalArgumentException("fixed resource policy is required");
        }
        this.fixedResourcePolicy = policy;
        return this;
    }

    /**
     * Restricts which entity write operations this generated route may perform.
     *
     * <p>The route may still be generated; the runtime chooses 405 when the concrete write
     * operation is outside this set.
     *
     * @param operations allowed create or update operations for the route
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule entityWriteOperations(final EntityWriteOperation... operations) {
        this.entityWriteOperations = entityOperations(operations);
        return this;
    }

    /**
     * Alias for {@link #entityWriteOperations(EntityWriteOperation...)}.
     *
     * @param operations allowed create or update operations for the route
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule entityCan(final EntityWriteOperation... operations) {
        return entityWriteOperations(operations);
    }

    /**
     * Restricts which patch document styles this generated entity route accepts.
     *
     * @param updateStyles accepted patch formats for the route
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule entityPatchCan(final EntityPatchUpdateStyle... updateStyles) {
        this.entityPatchUpdateStyles = patchStyles(updateStyles);
        return this;
    }

    /**
     * Restricts which relationship write operations this generated route may perform.
     *
     * @param operations allowed relationship operations for the route
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule relationshipWriteOperations(
            final RelationshipWriteOperation... operations) {
        this.relationshipWriteOperations = relationshipOperations(operations);
        return this;
    }

    /**
     * Alias for {@link #relationshipWriteOperations(RelationshipWriteOperation...)}.
     *
     * @param operations allowed relationship operations for the route
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule relationshipCan(final RelationshipWriteOperation... operations) {
        return relationshipWriteOperations(operations);
    }

    /**
     * Adds a request-aware validator for this public API operation.
     *
     * <p>Use operation validators when the rule belongs to the route contract rather than the
     * entity model. They run after authentication, data-scope selection, request parsing, fixed
     * route preparation, declarative write policy, and command/query mapping have succeeded, but
     * before entity field/instance/domain/global validation and before mutation.
     *
     * @param name stable validator name for diagnostics and future tooling
     * @param validator callback used to accept or reject this operation
     * @return this rule so route API configuration can be chained
     * @throws IllegalArgumentException when the name is blank or the validator is null
     */
    public ThingifierApiRouteRule withApiOperationValidator(
            final String name, final ApiOperationValidator validator) {
        apiOperationValidators.add(new ApiOperationValidatorDefinition(name, validator));
        return this;
    }

    /**
     * Reports whether this route has request-aware operation validators.
     *
     * @return true when validators are registered
     */
    public boolean hasApiOperationValidators() {
        return !apiOperationValidators.isEmpty();
    }

    /**
     * Returns the operation validators in declaration order.
     *
     * <p>Validators are intentionally runtime-only for v1. They are not serialized to YAML or
     * emitted into OpenAPI because Java callbacks cannot safely round-trip through those formats.
     *
     * @return immutable list of validator registrations
     */
    public List<ApiOperationValidatorDefinition> apiOperationValidators() {
        return Collections.unmodifiableList(apiOperationValidators);
    }

    /**
     * Registers a callback that runs after any completed outcome for this route.
     *
     * <p>Operation callbacks are trusted, code-only application side effects. They run after
     * Thingifier has created and route-shaped an {@link
     * uk.co.compendiumdev.thingifier.api.response.ApiResponse}, and before legacy response hooks
     * render or override the final HTTP response.
     *
     * @param callback callback to run
     * @return callback registration for optional failure-policy configuration
     */
    public ThingifierApiOperationCallbackDefinition afterOperation(
            final ThingifierApiOperationCallback callback) {
        return afterOperation(defaultCallbackName("after-operation"), callback);
    }

    /**
     * Registers a named callback that runs after any completed outcome for this route.
     *
     * @param name stable callback name used in diagnostics
     * @param callback callback to run
     * @return callback registration for optional failure-policy configuration
     */
    public ThingifierApiOperationCallbackDefinition afterOperation(
            final String name, final ThingifierApiOperationCallback callback) {
        return addOperationCallback(name, Outcome.ANY, null, callback);
    }

    /**
     * Registers a callback that runs only for successful route outcomes.
     *
     * <p>A successful outcome is based on the final route-shaped status code in the 2xx or 3xx
     * range.
     *
     * @param callback callback to run
     * @return callback registration for optional failure-policy configuration
     */
    public ThingifierApiOperationCallbackDefinition afterSuccessfulOperation(
            final ThingifierApiOperationCallback callback) {
        return afterSuccessfulOperation(
                defaultCallbackName("after-successful-operation"), callback);
    }

    /**
     * Registers a named callback that runs only for successful route outcomes.
     *
     * @param name stable callback name used in diagnostics
     * @param callback callback to run
     * @return callback registration for optional failure-policy configuration
     */
    public ThingifierApiOperationCallbackDefinition afterSuccessfulOperation(
            final String name, final ThingifierApiOperationCallback callback) {
        return addOperationCallback(name, Outcome.SUCCESS, null, callback);
    }

    /**
     * Registers a callback that runs only for failed route outcomes.
     *
     * <p>Use this for route-specific failure observation. It is not a replacement for response
     * policies; callbacks should perform application side effects rather than shape response
     * bodies.
     *
     * @param callback callback to run
     * @return callback registration for optional failure-policy configuration
     */
    public ThingifierApiOperationCallbackDefinition afterFailedOperation(
            final ThingifierApiOperationCallback callback) {
        return afterFailedOperation(defaultCallbackName("after-failed-operation"), callback);
    }

    /**
     * Registers a named callback that runs only for failed route outcomes.
     *
     * @param name stable callback name used in diagnostics
     * @param callback callback to run
     * @return callback registration for optional failure-policy configuration
     */
    public ThingifierApiOperationCallbackDefinition afterFailedOperation(
            final String name, final ThingifierApiOperationCallback callback) {
        return addOperationCallback(name, Outcome.FAILURE, null, callback);
    }

    /**
     * Registers a callback that runs only when the final status code matches.
     *
     * @param statusCode final API status code to match
     * @param callback callback to run
     * @return callback registration for optional failure-policy configuration
     */
    public ThingifierApiOperationCallbackDefinition afterStatus(
            final int statusCode, final ThingifierApiOperationCallback callback) {
        return afterStatus(defaultCallbackName("after-status-" + statusCode), statusCode, callback);
    }

    /**
     * Registers a named callback that runs only when the final status code matches.
     *
     * @param name stable callback name used in diagnostics
     * @param statusCode final API status code to match
     * @param callback callback to run
     * @return callback registration for optional failure-policy configuration
     */
    public ThingifierApiOperationCallbackDefinition afterStatus(
            final String name,
            final int statusCode,
            final ThingifierApiOperationCallback callback) {
        return addOperationCallback(name, Outcome.STATUS, statusCode, callback);
    }

    /**
     * Reports whether this route has operation callbacks.
     *
     * @return true when callbacks are registered
     */
    public boolean hasOperationCallbacks() {
        return !operationCallbacks.isEmpty();
    }

    /**
     * Returns operation callbacks in declaration order.
     *
     * <p>Callbacks are runtime-only and intentionally absent from YAML export/import and public
     * OpenAPI because Java functions cannot safely round-trip through those formats.
     *
     * @return immutable callback registrations
     */
    public List<ThingifierApiOperationCallbackDefinition> operationCallbacks() {
        return Collections.unmodifiableList(operationCallbacks);
    }

    /**
     * Configures response shaping for non-error responses returned by this route.
     *
     * <p>Route response policies let endpoint contracts adjust status codes, headers, bodies, and
     * response views without replacing the generated Thingifier operation. The policy is route
     * scoped, so other routes mapping to the same entity are not affected.
     *
     * @return mutable route response policy for successful outcomes
     */
    public RouteApiResponsePolicy onSuccess() {
        if (successResponsePolicy == null) {
            successResponsePolicy = new RouteApiResponsePolicy();
        }
        return successResponsePolicy;
    }

    /**
     * Configures response shaping for generated error responses with one status code.
     *
     * @param statusCode error status code to match
     * @return mutable route response policy for the status-specific error outcome
     */
    public RouteApiResponsePolicy onError(final int statusCode) {
        return errorResponsePolicies.computeIfAbsent(
                statusCode, ignored -> new RouteApiResponsePolicy());
    }

    /**
     * Adds a conditional response policy for generated error responses with one status code.
     *
     * <p>Unlike {@link #onError(int)}, each call creates a new policy and appends it to the route's
     * ordered conditional policy list. The unconditional {@code onError} policy, when configured,
     * runs first; matching conditional policies then run in declaration order. This lets a route
     * define a default error shape and request-specific overrides without relying on broad response
     * hooks.
     *
     * @param statusCode generated error status code to match
     * @return mutable route response policy for a conditional error outcome
     */
    public RouteApiResponsePolicy onErrorWhen(final int statusCode) {
        final RouteApiResponsePolicy policy = new RouteApiResponsePolicy();
        conditionalErrorResponsePolicies
                .computeIfAbsent(statusCode, ignored -> new java.util.ArrayList<>())
                .add(policy);
        return policy;
    }

    /**
     * Configures response shaping for Thingifier validation-style failures on this route.
     *
     * <p>This policy is checked before status-specific error policies so validation errors can have
     * one route-specific shape regardless of the final status code exposed by the policy.
     *
     * @return mutable route response policy for validation errors
     */
    public RouteApiResponsePolicy onValidationError() {
        if (validationErrorResponsePolicy == null) {
            validationErrorResponsePolicy = new RouteApiResponsePolicy();
        }
        return validationErrorResponsePolicy;
    }

    /**
     * Returns the success response policy when configured.
     *
     * @return success policy
     */
    public Optional<RouteApiResponsePolicy> successResponsePolicy() {
        return Optional.ofNullable(successResponsePolicy);
    }

    /**
     * Returns the validation error response policy when configured.
     *
     * @return validation error policy
     */
    public Optional<RouteApiResponsePolicy> validationErrorResponsePolicy() {
        return Optional.ofNullable(validationErrorResponsePolicy);
    }

    /**
     * Returns the error response policy for a status code.
     *
     * @param statusCode generated error status code
     * @return status-specific error policy
     */
    public Optional<RouteApiResponsePolicy> errorResponsePolicyFor(final int statusCode) {
        return Optional.ofNullable(errorResponsePolicies.get(statusCode));
    }

    /**
     * Returns conditional error response policies for a status code in declaration order.
     *
     * @param statusCode generated error status code
     * @return immutable conditional policies
     */
    public List<RouteApiResponsePolicy> conditionalErrorResponsePoliciesFor(final int statusCode) {
        return Collections.unmodifiableList(
                conditionalErrorResponsePolicies.getOrDefault(statusCode, List.of()));
    }

    /**
     * Returns all status-specific error response policies.
     *
     * @return immutable map of error status to policy
     */
    public Map<Integer, RouteApiResponsePolicy> errorResponsePolicies() {
        return Collections.unmodifiableMap(errorResponsePolicies);
    }

    /**
     * Returns all conditional status-specific error response policies.
     *
     * @return immutable map of error status to ordered conditional policies
     */
    public Map<Integer, List<RouteApiResponsePolicy>> conditionalErrorResponsePolicies() {
        final Map<Integer, List<RouteApiResponsePolicy>> snapshot = new HashMap<>();
        for (Map.Entry<Integer, List<RouteApiResponsePolicy>> entry :
                conditionalErrorResponsePolicies.entrySet()) {
            snapshot.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(snapshot);
    }

    /**
     * Uses the same entity view for request validation and successful responses on this route.
     *
     * <p>This route-level view overrides entity defaults and is kept for concise route contracts.
     *
     * @param viewName entity view name for route input and output
     * @return this rule so route API configuration can be chained
     */
    public ThingifierApiRouteRule entityView(final String viewName) {
        this.requestEntityView = viewName;
        this.defaultEntityView = viewName;
        return this;
    }

    /**
     * Reports whether this route has an explicit request entity view.
     *
     * @return true when request input should use the configured route view
     */
    public boolean hasRequestEntityView() {
        return requestEntityView != null;
    }

    /**
     * Returns the explicit request entity view configured for this route.
     *
     * @return request entity view name, or null when the route has none
     */
    public String getRequestEntityView() {
        return requestEntityView;
    }

    /**
     * Resolves the response entity view for the supplied status code.
     *
     * <p>Status-specific views win first. The route default applies only to 2xx responses so error
     * bodies are not accidentally shaped like normal entity resources.
     *
     * @param statusCode API response status code
     * @return entity view name, or null when this route does not define one for the status
     */
    public String responseEntityViewFor(final int statusCode) {
        if (responseEntityViews.containsKey(statusCode)) {
            return responseEntityViews.get(statusCode);
        }
        if (defaultEntityView != null && statusCode >= 200 && statusCode < 300) {
            return defaultEntityView;
        }
        return null;
    }

    /**
     * Returns the successful response shape configured for this route.
     *
     * @return route response shape, defaulting to {@link ResponseShape#DEFAULT}
     */
    public ResponseShape responseShape() {
        return responseShape;
    }

    /**
     * Reports whether this route overrides the generated response shape.
     *
     * @return true when a non-default response shape is configured
     */
    public boolean hasResponseShapeOverride() {
        return responseShape != ResponseShape.DEFAULT;
    }

    /**
     * Reports whether this route overrides entity write operation policy.
     *
     * @return true when entity write operations were explicitly configured
     */
    public boolean hasEntityWriteOperations() {
        return entityWriteOperations != null;
    }

    /**
     * Returns the entity write operations allowed by this route.
     *
     * @return immutable set of allowed entity write operations, or an empty set when none are
     *     allowed
     */
    public Set<EntityWriteOperation> entityWriteOperations() {
        if (entityWriteOperations == null || entityWriteOperations.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(entityWriteOperations));
    }

    /**
     * Reports whether this route overrides PATCH update style policy.
     *
     * @return true when PATCH styles were explicitly configured
     */
    public boolean hasEntityPatchUpdateStyles() {
        return entityPatchUpdateStyles != null;
    }

    /**
     * Returns the PATCH update styles allowed by this route.
     *
     * @return immutable set of accepted PATCH styles, or an empty set when none are allowed
     */
    public Set<EntityPatchUpdateStyle> entityPatchUpdateStyles() {
        if (entityPatchUpdateStyles == null || entityPatchUpdateStyles.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(entityPatchUpdateStyles));
    }

    /**
     * Reports whether this route overrides relationship write operation policy.
     *
     * @return true when relationship write operations were explicitly configured
     */
    public boolean hasRelationshipWriteOperations() {
        return relationshipWriteOperations != null;
    }

    /**
     * Returns the relationship write operations allowed by this route.
     *
     * @return immutable set of allowed relationship write operations, or an empty set when none are
     *     allowed
     */
    public Set<RelationshipWriteOperation> relationshipWriteOperations() {
        if (relationshipWriteOperations == null || relationshipWriteOperations.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(relationshipWriteOperations));
    }

    /**
     * Reports whether this rule maps a public path to a fixed entity instance.
     *
     * @return true when both entity and identifier are configured
     */
    public boolean hasFixedIdentifierMapping() {
        return mappedEntityName != null && fixedIdentifier != null;
    }

    /**
     * Returns the entity name configured for a fixed identifier route.
     *
     * @return singular or plural entity name, or null when not configured
     */
    public String fixedEntityName() {
        return mappedEntityName;
    }

    /**
     * Returns the fixed identifier configured for this route.
     *
     * @return fixed entity instance identifier, or null when not configured
     */
    public String fixedIdentifier() {
        return fixedIdentifier;
    }

    /**
     * Returns the configured missing-instance policy for this fixed route.
     *
     * @return fixed resource policy, defaulting to {@link FixedResourcePolicy#RETURN_404}
     */
    public FixedResourcePolicy fixedResourcePolicy() {
        return fixedResourcePolicy;
    }

    /**
     * Applies this API-spec rule to a generated route definition.
     *
     * <p>Documentation route metadata is updated here, while runtime policy is resolved separately
     * by the HTTP and direct API handlers so both execution paths remain consistent.
     *
     * @param route generated routing definition to update
     */
    void applyTo(final RoutingDefinition route) {
        if (hidden) {
            route.hideFromDocumentation();
        }
        if (disabled) {
            route.disable();
        }
        if (methodNotAllowed && !disabled) {
            route.replaceStatus(RoutingStatus.returnValue(405));
        }
        if (usesBasicAuth) {
            route.secureWithBasicAuth(basicAuthSchemeName);
        }
        if (usesBearerAuth) {
            route.secureWithBearerAuth(bearerAuthSchemeName);
        }
        if (usesApiKeyAuth) {
            route.secureWithApiKey(apiKeyAuthSchemeName);
        }
        if (hasAuthEnforcement() && !usesBasicAuth && !usesBearerAuth && !usesApiKeyAuth) {
            route.secureWithAnyOf(authEnforcementSchemeNames.toArray(new String[0]));
        }
        if (documentation != null) {
            route.addDocumentation(documentation);
        }
        if (requestPayload != null) {
            route.requestPayload(requestPayload);
        }
        if (requestEntityView != null) {
            route.requestEntityView(requestEntityView);
        }
        for (Map.Entry<Integer, String> responseView : responseEntityViews.entrySet()) {
            route.responseEntityView(responseView.getKey(), responseView.getValue());
        }
        if (defaultEntityView != null) {
            if (route.returnPayloadStatusCodes().isEmpty()) {
                route.responseEntityView(200, defaultEntityView);
                route.responseEntityView(201, defaultEntityView);
            } else {
                for (Integer statusCode : route.returnPayloadStatusCodes()) {
                    if (statusCode >= 200
                            && statusCode < 300
                            && !route.hasResponseEntityViewFor(statusCode)) {
                        route.responseEntityView(statusCode, defaultEntityView);
                    }
                }
            }
        }
        if (hasFixedIdentifierMapping()) {
            route.mapToFixedEntity(mappedEntityName, fixedIdentifier, fixedResourcePolicy);
        }
        if (hasResponseShapeOverride()) {
            route.responseShape(responseShape);
        }
        applyResponsePolicyMetadataTo(route);
    }

    private void applyResponsePolicyMetadataTo(final RoutingDefinition route) {
        successResponsePolicy().ifPresent(policy -> applyPolicyMetadata(route, policy));
        validationErrorResponsePolicy()
                .ifPresent(
                        policy -> {
                            route.addPossibleStatus(RoutingStatus.returnValue(422));
                            applyPolicyMetadata(route, policy);
                        });
        for (Map.Entry<Integer, RouteApiResponsePolicy> entry : errorResponsePolicies.entrySet()) {
            route.addPossibleStatus(RoutingStatus.returnValue(entry.getKey()));
            applyPolicyMetadata(route, entry.getValue());
        }
        for (Map.Entry<Integer, List<RouteApiResponsePolicy>> entry :
                conditionalErrorResponsePolicies.entrySet()) {
            route.addPossibleStatus(RoutingStatus.returnValue(entry.getKey()));
            for (RouteApiResponsePolicy policy : entry.getValue()) {
                applyPolicyMetadata(route, policy);
            }
        }
    }

    private void applyPolicyMetadata(
            final RoutingDefinition route, final RouteApiResponsePolicy policy) {
        if (policy.statusCode() != null) {
            route.addPossibleStatus(RoutingStatus.returnValue(policy.statusCode()));
        }
        for (RouteApiResponsePolicy.HeaderValue header : policy.staticHeaders()) {
            route.addResponseHeader(header.name(), header.value());
        }
        for (RouteApiResponsePolicy.InstanceFieldHeader header : policy.instanceFieldHeaders()) {
            route.addResponseHeader(
                    header.headerName(),
                    "Value from returned instance field " + header.fieldName());
        }
    }

    /**
     * Captures the configured entity write operations in an enum set.
     *
     * @param operations caller supplied operations, possibly null
     * @return mutable enum set stored by this rule
     */
    private EnumSet<EntityWriteOperation> entityOperations(
            final EntityWriteOperation... operations) {
        EnumSet<EntityWriteOperation> selected = EnumSet.noneOf(EntityWriteOperation.class);
        if (operations != null) {
            Collections.addAll(selected, operations);
        }
        return selected;
    }

    /**
     * Captures the configured patch styles in an enum set.
     *
     * @param styles caller supplied patch styles, possibly null
     * @return mutable enum set stored by this rule
     */
    private EnumSet<EntityPatchUpdateStyle> patchStyles(final EntityPatchUpdateStyle... styles) {
        EnumSet<EntityPatchUpdateStyle> selected = EnumSet.noneOf(EntityPatchUpdateStyle.class);
        if (styles != null) {
            Collections.addAll(selected, styles);
        }
        return selected;
    }

    /**
     * Captures the configured relationship write operations in an enum set.
     *
     * @param operations caller supplied operations, possibly null
     * @return mutable enum set stored by this rule
     */
    private EnumSet<RelationshipWriteOperation> relationshipOperations(
            final RelationshipWriteOperation... operations) {
        EnumSet<RelationshipWriteOperation> selected =
                EnumSet.noneOf(RelationshipWriteOperation.class);
        if (operations != null) {
            Collections.addAll(selected, operations);
        }
        return selected;
    }

    private void replaceAuthEnforcementWith(final String schemeName) {
        authEnforcementSchemeNames.clear();
        authEnforcementSchemeNames.add(schemeName);
    }

    private List<String> normalizedSchemeNames(final String... schemeNames) {
        if (schemeNames == null || schemeNames.length == 0) {
            throw new IllegalArgumentException("secureWithAnyOf requires at least one scheme");
        }
        final List<String> normalizedSchemeNames = new java.util.ArrayList<>();
        for (String schemeName : schemeNames) {
            final String normalizedSchemeName = SecuritySchemeNames.requireValid(schemeName);
            if (!normalizedSchemeNames.contains(normalizedSchemeName)) {
                normalizedSchemeNames.add(normalizedSchemeName);
            }
        }
        if (normalizedSchemeNames.isEmpty()) {
            throw new IllegalArgumentException("secureWithAnyOf requires at least one scheme");
        }
        return normalizedSchemeNames;
    }

    private ThingifierApiOperationCallbackDefinition addOperationCallback(
            final String name,
            final Outcome outcome,
            final Integer statusCode,
            final ThingifierApiOperationCallback callback) {
        final ThingifierApiOperationCallbackDefinition definition =
                new ThingifierApiOperationCallbackDefinition(
                        this, name, outcome, statusCode, callback);
        operationCallbacks.add(definition);
        return definition;
    }

    private String defaultCallbackName(final String prefix) {
        return prefix + "-" + (operationCallbacks.size() + 1);
    }

    private String requireText(final String value, final String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value.trim();
    }

    private boolean hasPathParameter(final String path) {
        final String normalized = path == null ? "" : path.trim();
        if (normalized.contains("{") || normalized.contains("}")) {
            return true;
        }
        for (String segment : normalized.split("/")) {
            if (segment.startsWith(":") && segment.length() > 1) {
                return true;
            }
        }
        return false;
    }
}
