package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.List;
import java.util.Optional;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.CollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.InstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipCollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipInstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.ApiKeyHeaderParser;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.BasicAuthHeaderParser;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.BearerAuthHeaderParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.security.DataScopeCreationPolicy;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthenticationContext;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthenticationResult;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthenticator;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthorizationContext;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthorizationResult;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthorizer;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiDataScopeSelection;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiRouteAuthDetails;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiSecuritySchemeType;
import uk.co.compendiumdev.thingifier.api.spec.ApiRoutePathMatcher;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiRouteRule;
import uk.co.compendiumdev.thingifier.application.schema.EntityTypeRef;
import uk.co.compendiumdev.thingifier.application.schema.RelationshipSpec;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

/**
 * Enforces route-level authentication and authorization configured in the API spec.
 *
 * <p>The policy handles the generic HTTP auth mechanics that belong in Thingifier and delegates
 * application decisions to registered authenticators and authorizers. It returns an {@link
 * ApiResponse} only when processing should stop.
 */
public final class RouteAuthPolicy {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String WWW_AUTHENTICATE_HEADER = "WWW-Authenticate";
    private static final String BEARER_CHALLENGE = "Bearer";

    private final ThingifierApiRuntime runtime;

    /**
     * Creates a route auth policy using the active Thingifier runtime.
     *
     * @param runtime runtime services used to resolve route rules, schema, and authenticators
     */
    public RouteAuthPolicy(final ThingifierApiRuntime runtime) {
        this.runtime = runtime;
    }

    /**
     * Rejects the request when route-level auth policy does not allow it to proceed.
     *
     * @param verb generated API verb
     * @param path generated API path
     * @param context active request context
     * @return rejection response, or null when the route is unprotected or authorized
     */
    public ApiResponse rejectIfNotAuthorized(
            final RoutingVerb verb, final String path, final ThingifierRequestContext context) {
        final ThingRoute route = runtime.routeFor(verb, path);
        return rejectIfNotAuthorized(verb, path, context, route);
    }

    /**
     * Rejects the request using a pre-mapped route.
     *
     * <p>HTTP routing already maps the route for lifecycle context creation, so this overload keeps
     * auth enforcement from doing duplicate route work in that path.
     *
     * @param verb generated API verb
     * @param path generated API path
     * @param context active request context
     * @param route mapped generated route
     * @return rejection response, or null when the route is unprotected or authorized
     */
    public ApiResponse rejectIfNotAuthorized(
            final RoutingVerb verb,
            final String path,
            final ThingifierRequestContext context,
            final ThingRoute route) {
        if (verb == null || context == null) {
            return null;
        }

        final String apiPathPrefix = runtime.apiConfig().getApiEndPointPrefix();
        final Optional<ThingifierApiRouteRule> matchingRule =
                runtime.apiSpec().ruleFor(verb, path, apiPathPrefix);
        if (matchingRule.isEmpty() || !requiresAuth(matchingRule.get())) {
            return null;
        }

        return rejectForAuthAlternatives(verb, path, context, route, matchingRule.get());
    }

    /**
     * Enforces the route's ordered authentication alternatives.
     *
     * <p>Only the first declared scheme whose credential source is present is authenticated. A
     * malformed or rejected selected credential stops the request immediately so a higher-priority
     * credential cannot silently fall through to a later alternative.
     *
     * @param verb generated API verb
     * @param path generated API path
     * @param context active request context
     * @param route mapped generated route
     * @param rule matching route rule
     * @return rejection response, or null when authentication and authorization succeed
     */
    private ApiResponse rejectForAuthAlternatives(
            final RoutingVerb verb,
            final String path,
            final ThingifierRequestContext context,
            final ThingRoute route,
            final ThingifierApiRouteRule rule) {
        final List<String> schemeNames = rule.authEnforcementSchemeNames();
        for (String schemeName : schemeNames) {
            final Optional<ThingifierApiSecuritySchemeType> schemeType =
                    authSchemeTypeFor(rule, schemeName);
            if (schemeType.isEmpty()) {
                return unknownAuthScheme(schemeName);
            }
            if (!credentialSourceIsPresent(context, schemeName, schemeType.get())) {
                continue;
            }
            return rejectForAuthScheme(
                    verb, path, context, route, rule, schemeName, schemeType.get());
        }

        final Optional<ThingifierApiSecuritySchemeType> firstSchemeType =
                authSchemeTypeFor(rule, schemeNames.get(0));
        if (firstSchemeType.isEmpty()) {
            return unknownAuthScheme(schemeNames.get(0));
        }
        return unauthorized(defaultChallengeFor(schemeNames.get(0), firstSchemeType.get()));
    }

    private ApiResponse rejectForAuthScheme(
            final RoutingVerb verb,
            final String path,
            final ThingifierRequestContext context,
            final ThingRoute route,
            final ThingifierApiRouteRule rule,
            final String schemeName,
            final ThingifierApiSecuritySchemeType schemeType) {
        switch (schemeType) {
            case API_KEY:
                return rejectForApiKeyAuth(verb, path, context, route, rule, schemeName);
            case BASIC:
                return rejectForBasicAuth(verb, path, context, route, rule, schemeName);
            case BEARER:
                return rejectForBearerAuth(verb, path, context, route, rule, schemeName);
            default:
                return unknownAuthScheme(schemeName);
        }
    }

    private Optional<ThingifierApiSecuritySchemeType> authSchemeTypeFor(
            final ThingifierApiRouteRule rule, final String schemeName) {
        if (schemeName.equals(rule.apiKeyAuthEnforcementSchemeName())) {
            return Optional.of(ThingifierApiSecuritySchemeType.API_KEY);
        }
        if (schemeName.equals(rule.basicAuthEnforcementSchemeName())) {
            return Optional.of(ThingifierApiSecuritySchemeType.BASIC);
        }
        if (schemeName.equals(rule.bearerAuthEnforcementSchemeName())) {
            return Optional.of(ThingifierApiSecuritySchemeType.BEARER);
        }
        return runtime.apiSpec().security().schemeType(schemeName);
    }

    private boolean credentialSourceIsPresent(
            final ThingifierRequestContext context,
            final String schemeName,
            final ThingifierApiSecuritySchemeType schemeType) {
        switch (schemeType) {
            case API_KEY:
                return context.headers()
                        .headerExists(runtime.apiSpec().security().apiKeyHeaderName(schemeName));
            case BASIC:
                return new BasicAuthHeaderParser(context.headers().get(AUTHORIZATION_HEADER))
                        .isBasicAuth();
            case BEARER:
                return new BearerAuthHeaderParser(context.headers().get(AUTHORIZATION_HEADER))
                        .isBearerToken();
            default:
                return false;
        }
    }

    private String defaultChallengeFor(
            final String schemeName, final ThingifierApiSecuritySchemeType schemeType) {
        switch (schemeType) {
            case BASIC:
                return basicChallengeFor(schemeName);
            case BEARER:
                return BEARER_CHALLENGE;
            case API_KEY:
            default:
                return "";
        }
    }

    private ApiResponse unknownAuthScheme(final String schemeName) {
        return ApiResponse.error(
                500, "No security scheme declaration configured for auth scheme " + schemeName);
    }

    /**
     * Enforces a named API key auth route rule.
     *
     * @param verb generated API verb
     * @param path generated API path
     * @param context active request context
     * @param route mapped generated route
     * @param rule matching route rule
     * @return rejection response, or null when authentication and authorization succeed
     */
    private ApiResponse rejectForApiKeyAuth(
            final RoutingVerb verb,
            final String path,
            final ThingifierRequestContext context,
            final ThingRoute route,
            final ThingifierApiRouteRule rule,
            final String schemeName) {
        final String headerName = runtime.apiSpec().security().apiKeyHeaderName(schemeName);
        final ApiKeyHeaderParser apiKey = new ApiKeyHeaderParser(context.headers().get(headerName));
        if (!apiKey.isValid()) {
            return unauthorized("");
        }

        final Optional<ThingifierApiAuthenticator> authenticator =
                runtime.apiSpec().authenticatorFor(schemeName);
        if (authenticator.isEmpty()) {
            return ApiResponse.error(
                    500, "No authenticator configured for API key auth scheme " + schemeName);
        }

        final ThingifierApiAuthenticationContext authenticationContext =
                ThingifierApiAuthenticationContext.apiKey(
                        authDetails(schemeName, verb, path, context, route, rule),
                        apiKey.credential(),
                        headerName);
        return rejectAfterAuthentication(
                rule, context, schemeName, authenticator.get(), authenticationContext, "");
    }

    /**
     * Enforces a named Bearer auth route rule.
     *
     * @param verb generated API verb
     * @param path generated API path
     * @param context active request context
     * @param route mapped generated route
     * @param rule matching route rule
     * @return rejection response, or null when authentication and authorization succeed
     */
    private ApiResponse rejectForBearerAuth(
            final RoutingVerb verb,
            final String path,
            final ThingifierRequestContext context,
            final ThingRoute route,
            final ThingifierApiRouteRule rule,
            final String schemeName) {
        final BearerAuthHeaderParser bearer =
                new BearerAuthHeaderParser(context.headers().get(AUTHORIZATION_HEADER));
        if (!bearer.isValid()) {
            return unauthorized(BEARER_CHALLENGE);
        }

        final Optional<ThingifierApiAuthenticator> authenticator =
                runtime.apiSpec().authenticatorFor(schemeName);
        if (authenticator.isEmpty()) {
            return ApiResponse.error(
                    500, "No authenticator configured for bearer auth scheme " + schemeName);
        }

        final ThingifierApiAuthenticationContext authenticationContext =
                new ThingifierApiAuthenticationContext(
                        authDetails(schemeName, verb, path, context, route, rule),
                        bearer.getToken());
        return rejectAfterAuthentication(
                rule,
                context,
                schemeName,
                authenticator.get(),
                authenticationContext,
                BEARER_CHALLENGE);
    }

    /**
     * Enforces a named Basic auth route rule.
     *
     * @param verb generated API verb
     * @param path generated API path
     * @param context active request context
     * @param route mapped generated route
     * @param rule matching route rule
     * @return rejection response, or null when authentication and authorization succeed
     */
    private ApiResponse rejectForBasicAuth(
            final RoutingVerb verb,
            final String path,
            final ThingifierRequestContext context,
            final ThingRoute route,
            final ThingifierApiRouteRule rule,
            final String schemeName) {
        final String challenge = basicChallengeFor(schemeName);
        final BasicAuthHeaderParser basic =
                new BasicAuthHeaderParser(context.headers().get(AUTHORIZATION_HEADER));
        if (!basic.isValid()) {
            return unauthorized(challenge);
        }

        final Optional<ThingifierApiAuthenticator> authenticator =
                runtime.apiSpec().authenticatorFor(schemeName);
        if (authenticator.isEmpty()) {
            return ApiResponse.error(
                    500, "No authenticator configured for basic auth scheme " + schemeName);
        }

        final ThingifierApiAuthenticationContext authenticationContext =
                new ThingifierApiAuthenticationContext(
                        authDetails(schemeName, verb, path, context, route, rule),
                        "",
                        basic.username(),
                        basic.password());
        return rejectAfterAuthentication(
                rule, context, schemeName, authenticator.get(), authenticationContext, challenge);
    }

    /**
     * Runs the shared authenticator/principal/authorizer flow after header parsing succeeds.
     *
     * @param rule matching route rule
     * @param context active request context
     * @param schemeName enforced security scheme name
     * @param authenticator application authenticator for the scheme
     * @param authenticationContext parsed auth context
     * @param challenge default challenge for framework-generated 401 responses
     * @return rejection response, or null when auth allows request processing
     */
    private ApiResponse rejectAfterAuthentication(
            final ThingifierApiRouteRule rule,
            final ThingifierRequestContext context,
            final String schemeName,
            final ThingifierApiAuthenticator authenticator,
            final ThingifierApiAuthenticationContext authenticationContext,
            final String challenge) {
        final ThingifierApiAuthenticationResult authentication =
                authenticator.authenticate(authenticationContext);
        if (authentication == null || !authentication.isAuthenticated()) {
            return authenticationRejected(authentication, challenge);
        }

        context.setAuthenticatedPrincipal(schemeName, authentication.principal());
        final ApiResponse dataScopeResponse = applyDataScopeSelection(context, authentication);
        if (dataScopeResponse != null) {
            return dataScopeResponse;
        }
        return rejectIfUnauthorizedByAuthorizer(rule, authenticationContext, authentication);
    }

    /**
     * Applies a trusted data-scope selection returned by the authenticator.
     *
     * <p>This runs before authorizers so permission checks, validators, handlers, lifecycle hooks,
     * and rendering all use the same selected request store. No selection preserves the context
     * chosen before auth, including the historical session-header behavior.
     *
     * @param context active request context to update
     * @param authentication successful authentication result
     * @return error response when the selected scope cannot be resolved, otherwise null
     */
    private ApiResponse applyDataScopeSelection(
            final ThingifierRequestContext context,
            final ThingifierApiAuthenticationResult authentication) {
        if (authentication.dataScopeSelection().isEmpty()) {
            return null;
        }

        final ThingifierApiDataScopeSelection selection = authentication.dataScopeSelection().get();
        if (requiresPreExistingScope(context, selection)) {
            return ApiResponse.error404("Could not find data scope " + selection.dataScopeName());
        }

        final Optional<ThingStore> selectedStore =
                runtime.storeForDataScope(selection.dataScopeName(), selection.creationPolicy());
        if (selectedStore.isEmpty()) {
            return ApiResponse.error404("Could not find data scope " + selection.dataScopeName());
        }

        if (requiresEmptyScopeAfterHeaderCreation(context, selection)) {
            selectedStore.get().administration().clearAllData();
        }
        context.useDataScope(selection.dataScopeName(), selectedStore.get());
        return null;
    }

    private boolean requiresPreExistingScope(
            final ThingifierRequestContext context,
            final ThingifierApiDataScopeSelection selection) {
        return selection.creationPolicy() == DataScopeCreationPolicy.USE_EXISTING_ONLY
                && selection.dataScopeName().equals(context.dataScopeName())
                && context.wasDataScopeCreatedWhenContextCreated();
    }

    private boolean requiresEmptyScopeAfterHeaderCreation(
            final ThingifierRequestContext context,
            final ThingifierApiDataScopeSelection selection) {
        return selection.creationPolicy() == DataScopeCreationPolicy.ENSURE_EXISTS
                && selection.dataScopeName().equals(context.dataScopeName())
                && context.wasDataScopeCreatedWhenContextCreated();
    }

    /**
     * Reports whether a route rule has enforceable auth policy.
     *
     * @param rule matching route rule
     * @return true when named auth should be enforced for this request
     */
    private boolean requiresAuth(final ThingifierApiRouteRule rule) {
        return !rule.isDisabled() && !rule.isMethodNotAllowed() && rule.hasAuthEnforcement();
    }

    /**
     * Runs route authorizers after authentication succeeds.
     *
     * @param rule matching route rule
     * @param authenticationContext context used by the authenticator
     * @param authentication successful authentication result
     * @return rejection response, or null when authorization succeeds
     */
    private ApiResponse rejectIfUnauthorizedByAuthorizer(
            final ThingifierApiRouteRule rule,
            final ThingifierApiAuthenticationContext authenticationContext,
            final ThingifierApiAuthenticationResult authentication) {
        for (ThingifierApiAuthorizer authorizer : rule.authorizers()) {
            final ThingifierApiAuthorizationResult authorization =
                    authorizer.authorize(
                            new ThingifierApiAuthorizationContext(
                                    authenticationContext, authentication.principal()));
            if (authorization == null || !authorization.isAuthorized()) {
                return authorizationRejected(authorization);
            }
        }
        return null;
    }

    /**
     * Creates the callback context details for the protected route.
     *
     * @param schemeName auth scheme name
     * @param verb generated API verb
     * @param path generated API path
     * @param context request context
     * @param route mapped generated route
     * @param rule matching route rule
     * @return shared route auth details
     */
    private ThingifierApiRouteAuthDetails authDetails(
            final String schemeName,
            final RoutingVerb verb,
            final String path,
            final ThingifierRequestContext context,
            final ThingRoute route,
            final ThingifierApiRouteRule rule) {
        return ThingifierApiRouteAuthDetails.builder()
                .schemeName(schemeName)
                .verb(verb)
                .path(path)
                .pathParameters(
                        ApiRoutePathMatcher.pathParameters(
                                rule.pathPattern(),
                                path,
                                runtime.apiConfig().getApiEndPointPrefix()))
                .route(route)
                .headers(context.headers())
                .requestContext(context)
                .targetEntity(targetEntity(route))
                .targetIdentifier(targetIdentifier(route))
                .parentEntity(parentEntity(route))
                .parentIdentifier(parentIdentifier(route))
                .relationshipName(relationshipName(route))
                .childIdentifier(childIdentifier(route))
                .build();
    }

    /**
     * Converts an authentication failure into a response.
     *
     * @param authentication failed authentication result, possibly null
     * @param challenge default challenge for framework-generated 401 responses
     * @return rejection response with a challenge when appropriate
     */
    private ApiResponse authenticationRejected(
            final ThingifierApiAuthenticationResult authentication, final String challenge) {
        if (authentication == null || authentication.rejectionResponse() == null) {
            return unauthorized(challenge);
        }
        final ApiResponse response = authentication.rejectionResponse();
        if (authentication.hasCustomRejectionResponse()) {
            return response;
        }
        return challengeIfUnauthorized(response, challenge);
    }

    /**
     * Converts an authorization failure into a response.
     *
     * @param authorization failed authorization result, possibly null
     * @return rejection response
     */
    private ApiResponse authorizationRejected(
            final ThingifierApiAuthorizationResult authorization) {
        if (authorization == null || authorization.rejectionResponse() == null) {
            return ApiResponse.error(403, "Forbidden");
        }
        return authorization.rejectionResponse();
    }

    /**
     * Creates a standard unauthorized response.
     *
     * @return 401 response with a WWW-Authenticate challenge
     */
    private ApiResponse unauthorized(final String challenge) {
        final ApiResponse response = ApiResponse.error(401, "Unauthorized");
        if (challenge != null && !challenge.trim().isEmpty()) {
            response.setHeader(WWW_AUTHENTICATE_HEADER, challenge);
        }
        return response;
    }

    /**
     * Ensures standard 401 responses include the enforced scheme challenge header.
     *
     * @param response response returned by an authenticator
     * @param challenge default challenge for the enforced scheme
     * @return same response with the challenge header when appropriate
     */
    private ApiResponse challengeIfUnauthorized(
            final ApiResponse response, final String challenge) {
        if (response.getStatusCode() == 401
                && challenge != null
                && !challenge.trim().isEmpty()
                && response.getHeaderValue(WWW_AUTHENTICATE_HEADER).isEmpty()) {
            response.setHeader(WWW_AUTHENTICATE_HEADER, challenge);
        }
        return response;
    }

    /**
     * Builds the Basic challenge header for a named scheme.
     *
     * @param schemeName Basic auth scheme name
     * @return challenge header value with configured realm
     */
    private String basicChallengeFor(final String schemeName) {
        return "Basic realm=\""
                + escapedRealm(runtime.apiSpec().security().basicRealm(schemeName))
                + "\"";
    }

    private String escapedRealm(final String realm) {
        return realm.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "");
    }

    /**
     * Resolves the target entity for entity and relationship routes.
     *
     * @param route mapped route
     * @return target entity, or null when the route is unmatched
     */
    private EntityDefinition targetEntity(final ThingRoute route) {
        if (route instanceof CollectionRoute) {
            return entityNamed(((CollectionRoute) route).entity().name());
        }
        if (route instanceof InstanceRoute) {
            return entityNamed(((InstanceRoute) route).entity().name());
        }
        if (route instanceof RelationshipCollectionRoute) {
            final RelationshipCollectionRoute relationship = (RelationshipCollectionRoute) route;
            return targetEntityForRelationship(
                    relationship.parentEntity(), relationship.relationshipName());
        }
        if (route instanceof RelationshipInstanceRoute) {
            final RelationshipInstanceRoute relationship = (RelationshipInstanceRoute) route;
            return targetEntityForRelationship(
                    relationship.parentEntity(), relationship.relationshipName());
        }
        return null;
    }

    /**
     * Resolves the parent entity for relationship routes.
     *
     * @param route mapped route
     * @return parent entity, or null for entity routes
     */
    private EntityDefinition parentEntity(final ThingRoute route) {
        if (route instanceof RelationshipCollectionRoute) {
            return entityNamed(((RelationshipCollectionRoute) route).parentEntity().name());
        }
        if (route instanceof RelationshipInstanceRoute) {
            return entityNamed(((RelationshipInstanceRoute) route).parentEntity().name());
        }
        return null;
    }

    /**
     * Resolves the target identifier for instance routes.
     *
     * @param route mapped route
     * @return target identifier, or null for collection routes
     */
    private String targetIdentifier(final ThingRoute route) {
        if (route instanceof InstanceRoute) {
            return ((InstanceRoute) route).identifier();
        }
        if (route instanceof RelationshipInstanceRoute) {
            return ((RelationshipInstanceRoute) route).childIdentifier();
        }
        return null;
    }

    /**
     * Resolves the parent identifier for relationship routes.
     *
     * @param route mapped route
     * @return parent identifier, or null for entity routes
     */
    private String parentIdentifier(final ThingRoute route) {
        if (route instanceof RelationshipCollectionRoute) {
            return ((RelationshipCollectionRoute) route).parentIdentifier();
        }
        if (route instanceof RelationshipInstanceRoute) {
            return ((RelationshipInstanceRoute) route).parentIdentifier();
        }
        return null;
    }

    /**
     * Resolves the relationship name for relationship routes.
     *
     * @param route mapped route
     * @return relationship name, or null for entity routes
     */
    private String relationshipName(final ThingRoute route) {
        if (route instanceof RelationshipCollectionRoute) {
            return ((RelationshipCollectionRoute) route).relationshipName();
        }
        if (route instanceof RelationshipInstanceRoute) {
            return ((RelationshipInstanceRoute) route).relationshipName();
        }
        return null;
    }

    /**
     * Resolves the child identifier for relationship instance routes.
     *
     * @param route mapped route
     * @return child identifier, or null when the route has no child
     */
    private String childIdentifier(final ThingRoute route) {
        if (route instanceof RelationshipInstanceRoute) {
            return ((RelationshipInstanceRoute) route).childIdentifier();
        }
        return null;
    }

    /**
     * Resolves the target entity for a relationship route.
     *
     * @param parentEntity parent entity reference
     * @param relationshipName relationship route name
     * @return related entity, or null when the relationship cannot be resolved
     */
    private EntityDefinition targetEntityForRelationship(
            final EntityTypeRef parentEntity, final String relationshipName) {
        if (parentEntity == null) {
            return null;
        }
        for (RelationshipSpec spec : parentEntity.relationships()) {
            if (spec.name().equals(relationshipName)) {
                return entityNamed(spec.toEntityName());
            }
        }
        return null;
    }

    /**
     * Looks up an entity definition by singular or plural model name.
     *
     * @param entityName singular or plural entity name
     * @return matching entity definition, or null
     */
    private EntityDefinition entityNamed(final String entityName) {
        return runtime.schema().definitionWithSingularOrPluralNamed(entityName);
    }
}
