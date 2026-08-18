package uk.co.compendiumdev.thingifier.apiconfig;

/**
 * Describes the relationship write operations that a generated API route may accept.
 *
 * <p>These values are used by {@code ThingifierApiConfig} and route-level API spec rules to keep
 * generated documentation, direct API calls, and HTTP handling aligned. Relationship POST defaults
 * to creating new children or connecting existing ones; updating an already connected child is
 * explicit opt-in.
 */
public enum RelationshipWriteOperation {
    /** Create a new target entity and connect it to the relationship source. */
    CREATE_AND_CONNECT,

    /** Connect an existing target entity without changing the target's fields. */
    CONNECT_EXISTING,

    /** Partially update an existing target entity that is already connected. */
    UPDATE_CONNECTED,

    /** Remove the connection between source and target entities. */
    DISCONNECT
}
