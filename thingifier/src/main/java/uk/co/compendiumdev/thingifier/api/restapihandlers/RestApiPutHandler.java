package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.ThingCommandResult;
import uk.co.compendiumdev.thingifier.application.ThingCommandService;

public class RestApiPutHandler {
    final Thingifier thingifier;

    public RestApiPutHandler(final Thingifier aThingifier) {
        thingifier = aThingifier;
    }

    public ApiResponse handle(
            final String url, final BodyParser args, final HttpHeadersBlock requestHeaders) {
        return handle(url, args, ThingifierRequestContext.from(thingifier, requestHeaders));
    }

    public ApiResponse handle(
            final String url, final BodyParser args, final ThingifierRequestContext context) {
        ThingWriteRequestMapping mapping =
                new ThingWriteRequestMapper(thingifier, context.store()).mapPut(url, args);
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
