package uk.co.compendiumdev.thingifier.adapter.http.apihandlers;

import java.util.List;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiSpec;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.ThingCommandService;
import uk.co.compendiumdev.thingifier.application.ThingQueryService;

public interface ThingifierApiRuntime {

    SchemaCatalog schema();

    ThingifierApiConfig apiConfig();

    ThingifierApiSpec apiSpec();

    List<String> thingNames();

    ThingifierRequestContext contextFrom(HttpHeadersBlock requestHeaders);

    ThingCommandService commandService(ThingifierRequestContext context);

    ThingQueryService queryService();
}
