package uk.co.compendiumdev.thingifier.api.spec;

import java.util.ArrayList;
import java.util.List;

/**
 * Declares one public route prefix that exposes the canonical Thingifier API route set.
 *
 * <p>A mount is intentionally a public-facing alias, not a second copy of the model routes. Runtime
 * handling strips the matched prefix before command/query mapping, while documentation and server
 * registration can show the mounted public paths. This lets applications publish the same
 * Thingifier-managed API under prefixes such as {@code /api} without custom bridge code.
 */
public final class ThingifierApiMountDefinition {

    private final String name;
    private String prefix;
    private final List<String> includeRoutePatterns;
    private boolean hiddenFromDocs;
    private boolean rewriteLocationHeaders;

    ThingifierApiMountDefinition(final String name) {
        this.name = requireName(name);
        this.prefix = "/";
        this.includeRoutePatterns = new ArrayList<>();
        this.hiddenFromDocs = false;
        this.rewriteLocationHeaders = false;
    }

    /**
     * Sets the public path prefix for this mount.
     *
     * <p>The root mount {@code /} is allowed and acts as a public alias for canonical routes.
     * Non-root prefixes match exact path segments, so {@code /api} matches {@code /api/todos} but
     * not {@code /apix/todos}.
     *
     * @param publicPathPrefix public prefix, with or without a leading slash
     * @return this mount definition so configuration can be chained
     */
    public ThingifierApiMountDefinition at(final String publicPathPrefix) {
        this.prefix = normalizePrefix(publicPathPrefix);
        return this;
    }

    /**
     * Limits the canonical Thingifier routes exposed through this mount.
     *
     * <p>Patterns are matched against internal route paths before the mount prefix is added. Exact
     * paths and route-parameter patterns use normal Thingifier matching. A pattern ending in {@code
     * /**} includes the base path and all descendants, e.g. {@code /todos/**} includes {@code
     * /todos} and {@code /todos/1}.
     *
     * @param routePatterns canonical route patterns to expose
     * @return this mount definition so configuration can be chained
     */
    public ThingifierApiMountDefinition includeRoutes(final String... routePatterns) {
        if (routePatterns == null) {
            return this;
        }
        for (String routePattern : routePatterns) {
            final String normalized = normalizePath(routePattern);
            if (!normalized.isEmpty() && !includeRoutePatterns.contains(normalized)) {
                includeRoutePatterns.add(normalized);
            }
        }
        return this;
    }

    /**
     * Rewrites relative Location headers created by Thingifier to use this mount prefix.
     *
     * <p>This is useful when a create operation internally returns {@code /todos/21} but the caller
     * used {@code /api/todos}. Absolute URLs are left alone.
     *
     * @return this mount definition so configuration can be chained
     */
    public ThingifierApiMountDefinition rewriteLocationHeadersToMount() {
        rewriteLocationHeaders = true;
        return this;
    }

    /**
     * Hides this public mount from generated documentation while leaving it callable.
     *
     * @return this mount definition so configuration can be chained
     */
    public ThingifierApiMountDefinition hideFromDocs() {
        hiddenFromDocs = true;
        return this;
    }

    /**
     * Shows this public mount in generated documentation.
     *
     * @return this mount definition so configuration can be chained
     */
    public ThingifierApiMountDefinition exposeInDocs() {
        hiddenFromDocs = false;
        return this;
    }

    /**
     * Returns the stable mount name.
     *
     * @return mount name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the public mount prefix with a leading slash.
     *
     * @return public prefix, or {@code /} for the root mount
     */
    public String prefix() {
        return prefix;
    }

    /**
     * Reports whether this mount should be hidden from generated documentation.
     *
     * @return true when hidden from docs
     */
    public boolean isHiddenFromDocs() {
        return hiddenFromDocs;
    }

    /**
     * Reports whether relative Location headers should be rewritten to this mount.
     *
     * @return true when Location headers should include the active mount prefix
     */
    public boolean shouldRewriteLocationHeaders() {
        return rewriteLocationHeaders;
    }

    /**
     * Returns the configured canonical include patterns.
     *
     * @return immutable include pattern list
     */
    public List<String> includeRoutePatterns() {
        return List.copyOf(includeRoutePatterns);
    }

    /**
     * Reports whether the supplied public request path belongs to this mount.
     *
     * @param requestPath request path, with or without a leading slash
     * @return true when this mount prefix matches
     */
    public boolean matchesRequestPath(final String requestPath) {
        final String normalizedRequestPath = normalizePath(requestPath);
        final String normalizedPrefix = normalizedPrefix();
        if (normalizedPrefix.isEmpty()) {
            return true;
        }
        return normalizedRequestPath.equals(normalizedPrefix)
                || normalizedRequestPath.startsWith(normalizedPrefix + "/");
    }

    /**
     * Converts a public request path into the canonical Thingifier route path for this mount.
     *
     * @param requestPath request path, with or without a leading slash
     * @return canonical internal path without a leading slash
     */
    public String internalPathFor(final String requestPath) {
        final String normalizedRequestPath = normalizePath(requestPath);
        final String normalizedPrefix = normalizedPrefix();
        if (normalizedPrefix.isEmpty()) {
            return normalizedRequestPath;
        }
        if (normalizedRequestPath.equals(normalizedPrefix)) {
            return "";
        }
        if (normalizedRequestPath.startsWith(normalizedPrefix + "/")) {
            return normalizedRequestPath.substring(normalizedPrefix.length() + 1);
        }
        return normalizedRequestPath;
    }

    /**
     * Reports whether this mount exposes a canonical route path.
     *
     * @param internalPath canonical Thingifier route path
     * @return true when the route is included
     */
    public boolean includesRoute(final String internalPath) {
        if (includeRoutePatterns.isEmpty()) {
            return true;
        }
        final String normalizedPath = normalizePath(internalPath);
        for (String pattern : includeRoutePatterns) {
            if (matchesIncludePattern(pattern, normalizedPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds the public route URL for a canonical Thingifier route.
     *
     * @param internalPath canonical route path
     * @return mounted route path without a leading slash for route metadata
     */
    public String publicRouteUrlFor(final String internalPath) {
        final String normalizedPath = normalizePath(internalPath);
        final String normalizedPrefix = normalizedPrefix();
        if (normalizedPrefix.isEmpty()) {
            return normalizedPath;
        }
        if (normalizedPath.isEmpty()) {
            return normalizedPrefix;
        }
        return normalizedPrefix + "/" + normalizedPath;
    }

    int prefixLength() {
        return normalizedPrefix().length();
    }

    private boolean matchesIncludePattern(final String pattern, final String normalizedPath) {
        if ("**".equals(pattern) || "*".equals(pattern)) {
            return true;
        }
        if (pattern.endsWith("/**")) {
            final String basePath = pattern.substring(0, pattern.length() - 3);
            return normalizedPath.equals(basePath) || normalizedPath.startsWith(basePath + "/");
        }
        return ApiRoutePathMatcher.pathsMatch(pattern, normalizedPath, "");
    }

    private String normalizedPrefix() {
        return "/".equals(prefix) ? "" : normalizePath(prefix);
    }

    private String normalizePrefix(final String rawPrefix) {
        final String normalized = normalizePath(rawPrefix);
        if (normalized.isEmpty()) {
            return "/";
        }
        return "/" + normalized;
    }

    private String normalizePath(final String rawPath) {
        String normalized = rawPath == null ? "" : rawPath.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/") && normalized.length() > 0) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String requireName(final String rawName) {
        final String normalizedName = rawName == null ? "" : rawName.trim();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("mount name is required");
        }
        return normalizedName;
    }
}
