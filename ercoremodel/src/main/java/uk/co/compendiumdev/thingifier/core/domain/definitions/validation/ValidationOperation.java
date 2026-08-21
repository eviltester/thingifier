package uk.co.compendiumdev.thingifier.core.domain.definitions.validation;

/**
 * Write operation being validated by custom validators.
 *
 * <p>Validators receive the operation so one rule can distinguish new instances from partial
 * amendment and full replacement without guessing from the shape of the candidate data.
 */
public enum ValidationOperation {
    /** Candidate is being inserted as a new instance. */
    CREATE,
    /** Candidate is the result of applying a partial amendment to an existing instance. */
    AMEND,
    /** Candidate is the result of replacing an existing instance's fields. */
    REPLACE
}
