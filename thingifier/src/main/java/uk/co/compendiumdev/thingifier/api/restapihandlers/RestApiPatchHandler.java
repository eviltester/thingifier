package uk.co.compendiumdev.thingifier.api.restapihandlers;

import java.util.Optional;
import java.util.Set;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.DefaultThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.EntityPatchDocumentMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingCommandResultApiMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingWriteRequestMapping;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.WriteMethodPolicy;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.InstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRouteMapper;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle;
import uk.co.compendiumdev.thingifier.application.ThingCommandResult;

public class RestApiPatchHandler {
    private final ThingifierApiRuntime runtime;

    public RestApiPatchHandler(final Thingifier aThingifier) {
        this(new DefaultThingifierApiRuntime(aThingifier));
    }

    public RestApiPatchHandler(final ThingifierApiRuntime runtime) {
        this.runtime = runtime;
    }

    public ApiResponse handle(
            final String url, final BodyParser args, final HttpHeadersBlock requestHeaders) {
        return handle(url, args.rawBody(), requestHeaders, runtime.contextFrom(requestHeaders));
    }

    public ApiResponse handle(
            final String url, final BodyParser args, final ThingifierRequestContext context) {
        return handle(url, args.rawBody(), new HttpHeadersBlock(), context);
    }

    public ApiResponse handle(
            final String url,
            final String rawBody,
            final HttpHeadersBlock requestHeaders,
            final ThingifierRequestContext context) {
        ThingRoute route = new ThingRouteMapper(runtime.schema()).map(url);
        WriteMethodPolicy policy = new WriteMethodPolicy(runtime);
        ApiResponse policyResponse =
                policy.rejectIfNotAllowed(
                        RoutingVerb.PATCH,
                        route,
                        uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields.empty(),
                        context);
        if (policyResponse != null) {
            return policyResponse;
        }

        Optional<EntityPatchUpdateStyle> style =
                EntityPatchUpdateStyle.fromContentType(requestHeaders.get("Content-Type"));
        if (route instanceof InstanceRoute) {
            Set<EntityPatchUpdateStyle> allowedStyles = policy.entityPatchUpdateStylesFor(route);
            if (style.isEmpty() || !allowedStyles.contains(style.get())) {
                return unsupportedPatchContentType(allowedStyles);
            }
        }

        ThingWriteRequestMapping mapping =
                new EntityPatchDocumentMapper(runtime)
                        .map(
                                style.orElse(EntityPatchUpdateStyle.PARTIAL_JSON_UPDATE),
                                route,
                                rawBody,
                                context);
        ThingCommandResultApiMapper apiMapper =
                new ThingCommandResultApiMapper(runtime.apiConfig());
        if (mapping.isError()) {
            return apiMapper.map(mapping.getError());
        }

        ThingCommandResult result = runtime.commandService(context).execute(mapping.getCommand());
        return apiMapper.map(mapping, result);
    }

    private ApiResponse unsupportedPatchContentType(
            final Set<EntityPatchUpdateStyle> allowedStyles) {
        return ApiResponse.error(415, "Unsupported PATCH Content Type")
                .setHeader(
                        "Accept-Patch",
                        EntityPatchUpdateStyle.acceptPatchHeaderValue(allowedStyles));
    }
}
