package uk.co.compendiumdev.thingifier.api.routings;

import uk.co.compendiumdev.thingifier.api.response.ResponseHeader;
import uk.co.compendiumdev.thingifier.core.domain.definitions.ThingDefinition;

import java.util.ArrayList;
import java.util.List;

public class RoutingDefinition {
    private final RoutingVerb verb;
    private final String url;
    private final RoutingStatus routingStatus;
    private final ResponseHeader header;
    private String documentation = "";
    private boolean isFilterable;
    private ThingDefinition filterableEntityDefn;
    private List<RoutingStatus> possibleStatusResponses;

    public RoutingDefinition(RoutingVerb verb, String url, RoutingStatus routingStatus, ResponseHeader header) {
        this.verb = verb;
        if(url.startsWith("/")){
            this.url = url.substring(1);
        }else{
            this.url = url;
        }
        this.routingStatus = routingStatus;
        this.header = header;
        this.isFilterable=false;
        filterableEntityDefn=null;
        this.possibleStatusResponses= new ArrayList<>();
    }

    private List<RoutingStatus> getDefaultPossibleStatusResponses() {
        List<RoutingStatus> defaultPossibleStatusResponses = new ArrayList<>();
        defaultPossibleStatusResponses.add(RoutingStatus.returnValue(200));
        defaultPossibleStatusResponses.add(RoutingStatus.returnValue(404));
        return defaultPossibleStatusResponses;
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

    public void setAsFilterableFrom(final ThingDefinition definition) {
        isFilterable=true;
        filterableEntityDefn = definition;
    }

    public ThingDefinition getFilterableEntity() {
        return filterableEntityDefn;
    }

    public RoutingDefinition addPossibleStatus(final RoutingStatus status) {
        possibleStatusResponses.add(status);
        return this;
    }

    public List<RoutingStatus> getPossibleStatusReponses() {
        if(possibleStatusResponses.size()==0){
            return getDefaultPossibleStatusResponses();
        }
        return possibleStatusResponses;
    }

    // quick hack method to allow creating a bunch of default rendered possible status codes
    public RoutingDefinition addPossibleStatuses(final Integer... statusCodes) {
        for(Integer statusCode : statusCodes){
            addPossibleStatus(RoutingStatus.returnValue(statusCode));
        }
        return this;
    }
}
