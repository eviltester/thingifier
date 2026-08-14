package uk.co.compendiumdev.thingifier.api.docgen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.ContentTypeHeaderParser;
import uk.co.compendiumdev.thingifier.api.response.ResponseHeader;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;

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
    private boolean usesBearerAuth = false;
    private boolean hiddenFromDocumentation = false;
    private boolean disabled = false;
    private String requestEntityViewName;
    private HashMap<Integer, String> responseEntityViewNames;

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
    }

    public RoutingVerb verb() {
        return this.verb;
    }

    public RoutingStatus status() {
        return routingStatus;
    }

    public RoutingDefinition replaceStatus(final RoutingStatus status) {
        routingStatus = status;
        return this;
    }

    public String url() {
        return url;
    }

    public String urlWithParamFormatter(String prefix, String postfix) {
        // replace \/:([^\/\?]+)
        return url.replaceAll("\\/:([^\\/\\?]+)", "/" + prefix + "$1" + postfix);
    }

    public String header() {
        if (header == null) {
            return "";
        }
        if (header.headerName == null) {
            return "";
        }

        return header.headerName;
    }

    public String headerValue() {
        if (header == null) {
            return "";
        }
        if (header.headerValue == null) {
            return "";
        }

        return header.headerValue;
    }

    public RoutingDefinition replaceHeader(final ResponseHeader header) {
        this.header = header;
        return this;
    }

    public RoutingDefinition addResponseHeader(final String headerName, final String value) {
        if (headerName != null) {
            responseHeaders.put(headerName, value == null ? "" : value);
        }
        return this;
    }

    public boolean hasResponseHeaders() {
        return !responseHeaders.isEmpty();
    }

    public Collection<String> getResponseHeaderNames() {
        return responseHeaders.keySet();
    }

    public String getResponseHeaderValue(final String headerName) {
        return responseHeaders.get(headerName);
    }

    public String getDocumentation() {
        return this.documentation;
    }

    public RoutingDefinition addDocumentation(String documentation) {
        this.documentation = documentation;
        return this;
    }

    public boolean isFilterable() {
        return isFilterable;
    }

    public RoutingDefinition setAsFilterableFrom(final EntityDefinition definition) {
        isFilterable = true;
        filterableEntityDefn = definition;
        return this;
    }

    public EntityDefinition getFilterableEntity() {
        return filterableEntityDefn;
    }

    public RoutingDefinition addPossibleStatus(final RoutingStatus status) {
        possibleStatusResponses.add(status);
        return this;
    }

    public RoutingDefinition clearPossibleStatuses() {
        possibleStatusResponses.clear();
        return this;
    }

    public List<RoutingStatus> getPossibleStatusReponses() {
        return possibleStatusResponses;
    }

    // quick hack method to allow creating a bunch of default rendered possible status codes
    public RoutingDefinition addPossibleStatuses(final Integer... statusCodes) {
        for (Integer statusCode : statusCodes) {
            addPossibleStatus(RoutingStatus.returnValue(statusCode));
        }
        return this;
    }

    public RoutingDefinition returnPayload(final Integer statusCode, String objectSchemaName) {
        returnPayload.put(statusCode, objectSchemaName);
        return this;
    }

    public RoutingDefinition clearReturnPayloads() {
        returnPayload.clear();
        return this;
    }

    public boolean hasReturnPayloadFor(final Integer statusCode) {
        return returnPayload.containsKey(statusCode);
    }

    public String getReturnPayloadFor(final Integer statusCode) {
        return returnPayload.get(statusCode);
    }

    public RoutingDefinition requestPayload(String payloadName) {
        requestPayload = payloadName;
        return this;
    }

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

    public Boolean hasRequestPayload() {
        return requestPayload != null;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public List<String> getRequestContentTypes() {
        if (requestContentTypes.isEmpty()) {
            return List.of(
                    "application/json",
                    ContentTypeHeaderParser.APPLICATION_XML,
                    ContentTypeHeaderParser.TEXT_XML);
        }
        return new ArrayList<>(requestContentTypes);
    }

    public RoutingDefinition requestEntityView(final String viewName) {
        requestEntityViewName = viewName;
        requestPayload("create_" + viewName);
        return this;
    }

    public boolean hasRequestEntityView() {
        return requestEntityViewName != null;
    }

    public String getRequestEntityView() {
        return requestEntityViewName;
    }

    public RoutingDefinition responseEntityView(final int statusCode, final String viewName) {
        responseEntityViewNames.put(statusCode, viewName);
        returnPayload(statusCode, viewName);
        return this;
    }

    public boolean hasResponseEntityViewFor(final int statusCode) {
        return responseEntityViewNames.containsKey(statusCode);
    }

    public String getResponseEntityViewFor(final int statusCode) {
        return responseEntityViewNames.get(statusCode);
    }

    public Collection<Integer> returnPayloadStatusCodes() {
        return new ArrayList<>(returnPayload.keySet());
    }

    public RoutingDefinition addRequestUrlParam(Field aField) {
        if (aField == null) {
            return this;
        }
        requestUrlParams.add(new RequestUrlParameter(aField.getName(), aField));
        return this;
    }

    public RoutingDefinition addRequestUrlParam(final String parameterName, final Field field) {
        if (parameterName == null || field == null) {
            return this;
        }
        requestUrlParams.add(new RequestUrlParameter(parameterName, field));
        return this;
    }

    public Boolean hasRequestUrlParams() {
        return !requestUrlParams.isEmpty();
    }

    public List<Field> getRequestUrlParams() {
        List<Field> fields = new ArrayList<>();
        for (RequestUrlParameter parameter : requestUrlParams) {
            fields.add(parameter.field());
        }
        return fields;
    }

    public List<RequestUrlParameter> getRequestUrlParameters() {
        return new ArrayList<>(requestUrlParams);
    }

    public RoutingDefinition addCustomHeader(String headerName, String headerType) {
        customHeaders.put(headerName, headerType);
        return this;
    }

    public boolean hasCustomHeaders() {
        return !customHeaders.keySet().isEmpty();
    }

    public Collection<String> getCustomHeaderNames() {
        return customHeaders.keySet();
    }

    public String getCustomHeaderType(String headerName) {
        return customHeaders.get(headerName);
    }

    public boolean hasCustomHeaderNamed(String headerName) {
        return customHeaders.containsKey(headerName);
    }

    public RoutingDefinition secureWithBasicAuth() {
        usesBasicAuth = true;
        return this;
    }

    public boolean isSecuredByBasicAuth() {
        return usesBasicAuth;
    }

    public RoutingDefinition secureWithBearerAuth() {
        usesBearerAuth = true;
        return this;
    }

    public boolean isSecuredByBearerAuth() {
        return usesBearerAuth;
    }

    public RoutingDefinition hideFromDocumentation() {
        hiddenFromDocumentation = true;
        return this;
    }

    public boolean isHiddenFromDocumentation() {
        return hiddenFromDocumentation;
    }

    public RoutingDefinition disable() {
        disabled = true;
        hiddenFromDocumentation = true;
        return this;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public static final class RequestUrlParameter {

        private final String name;
        private final Field field;

        public RequestUrlParameter(final String name, final Field field) {
            this.name = name;
            this.field = field;
        }

        public String name() {
            return name;
        }

        public Field field() {
            return field;
        }
    }
}
