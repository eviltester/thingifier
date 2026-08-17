package uk.co.compendiumdev.thingifier.api.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle;
import uk.co.compendiumdev.thingifier.apiconfig.EntityWriteOperation;
import uk.co.compendiumdev.thingifier.apiconfig.RelationshipWriteOperation;

public final class ThingifierApiSpec {

    private final List<ThingifierApiRouteRule> routeRules;
    private final List<EntityWritePolicyRule> entityWritePolicyRules;
    private final List<EntityPatchPolicyRule> entityPatchPolicyRules;
    private final List<RelationshipWritePolicyRule> relationshipWritePolicyRules;

    public ThingifierApiSpec() {
        routeRules = new ArrayList<>();
        entityWritePolicyRules = new ArrayList<>();
        entityPatchPolicyRules = new ArrayList<>();
        relationshipWritePolicyRules = new ArrayList<>();
    }

    public ThingifierApiRouteRule route(final RoutingVerb verb, final String pathPattern) {
        final ThingifierApiRouteRule rule = new ThingifierApiRouteRule(verb, pathPattern);
        routeRules.add(rule);
        return rule;
    }

    public ThingifierApiRouteRule route(final String verb, final String pathPattern) {
        return route(RoutingVerb.valueOf(verb.trim().toUpperCase()), pathPattern);
    }

    public ThingifierApiPathRule route(final String pathPattern) {
        return new ThingifierApiPathRule(this, pathPattern);
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

    public ThingifierApiSpec entityPostCan(
            final String entityPath, final EntityWriteOperation... operations) {
        configureEntityWritePolicy(RoutingVerb.POST, entityPath, operations);
        return this;
    }

    public ThingifierApiSpec entityPutCan(
            final String entityPath, final EntityWriteOperation... operations) {
        configureEntityWritePolicy(RoutingVerb.PUT, entityPath, operations);
        return this;
    }

    public ThingifierApiSpec entityPatchCan(
            final String entityPath, final EntityPatchUpdateStyle... updateStyles) {
        configureEntityPatchPolicy(entityPath, updateStyles);
        return this;
    }

    public ThingifierApiSpec relationshipPostCan(
            final String parentEntityPath,
            final String relationshipName,
            final RelationshipWriteOperation... operations) {
        configureRelationshipWritePolicy(
                RoutingVerb.POST, parentEntityPath, relationshipName, operations);
        return this;
    }

    public ThingifierApiSpec relationshipDeleteCan(
            final String parentEntityPath,
            final String relationshipName,
            final RelationshipWriteOperation... operations) {
        configureRelationshipWritePolicy(
                RoutingVerb.DELETE, parentEntityPath, relationshipName, operations);
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

    public boolean isMethodNotAllowed(
            final RoutingVerb verb, final String path, final String apiPathPrefix) {
        return ruleFor(verb, path, apiPathPrefix)
                .map(ThingifierApiRouteRule::isMethodNotAllowed)
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
                .filter(
                        rule ->
                                ApiRoutePathMatcher.pathsMatch(
                                        rule.pathPattern(), path, apiPathPrefix))
                .findFirst();
    }

    ThingifierApiRouteRule routeFor(final RoutingVerb verb, final String pathPattern) {
        return routeRules.stream()
                .filter(rule -> rule.verb() == verb)
                .filter(rule -> samePathPattern(rule.pathPattern(), pathPattern))
                .findFirst()
                .orElseGet(() -> route(verb, pathPattern));
    }

    public Optional<Set<EntityWriteOperation>> entityWriteOperationsFor(
            final RoutingVerb verb, final String path, final String apiPathPrefix) {
        Optional<ThingifierApiRouteRule> routeRule =
                ruleFor(verb, path, apiPathPrefix)
                        .filter(ThingifierApiRouteRule::hasEntityWriteOperations);
        if (routeRule.isPresent()) {
            return Optional.of(routeRule.get().entityWriteOperations());
        }

        return entityWritePolicyRules.stream()
                .filter(rule -> rule.verb() == verb)
                .filter(
                        rule ->
                                ApiRoutePathMatcher.pathsMatch(
                                        rule.pathPattern(), path, apiPathPrefix))
                .map(EntityWritePolicyRule::operations)
                .findFirst();
    }

    public Optional<Set<EntityPatchUpdateStyle>> entityPatchUpdateStylesFor(
            final String path, final String apiPathPrefix) {
        Optional<ThingifierApiRouteRule> routeRule =
                ruleFor(RoutingVerb.PATCH, path, apiPathPrefix)
                        .filter(ThingifierApiRouteRule::hasEntityPatchUpdateStyles);
        if (routeRule.isPresent()) {
            return Optional.of(routeRule.get().entityPatchUpdateStyles());
        }

        return entityPatchPolicyRules.stream()
                .filter(
                        rule ->
                                ApiRoutePathMatcher.pathsMatch(
                                        rule.pathPattern(), path, apiPathPrefix))
                .map(EntityPatchPolicyRule::updateStyles)
                .findFirst();
    }

    public Optional<Set<RelationshipWriteOperation>> relationshipWriteOperationsFor(
            final RoutingVerb verb, final String path, final String apiPathPrefix) {
        Optional<ThingifierApiRouteRule> routeRule =
                ruleFor(verb, path, apiPathPrefix)
                        .filter(ThingifierApiRouteRule::hasRelationshipWriteOperations);
        if (routeRule.isPresent()) {
            return Optional.of(routeRule.get().relationshipWriteOperations());
        }

        return relationshipWritePolicyRules.stream()
                .filter(rule -> rule.verb() == verb)
                .filter(
                        rule ->
                                ApiRoutePathMatcher.pathsMatch(
                                        rule.pathPattern(), path, apiPathPrefix))
                .map(RelationshipWritePolicyRule::operations)
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

    private void configureEntityWritePolicy(
            final RoutingVerb verb,
            final String entityPath,
            final EntityWriteOperation... operations) {
        final String collectionPath = "/" + normalize(entityPath);
        final String instancePath = collectionPath + "/{id}";
        if (verb == RoutingVerb.POST || verb == RoutingVerb.PUT) {
            entityWritePolicyRules.add(
                    new EntityWritePolicyRule(verb, collectionPath, entityOperations(operations)));
        }
        entityWritePolicyRules.add(
                new EntityWritePolicyRule(verb, instancePath, entityOperations(operations)));
    }

    private void configureEntityPatchPolicy(
            final String entityPath, final EntityPatchUpdateStyle... updateStyles) {
        final String instancePath = "/" + normalize(entityPath) + "/{id}";
        entityPatchPolicyRules.add(
                new EntityPatchPolicyRule(instancePath, entityPatchStyles(updateStyles)));
    }

    private void configureRelationshipWritePolicy(
            final RoutingVerb verb,
            final String parentEntityPath,
            final String relationshipName,
            final RelationshipWriteOperation... operations) {
        final String relationshipPath =
                "/" + normalize(parentEntityPath) + "/{id}/" + normalize(relationshipName);
        final String path =
                verb == RoutingVerb.DELETE ? relationshipPath + "/{relatedId}" : relationshipPath;
        relationshipWritePolicyRules.add(
                new RelationshipWritePolicyRule(verb, path, relationshipOperations(operations)));
    }

    private Set<EntityWriteOperation> entityOperations(final EntityWriteOperation... operations) {
        EnumSet<EntityWriteOperation> selected = EnumSet.noneOf(EntityWriteOperation.class);
        if (operations != null) {
            Collections.addAll(selected, operations);
        }
        if (selected.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(selected));
    }

    private Set<EntityPatchUpdateStyle> entityPatchStyles(
            final EntityPatchUpdateStyle... updateStyles) {
        EnumSet<EntityPatchUpdateStyle> selected = EnumSet.noneOf(EntityPatchUpdateStyle.class);
        if (updateStyles != null) {
            Collections.addAll(selected, updateStyles);
        }
        if (selected.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(selected));
    }

    private Set<RelationshipWriteOperation> relationshipOperations(
            final RelationshipWriteOperation... operations) {
        EnumSet<RelationshipWriteOperation> selected =
                EnumSet.noneOf(RelationshipWriteOperation.class);
        if (operations != null) {
            Collections.addAll(selected, operations);
        }
        if (selected.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(selected));
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

    private boolean samePathPattern(final String first, final String second) {
        return normalize(first).equals(normalize(second));
    }

    private static final class EntityWritePolicyRule {
        private final RoutingVerb verb;
        private final String pathPattern;
        private final Set<EntityWriteOperation> operations;

        EntityWritePolicyRule(
                final RoutingVerb verb,
                final String pathPattern,
                final Set<EntityWriteOperation> operations) {
            this.verb = verb;
            this.pathPattern = pathPattern;
            this.operations = operations;
        }

        RoutingVerb verb() {
            return verb;
        }

        String pathPattern() {
            return pathPattern;
        }

        Set<EntityWriteOperation> operations() {
            return operations;
        }
    }

    private static final class EntityPatchPolicyRule {
        private final String pathPattern;
        private final Set<EntityPatchUpdateStyle> updateStyles;

        EntityPatchPolicyRule(
                final String pathPattern, final Set<EntityPatchUpdateStyle> updateStyles) {
            this.pathPattern = pathPattern;
            this.updateStyles = updateStyles;
        }

        String pathPattern() {
            return pathPattern;
        }

        Set<EntityPatchUpdateStyle> updateStyles() {
            return updateStyles;
        }
    }

    private static final class RelationshipWritePolicyRule {
        private final RoutingVerb verb;
        private final String pathPattern;
        private final Set<RelationshipWriteOperation> operations;

        RelationshipWritePolicyRule(
                final RoutingVerb verb,
                final String pathPattern,
                final Set<RelationshipWriteOperation> operations) {
            this.verb = verb;
            this.pathPattern = pathPattern;
            this.operations = operations;
        }

        RoutingVerb verb() {
            return verb;
        }

        String pathPattern() {
            return pathPattern;
        }

        Set<RelationshipWriteOperation> operations() {
            return operations;
        }
    }
}
