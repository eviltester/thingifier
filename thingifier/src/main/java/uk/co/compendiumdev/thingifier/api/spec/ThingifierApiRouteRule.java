package uk.co.compendiumdev.thingifier.api.spec;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation;
import uk.co.compendiumdev.thingifier.apiconfig.RelationshipWriteOperation;

public final class ThingifierApiRouteRule {

    private final RoutingVerb verb;
    private final String pathPattern;
    private boolean hidden;
    private boolean disabled;
    private boolean usesBasicAuth;
    private boolean usesBearerAuth;
    private String documentation;
    private String requestPayload;
    private String requestEntityView;
    private String defaultEntityView;
    private Map<Integer, String> responseEntityViews;
    private EnumSet<EntityWriteOperation> entityWriteOperations;
    private EnumSet<RelationshipWriteOperation> relationshipWriteOperations;

    ThingifierApiRouteRule(final RoutingVerb verb, final String pathPattern) {
        this.verb = verb;
        this.pathPattern = pathPattern == null ? "" : pathPattern;
        this.hidden = false;
        this.disabled = false;
        this.usesBasicAuth = false;
        this.usesBearerAuth = false;
        this.documentation = null;
        this.requestPayload = null;
        this.requestEntityView = null;
        this.defaultEntityView = null;
        this.responseEntityViews = new HashMap<>();
        this.entityWriteOperations = null;
        this.relationshipWriteOperations = null;
    }

    public RoutingVerb verb() {
        return verb;
    }

    public String pathPattern() {
        return pathPattern;
    }

    public ThingifierApiRouteRule hide() {
        hidden = true;
        return this;
    }

    public ThingifierApiRouteRule hideFromDocs() {
        return hide();
    }

    public ThingifierApiRouteRule disable() {
        disabled = true;
        return this;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public ThingifierApiRouteRule secureWithBasicAuth() {
        usesBasicAuth = true;
        return this;
    }

    public ThingifierApiRouteRule secureWithBearerAuth() {
        usesBearerAuth = true;
        return this;
    }

    public ThingifierApiRouteRule addDocumentation(final String documentation) {
        this.documentation = documentation;
        return this;
    }

    public ThingifierApiRouteRule requestPayload(final String requestPayload) {
        this.requestPayload = requestPayload;
        return this;
    }

    public ThingifierApiRouteRule requestEntityView(final String viewName) {
        this.requestEntityView = viewName;
        return this;
    }

    public ThingifierApiRouteRule responseEntityView(final int statusCode, final String viewName) {
        this.responseEntityViews.put(statusCode, viewName);
        return this;
    }

    public ThingifierApiRouteRule entityWriteOperations(final EntityWriteOperation... operations) {
        this.entityWriteOperations = entityOperations(operations);
        return this;
    }

    public ThingifierApiRouteRule entityCan(final EntityWriteOperation... operations) {
        return entityWriteOperations(operations);
    }

    public ThingifierApiRouteRule relationshipWriteOperations(
            final RelationshipWriteOperation... operations) {
        this.relationshipWriteOperations = relationshipOperations(operations);
        return this;
    }

    public ThingifierApiRouteRule relationshipCan(final RelationshipWriteOperation... operations) {
        return relationshipWriteOperations(operations);
    }

    public ThingifierApiRouteRule entityView(final String viewName) {
        this.requestEntityView = viewName;
        this.defaultEntityView = viewName;
        return this;
    }

    public boolean hasRequestEntityView() {
        return requestEntityView != null;
    }

    public String getRequestEntityView() {
        return requestEntityView;
    }

    public String responseEntityViewFor(final int statusCode) {
        if (responseEntityViews.containsKey(statusCode)) {
            return responseEntityViews.get(statusCode);
        }
        if (defaultEntityView != null && statusCode >= 200 && statusCode < 300) {
            return defaultEntityView;
        }
        return null;
    }

    public boolean hasEntityWriteOperations() {
        return entityWriteOperations != null;
    }

    public Set<EntityWriteOperation> entityWriteOperations() {
        if (entityWriteOperations == null || entityWriteOperations.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(entityWriteOperations));
    }

    public boolean hasRelationshipWriteOperations() {
        return relationshipWriteOperations != null;
    }

    public Set<RelationshipWriteOperation> relationshipWriteOperations() {
        if (relationshipWriteOperations == null || relationshipWriteOperations.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(relationshipWriteOperations));
    }

    void applyTo(final RoutingDefinition route) {
        if (hidden) {
            route.hideFromDocumentation();
        }
        if (disabled) {
            route.disable();
        }
        if (usesBasicAuth) {
            route.secureWithBasicAuth();
        }
        if (usesBearerAuth) {
            route.secureWithBearerAuth();
        }
        if (documentation != null) {
            route.addDocumentation(documentation);
        }
        if (requestPayload != null) {
            route.requestPayload(requestPayload);
        }
        if (requestEntityView != null) {
            route.requestEntityView(requestEntityView);
        }
        for (Map.Entry<Integer, String> responseView : responseEntityViews.entrySet()) {
            route.responseEntityView(responseView.getKey(), responseView.getValue());
        }
        if (defaultEntityView != null) {
            if (route.returnPayloadStatusCodes().isEmpty()) {
                route.responseEntityView(200, defaultEntityView);
                route.responseEntityView(201, defaultEntityView);
            } else {
                for (Integer statusCode : route.returnPayloadStatusCodes()) {
                    if (statusCode >= 200
                            && statusCode < 300
                            && !route.hasResponseEntityViewFor(statusCode)) {
                        route.responseEntityView(statusCode, defaultEntityView);
                    }
                }
            }
        }
    }

    private EnumSet<EntityWriteOperation> entityOperations(
            final EntityWriteOperation... operations) {
        EnumSet<EntityWriteOperation> selected = EnumSet.noneOf(EntityWriteOperation.class);
        if (operations != null) {
            Collections.addAll(selected, operations);
        }
        return selected;
    }

    private EnumSet<RelationshipWriteOperation> relationshipOperations(
            final RelationshipWriteOperation... operations) {
        EnumSet<RelationshipWriteOperation> selected =
                EnumSet.noneOf(RelationshipWriteOperation.class);
        if (operations != null) {
            Collections.addAll(selected, operations);
        }
        return selected;
    }
}
