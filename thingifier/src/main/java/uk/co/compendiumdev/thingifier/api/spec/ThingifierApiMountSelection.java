package uk.co.compendiumdev.thingifier.api.spec;

/**
 * Immutable result of resolving one request against configured API mounts.
 *
 * <p>The selection records both the public path that arrived at Thingifier and the canonical
 * internal path that generated handlers should process. It is created before request hooks,
 * authentication, validators, and callbacks so every later phase can agree on the same mount and
 * route decision.
 */
public final class ThingifierApiMountSelection {

    private final boolean mounted;
    private final String mountName;
    private final String mountPrefix;
    private final String requestPath;
    private final String internalPath;
    private final boolean rewriteLocationHeaders;

    private ThingifierApiMountSelection(
            final boolean mounted,
            final String mountName,
            final String mountPrefix,
            final String requestPath,
            final String internalPath,
            final boolean rewriteLocationHeaders) {
        this.mounted = mounted;
        this.mountName = mountName;
        this.mountPrefix = normalizedPrefix(mountPrefix);
        this.requestPath = normalizePath(requestPath);
        this.internalPath = normalizePath(internalPath);
        this.rewriteLocationHeaders = rewriteLocationHeaders;
    }

    static ThingifierApiMountSelection none(final String requestPath) {
        return new ThingifierApiMountSelection(false, null, "", requestPath, requestPath, false);
    }

    static ThingifierApiMountSelection forMount(
            final ThingifierApiMountDefinition mount,
            final String requestPath,
            final String internalPath) {
        return new ThingifierApiMountSelection(
                true,
                mount.name(),
                mount.prefix(),
                requestPath,
                internalPath,
                mount.shouldRewriteLocationHeaders());
    }

    static ThingifierApiMountSelection forLegacyPrefix(
            final String prefix, final String requestPath, final String internalPath) {
        return new ThingifierApiMountSelection(
                false, null, prefix, requestPath, internalPath, false);
    }

    /**
     * Reports whether a named mount matched this request.
     *
     * @return true when the request matched a configured mount
     */
    public boolean isMounted() {
        return mounted;
    }

    /**
     * Returns the active mount name.
     *
     * @return mount name, or null when no named mount matched
     */
    public String mountName() {
        return mountName;
    }

    /**
     * Returns the active public prefix with a leading slash.
     *
     * @return active prefix, or an empty string when no prefix was applied
     */
    public String mountPrefix() {
        return mountPrefix;
    }

    /**
     * Returns the public request path as received by Thingifier, without a leading slash.
     *
     * @return public request path
     */
    public String requestPath() {
        return requestPath;
    }

    /**
     * Returns the mounted public path for the request, without a leading slash.
     *
     * @return mounted path, or the request path when no named mount matched
     */
    public String mountedPath() {
        return requestPath;
    }

    /**
     * Returns the canonical Thingifier path handlers should process, without a leading slash.
     *
     * @return internal route path
     */
    public String internalPath() {
        return internalPath;
    }

    /**
     * Reports whether generated relative Location headers should be rewritten through this mount.
     *
     * @return true when Location rewriting is enabled for the active mount
     */
    public boolean shouldRewriteLocationHeaders() {
        return rewriteLocationHeaders && !mountPrefix.isEmpty() && !"/".equals(mountPrefix);
    }

    private static String normalizedPrefix(final String rawPrefix) {
        final String normalized = normalizePath(rawPrefix);
        if (normalized.isEmpty()) {
            return "";
        }
        return "/" + normalized;
    }

    private static String normalizePath(final String rawPath) {
        String normalized = rawPath == null ? "" : rawPath.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/") && normalized.length() > 0) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
