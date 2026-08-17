package uk.co.compendiumdev.thingifier.api.spec;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Compares API spec route patterns and request paths using Thingifier route parameter rules.
 *
 * <p>API spec users can write either {@code :id} or {@code {id}} parameters. This matcher treats
 * both forms as wildcards for route matching and can also extract named parameter values for policy
 * callbacks such as route authorization.
 */
public final class ApiRoutePathMatcher {

    /** Utility class; callers use the static matching helpers. */
    private ApiRoutePathMatcher() {}

    /**
     * Reports whether a rule path and candidate path describe the same generated route.
     *
     * @param rulePath configured route pattern
     * @param candidatePath generated route path or request path
     * @param apiPathPrefix configured API prefix to ignore when matching
     * @return true when the paths have the same shape
     */
    public static boolean pathsMatch(
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

    /**
     * Extracts named parameter values from a matching rule path and candidate path.
     *
     * <p>Only parameter names from the rule path are returned. If the paths do not have the same
     * number of segments, an empty map is returned because there is no safe one-to-one binding.
     *
     * @param rulePath configured route pattern
     * @param candidatePath generated route path or request path
     * @param apiPathPrefix configured API prefix to ignore when matching
     * @return immutable map of parameter names to candidate segment values
     */
    public static Map<String, String> pathParameters(
            final String rulePath, final String candidatePath, final String apiPathPrefix) {
        final List<String> ruleSegments = rawSegments(rulePath, apiPathPrefix);
        final List<String> candidateSegments = rawSegments(candidatePath, apiPathPrefix);
        if (ruleSegments.size() != candidateSegments.size()) {
            return Map.of();
        }

        final Map<String, String> parameters = new LinkedHashMap<>();
        for (int index = 0; index < ruleSegments.size(); index++) {
            final int parameterIndex = index;
            parameterName(ruleSegments.get(index))
                    .ifPresent(name -> parameters.put(name, candidateSegments.get(parameterIndex)));
        }
        return Map.copyOf(parameters);
    }

    /**
     * Splits a normalized path into comparable route segments.
     *
     * @param path route path or pattern
     * @param apiPathPrefix configured API prefix
     * @return route segments with parameter segments normalized to wildcards
     */
    private static List<String> segments(final String path, final String apiPathPrefix) {
        return rawSegments(path, apiPathPrefix).stream()
                .map(ApiRoutePathMatcher::normalizeParameterSegment)
                .toList();
    }

    /**
     * Splits a normalized path into raw route segments.
     *
     * @param path route path or pattern
     * @param apiPathPrefix configured API prefix
     * @return route segments with parameter names preserved
     */
    private static List<String> rawSegments(final String path, final String apiPathPrefix) {
        final String normalized = removePrefix(normalize(path), normalize(apiPathPrefix));
        if (normalized.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(normalized.split("/")).toList();
    }

    /**
     * Normalizes slash usage in a path.
     *
     * @param path path or path pattern
     * @return path without leading or trailing slashes
     */
    private static String normalize(final String path) {
        String normalized = path == null ? "" : path.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    /**
     * Removes the configured API prefix when it is present.
     *
     * @param path normalized path
     * @param apiPathPrefix normalized API prefix
     * @return path without the prefix
     */
    private static String removePrefix(final String path, final String apiPathPrefix) {
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

    /**
     * Converts supported parameter syntaxes to a wildcard token.
     *
     * @param segment path segment
     * @return wildcard token for parameter segments, otherwise the original segment
     */
    private static String normalizeParameterSegment(final String segment) {
        if (segment.startsWith(":")) {
            return "*";
        }
        if (segment.startsWith("{") && segment.endsWith("}")) {
            return "*";
        }
        return segment;
    }

    /**
     * Extracts the parameter name from a path segment.
     *
     * @param segment path pattern segment
     * @return parameter name when the segment uses a supported parameter syntax
     */
    private static java.util.Optional<String> parameterName(final String segment) {
        if (segment.startsWith(":") && segment.length() > 1) {
            return java.util.Optional.of(segment.substring(1));
        }
        if (segment.startsWith("{") && segment.endsWith("}") && segment.length() > 2) {
            return java.util.Optional.of(segment.substring(1, segment.length() - 1));
        }
        return java.util.Optional.empty();
    }

    /**
     * Reports whether a comparable segment is a route wildcard.
     *
     * @param segment comparable route segment
     * @return true when the segment is a wildcard
     */
    private static boolean isWildcard(final String segment) {
        return "*".equals(segment);
    }
}
