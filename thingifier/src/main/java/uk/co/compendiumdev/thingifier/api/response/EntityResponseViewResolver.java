package uk.co.compendiumdev.thingifier.api.response;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityViewDefinition;

/**
 * Resolves the response view to use for each entity rendered in an {@link ApiResponse}.
 *
 * <p>A resolver is used when the correct response view depends on the entity being serialized, such
 * as entity-level API defaults on relationship routes. The older single-view response setting is
 * still useful when one explicit route view should be applied to every rendered instance.
 */
public interface EntityResponseViewResolver {

    /**
     * Finds the response view that should be applied to the supplied entity.
     *
     * @param entity entity currently being serialized
     * @return entity view to apply, or null when the entity should render with its full model shape
     */
    EntityViewDefinition viewFor(EntityDefinition entity);
}
