package uk.co.compendiumdev.thingifier.api.docgen;

import uk.co.compendiumdev.thingifier.api.response.ResponseHeader;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class RoutingDefinition {
    private final RoutingVerb verb;
    private final String url;
    private final RoutingStatus routingStatus;
    private final ResponseHeader header;
    private String documentation = "";
    private boolean isFilterable;
    private EntityDefinition filterableEntityDefn;
    private List<RoutingStatus> possibleStatusResponses;
    private HashMap<Integer,String> returnPayload;
    private String requestPayload;
    private List<Field> requestUrlParams;
    private HashMap<String,String> customHeaders;
    private boolean usesBasicAuth = false;

    public RoutingDefinition(RoutingVerb verb, String url, RoutingStatus routingStatus, ResponseHeader header) {
        this.verb = verb;
        if(url.startsWith("/")){
            this.url = url.substring(1);
        }else{
            this.url = url;
        }
        this.routingStatus = routingStatus;
        this.header = header;

        // defaults
        isFilterable=false;
        filterableEntityDefn=null;
        possibleStatusResponses= new ArrayList<>();
        requestUrlParams = new ArrayList<>();
        returnPayload=new HashMap<>();
        requestPayload=null;
        customHeaders = new HashMap<>();
    }

    public RoutingVerb verb() {
        return this.verb;
    }

    public RoutingStatus status() {
        return routingStatus;
    }

    public String url() {
        return url;
    }

    public String urlWithParamFormatter(String prefix, String postfix) {
        // replace \/:([^\/\?]+)
        return url.replaceAll("\\/:([^\\/\\?]+)", "/" + prefix + "$1" + postfix);
    }

    public String header() {
        if (header.headerName == null) {
            return "";
        }

        return header.headerName;
    }

    public String headerValue() {
        if (header.headerValue == null) {
            return "";
        }

        return header.headerValue;
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
        isFilterable=true;
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

    public List<RoutingStatus> getPossibleStatusReponses() {
        return possibleStatusResponses;
    }

    // quick hack method to allow creating a bunch of default rendered possible status codes
    public RoutingDefinition addPossibleStatuses(final Integer... statusCodes) {
        for(Integer statusCode : statusCodes){
            addPossibleStatus(RoutingStatus.returnValue(statusCode));
        }
        return this;
    }

    public RoutingDefinition returnPayload(final Integer statusCode, String objectSchemaName) {
        returnPayload.put(statusCode, objectSchemaName);
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

    public Boolean hasRequestPayload(){
        return requestPayload!=null;
    }

    public String getRequestPayload(){
        return requestPayload;
    }

    public RoutingDefinition addRequestUrlParam(Field aField) {
        requestUrlParams.add(aField);
        return this;
    }

    public Boolean hasRequestUrlParams(){
        return !requestUrlParams.isEmpty();
    }

    public List<Field> getRequestUrlParams(){
        return new ArrayList<>(requestUrlParams);
    }

    public RoutingDefinition addCustomHeader(String headerName, String headerType) {
        customHeaders.put(headerName,headerType);
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

    public boolean isSecuredByBasicAuth(){
        return usesBasicAuth;
    }
}
