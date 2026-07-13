package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.ThingCommandResult;
import uk.co.compendiumdev.thingifier.application.ThingCommandService;

public class RestApiDeleteHandler {
    private final Thingifier thingifier;

    public RestApiDeleteHandler(final Thingifier aThingifier) {
        this.thingifier = aThingifier;
    }

    public ApiResponse handle(final String url, HttpHeadersBlock requestHeaders) {
        return handle(url, ThingifierRequestContext.from(thingifier, requestHeaders));
    }

    public ApiResponse handle(final String url, final ThingifierRequestContext context) {
        ThingWriteRequestMapping mapping =
                new ThingWriteRequestMapper(thingifier, context.store()).mapDelete(url);
        ThingCommandResultApiMapper apiMapper =
                new ThingCommandResultApiMapper(thingifier.apiConfig());
        if (mapping.isError()) {
            return apiMapper.map(mapping.getError());
        }

        ThingCommandResult result =
                new ThingCommandService(context.store()).execute(mapping.getCommand());
        return apiMapper.map(mapping.getCommand(), result);
    }
}
