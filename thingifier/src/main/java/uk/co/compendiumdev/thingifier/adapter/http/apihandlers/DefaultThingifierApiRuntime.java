package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.List;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiSpec;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.ThingCommandService;
import uk.co.compendiumdev.thingifier.application.ThingQueryService;

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
