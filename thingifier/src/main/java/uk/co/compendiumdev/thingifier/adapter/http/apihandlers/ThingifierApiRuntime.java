package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.List;
import java.util.Optional;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.security.DataScopeCreationPolicy;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiSpec;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.ThingCommandService;
import uk.co.compendiumdev.thingifier.application.ThingQueryService;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public interface ThingifierApiRuntime {

    SchemaCatalog schema();

    ThingifierApiConfig apiConfig();

    ThingifierApiSpec apiSpec();

    List<String> thingNames();

    ThingifierRequestContext contextFrom(HttpHeadersBlock requestHeaders);

    /**
     * Resolves the store backing a trusted auth-selected data scope.
     *
     * <p>The runtime owns this because different Thingifier deployments can back data scopes with
     * different store providers. Auth policy supplies only the trusted scope name and creation
     * policy returned by application authentication code.
     *
     * @param dataScopeName trusted data-scope name
     * @param creationPolicy policy for missing data scopes
     * @return resolved store, or empty when the policy requires an existing scope and none exists
     */
    Optional<ThingStore> storeForDataScope(
            String dataScopeName, DataScopeCreationPolicy creationPolicy);

    ThingCommandService commandService(ThingifierRequestContext context);

    ThingQueryService queryService();
}
