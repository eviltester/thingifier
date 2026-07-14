package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.DefaultThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingCommandResultApiMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingWriteRequestMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingWriteRequestMapping;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRouteMapper;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.ThingCommandResult;

public class RestApiDeleteHandler {
    private final ThingifierApiRuntime runtime;

    public RestApiDeleteHandler(final Thingifier aThingifier) {
        this(new DefaultThingifierApiRuntime(aThingifier));
    }

    public RestApiDeleteHandler(final ThingifierApiRuntime runtime) {
        this.runtime = runtime;
    }

    public ApiResponse handle(final String url, HttpHeadersBlock requestHeaders) {
        return handle(url, runtime.contextFrom(requestHeaders));
    }

    public ApiResponse handle(final String url, final ThingifierRequestContext context) {
        ThingWriteRequestMapping mapping =
                new ThingWriteRequestMapper(runtime.schema())
                        .mapDelete(new ThingRouteMapper(runtime.schema()).map(url));
        ThingCommandResultApiMapper apiMapper =
                new ThingCommandResultApiMapper(runtime.apiConfig());
        if (mapping.isError()) {
            return apiMapper.map(mapping.getError());
        }

        ThingCommandResult result = runtime.commandService(context).execute(mapping.getCommand());
        return apiMapper.map(mapping, result);
    }
}
