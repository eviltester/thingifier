package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.Optional;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.CollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.InstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipCollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipInstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRouteMapper;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.BearerAuthHeaderParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthenticationContext;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthenticationResult;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthenticator;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthorizationContext;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthorizationResult;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthorizer;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiRouteAuthDetails;
import uk.co.compendiumdev.thingifier.api.spec.ApiRoutePathMatcher;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiRouteRule;
import uk.co.compendiumdev.thingifier.application.schema.EntityTypeRef;
import uk.co.compendiumdev.thingifier.application.schema.RelationshipSpec;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;

/**
 * Enforces route-level authentication and authorization configured in the API spec.
 *
 * <p>The policy handles the generic HTTP bearer mechanics that belong in Thingifier and delegates
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
        final ThingRoute route = new ThingRouteMapper(runtime.schema()).map(path);
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

        final ThingifierApiRouteRule rule = matchingRule.get();
        final String schemeName = rule.bearerAuthEnforcementSchemeName();
        final Optional<ThingifierApiAuthenticator> authenticator =
                runtime.apiSpec().authenticatorFor(schemeName);
        if (authenticator.isEmpty()) {
            return ApiResponse.error(
                    500, "No authenticator configured for bearer auth scheme " + schemeName);
        }

        final BearerAuthHeaderParser bearer =
                new BearerAuthHeaderParser(context.headers().get(AUTHORIZATION_HEADER));
        if (!bearer.isValid()) {
            return unauthorized();
        }

        final ThingifierApiAuthenticationContext authenticationContext =
                new ThingifierApiAuthenticationContext(
                        authDetails(schemeName, verb, path, context, route, rule),
                        bearer.getToken());
        final ThingifierApiAuthenticationResult authentication =
                authenticator.get().authenticate(authenticationContext);
        if (authentication == null || !authentication.isAuthenticated()) {
            return authenticationRejected(authentication);
        }

        context.setAuthenticatedPrincipal(schemeName, authentication.principal());
        return rejectIfUnauthorizedByAuthorizer(rule, authenticationContext, authentication);
    }

    /**
     * Reports whether a route rule has enforceable auth policy.
     *
     * @param rule matching route rule
     * @return true when bearer auth should be enforced for this request
     */
    private boolean requiresAuth(final ThingifierApiRouteRule rule) {
        return !rule.isDisabled() && !rule.isMethodNotAllowed() && rule.hasBearerAuthEnforcement();
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
     * @param schemeName bearer scheme name
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
     * @return rejection response with a bearer challenge for 401 responses
     */
    private ApiResponse authenticationRejected(
            final ThingifierApiAuthenticationResult authentication) {
        final ApiResponse response =
                authentication == null || authentication.rejectionResponse() == null
                        ? unauthorized()
                        : authentication.rejectionResponse();
        return challengeIfUnauthorized(response);
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
     * Creates the standard bearer unauthorized response.
     *
     * @return 401 response with a WWW-Authenticate challenge
     */
    private ApiResponse unauthorized() {
        return ApiResponse.error(401, "Unauthorized")
                .setHeader(WWW_AUTHENTICATE_HEADER, BEARER_CHALLENGE);
    }

    /**
     * Ensures authenticator-supplied 401 responses include the bearer challenge header.
     *
     * @param response response returned by an authenticator
     * @return same response with the challenge header when appropriate
     */
    private ApiResponse challengeIfUnauthorized(final ApiResponse response) {
        if (response.getStatusCode() == 401
                && response.getHeaderValue(WWW_AUTHENTICATE_HEADER).isEmpty()) {
            response.setHeader(WWW_AUTHENTICATE_HEADER, BEARER_CHALLENGE);
        }
        return response;
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
