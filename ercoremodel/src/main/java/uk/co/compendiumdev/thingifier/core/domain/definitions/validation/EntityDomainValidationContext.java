package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import uk.co.compendiumdev.thingifier.core.domain.definitions.ERSchema;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

/**
 * Context passed to an entity domain validator.
 *
 * <p>Entity domain validation is attached to a single entity definition, but it can still inspect
 * the wider schema and active store. This exists for rules such as "a task title must be unique
 * within its project" where the rule belongs to the task entity but needs relationship or other
 * stored data to make the decision.
 */
public final class EntityDomainValidationContext {

    private final ERSchema schema;
    private final ThingStore store;
    private final EntityInstance candidate;
    private final EntityInstance existing;
    private final ValidationOperation operation;

    /**
     * Creates an immutable validation context for one candidate write.
     *
     * @param schema active schema for resolving entities and relationships
     * @param store active store, including existing persisted data
     * @param candidate fully materialized instance about to be written
     * @param existing current persisted instance for amend/replace, or null for create
     * @param operation write operation being validated
     */
    public EntityDomainValidationContext(
            final ERSchema schema,
            final ThingStore store,
            final EntityInstance candidate,
            final EntityInstance existing,
            final ValidationOperation operation) {
        this.schema = schema;
        this.store = store;
        this.candidate = candidate;
        this.existing = existing;
        this.operation = operation;
    }

    /**
     * Active schema for resolving definitions and relationships.
     *
     * @return active schema
     */
    public ERSchema schema() {
        return schema;
    }

    /**
     * Active store for querying persisted instances and relationships.
     *
     * @return active store
     */
    public ThingStore store() {
        return store;
    }

    /**
     * Candidate instance after create IDs have been prepared or amend/replace values materialized.
     *
     * @return candidate instance
     */
    public EntityInstance candidate() {
        return candidate;
    }

    /**
     * Existing persisted instance for amend/replace operations.
     *
     * @return existing instance, or null for create
     */
    public EntityInstance existing() {
        return existing;
    }

    /**
     * Operation being validated.
     *
     * @return validation operation
     */
    public ValidationOperation operation() {
        return operation;
    }
}
