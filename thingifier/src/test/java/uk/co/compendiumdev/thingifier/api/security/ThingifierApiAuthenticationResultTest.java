package uk.co.compendiumdev.thingifier.api.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;

class ThingifierApiAuthenticationResultTest {

    @Test
    void authenticatedResultHasNoDataScopeSelectionByDefault() {
        final ThingifierApiAuthenticationResult result =
                ThingifierApiAuthenticationResult.authenticated("principal");

        Assertions.assertTrue(result.dataScopeSelection().isEmpty());
    }

    @Test
    void namedDataScopeUsesExistingOnlyPolicyByDefault() {
        final ThingifierApiAuthenticationResult result =
                ThingifierApiAuthenticationResult.authenticated("principal")
                        .useDataScope("tenant-one");

        final ThingifierApiDataScopeSelection selection = result.dataScopeSelection().get();
        Assertions.assertEquals("tenant-one", selection.dataScopeName());
        Assertions.assertEquals(
                DataScopeCreationPolicy.USE_EXISTING_ONLY, selection.creationPolicy());
        Assertions.assertTrue(selection.isExplicit());
    }

    @Test
    void namedDataScopeRecordsCreationPolicy() {
        final ThingifierApiAuthenticationResult result =
                ThingifierApiAuthenticationResult.authenticated("principal")
                        .useDataScope(
                                "tenant-one", DataScopeCreationPolicy.ENSURE_CREATED_AND_POPULATED);

        Assertions.assertEquals(
                DataScopeCreationPolicy.ENSURE_CREATED_AND_POPULATED,
                result.dataScopeSelection().get().creationPolicy());
    }

    @Test
    void defaultDataScopeExplicitlySelectsDefaultStore() {
        final ThingifierApiAuthenticationResult result =
                ThingifierApiAuthenticationResult.authenticated("principal").useDefaultDataScope();

        final ThingifierApiDataScopeSelection selection = result.dataScopeSelection().get();
        Assertions.assertEquals(EntityRelModel.DEFAULT_DATABASE_NAME, selection.dataScopeName());
        Assertions.assertEquals(
                DataScopeCreationPolicy.USE_EXISTING_ONLY, selection.creationPolicy());
        Assertions.assertTrue(selection.isExplicit());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
            strings = {
                " ",
                "\t",
                "tenant one",
                "tenant/one",
                "tenant\\one",
                "tenant\none",
                "tenanté"
            })
    void namedDataScopeRejectsUnsafeNames(final String dataScopeName) {
        final ThingifierApiAuthenticationResult result =
                ThingifierApiAuthenticationResult.authenticated("principal");

        Assertions.assertThrows(
                IllegalArgumentException.class, () -> result.useDataScope(dataScopeName));
    }

    @Test
    void rejectedResultCannotSelectDataScope() {
        final ThingifierApiAuthenticationResult result =
                ThingifierApiAuthenticationResult.rejected(403, "Forbidden");

        Assertions.assertThrows(IllegalStateException.class, result::useDefaultDataScope);
    }
}
