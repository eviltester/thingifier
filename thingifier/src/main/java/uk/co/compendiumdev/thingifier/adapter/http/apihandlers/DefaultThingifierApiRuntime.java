package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.List;
import java.util.Optional;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.security.DataScopeCreationPolicy;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiSpec;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.ThingCommandService;
import uk.co.compendiumdev.thingifier.application.ThingQueryService;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

public final class DefaultThingifierApiRuntime implements ThingifierApiRuntime {

    private final Thingifier thingifier;
    private final SchemaCatalog schema;
    private final ThingQueryService queryService;

    public DefaultThingifierApiRuntime(final Thingifier thingifier) {
        this.thingifier = thingifier;
        this.schema = new ThingifierSchemaCatalog(thingifier);
        this.queryService = new ThingQueryService(schema);
    }

    @Override
    public SchemaCatalog schema() {
        return schema;
    }

    @Override
    public ThingifierApiConfig apiConfig() {
        return thingifier.apiConfig();
    }

    @Override
    public ThingifierApiSpec apiSpec() {
        return thingifier.apiSpec();
    }

    @Override
    public List<String> thingNames() {
        return thingifier.getThingNames();
    }

    @Override
    public ThingifierRequestContext contextFrom(final HttpHeadersBlock requestHeaders) {
        return ThingifierRequestContext.from(thingifier, requestHeaders);
    }

    @Override
    public Optional<ThingStore> storeForDataScope(
            final String dataScopeName, final DataScopeCreationPolicy creationPolicy) {
        final DataScopeCreationPolicy policy =
                creationPolicy == null ? DataScopeCreationPolicy.USE_EXISTING_ONLY : creationPolicy;

        switch (policy) {
            case ENSURE_EXISTS:
                thingifier.getERmodel().createInstanceDatabaseIfNotExisting(dataScopeName);
                break;
            case ENSURE_CREATED_AND_POPULATED:
                thingifier.ensureCreatedAndPopulatedInstanceDatabaseNamed(dataScopeName);
                break;
            case USE_EXISTING_ONLY:
            default:
                break;
        }

        return Optional.ofNullable(thingifier.getStore(dataScopeName));
    }

    @Override
    public ThingCommandService commandService(final ThingifierRequestContext context) {
        return new ThingCommandService(
                context.store(),
                schema,
                thingifier.apiConfig().willApiEnforceDeclaredTypesInInput(),
                thingifier.apiConfig().jsonOutput());
    }

    @Override
    public ThingQueryService queryService() {
        return queryService;
    }
}
