package uk.co.compendiumdev.thingifier.api.spec;

/**
 * Controls what a fixed-instance route does when its configured target instance is missing.
 *
 * <p>Fixed-instance routes are public API paths that do not expose an identifier in the URL, but
 * still route to one known Thingifier entity instance. The policy makes the missing-instance
 * behaviour explicit so applications do not accidentally create data when a read-only singleton
 * style route was intended.
 */
public enum FixedResourcePolicy {
    /**
     * Leave missing targets to normal entity handling.
     *
     * <p>For instance-shaped routes this normally means GET, HEAD, POST, PATCH, PUT, and DELETE
     * surface the same not-found response they would for a generated {@code /entities/{id}} route.
     */
    RETURN_404,

    /**
     * Create the fixed target instance when it is missing before normal route handling continues.
     *
     * <p>The instance is created through Thingifier's normal command service using only the fixed
     * identifier. If the entity cannot be created without additional required fields, the route
     * reports a configuration error rather than inserting invalid data.
     */
    ENSURE_EXISTS
}
