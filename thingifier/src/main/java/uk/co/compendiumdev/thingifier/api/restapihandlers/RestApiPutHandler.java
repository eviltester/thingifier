package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.DefaultThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingCommandResultApiMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingWriteRequestMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingWriteRequestMapping;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.WriteMethodPolicy;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRouteMapper;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.ThingCommandResult;

public class RestApiPutHandler {
    private final ThingifierApiRuntime runtime;

    public RestApiPutHandler(final Thingifier aThingifier) {
        this(new DefaultThingifierApiRuntime(aThingifier));
    }

    public RestApiPutHandler(final ThingifierApiRuntime runtime) {
        this.runtime = runtime;
    }

    public ApiResponse handle(
            final String url, final BodyParser args, final HttpHeadersBlock requestHeaders) {
        return handle(url, args.bodyFields(), runtime.contextFrom(requestHeaders));
    }

    public ApiResponse handle(
            final String url, final BodyParser args, final ThingifierRequestContext context) {
        return handle(url, args.bodyFields(), context);
    }

    public ApiResponse handle(
            final String url,
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context) {
        ThingRoute route = new ThingRouteMapper(runtime.schema()).map(url);
        ApiResponse policyResponse =
                new WriteMethodPolicy(runtime)
                        .rejectIfNotAllowed(RoutingVerb.PUT, route, bodyFields, context);
        if (policyResponse != null) {
            return policyResponse;
        }

        ThingWriteRequestMapping mapping =
                new ThingWriteRequestMapper(runtime.schema()).mapPut(route, bodyFields);
        ThingCommandResultApiMapper apiMapper =
                new ThingCommandResultApiMapper(runtime.apiConfig());
        if (mapping.isError()) {
            return apiMapper.map(mapping.getError());
        }

        ThingCommandResult result = runtime.commandService(context).execute(mapping.getCommand());
        return apiMapper.map(mapping, result);
    }
}
