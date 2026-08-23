package uk.co.compendiumdev.thingifier.api.spec;

/**
 * Declares how a route wants successful entity responses to be represented.
 *
 * <p>Thingifier normally derives response shape from the generated route and legacy API
 * configuration. Route-level response shape is an explicit public contract override for endpoints
 * such as fixed-resource routes where the URL represents one known instance even though the
 * generated model still supports collection-style access elsewhere.
 */
public enum ResponseShape {
    /** Preserve the shape that normal generated Thingifier routing would have produced. */
    DEFAULT,

    /** Require the successful response to contain one persisted entity instance. */
    SINGLE_INSTANCE,

    /** Require the successful response to be represented as a collection of entity instances. */
    COLLECTION
}
