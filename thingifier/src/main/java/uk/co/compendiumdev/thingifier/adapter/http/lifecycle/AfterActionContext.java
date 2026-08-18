package uk.co.compendiumdev.thingifier.adapter.http.lifecycle;

/**
 * Context view exposed after Thingifier has executed a read query or write command.
 *
 * <p>Hooks at this phase can inspect command/query results and replace the final {@code
 * ApiResponse} before legacy HTTP response hooks and content rendering run.
 */
public interface AfterActionContext extends ThingifierApiLifecycleContextView {}
