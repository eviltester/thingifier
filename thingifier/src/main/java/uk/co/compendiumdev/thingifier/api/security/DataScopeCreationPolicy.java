package uk.co.compendiumdev.thingifier.api.security;

/**
 * Controls how Thingifier resolves an auth-selected data scope.
 *
 * <p>Authenticators use this policy after they have validated credentials and decided which
 * application-owned data scope should handle the request. The public API says "data scope" because
 * callers are choosing an isolation boundary; internally Thingifier may back that scope with a
 * database, session store, in-memory store, or another {@code ThingStoreProvider} implementation.
 */
public enum DataScopeCreationPolicy {
    /**
     * Use only a store that already exists.
     *
     * <p>This is the safest default because authentication selects a scope without implicitly
     * provisioning storage.
     */
    USE_EXISTING_ONLY,

    /** Create an empty store when the selected data scope does not already exist. */
    ENSURE_EXISTS,

    /**
     * Create and populate the selected data scope when it does not already exist.
     *
     * <p>Population uses the Thingifier model's configured data generator. Application-specific
     * synchronization still belongs in application code before returning the auth result.
     */
    ENSURE_CREATED_AND_POPULATED
}
