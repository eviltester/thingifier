package uk.co.compendiumdev.thingifier.adapter.http.lifecycle;

/**
 * Context view exposed immediately before Thingifier executes a read query or write command.
 *
 * <p>Hooks at this phase are the last chance to replace the command or query before persistence or
 * repository read behavior occurs.
 */
public interface BeforeActionContext extends ThingifierApiLifecycleContextView {}
