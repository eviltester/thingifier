package uk.co.compendiumdev.thingifier.api.security;

import uk.co.compendiumdev.thingifier.core.EntityRelModel;

/**
 * Trusted data-scope decision returned by an authenticator.
 *
 * <p>Thingifier never infers this from arbitrary request input. An authenticator may inspect
 * headers, route parameters, tokens, claims, or credentials, but this object is only created after
 * application code has decided that those inputs identify a scope the request may use.
 */
public final class ThingifierApiDataScopeSelection {

    private final String dataScopeName;
    private final DataScopeCreationPolicy creationPolicy;
    private final boolean explicit;

    private ThingifierApiDataScopeSelection(
            final String dataScopeName,
            final DataScopeCreationPolicy creationPolicy,
            final boolean explicit) {
        this.dataScopeName = dataScopeName;
        this.creationPolicy =
                creationPolicy == null ? DataScopeCreationPolicy.USE_EXISTING_ONLY : creationPolicy;
        this.explicit = explicit;
    }

    /**
     * Creates an explicit selection for the named data scope.
     *
     * @param dataScopeName data scope name chosen by trusted application auth code
     * @param creationPolicy missing-scope handling policy
     * @return immutable data-scope selection
     * @throws IllegalArgumentException when the name is blank or not portable across store
     *     providers
     */
    public static ThingifierApiDataScopeSelection named(
            final String dataScopeName, final DataScopeCreationPolicy creationPolicy) {
        return new ThingifierApiDataScopeSelection(
                requireValidDataScopeName(dataScopeName), creationPolicy, true);
    }

    /**
     * Creates an explicit selection for the model's default data scope.
     *
     * <p>This intentionally overrides any request header or session-selected scope.
     *
     * @return immutable default data-scope selection
     */
    public static ThingifierApiDataScopeSelection defaultDataScope() {
        return new ThingifierApiDataScopeSelection(
                EntityRelModel.DEFAULT_DATABASE_NAME,
                DataScopeCreationPolicy.USE_EXISTING_ONLY,
                true);
    }

    /**
     * Returns the selected data scope name.
     *
     * @return selected scope name
     */
    public String dataScopeName() {
        return dataScopeName;
    }

    /**
     * Returns the missing-scope policy requested by the authenticator.
     *
     * @return creation policy
     */
    public DataScopeCreationPolicy creationPolicy() {
        return creationPolicy;
    }

    /**
     * Reports whether application code explicitly selected this scope.
     *
     * @return true when an authenticator chose the data scope
     */
    public boolean isExplicit() {
        return explicit;
    }

    private static String requireValidDataScopeName(final String dataScopeName) {
        if (dataScopeName == null || dataScopeName.trim().isEmpty()) {
            throw new IllegalArgumentException("dataScopeName is required");
        }

        final String normalized = dataScopeName.trim();
        for (int index = 0; index < normalized.length(); index++) {
            if (!isPortableDataScopeNameCharacter(normalized.charAt(index))) {
                throw new IllegalArgumentException(
                        "dataScopeName may only contain letters, numbers, '.', '_' and '-'");
            }
        }
        return normalized;
    }

    private static boolean isPortableDataScopeNameCharacter(final char character) {
        return isAsciiLetterOrDigit(character)
                || character == '.'
                || character == '_'
                || character == '-';
    }

    private static boolean isAsciiLetterOrDigit(final char character) {
        return (character >= 'A' && character <= 'Z')
                || (character >= 'a' && character <= 'z')
                || (character >= '0' && character <= '9');
    }
}
