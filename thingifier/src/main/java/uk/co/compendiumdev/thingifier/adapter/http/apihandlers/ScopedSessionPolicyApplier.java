package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.CollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.InstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipCollectionRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.RelationshipInstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.UnmatchedRoute;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthenticationContext;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthorizationContext;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthorizationResult;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiAuthorizer;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiDataScopeSelection;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiRouteAuthDetails;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiScopedSessionContext;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiScopedSessionCredentialSourceType;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiScopedSessionDefinition;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiScopedSessionPolicy;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiScopedSessionResult;
import uk.co.compendiumdev.thingifier.api.spec.ApiRoutePathMatcher;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiRouteRule;
import uk.co.compendiumdev.thingifier.application.schema.EntityTypeRef;
import uk.co.compendiumdev.thingifier.application.schema.RelationshipSpec;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.query.FilterBy;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

/**
 * Resolves configured scoped-session credentials before route auth, validators, and handlers run.
 *
 * <p>The policy exists to keep session-like request values out of low-level hooks. Thingifier reads
 * the configured credential source, asks trusted application code to validate it, and only then
 * applies any returned data-scope selection to the shared request context.
 */
public final class ScopedSessionPolicyApplier {

    private static final String COOKIE_HEADER = "Cookie";

    private final ThingifierApiRuntime runtime;
    private final DataScopeSelectionApplier dataScopeSelectionApplier;

    /**
     * Creates a scoped-session policy applier.
     *
     * @param runtime runtime services used to resolve routes, stores, and API spec policy
     */
    public ScopedSessionPolicyApplier(final ThingifierApiRuntime runtime) {
        this.runtime = runtime;
        this.dataScopeSelectionApplier = new DataScopeSelectionApplier(runtime);
    }

    /**
     * Applies scoped-session policy for one route.
     *
     * @param verb generated API verb
     * @param path request path
     * @param context active request context to update
     * @param route resolved route, or null to resolve here
     * @param queryParams request query parameters
     * @return rejection response when processing should stop, otherwise null
     */
    public ApiResponse rejectIfNotResolved(
            final RoutingVerb verb,
            final String path,
            final ThingifierRequestContext context,
            final ThingRoute route,
            final QueryFilterParams queryParams) {
        if (verb == null || context == null) {
            return null;
        }

        final String apiPathPrefix = runtime.apiConfig().getApiEndPointPrefix();
        final ThingRoute resolvedRoute = route == null ? runtime.routeFor(verb, path) : route;
        final Optional<ThingifierApiRouteRule> matchingRule =
                runtime.apiSpec().ruleFor(verb, path, apiPathPrefix);
        if (resolvedRoute instanceof UnmatchedRoute && matchingRule.isEmpty()) {
            return null;
        }

        final Optional<ThingifierApiScopedSessionPolicy> scopedSessionPolicy =
                runtime.apiSpec().scopedSessionPolicyFor(verb, path, apiPathPrefix);
        if (scopedSessionPolicy.isEmpty()) {
            return null;
        }

        final ThingifierApiScopedSessionPolicy policy = scopedSessionPolicy.get();
        if (policy.definition().isEmpty()) {
            return ApiResponse.error(
                    500, "No scoped-session definition configured for " + policy.sessionName());
        }

        final ThingifierApiScopedSessionDefinition definition = policy.definition().get();
        if (!definition.hasCredentialSource()) {
            return ApiResponse.error(
                    500, "No credential source configured for scoped-session " + definition.name());
        }

        final CredentialValue credential =
                credentialFrom(definition, context, safeQueryParams(queryParams));
        if (!credential.present()) {
            if (policy.requiresAuthenticatedScope()) {
                return definition.missingRequiredCredentialResponse();
            }
            return applyAnonymousScope(
                    policy,
                    definition,
                    verb,
                    path,
                    context,
                    resolvedRoute,
                    matchingRule,
                    safeQueryParams(queryParams));
        }

        if (definition.authenticator() == null) {
            return ApiResponse.error(
                    500, "No scoped-session authenticator configured for " + definition.name());
        }

        final ThingifierApiScopedSessionResult result =
                definition
                        .authenticator()
                        .authenticate(
                                scopedSessionContext(
                                        definition,
                                        credential.value(),
                                        verb,
                                        path,
                                        context,
                                        resolvedRoute,
                                        safeQueryParams(queryParams)));
        if (result == null || !result.isAuthenticated()) {
            return scopedSessionRejected(definition, result);
        }

        context.setAuthenticatedPrincipal(definition.name(), result.principal());
        if (result.dataScopeSelection().isPresent()) {
            final ApiResponse dataScopeResponse =
                    dataScopeSelectionApplier.apply(context, result.dataScopeSelection().get());
            if (dataScopeResponse != null) {
                return dataScopeResponse;
            }
        }
        if (matchingRule.isPresent() && !matchingRule.get().hasAuthEnforcement()) {
            return rejectIfUnauthorizedByAuthorizer(
                    matchingRule.get(),
                    scopedSessionAuthContext(
                            definition,
                            credential.value(),
                            verb,
                            path,
                            context,
                            resolvedRoute,
                            matchingRule.get()),
                    result.principal());
        }
        return null;
    }

    private ApiResponse applyAnonymousScope(
            final ThingifierApiScopedSessionPolicy policy,
            final ThingifierApiScopedSessionDefinition definition,
            final RoutingVerb verb,
            final String path,
            final ThingifierRequestContext context,
            final ThingRoute route,
            final Optional<ThingifierApiRouteRule> matchingRule,
            final QueryFilterParams queryParams) {
        final ThingifierApiDataScopeSelection selection =
                anonymousDataScopeSelection(
                        policy, definition, verb, path, context, route, queryParams);
        if (selection == null) {
            return ApiResponse.error(
                    500,
                    "No anonymous data scope selected for scoped-session " + definition.name());
        }

        final ApiResponse dataScopeResponse = dataScopeSelectionApplier.apply(context, selection);
        if (dataScopeResponse != null) {
            return dataScopeResponse;
        }

        if (matchingRule.isPresent() && !matchingRule.get().hasAuthEnforcement()) {
            return rejectIfUnauthorizedByAuthorizer(
                    matchingRule.get(),
                    scopedSessionAuthContext(
                            definition, "", verb, path, context, route, matchingRule.get()),
                    null);
        }
        return null;
    }

    private ThingifierApiDataScopeSelection anonymousDataScopeSelection(
            final ThingifierApiScopedSessionPolicy policy,
            final ThingifierApiScopedSessionDefinition definition,
            final RoutingVerb verb,
            final String path,
            final ThingifierRequestContext context,
            final ThingRoute route,
            final QueryFilterParams queryParams) {
        if (policy.allowsAnonymousDefaultScope()) {
            return ThingifierApiDataScopeSelection.defaultDataScope();
        }
        return definition.anonymousDataScopeSelection(
                scopedSessionContext(definition, "", verb, path, context, route, queryParams));
    }

    private ApiResponse scopedSessionRejected(
            final ThingifierApiScopedSessionDefinition definition,
            final ThingifierApiScopedSessionResult result) {
        if (result == null || result.rejectionResponse() == null) {
            return definition.invalidCredentialResponse();
        }
        return result.rejectionResponse();
    }

    private ThingifierApiScopedSessionContext scopedSessionContext(
            final ThingifierApiScopedSessionDefinition definition,
            final String credential,
            final RoutingVerb verb,
            final String path,
            final ThingifierRequestContext context,
            final ThingRoute route,
            final QueryFilterParams queryParams) {
        final Optional<ThingifierApiRouteRule> rule =
                runtime.apiSpec().ruleFor(verb, path, runtime.apiConfig().getApiEndPointPrefix());
        return ThingifierApiScopedSessionContext.builder()
                .sessionName(definition.name())
                .credential(credential)
                .credentialSourceType(definition.credentialSourceType())
                .credentialSourceName(definition.credentialSourceName())
                .verb(verb)
                .path(path)
                .pathParameters(
                        rule.map(
                                        value ->
                                                ApiRoutePathMatcher.pathParameters(
                                                        value.pathPattern(),
                                                        path,
                                                        runtime.apiConfig().getApiEndPointPrefix()))
                                .orElseGet(LinkedHashMap::new))
                .route(route)
                .headers(context.headers())
                .queryParams(queryParams)
                .requestContext(context)
                .targetEntity(targetEntity(route))
                .targetIdentifier(targetIdentifier(route))
                .parentEntity(parentEntity(route))
                .parentIdentifier(parentIdentifier(route))
                .relationshipName(relationshipName(route))
                .childIdentifier(childIdentifier(route))
                .build();
    }

    private ThingifierApiAuthenticationContext scopedSessionAuthContext(
            final ThingifierApiScopedSessionDefinition definition,
            final String credential,
            final RoutingVerb verb,
            final String path,
            final ThingifierRequestContext context,
            final ThingRoute route,
            final ThingifierApiRouteRule rule) {
        return ThingifierApiAuthenticationContext.apiKey(
                ThingifierApiRouteAuthDetails.builder()
                        .schemeName(definition.name())
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
                        .build(),
                credential,
                definition.credentialSourceName());
    }

    private ApiResponse rejectIfUnauthorizedByAuthorizer(
            final ThingifierApiRouteRule rule,
            final ThingifierApiAuthenticationContext authenticationContext,
            final Object principal) {
        for (ThingifierApiAuthorizer authorizer : rule.authorizers()) {
            final ThingifierApiAuthorizationResult authorization =
                    authorizer.authorize(
                            new ThingifierApiAuthorizationContext(
                                    authenticationContext, principal));
            if (authorization == null || !authorization.isAuthorized()) {
                return authorizationRejected(authorization);
            }
        }
        return null;
    }

    private ApiResponse authorizationRejected(
            final ThingifierApiAuthorizationResult authorization) {
        if (authorization == null || authorization.rejectionResponse() == null) {
            return ApiResponse.error(403, "Forbidden");
        }
        return authorization.rejectionResponse();
    }

    private CredentialValue credentialFrom(
            final ThingifierApiScopedSessionDefinition definition,
            final ThingifierRequestContext context,
            final QueryFilterParams queryParams) {
        final ThingifierApiScopedSessionCredentialSourceType sourceType =
                definition.credentialSourceType();
        switch (sourceType) {
            case HEADER:
                return credentialFromHeader(context, definition.credentialSourceName());
            case QUERY_PARAM:
                return credentialFromQueryParam(queryParams, definition.credentialSourceName());
            case COOKIE:
                return credentialFromCookie(context, definition.credentialSourceName());
            default:
                return CredentialValue.missing();
        }
    }

    private CredentialValue credentialFromHeader(
            final ThingifierRequestContext context, final String headerName) {
        if (!context.headers().headerExists(headerName)) {
            return CredentialValue.missing();
        }
        return CredentialValue.present(context.headers().get(headerName));
    }

    private CredentialValue credentialFromQueryParam(
            final QueryFilterParams queryParams, final String queryParamName) {
        for (FilterBy filterBy : safeQueryParams(queryParams).toList()) {
            if (queryParamName.equals(filterBy.fieldName)) {
                return CredentialValue.present(filterBy.fieldValue);
            }
        }
        return CredentialValue.missing();
    }

    private CredentialValue credentialFromCookie(
            final ThingifierRequestContext context, final String cookieName) {
        if (!context.headers().headerExists(COOKIE_HEADER)) {
            return CredentialValue.missing();
        }
        Map<String, String> cookies = cookiesFrom(context.headers().get(COOKIE_HEADER));
        if (!cookies.containsKey(cookieName)) {
            return CredentialValue.missing();
        }
        return CredentialValue.present(cookies.get(cookieName));
    }

    private Map<String, String> cookiesFrom(final String cookieHeader) {
        Map<String, String> cookies = new LinkedHashMap<>();
        if (cookieHeader == null || cookieHeader.trim().isEmpty()) {
            return cookies;
        }
        String[] cookieParts = cookieHeader.split(";");
        for (String cookiePart : cookieParts) {
            int separator = cookiePart.indexOf('=');
            if (separator < 0) {
                continue;
            }
            String name = cookiePart.substring(0, separator).trim();
            String value = cookiePart.substring(separator + 1).trim();
            if (!name.isEmpty()) {
                cookies.put(name, value);
            }
        }
        return cookies;
    }

    private QueryFilterParams safeQueryParams(final QueryFilterParams queryParams) {
        return queryParams == null ? new QueryFilterParams() : queryParams;
    }

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

    private EntityDefinition parentEntity(final ThingRoute route) {
        if (route instanceof RelationshipCollectionRoute) {
            return entityNamed(((RelationshipCollectionRoute) route).parentEntity().name());
        }
        if (route instanceof RelationshipInstanceRoute) {
            return entityNamed(((RelationshipInstanceRoute) route).parentEntity().name());
        }
        return null;
    }

    private String targetIdentifier(final ThingRoute route) {
        if (route instanceof InstanceRoute) {
            return ((InstanceRoute) route).identifier();
        }
        if (route instanceof RelationshipInstanceRoute) {
            return ((RelationshipInstanceRoute) route).childIdentifier();
        }
        return null;
    }

    private String parentIdentifier(final ThingRoute route) {
        if (route instanceof RelationshipCollectionRoute) {
            return ((RelationshipCollectionRoute) route).parentIdentifier();
        }
        if (route instanceof RelationshipInstanceRoute) {
            return ((RelationshipInstanceRoute) route).parentIdentifier();
        }
        return null;
    }

    private String relationshipName(final ThingRoute route) {
        if (route instanceof RelationshipCollectionRoute) {
            return ((RelationshipCollectionRoute) route).relationshipName();
        }
        if (route instanceof RelationshipInstanceRoute) {
            return ((RelationshipInstanceRoute) route).relationshipName();
        }
        return null;
    }

    private String childIdentifier(final ThingRoute route) {
        if (route instanceof RelationshipInstanceRoute) {
            return ((RelationshipInstanceRoute) route).childIdentifier();
        }
        return null;
    }

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

    private EntityDefinition entityNamed(final String entityName) {
        return runtime.schema().definitionWithSingularOrPluralNamed(entityName);
    }

    private static final class CredentialValue {
        private final boolean present;
        private final String value;

        private CredentialValue(final boolean present, final String value) {
            this.present = present;
            this.value = value == null ? "" : value;
        }

        static CredentialValue missing() {
            return new CredentialValue(false, "");
        }

        static CredentialValue present(final String value) {
            return new CredentialValue(true, value);
        }

        boolean present() {
            return present;
        }

        String value() {
            return value;
        }
    }
}
