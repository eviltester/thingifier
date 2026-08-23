package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.List;
import java.util.Optional;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.response.RouteApiResponsePolicy;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiRouteRule;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;

/**
 * Applies route-level response policies to generated API responses.
 *
 * <p>The applier sits after Thingifier has created a structured {@link ApiResponse}. That keeps
 * route policies focused on public response shape while generated command/query handling,
 * validation, persistence, and hooks continue to produce the canonical result first.
 */
public final class RouteApiResponsePolicyApplier {

    private final ThingifierApiRuntime runtime;

    /**
     * Creates an applier using the current API runtime.
     *
     * @param runtime runtime used to find route rules and entity definitions
     */
    public RouteApiResponsePolicyApplier(final ThingifierApiRuntime runtime) {
        this.runtime = runtime;
    }

    /**
     * Applies the matching route response policy, if one exists.
     *
     * <p>Status and headers are applied before the supplied response-view callback so route
     * status-specific views can see any status override. Body actions are applied last so a policy
     * can suppress or replace the rendered body after normal view selection.
     *
     * @param verb routing verb for route-rule lookup
     * @param publicPath public request path
     * @param response generated response
     * @param responseViewApplicator normal route/entity response-view applicator
     * @return the same response after policy actions have been applied
     */
    public ApiResponse apply(
            final RoutingVerb verb,
            final String publicPath,
            final ApiResponse response,
            final ResponseViewApplicator responseViewApplicator) {
        return apply(verb, publicPath, response, new HttpHeadersBlock(), responseViewApplicator);
    }

    /**
     * Applies the matching route response policies, including request-aware conditional policies.
     *
     * <p>The unconditional status policy runs first, then each matching conditional policy runs in
     * declaration order. This lets a route describe its default response shape and then layer
     * request-specific adjustments over the top.
     *
     * @param verb routing verb for route-rule lookup
     * @param publicPath public request path
     * @param response generated response
     * @param requestHeaders request headers used by conditional policies
     * @param responseViewApplicator normal route/entity response-view applicator
     * @return the same response after policy actions have been applied
     */
    public ApiResponse apply(
            final RoutingVerb verb,
            final String publicPath,
            final ApiResponse response,
            final HttpHeadersBlock requestHeaders,
            final ResponseViewApplicator responseViewApplicator) {
        if (response == null) {
            return null;
        }

        final Optional<ThingifierApiRouteRule> selectedRule = routeRuleFor(verb, publicPath);
        final ApiResponse shapedResponse =
                selectedRule
                        .map(rule -> applyResponseShape(rule, publicPath, response))
                        .orElse(response);
        final List<RouteApiResponsePolicy> selectedPolicies =
                selectedRule
                        .map(rule -> policiesFor(rule, shapedResponse, requestHeaders))
                        .orElse(List.of());

        selectedPolicies.forEach(policy -> applyStatusAndHeaders(policy, shapedResponse));
        if (responseViewApplicator != null) {
            responseViewApplicator.apply(shapedResponse);
        }
        selectedPolicies.forEach(policy -> applyBodyPolicy(policy, shapedResponse));

        return shapedResponse;
    }

    private Optional<ThingifierApiRouteRule> routeRuleFor(
            final RoutingVerb verb, final String publicPath) {
        return runtime.apiSpec()
                .ruleFor(verb, publicPath, runtime.apiConfig().getApiEndPointPrefix());
    }

    private List<RouteApiResponsePolicy> policiesFor(
            final ThingifierApiRouteRule rule,
            final ApiResponse response,
            final HttpHeadersBlock requestHeaders) {
        final List<RouteApiResponsePolicy> policies = new java.util.ArrayList<>();
        if (response.isValidationErrorResponse()) {
            rule.validationErrorResponsePolicy()
                    .filter(policy -> policy.matchesRequest(requestHeaders))
                    .ifPresent(policies::add);
            return policies;
        }
        if (response.isErrorResponse() || response.getStatusCode() >= 400) {
            rule.errorResponsePolicyFor(response.getStatusCode())
                    .filter(policy -> policy.matchesRequest(requestHeaders))
                    .ifPresent(policies::add);
            for (RouteApiResponsePolicy policy :
                    rule.conditionalErrorResponsePoliciesFor(response.getStatusCode())) {
                if (policy.matchesRequest(requestHeaders)) {
                    policies.add(policy);
                }
            }
            return policies;
        }
        rule.successResponsePolicy()
                .filter(policy -> policy.matchesRequest(requestHeaders))
                .ifPresent(policies::add);
        return policies;
    }

    private ApiResponse applyResponseShape(
            final ThingifierApiRouteRule rule,
            final String publicPath,
            final ApiResponse response) {
        if (!rule.hasResponseShapeOverride()
                || response.isErrorResponse()
                || response.hasABodyOverride()
                || response.getStatusCode() < 200
                || response.getStatusCode() >= 300) {
            return response;
        }

        switch (rule.responseShape()) {
            case SINGLE_INSTANCE:
                return singleInstanceResponse(rule, publicPath, response);
            case COLLECTION:
                return collectionResponse(rule, publicPath, response);
            case DEFAULT:
            default:
                return response;
        }
    }

    private ApiResponse singleInstanceResponse(
            final ThingifierApiRouteRule rule,
            final String publicPath,
            final ApiResponse response) {
        if (!rule.hasFixedIdentifierMapping()) {
            return ApiResponse.error(
                    500,
                    String.format(
                            "Route %s is configured for a single instance response but is not a fixed identifier route",
                            publicPath));
        }
        if (response.hasReturnedInstance()) {
            return response;
        }
        if (response.hasReturnedDraft()) {
            return ApiResponse.error(
                    500,
                    String.format(
                            "Route %s is configured for a single instance response but returned a draft instance",
                            publicPath));
        }
        if (!response.isCollection()) {
            return response;
        }

        final boolean hadBody = response.hasABody();
        final List<EntityInstance> instances = response.getReturnedInstanceCollection();
        if (instances.isEmpty()) {
            return ApiResponse.error404(
                    String.format("Could not find an instance with %s", publicPath));
        }
        if (instances.size() > 1) {
            return ApiResponse.error(
                    500,
                    String.format(
                            "Route %s is configured for a single instance response but returned %d instances",
                            publicPath, instances.size()));
        }
        return preserveBodyPresence(response.returnSingleInstance(instances.get(0)), hadBody);
    }

    private ApiResponse collectionResponse(
            final ThingifierApiRouteRule rule,
            final String publicPath,
            final ApiResponse response) {
        if (!rule.hasFixedIdentifierMapping()) {
            return ApiResponse.error(
                    500,
                    String.format(
                            "Route %s is configured for a collection response but is not a fixed identifier route",
                            publicPath));
        }
        if (response.isCollection()) {
            return response;
        }
        if (response.hasReturnedInstance()) {
            final boolean hadBody = response.hasABody();
            return preserveBodyPresence(
                    response.returnInstanceCollection(List.of(response.getReturnedInstance())),
                    hadBody);
        }
        if (response.hasReturnedDraft()) {
            return ApiResponse.error(
                    500,
                    String.format(
                            "Route %s is configured for a collection response but returned a draft instance",
                            publicPath));
        }
        return response;
    }

    private ApiResponse preserveBodyPresence(final ApiResponse response, final boolean hadBody) {
        if (!hadBody) {
            response.clearBody();
        }
        return response;
    }

    private void applyStatusAndHeaders(
            final RouteApiResponsePolicy policy, final ApiResponse response) {
        if (policy.statusCode() != null) {
            response.withStatusCode(policy.statusCode());
        }

        for (RouteApiResponsePolicy.HeaderValue header : policy.staticHeaders()) {
            response.setHeader(header.name(), header.value());
        }

        for (String headerName : policy.removedHeaders()) {
            response.removeHeader(headerName);
        }

        for (RouteApiResponsePolicy.InstanceFieldHeader header : policy.instanceFieldHeaders()) {
            returnedFieldValue(response, header.fieldName())
                    .ifPresent(value -> response.setHeader(header.headerName(), value));
        }
    }

    private Optional<String> returnedFieldValue(
            final ApiResponse response, final String fieldName) {
        if (response.hasReturnedInstance()) {
            final EntityInstance instance = response.getReturnedInstance();
            if (instance.hasInstantiatedFieldNamed(fieldName)) {
                final FieldValue value = instance.getFieldValue(fieldName);
                return value == null ? Optional.empty() : Optional.ofNullable(value.asString());
            }
        }

        if (response.hasReturnedDraft()) {
            final EntityInstanceDraft draft = response.getReturnedDraft();
            return draftFieldValue(draft, fieldName);
        }

        return Optional.empty();
    }

    private Optional<String> draftFieldValue(
            final EntityInstanceDraft draft, final String fieldName) {
        for (NamedValue value : draft.getFieldValues()) {
            if (value.getName().equals(fieldName)) {
                return Optional.ofNullable(value.asString());
            }
        }
        for (NamedValue value : draft.getProtectedFieldValues()) {
            if (value.getName().equals(fieldName)) {
                return Optional.ofNullable(value.asString());
            }
        }
        return Optional.empty();
    }

    private void applyBodyPolicy(final RouteApiResponsePolicy policy, final ApiResponse response) {
        switch (policy.bodyAction()) {
            case SUPPRESS:
                response.clearBody();
                break;
            case TEXT:
                response.setBody(policy.bodyText());
                break;
            case ENTITY_VIEW:
                applyEntityView(response, policy.entityViewName());
                break;
            case PRESERVE:
            default:
                break;
        }
    }

    private void applyEntityView(final ApiResponse response, final String viewName) {
        final EntityDefinition entity = response.getTypeOfThingReturned();
        if (entity == null || viewName == null || !entity.hasViewNamed(viewName)) {
            return;
        }
        response.usingEntityView(entity.getViewNamed(viewName));
    }

    /** Applies normal route/entity response view policy before final body actions. */
    @FunctionalInterface
    public interface ResponseViewApplicator {
        /**
         * Applies response view configuration to the generated response.
         *
         * @param response response to update
         */
        void apply(ApiResponse response);
    }
}
