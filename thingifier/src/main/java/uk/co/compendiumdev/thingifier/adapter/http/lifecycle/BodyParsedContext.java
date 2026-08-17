package uk.co.compendiumdev.thingifier.adapter.http.lifecycle;

/**
 * Context view exposed after request body and query data have been parsed.
 *
 * <p>Hooks at this phase can replace parsed body fields, raw body text, query parameters, or query
 * body format before Thingifier maps the request into a read query or write command.
 */
public interface BodyParsedContext extends ThingifierApiLifecycleContextView {}
