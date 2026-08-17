package uk.co.compendiumdev.thingifier.api.restapihandlers;

import java.util.Optional;
import java.util.Set;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.DefaultThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.EntityPatchDocumentMapper;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingWriteRequestMapping;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.WriteMethodPolicy;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.InstanceRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRoute;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.route.ThingRouteMapper;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleContext;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleHookRegistry;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle;

public class RestApiPatchHandler {
    private final ThingifierApiRuntime runtime;
    private final ThingifierApiLifecycleHookRegistry lifecycleHooks;

    public RestApiPatchHandler(final Thingifier aThingifier) {
        this(new DefaultThingifierApiRuntime(aThingifier));
    }

    public RestApiPatchHandler(final ThingifierApiRuntime runtime) {
        this(runtime, new ThingifierApiLifecycleHookRegistry());
    }

    public RestApiPatchHandler(
            final ThingifierApiRuntime runtime,
            final ThingifierApiLifecycleHookRegistry lifecycleHooks) {
        this.runtime = runtime;
        this.lifecycleHooks =
                lifecycleHooks == null ? new ThingifierApiLifecycleHookRegistry() : lifecycleHooks;
    }

    public ApiResponse handle(
            final String url,
            final String rawBody,
            final HttpHeadersBlock requestHeaders,
            final ThingifierRequestContext context) {
        return handle(url, rawBody, requestHeaders, context, null);
    }

    public ApiResponse handle(
            final String url,
            final String rawBody,
            final HttpHeadersBlock requestHeaders,
            final ThingifierRequestContext context,
            final ThingifierApiLifecycleContext lifecycle) {
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
                new EntityPatchDocumentMapper(runtime.schema())
                        .map(
                                style.orElse(EntityPatchUpdateStyle.PARTIAL_JSON_UPDATE),
                                route,
                                rawBody);
        return LifecycleWriteSupport.execute(
                runtime,
                lifecycleHooks,
                lifecycle,
                mapping,
                context,
                () ->
                        new EntityPatchDocumentMapper(runtime.schema())
                                .map(patchStyle(requestHeaders), route, lifecycle.rawBody()));
    }

    private EntityPatchUpdateStyle patchStyle(final HttpHeadersBlock requestHeaders) {
        return EntityPatchUpdateStyle.fromContentType(requestHeaders.get("Content-Type"))
                .orElse(EntityPatchUpdateStyle.PARTIAL_JSON_UPDATE);
    }

    private ApiResponse unsupportedPatchContentType(
            final Set<EntityPatchUpdateStyle> allowedStyles) {
        return ApiResponse.error(415, "Unsupported PATCH Content Type")
                .setHeader(
                        "Accept-Patch",
                        EntityPatchUpdateStyle.acceptPatchHeaderValue(allowedStyles));
    }
}
