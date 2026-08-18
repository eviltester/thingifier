package uk.co.compendiumdev.thingifier.api.security;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Declares named security schemes used by a Thingifier API specification.
 *
 * <p>The security spec is intentionally small at the moment. It lets API authors give bearer
 * schemes meaningful names, such as {@code cartToken}, so generated OpenAPI documents and runtime
 * enforcement can refer to the same policy without hard-coding application behaviour in Thingifier.
 */
public final class ThingifierApiSecuritySpec {

    private final Set<String> bearerSchemes;

    /** Creates an empty security declaration set. */
    public ThingifierApiSecuritySpec() {
        bearerSchemes = new LinkedHashSet<>();
    }

    /**
     * Declares a named bearer authentication scheme.
     *
     * @param schemeName name to use in API spec route rules and OpenAPI security schemes
     * @return this security spec so declarations can be chained
     */
    public ThingifierApiSecuritySpec bearer(final String schemeName) {
        bearerSchemes.add(SecuritySchemeNames.requireValid(schemeName));
        return this;
    }

    /**
     * Reports whether the named bearer scheme has been declared.
     *
     * @param schemeName security scheme name
     * @return true when a matching bearer scheme is known
     */
    public boolean hasBearer(final String schemeName) {
        return bearerSchemes.contains(SecuritySchemeNames.requireValid(schemeName));
    }

    /**
     * Returns all declared bearer scheme names in declaration order.
     *
     * @return immutable set of bearer scheme names
     */
    public Set<String> bearerSchemes() {
        return Collections.unmodifiableSet(bearerSchemes);
    }
}
