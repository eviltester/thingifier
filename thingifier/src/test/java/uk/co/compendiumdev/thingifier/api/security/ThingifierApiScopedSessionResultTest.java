package uk.co.compendiumdev.thingifier.api.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;

class ThingifierApiScopedSessionResultTest {

    @Test
    void authenticatedResultHasNoDataScopeSelectionByDefault() {
        final ThingifierApiScopedSessionResult result =
                ThingifierApiScopedSessionResult.authenticated("principal");

        Assertions.assertTrue(result.isAuthenticated());
        Assertions.assertEquals("principal", result.principal());
        Assertions.assertTrue(result.dataScopeSelection().isEmpty());
    }

    @Test
    void authenticatedResultCanSelectNamedDataScope() {
        final ThingifierApiScopedSessionResult result =
                ThingifierApiScopedSessionResult.authenticated("principal")
                        .useDataScope("tenant-one", DataScopeCreationPolicy.ENSURE_EXISTS);

        final ThingifierApiDataScopeSelection selection = result.dataScopeSelection().orElseThrow();
        Assertions.assertEquals("tenant-one", selection.dataScopeName());
        Assertions.assertEquals(DataScopeCreationPolicy.ENSURE_EXISTS, selection.creationPolicy());
    }

    @Test
    void authenticatedResultCanSelectDefaultDataScope() {
        final ThingifierApiScopedSessionResult result =
                ThingifierApiScopedSessionResult.authenticated("principal").useDefaultDataScope();

        final ThingifierApiDataScopeSelection selection = result.dataScopeSelection().orElseThrow();
        Assertions.assertEquals(EntityRelModel.DEFAULT_DATABASE_NAME, selection.dataScopeName());
        Assertions.assertTrue(selection.isExplicit());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "../tenant", "tenant/one", "tenant one"})
    void namedDataScopeRejectsUnsafeNames(final String dataScopeName) {
        final ThingifierApiScopedSessionResult result =
                ThingifierApiScopedSessionResult.authenticated("principal");

        Assertions.assertThrows(
                IllegalArgumentException.class, () -> result.useDataScope(dataScopeName));
    }

    @Test
    void unauthenticatedResultCannotSelectDataScope() {
        final ThingifierApiScopedSessionResult result =
                ThingifierApiScopedSessionResult.unauthenticated();

        Assertions.assertThrows(IllegalStateException.class, result::useDefaultDataScope);
    }
}
