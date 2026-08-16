package uk.co.compendiumdev.thingifier.api.spec;

import java.util.Arrays;
import java.util.List;

public final class ApiRoutePathMatcher {

    private ApiRoutePathMatcher() {}

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

    private static List<String> segments(final String path, final String apiPathPrefix) {
        final String normalized = removePrefix(normalize(path), normalize(apiPathPrefix));
        if (normalized.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(normalized.split("/"))
                .map(ApiRoutePathMatcher::normalizeParameterSegment)
                .toList();
    }

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

    private static String normalizeParameterSegment(final String segment) {
        if (segment.startsWith(":")) {
            return "*";
        }
        if (segment.startsWith("{") && segment.endsWith("}")) {
            return "*";
        }
        return segment;
    }

    private static boolean isWildcard(final String segment) {
        return "*".equals(segment);
    }
}
