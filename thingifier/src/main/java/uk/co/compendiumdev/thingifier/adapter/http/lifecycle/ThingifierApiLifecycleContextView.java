package uk.co.compendiumdev.thingifier.adapter.http.lifecycle;

import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.api.http.ApiRequestEnvelope;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.ThingCommandResult;
import uk.co.compendiumdev.thingifier.application.command.ThingWriteCommand;
import uk.co.compendiumdev.thingifier.application.query.ThingReadQuery;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.query.RepositoryQueryResult;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

/**
 * Shared context contract exposed to all Thingifier API lifecycle hook phases.
 *
 * <p>The context exposes route, request, mapped command/query, validation, result, and response
 * information as it becomes available. Mutations are explicit methods so hook code documents when
 * it intentionally changes the request, command, query, result, or response.
 */
public interface ThingifierApiLifecycleContextView {

    /**
     * Returns the effective HTTP verb after method override handling.
     *
     * @return effective HTTP API verb
     */
    ThingifierHttpApi.HttpVerb effectiveVerb();

    /**
     * Returns the normalized request path used for Thingifier routing.
     *
     * @return request path without the configured API prefix
     */
    String path();

    /**
     * Returns the mapped Thingifier route.
     *
     * @return matched route abstraction
     */
    ThingRoute route();

    /**
     * Returns the entity targeted by the request or returned relationship.
     *
     * @return target entity definition, or null when no entity route matched
     */
    EntityDefinition targetEntity();

    /**
     * Returns the target instance identifier for instance routes.
     *
     * @return instance identifier, or null for collection routes
     */
    String targetIdentifier();

    /**
     * Returns the parent entity for relationship routes.
     *
     * @return parent entity definition, or null for non-relationship routes
     */
    EntityDefinition parentEntity();

    /**
     * Returns the parent instance identifier for relationship routes.
     *
     * @return parent identifier, or null for non-relationship routes
     */
    String parentIdentifier();

    /**
     * Returns the relationship route name.
     *
     * @return relationship name, or null for entity routes
     */
    String relationshipName();

    /**
     * Returns the child identifier for relationship instance routes.
     *
     * @return child identifier, or null when the route is not a relationship instance route
     */
    String childIdentifier();

    /**
     * Returns the request headers.
     *
     * @return mutable header block used by the request
     */
    HttpHeadersBlock headers();

    /**
     * Sets or replaces a request header for the rest of lifecycle processing.
     *
     * @param name header name
     * @param value header value
     */
    void setHeader(String name, String value);

    /**
     * Returns a copy of the current query parameters.
     *
     * @return query filter parameters
     */
    QueryFilterParams queryParams();

    /**
     * Replaces the query parameters used by later mapping or action phases.
     *
     * @param queryParams replacement query parameters
     */
    void replaceQueryParams(QueryFilterParams queryParams);

    /**
     * Returns the current raw request body.
     *
     * @return raw body text, never null
     */
    String rawBody();

    /**
     * Replaces the raw body used by later phases.
     *
     * @param rawBody replacement raw body text
     */
    void replaceRawBody(String rawBody);

    /**
     * Returns the currently parsed body fields.
     *
     * @return parsed body fields, or an empty field set before parsing
     */
    ApiBodyFields bodyFields();

    /**
     * Replaces the parsed body fields used by write command mapping.
     *
     * @param bodyFields replacement parsed body fields
     */
    void replaceBodyFields(ApiBodyFields bodyFields);

    /**
     * Returns the selected QUERY body format.
     *
     * @return query body format
     */
    ApiRequestEnvelope.QueryBodyFormat queryBodyFormat();

    /**
     * Replaces the QUERY body format used by query remapping.
     *
     * @param queryBodyFormat replacement query body format
     */
    void replaceQueryBodyFormat(ApiRequestEnvelope.QueryBodyFormat queryBodyFormat);

    /**
     * Returns the request context for the active store/session.
     *
     * @return request context
     */
    ThingifierRequestContext requestContext();

    /**
     * Returns the active data scope for this request.
     *
     * <p>When an authenticator selects a data scope, hooks that run after auth see the trusted
     * selected scope here.
     *
     * @return active data-scope name
     */
    String dataScopeName();

    /**
     * Returns the active Thingifier store for the request.
     *
     * @return active store
     */
    ThingStore store();

    /**
     * Returns the mapped write command when the request is a write operation.
     *
     * @return mapped write command, or null before mapping or for read operations
     */
    ThingWriteCommand writeCommand();

    /**
     * Replaces the mapped write command to be validated or executed.
     *
     * @param command replacement write command
     */
    void replaceWriteCommand(ThingWriteCommand command);

    /**
     * Returns the mapped read query when the request is a read operation.
     *
     * @return mapped read query, or null before mapping or for write operations
     */
    ThingReadQuery readQuery();

    /**
     * Replaces the mapped read query to be executed.
     *
     * @param query replacement read query
     */
    void replaceReadQuery(ThingReadQuery query);

    /**
     * Returns the Thingifier validation result.
     *
     * @return validation result, or null before validation
     */
    ThingCommandResult validationResult();

    /**
     * Replaces the validation result used by later phases.
     *
     * @param result replacement validation result
     */
    void replaceValidationResult(ThingCommandResult result);

    /** Clears the validation result so later processing can proceed. */
    void clearValidationResult();

    /**
     * Returns the write command result after action execution.
     *
     * @return write result, or null before action execution or for read operations
     */
    ThingCommandResult writeCommandResult();

    /**
     * Replaces the write command result seen by later response mapping.
     *
     * @param result replacement write result
     */
    void replaceWriteCommandResult(ThingCommandResult result);

    /**
     * Returns the read query result after action execution.
     *
     * @return query result, or null before action execution or for write operations
     */
    RepositoryQueryResult readQueryResult();

    /**
     * Replaces the read query result seen by later response mapping.
     *
     * @param result replacement query result
     */
    void replaceReadQueryResult(RepositoryQueryResult result);

    /**
     * Returns the current API response.
     *
     * @return API response, or null before one has been created
     */
    ApiResponse apiResponse();

    /**
     * Replaces the API response without stopping lifecycle processing.
     *
     * @param response replacement response
     */
    void replaceApiResponse(ApiResponse response);

    /**
     * Replaces the API response and stops remaining lifecycle processing for this request.
     *
     * @param response response to return immediately
     */
    void shortCircuitWith(ApiResponse response);

    /**
     * Reports whether lifecycle processing should stop and return the current response.
     *
     * @return true when a hook has short-circuited the request
     */
    boolean shouldShortCircuit();
}
