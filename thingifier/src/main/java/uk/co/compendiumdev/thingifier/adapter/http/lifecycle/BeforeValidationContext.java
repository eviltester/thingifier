package uk.co.compendiumdev.thingifier.adapter.http.lifecycle;

/**
 * Context view exposed after a read query or write command has been mapped and before validation.
 *
 * <p>Hooks at this phase are intended for explicit input shaping, such as replacing a mapped write
 * command so Thingifier validation evaluates the amended command.
 */
public interface BeforeValidationContext extends ThingifierApiLifecycleContextView {}
