package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

/**
 * Context passed to an instance validator.
 *
 * <p>This context deliberately contains only the candidate, existing instance, and operation. It
 * keeps instance validation focused on consistency within one instance rather than allowing hidden
 * dependencies on repository state.
 */
public final class InstanceValidationContext {

    private final EntityInstance candidate;
    private final EntityInstance existing;
    private final ValidationOperation operation;

    /**
     * Creates an immutable context for one instance-level validation.
     *
     * @param candidate fully materialized instance about to be written
     * @param existing current persisted instance for amend/replace, or null for create
     * @param operation write operation being validated
     */
    public InstanceValidationContext(
            final EntityInstance candidate,
            final EntityInstance existing,
            final ValidationOperation operation) {
        this.candidate = candidate;
        this.existing = existing;
        this.operation = operation;
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
