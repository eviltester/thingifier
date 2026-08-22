package uk.co.compendiumdev.thingifier.api.response;

import java.util.*;
import uk.co.compendiumdev.thingifier.api.ApiUrls;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityViewDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.RelationshipRepository;

/**
 * Represents a Thingifier API result before it is rendered as HTTP, JSON, XML, or another format.
 *
 * <p>The response keeps structured domain data instead of eagerly storing a serialized body. This
 * lets adapters apply entity views, relationship rendering, content negotiation, and header
 * behavior consistently across direct API calls and HTTP requests.
 */
public final class ApiResponse {

    // TODO: instance GUID or instance-id should actually be the primary key
    public static final String PRIMARY_KEY_HEADER = "X-Thing-Instance-Primary-Key";

    private int statusCode;
    private boolean hasBody;
    // instead of storing a json as the body, store the things to return
    // let getBody do the conversion to json or xml
    private List<EntityInstance> thingsToReturn;
    private EntityInstanceDraft draftToReturn;
    // isCollection true, return as collection, false, return as instance
    private boolean isCollection;
    // isErrorResponse true, return the stored collection of error messages
    private boolean isErrorResponse;
    private boolean isValidationErrorResponse;
    private Collection<String> errorMessages;

    private HttpHeadersBlock headers;
    private EntityDefinition typeOfResults;
    private EntityViewDefinition responseView;
    private EntityResponseViewResolver responseViewResolver;
    private String body;
    private RelationshipRepository relationshipRepository;

    /**
     * Creates an empty API response for a status code.
     *
     * @param aStatusCode HTTP-style status code to expose through adapters
     */
    public ApiResponse(final int aStatusCode) {
        this.statusCode = aStatusCode;
        headers = new HttpHeadersBlock();
        thingsToReturn = new ArrayList();
        draftToReturn = null;
        isCollection = false;
        isErrorResponse = false;
        isValidationErrorResponse = false;
        errorMessages = new ArrayList<>();
        hasBody = false;
        body = null;
        responseView = null;
        responseViewResolver = null;
    }

    /**
     * Creates an API response that may contain error messages.
     *
     * @param aStatusCode HTTP-style status code to expose through adapters
     * @param isError true when the body should be rendered as error messages
     * @param theErrorMessages error messages to include in the response body
     */
    public ApiResponse(
            final int aStatusCode,
            final boolean isError,
            final Collection<String> theErrorMessages) {
        this(aStatusCode);
        isErrorResponse = isError;
        if (isError) {
            isCollection = false;
        }
        this.hasBody = true;
        this.errorMessages.addAll(theErrorMessages);
    }

    /**
     * Returns the HTTP-style status code for this API result.
     *
     * @return response status code
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Replaces the HTTP-style status code while preserving the structured response body.
     *
     * <p>Route-level response policies use this when the same generated Thingifier operation should
     * keep its returned entity/error payload but expose a route-specific status code.
     *
     * @param newStatusCode status code to expose through adapters
     * @return this response so additional response policy actions can be chained
     */
    public ApiResponse withStatusCode(final int newStatusCode) {
        this.statusCode = newStatusCode;
        return this;
    }

    /**
     * Creates a normal 200 OK response with no body.
     *
     * @return empty success response
     */
    public static ApiResponse success() {
        return new ApiResponse(200);
    }

    /**
     * Creates a 204 No Content response.
     *
     * @return empty no-content response
     */
    public static ApiResponse noContent() {
        return new ApiResponse(204);
    }

    /**
     * Sets the response body to one persisted entity instance.
     *
     * @param instance instance to render
     * @return this response so additional metadata can be chained
     */
    public ApiResponse returnSingleInstance(final EntityInstance instance) {
        this.isCollection = false;
        draftToReturn = null;
        thingsToReturn.clear();
        thingsToReturn.add(instance);
        typeOfResults = instance.getEntity();
        this.hasBody = true;
        return this;
    }

    /**
     * Sets the response body to a collection of persisted entity instances.
     *
     * @param items instances to render as a collection
     * @return this response so additional metadata can be chained
     */
    public ApiResponse returnInstanceCollection(final List<EntityInstance> items) {
        thingsToReturn.clear();
        draftToReturn = null;
        thingsToReturn.addAll(items);
        isCollection = true;
        if (items.size() > 0) {
            typeOfResults = items.get(0).getEntity();
        }
        this.hasBody = true;
        return this;
    }

    /**
     * Sets the response body to one draft entity.
     *
     * <p>Drafts are used when validation or parsing needs to return the would-be entity shape
     * before an instance has been persisted.
     *
     * @param draft draft instance to render
     * @return this response so additional metadata can be chained
     */
    public ApiResponse returnSingleDraft(final EntityInstanceDraft draft) {
        this.isCollection = false;
        thingsToReturn.clear();
        draftToReturn = draft;
        typeOfResults = draft.getEntity();
        this.hasBody = true;
        return this;
    }

    /*
           HEADERS
    */

    /**
     * Sets or replaces a response header.
     *
     * @param headername header name
     * @param value header value
     * @return this response so additional metadata can be chained
     */
    public ApiResponse setHeader(final String headername, final String value) {
        this.headers.put(headername, value);
        return this;
    }

    /**
     * Returns a response header value.
     *
     * @param headername header name
     * @return header value, or null when the header is not present
     */
    public String getHeaderValue(final String headername) {
        return headers.get(headername);
    }

    /**
     * Sets the Location header for create-style responses.
     *
     * @param location resource location to advertise
     * @return this response so additional metadata can be chained
     */
    public ApiResponse setLocationHeader(final String location) {
        return setHeader("Location", location);
    }

    /**
     * Returns the mutable header block associated with this response.
     *
     * @return response headers
     */
    public HttpHeadersBlock getHeaders() {
        return headers;
    }

    /*
           SPECIAL CASE RESPONSES
    */

    /**
     * Creates the standard 201 Created response for a persisted entity instance.
     *
     * <p>The response includes the created instance, Location header, and optional primary-key
     * header according to the API configuration.
     *
     * @param thingInstance created entity instance, possibly null
     * @param apiConfig API configuration controlling response headers and URLs
     * @return created response
     */
    public static ApiResponse created(
            final EntityInstance thingInstance, ThingifierApiConfig apiConfig) {
        ApiResponse response = new ApiResponse(201);

        if (thingInstance != null) {
            response.returnSingleInstance(thingInstance);

            response.setLocationHeader(
                    new ApiUrls(apiConfig).getCreatedLocationHeader(thingInstance));

            if (apiConfig.willResponsesShowPrimaryKeyHeader()) {
                response.setHeader(
                        ApiResponse.PRIMARY_KEY_HEADER, thingInstance.getPrimaryKeyValue());
            }
            response.hasBody = true;
        }

        return response;
    }

    /*
           ERROR MESSAGES
    */

    /**
     * Creates a 404 error response with one message.
     *
     * @param errorMessage message to render in the error body
     * @return error response
     */
    public static ApiResponse error404(final String errorMessage) {
        return error(404, errorMessage);
    }

    /**
     * Creates an error response with one message.
     *
     * @param statusCode HTTP-style error status code
     * @param errorMessage message to render in the error body
     * @return error response
     */
    public static ApiResponse error(final int statusCode, final String errorMessage) {
        Collection<String> localErrorMessages = new ArrayList<>();
        localErrorMessages.add(errorMessage);
        return error(statusCode, localErrorMessages);
    }

    /**
     * Creates an error response with multiple messages.
     *
     * @param statusCode HTTP-style error status code
     * @param errorMessages messages to render in the error body
     * @return error response
     */
    public static ApiResponse error(final int statusCode, final Collection<String> errorMessages) {
        return new ApiResponse(statusCode, true, errorMessages);
    }

    /**
     * Creates a validation-style error response with one message.
     *
     * <p>The validation marker lets route response policies distinguish model/API validation
     * failures from other errors that happen to share a status code.
     *
     * @param statusCode HTTP-style error status code
     * @param errorMessage validation message to render in the error body
     * @return validation error response
     */
    public static ApiResponse validationError(final int statusCode, final String errorMessage) {
        Collection<String> localErrorMessages = new ArrayList<>();
        localErrorMessages.add(errorMessage);
        return validationError(statusCode, localErrorMessages);
    }

    /**
     * Creates a validation-style error response with multiple messages.
     *
     * @param statusCode HTTP-style error status code
     * @param errorMessages validation messages to render in the error body
     * @return validation error response
     */
    public static ApiResponse validationError(
            final int statusCode, final Collection<String> errorMessages) {
        ApiResponse response = error(statusCode, errorMessages);
        response.isValidationErrorResponse = true;
        return response;
    }

    /**
     * Reports whether this response body should be rendered as errors.
     *
     * @return true when this is an error response
     */
    public boolean isErrorResponse() {
        return isErrorResponse;
    }

    /**
     * Reports whether this error represents validation of the request or candidate operation.
     *
     * <p>This deliberately narrows route-level {@code onValidationError()} policies to responses
     * produced by Thingifier validation rather than every response using status 422.
     *
     * @return true when this response was marked as a validation error
     */
    public boolean isValidationErrorResponse() {
        return isValidationErrorResponse;
    }

    /**
     * Returns the response error messages.
     *
     * @return error messages, empty when no errors were recorded
     */
    public Collection<String> getErrorMessages() {
        return errorMessages;
    }

    /**
     * Returns the single persisted instance in the response body.
     *
     * @return returned persisted entity instance
     * @throws IllegalStateException when the response contains a collection or draft
     */
    public EntityInstance getReturnedInstance() {
        if (isCollection) {
            throw new IllegalStateException("response contains a collection, not an instance");
        }
        if (draftToReturn != null) {
            throw new IllegalStateException(
                    "response contains a draft instance, not a persisted instance");
        }

        return thingsToReturn.get(0);
    }

    /**
     * Reports whether the response contains exactly one persisted instance.
     *
     * @return true when {@link #getReturnedInstance()} can be called safely
     */
    public boolean hasReturnedInstance() {
        return !isCollection && draftToReturn == null && thingsToReturn.size() == 1;
    }

    /**
     * Reports whether the response contains a draft instance.
     *
     * @return true when the response body is a draft
     */
    public boolean hasReturnedDraft() {
        return draftToReturn != null;
    }

    /**
     * Returns the draft instance in the response body.
     *
     * @return returned draft entity
     * @throws IllegalStateException when the response does not contain a draft
     */
    public EntityInstanceDraft getReturnedDraft() {
        if (isCollection || draftToReturn == null) {
            throw new IllegalStateException("response does not contain a draft instance");
        }
        return draftToReturn;
    }

    /**
     * Returns the persisted instance collection in the response body.
     *
     * @return returned entity instances
     * @throws IllegalStateException when the response contains a single instance
     */
    public List<EntityInstance> getReturnedInstanceCollection() {
        if (!isCollection) {
            throw new IllegalStateException("response contains an instance, not a collection");
        }
        return thingsToReturn;
    }

    /**
     * Reports whether the response body is a collection.
     *
     * @return true when the body is a collection of entity instances
     */
    public boolean isCollection() {
        return isCollection;
    }

    /**
     * Sets the entity definition for results when it cannot be inferred from instances.
     *
     * <p>This matters for empty collections because serializers still need to know the entity type
     * to choose collection names and response views.
     *
     * @param thingDefinition entity definition represented by the response
     * @return this response so additional metadata can be chained
     */
    public ApiResponse resultContainsType(final EntityDefinition thingDefinition) {
        if (thingDefinition != null) {
            this.typeOfResults = thingDefinition;
        }
        return this;
    }

    /**
     * Supplies the relationship repository needed when serializers include relationship fields.
     *
     * @param relationships relationship repository associated with the request context
     * @return this response so additional metadata can be chained
     */
    public ApiResponse usingRelationships(final RelationshipRepository relationships) {
        this.relationshipRepository = relationships;
        return this;
    }

    /**
     * Returns the relationship repository available to serializers.
     *
     * @return relationship repository, or null when relationship expansion is unavailable
     */
    public RelationshipRepository getRelationshipRepository() {
        return relationshipRepository;
    }

    /**
     * Returns the entity type represented by this response.
     *
     * @return entity definition for the response body, or null when the response has no entity body
     */
    public EntityDefinition getTypeOfThingReturned() {
        return typeOfResults;
    }

    /**
     * Applies one explicit entity view to this response.
     *
     * <p>This is used for route-level response views where every rendered entity should use the
     * same view. Setting it clears any entity-dependent resolver.
     *
     * @param view view to apply during response serialization
     * @return this response so additional metadata can be chained
     */
    public ApiResponse usingEntityView(final EntityViewDefinition view) {
        responseView = view;
        responseViewResolver = null;
        return this;
    }

    /**
     * Applies an entity-dependent response view resolver.
     *
     * <p>Use this when the response may render entities whose default views differ, for example a
     * relationship collection returning related entities rather than the route's parent entity.
     * Setting a resolver clears any explicit single response view.
     *
     * @param viewResolver resolver consulted during serialization
     * @return this response so additional metadata can be chained
     */
    public ApiResponse usingEntityResponseViewResolver(
            final EntityResponseViewResolver viewResolver) {
        responseViewResolver = viewResolver;
        responseView = null;
        return this;
    }

    /**
     * Reports whether response serialization should apply a view.
     *
     * @return true when either an explicit view or a resolver is present
     */
    public boolean hasResponseView() {
        return responseView != null || responseViewResolver != null;
    }

    /**
     * Returns the explicit single response view.
     *
     * <p>This method remains for backwards compatibility. Prefer {@link #responseViewFor} when
     * rendering because a resolver may be in use.
     *
     * @return explicit response view, or null when none is configured
     */
    public EntityViewDefinition getResponseView() {
        return responseView;
    }

    /**
     * Resolves the response view for a specific entity being serialized.
     *
     * @param entity entity currently being rendered
     * @return matching response view, or null when no response view applies
     */
    public EntityViewDefinition responseViewFor(final EntityDefinition entity) {
        if (responseViewResolver != null) {
            return responseViewResolver.viewFor(entity);
        }
        return responseView;
    }

    /**
     * Reports whether the response has a body to serialize.
     *
     * @return true when a structured body or body override is present
     */
    public boolean hasABody() {
        return this.hasBody;
    }

    /**
     * Appends an additional error message to the response.
     *
     * @param message error message to append
     * @return this response so additional metadata can be chained
     */
    public ApiResponse addToErrorMessages(final String message) {
        errorMessages.add(message);
        return this;
    }

    /**
     * Removes any response body while leaving status and headers intact.
     *
     * <p>HEAD handling uses this so the response can advertise the body length without returning
     * the body content.
     */
    public void clearBody() {
        this.hasBody = false;
        this.body = null;
    }

    /**
     * Replaces structured response serialization with an explicit body string.
     *
     * <p>Hooks and special handlers use this when they need full control over the response body.
     *
     * @param bodyDetails body text to return through adapters
     */
    public void setBody(final String bodyDetails) {
        this.hasBody = true;
        this.body = bodyDetails;
    }

    /**
     * Reports whether this response has an explicit body override.
     *
     * @return true when {@link #setBody(String)} has supplied body text
     */
    public boolean hasABodyOverride() {
        return this.body != null;
    }

    /**
     * Returns the explicit body override.
     *
     * @return body override text, or null when structured serialization should be used
     */
    public String getBody() {
        return this.body;
    }
}
