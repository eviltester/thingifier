package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.Optional;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.security.DataScopeCreationPolicy;
import uk.co.compendiumdev.thingifier.api.security.ThingifierApiDataScopeSelection;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

/**
 * Applies trusted data-scope selections to the shared request context.
 *
 * <p>Route auth and scoped sessions both use this so their scope-switching rules stay identical.
 * The selection is trusted because caller code only passes decisions returned by an authenticator
 * or scoped-session resolver.
 */
final class DataScopeSelectionApplier {

    private final ThingifierApiRuntime runtime;

    DataScopeSelectionApplier(final ThingifierApiRuntime runtime) {
        this.runtime = runtime;
    }

    /**
     * Switches the request context to the selected data scope.
     *
     * @param context active request context to update
     * @param selection trusted data-scope selection
     * @return error response when the selected scope cannot be resolved, otherwise null
     */
    ApiResponse apply(
            final ThingifierRequestContext context,
            final ThingifierApiDataScopeSelection selection) {
        if (selection == null) {
            return null;
        }

        if (requiresPreExistingScope(context, selection)) {
            return ApiResponse.error404("Could not find data scope " + selection.dataScopeName());
        }

        final Optional<ThingStore> selectedStore =
                runtime.storeForDataScope(selection.dataScopeName(), selection.creationPolicy());
        if (selectedStore.isEmpty()) {
            return ApiResponse.error404("Could not find data scope " + selection.dataScopeName());
        }

        if (requiresEmptyScopeAfterHeaderCreation(context, selection)) {
            selectedStore.get().administration().clearAllData();
        }
        context.useDataScope(selection.dataScopeName(), selectedStore.get());
        return null;
    }

    private boolean requiresPreExistingScope(
            final ThingifierRequestContext context,
            final ThingifierApiDataScopeSelection selection) {
        return selection.creationPolicy() == DataScopeCreationPolicy.USE_EXISTING_ONLY
                && selection.dataScopeName().equals(context.dataScopeName())
                && context.wasDataScopeCreatedWhenContextCreated();
    }

    private boolean requiresEmptyScopeAfterHeaderCreation(
            final ThingifierRequestContext context,
            final ThingifierApiDataScopeSelection selection) {
        return selection.creationPolicy() == DataScopeCreationPolicy.ENSURE_EXISTS
                && selection.dataScopeName().equals(context.dataScopeName())
                && context.wasDataScopeCreatedWhenContextCreated();
    }
}
