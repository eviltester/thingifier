package uk.co.compendiumdev.thingifier.api.spec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;

public final class ThingifierApiSpec {

    private final List<ThingifierApiRouteRule> routeRules;

    public ThingifierApiSpec() {
        routeRules = new ArrayList<>();
    }

    public ThingifierApiRouteRule route(final RoutingVerb verb, final String pathPattern) {
        final ThingifierApiRouteRule rule = new ThingifierApiRouteRule(verb, pathPattern);
        routeRules.add(rule);
        return rule;
    }

    public ThingifierApiRouteRule route(final String verb, final String pathPattern) {
        return route(RoutingVerb.valueOf(verb.trim().toUpperCase()), pathPattern);
    }

    public ThingifierApiSpec hideEntityRoutes(final String entityPath) {
        configureEntityRoutes(entityPath, false);
        return this;
    }

    public ThingifierApiSpec disableEntityRoutes(final String entityPath) {
        configureEntityRoutes(entityPath, true);
        return this;
    }

    public ThingifierApiSpec hideRelationshipRoutes(
            final String parentEntityPath, final String relationshipName) {
        configureRelationshipRoutes(parentEntityPath, relationshipName, false);
        return this;
    }

    public ThingifierApiSpec disableRelationshipRoutes(
            final String parentEntityPath, final String relationshipName) {
        configureRelationshipRoutes(parentEntityPath, relationshipName, true);
        return this;
    }

    public void applyTo(final ApiRoutingDefinition routingDefinition, final String apiPathPrefix) {
        for (RoutingDefinition route : routingDefinition.definitions()) {
            ruleFor(route.verb(), route.url(), apiPathPrefix)
                    .ifPresent(rule -> rule.applyTo(route));
        }
        routingDefinition.updateOptionsAllowHeaders();
    }

    public boolean isDisabled(final String verb, final String path, final String apiPathPrefix) {
        return ruleFor(verb, path, apiPathPrefix)
                .map(ThingifierApiRouteRule::isDisabled)
                .orElse(false);
    }

    public boolean isDisabled(
            final RoutingVerb verb, final String path, final String apiPathPrefix) {
        return ruleFor(verb, path, apiPathPrefix)
                .map(ThingifierApiRouteRule::isDisabled)
                .orElse(false);
    }

    public Optional<ThingifierApiRouteRule> ruleFor(
            final String verb, final String path, final String apiPathPrefix) {
        return ruleFor(RoutingVerb.valueOf(verb.trim().toUpperCase()), path, apiPathPrefix);
    }

    public Optional<ThingifierApiRouteRule> ruleFor(
            final RoutingVerb verb, final String path, final String apiPathPrefix) {
        return routeRules.stream()
                .filter(rule -> rule.verb() == verb)
                .filter(rule -> pathsMatch(rule.pathPattern(), path, apiPathPrefix))
                .findFirst();
    }

    private void configureEntityRoutes(final String entityPath, final boolean disable) {
        final String collectionPath = "/" + normalize(entityPath);
        final String instancePath = collectionPath + "/{id}";
        for (RoutingVerb verb : RoutingVerb.values()) {
            configureRoute(verb, collectionPath, disable);
            configureRoute(verb, instancePath, disable);
        }
    }

    private void configureRelationshipRoutes(
            final String parentEntityPath, final String relationshipName, final boolean disable) {
        final String relationshipPath =
                "/" + normalize(parentEntityPath) + "/{id}/" + normalize(relationshipName);
        final String relationshipInstancePath = relationshipPath + "/{relatedId}";
        for (RoutingVerb verb : RoutingVerb.values()) {
            configureRoute(verb, relationshipPath, disable);
            configureRoute(verb, relationshipInstancePath, disable);
        }
    }

    private void configureRoute(
            final RoutingVerb verb, final String pathPattern, final boolean disable) {
        final ThingifierApiRouteRule rule = route(verb, pathPattern);
        if (disable) {
            rule.disable();
        } else {
            rule.hide();
        }
    }

    private boolean pathsMatch(
            final String rulePath, final String candidatePath, final String apiPathPrefix) {
        final List<String> ruleSegments = segments(rulePath, apiPathPrefix);
        final List<String> candidateSegments = segments(candidatePath, apiPathPrefix);
        if (ruleSegments.size() != candidateSegments.size()) {
            return false;
        }
        for (int index = 0; index < ruleSegments.size(); index++) {
            final String ruleSegment = ruleSegments.get(index);
            final String candidateSegment = candidateSegments.get(index);
            if (isWildcard(ruleSegment) || isWildcard(candidateSegment)) {
                continue;
            }
            if (!ruleSegment.equals(candidateSegment)) {
                return false;
            }
        }
        return true;
    }

    private List<String> segments(final String path, final String apiPathPrefix) {
        final String normalized = removePrefix(normalize(path), normalize(apiPathPrefix));
        if (normalized.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(normalized.split("/")).map(this::normalizeParameterSegment).toList();
    }

    private String normalize(final String path) {
        String normalized = path == null ? "" : path.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String removePrefix(final String path, final String apiPathPrefix) {
        if (apiPathPrefix == null || apiPathPrefix.isEmpty()) {
            return path;
        }
        if (path.equals(apiPathPrefix)) {
            return "";
        }
        if (path.startsWith(apiPathPrefix + "/")) {
            return path.substring(apiPathPrefix.length() + 1);
        }
        return path;
    }

    private String normalizeParameterSegment(final String segment) {
        if (segment.startsWith(":")) {
            return "*";
        }
        if (segment.startsWith("{") && segment.endsWith("}")) {
            return "*";
        }
        return segment;
    }

    private boolean isWildcard(final String segment) {
        return "*".equals(segment);
    }
}
