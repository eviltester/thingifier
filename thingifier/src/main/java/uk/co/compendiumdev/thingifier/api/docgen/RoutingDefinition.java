package uk.co.compendiumdev.thingifier.api.docgen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.ContentTypeHeaderParser;
import uk.co.compendiumdev.thingifier.api.response.ResponseHeader;
import uk.co.compendiumdev.thingifier.api.security.SecuritySchemeNames;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiSecuritySpec;
import uk.co.compendiumdev.thingifier.api.spec.FixedResourcePolicy;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;

/**
 * Describes one generated or additional API route for documentation and registration.
 *
 * <p>Route definitions are deliberately metadata-heavy. They capture the public route contract,
 * response headers, payload schema names, visibility, status behaviour, and security scheme names
 * that adapters such as the OpenAPI generator need after Thingifier has generated its routes.
 */
public class RoutingDefinition {
    private final RoutingVerb verb;
    private final String url;
    private RoutingStatus routingStatus;
    private ResponseHeader header;
    private String documentation = "";
    private boolean isFilterable;
    private EntityDefinition filterableEntityDefn;
    private List<RoutingStatus> possibleStatusResponses;
    private HashMap<Integer, String> returnPayload;
    private String requestPayload;
    private List<String> requestContentTypes;
    private List<RequestUrlParameter> requestUrlParams;
    private HashMap<String, String> customHeaders;
    private HashMap<String, String> responseHeaders;
    private boolean usesBasicAuth = false;
    private String basicAuthSchemeName = SecuritySchemeNames.DEFAULT_BASIC_AUTH_SCHEME;
    private boolean usesBearerAuth = false;
    private String bearerAuthSchemeName = SecuritySchemeNames.DEFAULT_BEARER_AUTH_SCHEME;
    private boolean usesApiKeyAuth = false;
    private String apiKeyAuthSchemeName = SecuritySchemeNames.DEFAULT_API_KEY_AUTH_SCHEME;
    private String apiKeyHeaderName = ThingifierApiSecuritySpec.DEFAULT_API_KEY_HEADER;
    private boolean apiKeyHeaderNameConfigured = false;
    private List<String> authSchemeNames;
    private boolean hiddenFromDocumentation = false;
    private boolean disabled = false;
    private String requestEntityViewName;
    private HashMap<Integer, String> responseEntityViewNames;
    private String fixedEntityName;
    private String fixedIdentifier;
    private FixedResourcePolicy fixedResourcePolicy;

    /**
     * Creates route metadata for one verb and path.
     *
     * @param verb generated route verb
     * @param url generated route path
     * @param routingStatus status behaviour for the route
     * @param header optional legacy response header definition
     */
    public RoutingDefinition(
            RoutingVerb verb, String url, RoutingStatus routingStatus, ResponseHeader header) {
        this.verb = verb;
        if (url.startsWith("/")) {
            this.url = url.substring(1);
        } else {
            this.url = url;
        }
        this.routingStatus = routingStatus;
        this.header = header;

        // defaults
        isFilterable = false;
        filterableEntityDefn = null;
        possibleStatusResponses = new ArrayList<>();
        requestUrlParams = new ArrayList<>();
        returnPayload = new HashMap<>();
        requestPayload = null;
        requestContentTypes = new ArrayList<>();
        customHeaders = new HashMap<>();
        responseHeaders = new HashMap<>();
        requestEntityViewName = null;
        responseEntityViewNames = new HashMap<>();
        authSchemeNames = new ArrayList<>();
        fixedEntityName = null;
        fixedIdentifier = null;
        fixedResourcePolicy = FixedResourcePolicy.RETURN_404;
    }

    /**
     * Returns the generated route verb.
     *
     * @return routing verb
     */
    public RoutingVerb verb() {
        return this.verb;
    }

    /**
     * Returns the route status behaviour.
     *
     * @return routing status metadata
     */
    public RoutingStatus status() {
        return routingStatus;
    }

    /**
     * Replaces the route status behaviour.
     *
     * @param status replacement status metadata
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition replaceStatus(final RoutingStatus status) {
        routingStatus = status;
        return this;
    }

    /**
     * Returns the normalized route path.
     *
     * @return route path without a leading slash
     */
    public String url() {
        return url;
    }

    /**
     * Formats colon-prefixed path parameters with caller-supplied delimiters.
     *
     * <p>OpenAPI generation uses this to convert Thingifier's {@code :id} path parameters into
     * {@code {id}} parameters while other renderers can choose different delimiters.
     *
     * @param prefix text to place before the parameter name
     * @param postfix text to place after the parameter name
     * @return formatted route path
     */
    public String urlWithParamFormatter(String prefix, String postfix) {
        // replace \/:([^\/\?]+)
        return url.replaceAll("\\/:([^\\/\\?]+)", "/" + prefix + "$1" + postfix);
    }

    /**
     * Returns the legacy response header name attached to the route.
     *
     * @return header name, or an empty string when none is configured
     */
    public String header() {
        if (header == null) {
            return "";
        }
        if (header.headerName == null) {
            return "";
        }

        return header.headerName;
    }

    /**
     * Returns the legacy response header value attached to the route.
     *
     * @return header value, or an empty string when none is configured
     */
    public String headerValue() {
        if (header == null) {
            return "";
        }
        if (header.headerValue == null) {
            return "";
        }

        return header.headerValue;
    }

    /**
     * Replaces the legacy response header metadata.
     *
     * @param header replacement header metadata
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition replaceHeader(final ResponseHeader header) {
        this.header = header;
        return this;
    }

    /**
     * Adds a named response header to the route metadata.
     *
     * @param headerName header name
     * @param value header value
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition addResponseHeader(final String headerName, final String value) {
        if (headerName != null) {
            responseHeaders.put(headerName, value == null ? "" : value);
        }
        return this;
    }

    /**
     * Reports whether the route defines response headers.
     *
     * @return true when response headers are present
     */
    public boolean hasResponseHeaders() {
        return !responseHeaders.isEmpty();
    }

    /**
     * Returns the response header names configured for the route.
     *
     * @return response header names
     */
    public Collection<String> getResponseHeaderNames() {
        return responseHeaders.keySet();
    }

    /**
     * Returns one configured response header value.
     *
     * @param headerName header name
     * @return configured value, or null when absent
     */
    public String getResponseHeaderValue(final String headerName) {
        return responseHeaders.get(headerName);
    }

    /**
     * Returns the route documentation text.
     *
     * @return documentation text, possibly empty
     */
    public String getDocumentation() {
        return this.documentation;
    }

    /**
     * Replaces the route documentation text.
     *
     * @param documentation documentation text
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition addDocumentation(String documentation) {
        this.documentation = documentation;
        return this;
    }

    /**
     * Reports whether the route supports query filtering.
     *
     * @return true when filter metadata is present
     */
    public boolean isFilterable() {
        return isFilterable;
    }

    /**
     * Marks the route as filterable using fields from an entity definition.
     *
     * @param definition entity whose fields can be used as filters
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition setAsFilterableFrom(final EntityDefinition definition) {
        isFilterable = true;
        filterableEntityDefn = definition;
        return this;
    }

    /**
     * Returns the entity whose fields can be used as filters.
     *
     * @return filterable entity definition, or null when the route is not filterable
     */
    public EntityDefinition getFilterableEntity() {
        return filterableEntityDefn;
    }

    /**
     * Adds a possible status response to the route metadata.
     *
     * @param status status metadata to add
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition addPossibleStatus(final RoutingStatus status) {
        possibleStatusResponses.add(status);
        return this;
    }

    /**
     * Clears all possible status response metadata.
     *
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition clearPossibleStatuses() {
        possibleStatusResponses.clear();
        return this;
    }

    /**
     * Returns possible status responses for documentation.
     *
     * @return mutable list of possible status metadata
     */
    public List<RoutingStatus> getPossibleStatusReponses() {
        return possibleStatusResponses;
    }

    // quick hack method to allow creating a bunch of default rendered possible status codes
    /**
     * Adds several simple possible status responses.
     *
     * @param statusCodes status codes to add
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition addPossibleStatuses(final Integer... statusCodes) {
        for (Integer statusCode : statusCodes) {
            addPossibleStatus(RoutingStatus.returnValue(statusCode));
        }
        return this;
    }

    /**
     * Associates a response status with a payload schema name.
     *
     * @param statusCode response status code
     * @param objectSchemaName schema name returned for that status
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition returnPayload(final Integer statusCode, String objectSchemaName) {
        returnPayload.put(statusCode, objectSchemaName);
        return this;
    }

    /**
     * Clears all response payload schema metadata.
     *
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition clearReturnPayloads() {
        returnPayload.clear();
        return this;
    }

    /**
     * Reports whether a status has response payload metadata.
     *
     * @param statusCode response status code
     * @return true when a schema name is configured
     */
    public boolean hasReturnPayloadFor(final Integer statusCode) {
        return returnPayload.containsKey(statusCode);
    }

    /**
     * Returns the response payload schema for a status.
     *
     * @param statusCode response status code
     * @return schema name, or null when absent
     */
    public String getReturnPayloadFor(final Integer statusCode) {
        return returnPayload.get(statusCode);
    }

    /**
     * Sets the request payload schema name.
     *
     * @param payloadName request payload schema name
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition requestPayload(String payloadName) {
        requestPayload = payloadName;
        return this;
    }

    /**
     * Replaces accepted request content types for routes with a request payload.
     *
     * @param contentTypes accepted content types
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition requestContentTypes(final String... contentTypes) {
        requestContentTypes.clear();
        if (contentTypes != null) {
            for (String contentType : contentTypes) {
                if (contentType != null && !contentType.trim().isEmpty()) {
                    requestContentTypes.add(contentType.trim());
                }
            }
        }
        return this;
    }

    /**
     * Reports whether the route defines a request payload schema.
     *
     * @return true when request payload metadata is present
     */
    public Boolean hasRequestPayload() {
        return requestPayload != null;
    }

    /**
     * Returns the request payload schema name.
     *
     * @return request payload schema name, or null when absent
     */
    public String getRequestPayload() {
        return requestPayload;
    }

    /**
     * Returns accepted request content types.
     *
     * <p>When none are explicitly configured, Thingifier advertises its normal JSON and XML request
     * content types.
     *
     * @return accepted content types
     */
    public List<String> getRequestContentTypes() {
        if (requestContentTypes.isEmpty()) {
            return List.of(
                    "application/json",
                    ContentTypeHeaderParser.APPLICATION_XML,
                    ContentTypeHeaderParser.TEXT_XML);
        }
        return new ArrayList<>(requestContentTypes);
    }

    /**
     * Sets the request entity view and matching create-payload schema name.
     *
     * @param viewName entity view used to validate request fields
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition requestEntityView(final String viewName) {
        requestEntityViewName = viewName;
        requestPayload("create_" + viewName);
        return this;
    }

    /**
     * Reports whether a request entity view is configured.
     *
     * @return true when a request entity view is present
     */
    public boolean hasRequestEntityView() {
        return requestEntityViewName != null;
    }

    /**
     * Returns the request entity view name.
     *
     * @return request entity view name, or null when absent
     */
    public String getRequestEntityView() {
        return requestEntityViewName;
    }

    /**
     * Sets the response entity view and matching payload schema for a status code.
     *
     * @param statusCode response status code
     * @param viewName entity view used to render the response
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition responseEntityView(final int statusCode, final String viewName) {
        responseEntityViewNames.put(statusCode, viewName);
        returnPayload(statusCode, viewName);
        return this;
    }

    /**
     * Reports whether a response entity view is configured for a status.
     *
     * @param statusCode response status code
     * @return true when a response view is configured
     */
    public boolean hasResponseEntityViewFor(final int statusCode) {
        return responseEntityViewNames.containsKey(statusCode);
    }

    /**
     * Returns the response entity view for a status.
     *
     * @param statusCode response status code
     * @return response entity view name, or null when absent
     */
    public String getResponseEntityViewFor(final int statusCode) {
        return responseEntityViewNames.get(statusCode);
    }

    /**
     * Marks this public route as mapping to one fixed entity instance.
     *
     * <p>Documentation generators keep the public URL exactly as declared, while runtime route
     * resolution uses this metadata to process the request as an internal instance route.
     *
     * @param entityName singular or plural target entity name
     * @param identifier fixed entity instance identifier
     * @param policy behaviour when the target instance is missing
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition mapToFixedEntity(
            final String entityName, final String identifier, final FixedResourcePolicy policy) {
        fixedEntityName = entityName;
        fixedIdentifier = identifier;
        fixedResourcePolicy = policy == null ? FixedResourcePolicy.RETURN_404 : policy;
        return this;
    }

    /**
     * Reports whether this route maps to a fixed entity instance.
     *
     * @return true when fixed route metadata is present
     */
    public boolean hasFixedIdentifierMapping() {
        return fixedEntityName != null && fixedIdentifier != null;
    }

    /**
     * Returns the fixed route's target entity name.
     *
     * @return singular or plural entity name, or null when not fixed
     */
    public String fixedEntityName() {
        return fixedEntityName;
    }

    /**
     * Returns the fixed route's target entity identifier.
     *
     * @return fixed identifier, or null when not fixed
     */
    public String fixedIdentifier() {
        return fixedIdentifier;
    }

    /**
     * Returns the fixed route's missing-instance policy.
     *
     * @return configured fixed resource policy
     */
    public FixedResourcePolicy fixedResourcePolicy() {
        return fixedResourcePolicy;
    }

    /**
     * Returns all status codes with response payload schemas.
     *
     * @return response payload status codes
     */
    public Collection<Integer> returnPayloadStatusCodes() {
        return new ArrayList<>(returnPayload.keySet());
    }

    /**
     * Adds a request path parameter using the field name.
     *
     * @param aField field metadata for the path parameter
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition addRequestUrlParam(Field aField) {
        if (aField == null) {
            return this;
        }
        requestUrlParams.add(new RequestUrlParameter(aField.getName(), aField));
        return this;
    }

    /**
     * Adds a request path parameter with an explicit parameter name.
     *
     * @param parameterName path parameter name
     * @param field field metadata for the path parameter
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition addRequestUrlParam(final String parameterName, final Field field) {
        if (parameterName == null || field == null) {
            return this;
        }
        requestUrlParams.add(new RequestUrlParameter(parameterName, field));
        return this;
    }

    /**
     * Reports whether the route defines request path parameters.
     *
     * @return true when path parameter metadata is present
     */
    public Boolean hasRequestUrlParams() {
        return !requestUrlParams.isEmpty();
    }

    /**
     * Returns field metadata for request path parameters.
     *
     * @return list of path parameter fields
     */
    public List<Field> getRequestUrlParams() {
        List<Field> fields = new ArrayList<>();
        for (RequestUrlParameter parameter : requestUrlParams) {
            fields.add(parameter.field());
        }
        return fields;
    }

    /**
     * Returns full request path parameter metadata.
     *
     * @return path parameter metadata
     */
    public List<RequestUrlParameter> getRequestUrlParameters() {
        return new ArrayList<>(requestUrlParams);
    }

    /**
     * Adds custom request header documentation.
     *
     * @param headerName header name
     * @param headerType header schema type
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition addCustomHeader(String headerName, String headerType) {
        customHeaders.put(headerName, headerType);
        return this;
    }

    /**
     * Reports whether the route defines custom request headers.
     *
     * @return true when custom headers are present
     */
    public boolean hasCustomHeaders() {
        return !customHeaders.keySet().isEmpty();
    }

    /**
     * Returns custom request header names.
     *
     * @return custom header names
     */
    public Collection<String> getCustomHeaderNames() {
        return customHeaders.keySet();
    }

    /**
     * Returns the schema type for a custom request header.
     *
     * @param headerName header name
     * @return header type, or null when absent
     */
    public String getCustomHeaderType(String headerName) {
        return customHeaders.get(headerName);
    }

    /**
     * Reports whether a custom request header is already defined.
     *
     * @param headerName header name
     * @return true when the header is present
     */
    public boolean hasCustomHeaderNamed(String headerName) {
        return customHeaders.containsKey(headerName);
    }

    /**
     * Marks the route as requiring Basic authentication in generated documentation.
     *
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition secureWithBasicAuth() {
        return secureWithBasicAuth(SecuritySchemeNames.DEFAULT_BASIC_AUTH_SCHEME);
    }

    /**
     * Marks the route as requiring a named Basic auth scheme in generated documentation.
     *
     * @param schemeName Basic security scheme name
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition secureWithBasicAuth(final String schemeName) {
        usesBasicAuth = true;
        basicAuthSchemeName = SecuritySchemeNames.requireValid(schemeName);
        replaceAuthSchemeNamesWith(basicAuthSchemeName);
        return this;
    }

    /**
     * Reports whether the route is documented as Basic-auth secured.
     *
     * @return true when Basic auth should be documented
     */
    public boolean isSecuredByBasicAuth() {
        return usesBasicAuth;
    }

    /**
     * Returns the Basic auth security scheme name used in generated documentation.
     *
     * @return Basic security scheme name
     */
    public String basicAuthSchemeName() {
        return basicAuthSchemeName;
    }

    /**
     * Marks the route as requiring the legacy bearer auth scheme in generated documentation.
     *
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition secureWithBearerAuth() {
        return secureWithBearerAuth(SecuritySchemeNames.DEFAULT_BEARER_AUTH_SCHEME);
    }

    /**
     * Marks the route as requiring a named bearer auth scheme in generated documentation.
     *
     * @param schemeName bearer security scheme name
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition secureWithBearerAuth(final String schemeName) {
        usesBearerAuth = true;
        bearerAuthSchemeName = SecuritySchemeNames.requireValid(schemeName);
        replaceAuthSchemeNamesWith(bearerAuthSchemeName);
        return this;
    }

    /**
     * Reports whether the route is documented as bearer-auth secured.
     *
     * @return true when bearer auth should be documented
     */
    public boolean isSecuredByBearerAuth() {
        return usesBearerAuth;
    }

    /**
     * Returns the bearer auth security scheme name used in generated documentation.
     *
     * @return bearer security scheme name
     */
    public String bearerAuthSchemeName() {
        return bearerAuthSchemeName;
    }

    /**
     * Marks the route as requiring the default API key auth scheme in generated documentation.
     *
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition secureWithApiKey() {
        return secureWithApiKey(SecuritySchemeNames.DEFAULT_API_KEY_AUTH_SCHEME);
    }

    /**
     * Marks the route as requiring a named API key auth scheme in generated documentation.
     *
     * <p>The concrete header name is normally supplied by {@link ThingifierApiSecuritySpec}. This
     * overload keeps generated route metadata focused on the scheme selected by the route.
     *
     * @param schemeName API key security scheme name
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition secureWithApiKey(final String schemeName) {
        usesApiKeyAuth = true;
        apiKeyAuthSchemeName = SecuritySchemeNames.requireValid(schemeName);
        replaceAuthSchemeNamesWith(apiKeyAuthSchemeName);
        return this;
    }

    /**
     * Marks the route as requiring a named API key auth scheme with an explicit header.
     *
     * <p>This is useful for manually added documentation routes that do not have a Thingifier API
     * security declaration to resolve the header name from.
     *
     * @param schemeName API key security scheme name
     * @param headerName request header carrying the API key credential
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition secureWithApiKey(final String schemeName, final String headerName) {
        secureWithApiKey(schemeName);
        apiKeyHeaderName = SecuritySchemeNames.requireValidHeaderName(headerName);
        apiKeyHeaderNameConfigured = true;
        return this;
    }

    /**
     * Marks the route as accepting any of several named auth schemes in declaration order.
     *
     * <p>This metadata is used by OpenAPI generation to render OR-style security requirements.
     * Generated routes normally resolve each scheme type from the owning Thingifier API security
     * spec. Manually documented routes can still call the scheme-specific helpers to provide
     * fallback type/header metadata.
     *
     * @param schemeNames ordered security scheme names accepted by the route
     * @return this definition so route metadata can be chained
     * @throws IllegalArgumentException when no scheme names are supplied or a name is blank
     */
    public RoutingDefinition secureWithAnyOf(final String... schemeNames) {
        authSchemeNames = normalizedSchemeNames(schemeNames);
        return this;
    }

    /**
     * Reports whether the route has explicit ordered auth scheme metadata.
     *
     * @return true when one or more auth scheme names are configured
     */
    public boolean hasAuthSchemeNames() {
        return !authSchemeNames.isEmpty();
    }

    /**
     * Returns ordered auth scheme names for this route.
     *
     * @return immutable ordered scheme names
     */
    public List<String> authSchemeNames() {
        return List.copyOf(authSchemeNames);
    }

    /**
     * Reports whether the route is documented as API-key secured.
     *
     * @return true when API key auth should be documented
     */
    public boolean isSecuredByApiKeyAuth() {
        return usesApiKeyAuth;
    }

    /**
     * Returns the API key security scheme name used in generated documentation.
     *
     * @return API key security scheme name
     */
    public String apiKeyAuthSchemeName() {
        return apiKeyAuthSchemeName;
    }

    /**
     * Reports whether this route carries an explicit API key header name.
     *
     * @return true when the header was configured directly on this route definition
     */
    public boolean hasApiKeyHeaderName() {
        return apiKeyHeaderNameConfigured;
    }

    /**
     * Returns the route's API key header name.
     *
     * @return configured API key header, or the default API key header
     */
    public String apiKeyHeaderName() {
        return apiKeyHeaderName;
    }

    /**
     * Hides the route from generated documentation without disabling runtime registration.
     *
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition hideFromDocumentation() {
        hiddenFromDocumentation = true;
        return this;
    }

    /**
     * Reports whether the route is hidden from generated documentation.
     *
     * @return true when hidden from docs
     */
    public boolean isHiddenFromDocumentation() {
        return hiddenFromDocumentation;
    }

    /**
     * Disables the generated route and hides it from documentation.
     *
     * @return this definition so route metadata can be chained
     */
    public RoutingDefinition disable() {
        disabled = true;
        hiddenFromDocumentation = true;
        return this;
    }

    /**
     * Reports whether the generated route is disabled.
     *
     * @return true when disabled
     */
    public boolean isDisabled() {
        return disabled;
    }

    private void replaceAuthSchemeNamesWith(final String schemeName) {
        authSchemeNames.clear();
        authSchemeNames.add(schemeName);
    }

    private List<String> normalizedSchemeNames(final String... schemeNames) {
        if (schemeNames == null || schemeNames.length == 0) {
            throw new IllegalArgumentException("secureWithAnyOf requires at least one scheme");
        }
        final List<String> normalizedSchemeNames = new ArrayList<>();
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

    public static final class RequestUrlParameter {

        private final String name;
        private final Field field;

        /**
         * Creates request path parameter metadata.
         *
         * @param name path parameter name
         * @param field field metadata for documentation
         */
        public RequestUrlParameter(final String name, final Field field) {
            this.name = name;
            this.field = field;
        }

        /**
         * Returns the path parameter name.
         *
         * @return path parameter name
         */
        public String name() {
            return name;
        }

        /**
         * Returns the field metadata for the path parameter.
         *
         * @return field metadata
         */
        public Field field() {
            return field;
        }
    }
}
