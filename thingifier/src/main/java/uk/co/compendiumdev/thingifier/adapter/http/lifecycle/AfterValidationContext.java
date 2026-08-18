package uk.co.compendiumdev.thingifier.adapter.http.lifecycle;

/**
 * Context view exposed after Thingifier has validated a mapped read query or write command.
 *
 * <p>Hooks at this phase can inspect, replace, reject, or clear validation results before the
 * action phase decides whether the command or query should execute.
 */
public interface AfterValidationContext extends ThingifierApiLifecycleContextView {}
