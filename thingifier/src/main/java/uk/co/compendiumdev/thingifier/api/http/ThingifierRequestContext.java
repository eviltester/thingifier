package uk.co.compendiumdev.thingifier.api.http;

import static uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi.HTTP_SESSION_HEADER_NAME;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.security.SecuritySchemeNames;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

/**
 * Request-scoped Thingifier runtime context.
 *
 * <p>The context selects the active store for a request, preserves the headers used to make that
 * decision, and carries authenticated principals produced by API-spec auth policy. It is shared by
 * direct and HTTP request handling so both paths see the same store and security state.
 */
public final class ThingifierRequestContext {

    private final String databaseName;
    private final ThingStore store;
    private final HttpHeadersBlock headers;
    private final Map<String, Object> authenticatedPrincipals;

    /**
     * Creates a request context for one selected store.
     *
     * @param databaseName active database/store name
     * @param store active Thingifier store
     * @param headers request headers used to create the context
     */
    private ThingifierRequestContext(
            final String databaseName, final ThingStore store, final HttpHeadersBlock headers) {
        this.databaseName = databaseName;
        this.store = store;
        this.headers = headers;
        this.authenticatedPrincipals = new HashMap<>();
    }

    /**
     * Creates a request context from request headers.
     *
     * <p>The session header selects a per-session store. When no session header is present,
     * Thingifier uses the default store.
     *
     * @param thingifier model and store owner
     * @param requestHeaders request headers, possibly null
     * @return request context for the selected store
     */
    public static ThingifierRequestContext from(
            final Thingifier thingifier, final HttpHeadersBlock requestHeaders) {
        HttpHeadersBlock safeHeaders =
                requestHeaders == null ? new HttpHeadersBlock() : requestHeaders;
        String databaseName = databaseNameFrom(safeHeaders);
        thingifier.ensureCreatedAndPopulatedInstanceDatabaseNamed(databaseName);
        return new ThingifierRequestContext(
                databaseName, thingifier.getStore(databaseName), safeHeaders);
    }

    /**
     * Resolves the database name from the Thingifier session header.
     *
     * @param requestHeaders request headers
     * @return selected database/store name
     */
    private static String databaseNameFrom(final HttpHeadersBlock requestHeaders) {
        String sessionHeaderValue = requestHeaders.get(HTTP_SESSION_HEADER_NAME);
        if (sessionHeaderValue.isEmpty()) {
            return EntityRelModel.DEFAULT_DATABASE_NAME;
        }
        return sessionHeaderValue;
    }

    /**
     * Returns the active database/store name.
     *
     * @return database name
     */
    public String databaseName() {
        return databaseName;
    }

    /**
     * Returns the active store for the request.
     *
     * @return request store
     */
    public ThingStore store() {
        return store;
    }

    /**
     * Reports whether an entity instance exists with a route/query identifier.
     *
     * @param entity entity definition to search
     * @param identifier route or query identifier
     * @return true when a matching entity instance exists
     */
    public boolean hasEntityInstanceWithIdentifier(
            final EntityDefinition entity, final String identifier) {
        if (entity == null) {
            return false;
        }
        EntityInstance found = store.entityQueries().findByQueryIdentifier(entity, identifier);
        return found != null;
    }

    /**
     * Returns the request headers associated with this context.
     *
     * @return header block used by the request
     */
    public HttpHeadersBlock headers() {
        return headers;
    }

    /**
     * Stores an authenticated principal for a named security scheme.
     *
     * <p>Applications can retrieve this later from lifecycle hooks or direct handler code when
     * route-level auth has already established a principal.
     *
     * @param schemeName security scheme name
     * @param principal authenticated principal object
     */
    public void setAuthenticatedPrincipal(final String schemeName, final Object principal) {
        authenticatedPrincipals.put(SecuritySchemeNames.requireValid(schemeName), principal);
    }

    /**
     * Returns the authenticated principal for a named security scheme.
     *
     * @param schemeName security scheme name
     * @return principal object, or null when no principal has been stored
     */
    public Object authenticatedPrincipal(final String schemeName) {
        return authenticatedPrincipals.get(SecuritySchemeNames.requireValid(schemeName));
    }

    /**
     * Reports whether a named security scheme has authenticated this request.
     *
     * @param schemeName security scheme name
     * @return true when the scheme has an authenticated principal entry
     */
    public boolean hasAuthenticatedPrincipal(final String schemeName) {
        return authenticatedPrincipals.containsKey(SecuritySchemeNames.requireValid(schemeName));
    }

    /**
     * Returns all authenticated principals by scheme name.
     *
     * @return immutable copy of authenticated principal entries
     */
    public Map<String, Object> authenticatedPrincipals() {
        return Collections.unmodifiableMap(new HashMap<>(authenticatedPrincipals));
    }
}
