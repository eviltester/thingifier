package uk.co.compendiumdev.thingifier.api.security;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Declares named security schemes used by a Thingifier API specification.
 *
 * <p>The security spec lets API authors give authentication schemes meaningful names, such as
 * {@code cartToken} or {@code adminPassword}, so generated OpenAPI documents and runtime
 * enforcement can refer to the same policy without hard-coding application behaviour in Thingifier.
 */
public final class ThingifierApiSecuritySpec {

    /** Default Basic realm used when a scheme has not configured one. */
    public static final String DEFAULT_BASIC_REALM = "Thingifier";

    private final Set<String> bearerSchemes;
    private final Map<String, String> basicRealms;

    /** Creates an empty security declaration set. */
    public ThingifierApiSecuritySpec() {
        bearerSchemes = new LinkedHashSet<>();
        basicRealms = new LinkedHashMap<>();
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
     * Declares a named Basic authentication scheme using the default realm.
     *
     * @param schemeName name to use in API spec route rules and OpenAPI security schemes
     * @return this security spec so declarations can be chained
     */
    public ThingifierApiSecuritySpec basic(final String schemeName) {
        return basic(schemeName, DEFAULT_BASIC_REALM);
    }

    /**
     * Declares a named Basic authentication scheme and challenge realm.
     *
     * <p>The realm is visible in the {@code WWW-Authenticate} challenge when Thingifier rejects a
     * missing or malformed Basic header before application authentication runs. Blank realms fall
     * back to the default so the framework does not emit an empty user-facing prompt.
     *
     * @param schemeName name to use in API spec route rules and OpenAPI security schemes
     * @param realm Basic realm to advertise in default challenges
     * @return this security spec so declarations can be chained
     */
    public ThingifierApiSecuritySpec basic(final String schemeName, final String realm) {
        basicRealms.put(SecuritySchemeNames.requireValid(schemeName), normalizedRealm(realm));
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
     * Reports whether the named Basic scheme has been declared.
     *
     * @param schemeName security scheme name
     * @return true when a matching Basic scheme is known
     */
    public boolean hasBasic(final String schemeName) {
        return basicRealms.containsKey(SecuritySchemeNames.requireValid(schemeName));
    }

    /**
     * Returns all declared bearer scheme names in declaration order.
     *
     * @return immutable set of bearer scheme names
     */
    public Set<String> bearerSchemes() {
        return Collections.unmodifiableSet(bearerSchemes);
    }

    /**
     * Returns all declared Basic scheme names in declaration order.
     *
     * @return immutable set of Basic scheme names
     */
    public Set<String> basicSchemes() {
        return Collections.unmodifiableSet(basicRealms.keySet());
    }

    /**
     * Returns the Basic realm for a named scheme.
     *
     * <p>Routes may enforce a named Basic scheme without an explicit security declaration. In that
     * case the default realm keeps runtime challenges predictable.
     *
     * @param schemeName security scheme name
     * @return configured realm, or the default realm when no explicit declaration exists
     */
    public String basicRealm(final String schemeName) {
        return basicRealms.getOrDefault(
                SecuritySchemeNames.requireValid(schemeName), DEFAULT_BASIC_REALM);
    }

    private String normalizedRealm(final String realm) {
        if (realm == null || realm.trim().isEmpty()) {
            return DEFAULT_BASIC_REALM;
        }
        return realm.trim();
    }
}
