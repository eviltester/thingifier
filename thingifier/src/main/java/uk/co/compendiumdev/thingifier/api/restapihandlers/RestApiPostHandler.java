package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.ThingCommandResult;
import uk.co.compendiumdev.thingifier.application.ThingCommandService;

public class RestApiPostHandler {
    private final Thingifier thingifier;

    public RestApiPostHandler(final Thingifier aThingifier) {
        thingifier = aThingifier;
    }

    public ApiResponse handle(
            final String url, final BodyParser args, final HttpHeadersBlock requestHeaders) {
        String instanceDatabaseName =
                SessionHeaderParser.getDatabaseNameFromHeaderValue(requestHeaders);

        ThingWriteRequestMapping mapping =
                new ThingWriteRequestMapper(thingifier, instanceDatabaseName).mapPost(url, args);
        if (mapping.isError()) {
            return mapping.getErrorResponse();
        }

        ThingCommandResult result =
                new ThingCommandService(thingifier.getStore(instanceDatabaseName))
                        .execute(mapping.getCommand());
        return new ThingCommandResultApiMapper(thingifier.apiConfig())
                .map(mapping.getCommand(), result);
    }
}
