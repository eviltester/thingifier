package uk.co.compendiumdev.thingifier.adapter.http.lifecycle;

/**
 * Context view exposed after a dynamic Thingifier API route has been matched.
 *
 * <p>At this phase hooks can inspect route, path, headers, query parameters, and target entity
 * information before request syntax and entity-view checks continue.
 */
public interface RouteMatchedContext extends ThingifierApiLifecycleContextView {}
