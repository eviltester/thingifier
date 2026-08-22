package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.Optional;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
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
        if (response == null) {
            return null;
        }

        final Optional<RouteApiResponsePolicy> selectedPolicy =
                routeRuleFor(verb, publicPath).flatMap(rule -> policyFor(rule, response));

        selectedPolicy.ifPresent(policy -> applyStatusAndHeaders(policy, response));
        if (responseViewApplicator != null) {
            responseViewApplicator.apply(response);
        }
        selectedPolicy.ifPresent(policy -> applyBodyPolicy(policy, response));

        return response;
    }

    private Optional<ThingifierApiRouteRule> routeRuleFor(
            final RoutingVerb verb, final String publicPath) {
        return runtime.apiSpec()
                .ruleFor(verb, publicPath, runtime.apiConfig().getApiEndPointPrefix());
    }

    private Optional<RouteApiResponsePolicy> policyFor(
            final ThingifierApiRouteRule rule, final ApiResponse response) {
        if (response.isValidationErrorResponse()) {
            return rule.validationErrorResponsePolicy();
        }
        if (response.isErrorResponse()) {
            return rule.errorResponsePolicyFor(response.getStatusCode());
        }
        return rule.successResponsePolicy();
    }

    private void applyStatusAndHeaders(
            final RouteApiResponsePolicy policy, final ApiResponse response) {
        if (policy.statusCode() != null) {
            response.withStatusCode(policy.statusCode());
        }

        for (RouteApiResponsePolicy.HeaderValue header : policy.staticHeaders()) {
            response.setHeader(header.name(), header.value());
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
