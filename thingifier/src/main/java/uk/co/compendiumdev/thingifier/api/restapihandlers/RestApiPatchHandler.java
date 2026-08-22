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
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleContext;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleHookRegistry;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.EntityPatchUpdateStyle;

/**
 * Handles generated Thingifier PATCH routes.
 *
 * <p>The handler validates PATCH content type policy, maps the patch document to a write command,
 * and delegates lifecycle-aware validation and execution.
 */
public class RestApiPatchHandler {
    private final ThingifierApiRuntime runtime;
    private final ThingifierApiLifecycleHookRegistry lifecycleHooks;

    /**
     * Creates a PATCH handler from a Thingifier model.
     *
     * @param aThingifier Thingifier model and configuration
     */
    public RestApiPatchHandler(final Thingifier aThingifier) {
        this(new DefaultThingifierApiRuntime(aThingifier));
    }

    /**
     * Creates a PATCH handler with no lifecycle hooks.
     *
     * @param runtime runtime services used by the handler
     */
    public RestApiPatchHandler(final ThingifierApiRuntime runtime) {
        this(runtime, new ThingifierApiLifecycleHookRegistry());
    }

    /**
     * Creates a PATCH handler with lifecycle hooks.
     *
     * @param runtime runtime services used by the handler
     * @param lifecycleHooks lifecycle hooks for write processing
     */
    public RestApiPatchHandler(
            final ThingifierApiRuntime runtime,
            final ThingifierApiLifecycleHookRegistry lifecycleHooks) {
        this.runtime = runtime;
        this.lifecycleHooks =
                lifecycleHooks == null ? new ThingifierApiLifecycleHookRegistry() : lifecycleHooks;
    }

    /**
     * Handles a PATCH request without lifecycle hook state.
     *
     * @param url generated API path
     * @param rawBody raw patch body
     * @param requestHeaders request headers used for content type policy
     * @param context request context containing the active store
     * @return API response for the PATCH
     */
    public ApiResponse handle(
            final String url,
            final String rawBody,
            final HttpHeadersBlock requestHeaders,
            final ThingifierRequestContext context) {
        return handle(url, rawBody, requestHeaders, context, null);
    }

    /**
     * Handles a PATCH request with optional lifecycle hook state.
     *
     * @param url generated API path
     * @param rawBody raw patch body
     * @param requestHeaders request headers used for content type policy
     * @param context request context containing the active store
     * @param lifecycle lifecycle context, or null for direct processing
     * @return API response for the PATCH
     */
    public ApiResponse handle(
            final String url,
            final String rawBody,
            final HttpHeadersBlock requestHeaders,
            final ThingifierRequestContext context,
            final ThingifierApiLifecycleContext lifecycle) {
        ThingRoute route = routeFor(url, lifecycle);
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

    private ThingRoute routeFor(final String url, final ThingifierApiLifecycleContext lifecycle) {
        return lifecycle == null ? runtime.routeFor(RoutingVerb.PATCH, url) : lifecycle.route();
    }

    /**
     * Resolves the PATCH update style from the request headers, defaulting to partial JSON update.
     *
     * @param requestHeaders request headers containing Content-Type
     * @return selected patch style
     */
    private EntityPatchUpdateStyle patchStyle(final HttpHeadersBlock requestHeaders) {
        return EntityPatchUpdateStyle.fromContentType(requestHeaders.get("Content-Type"))
                .orElse(EntityPatchUpdateStyle.PARTIAL_JSON_UPDATE);
    }

    /**
     * Creates the standard response for unsupported PATCH media types.
     *
     * @param allowedStyles PATCH styles allowed by policy
     * @return unsupported-media-type response with Accept-Patch header
     */
    private ApiResponse unsupportedPatchContentType(
            final Set<EntityPatchUpdateStyle> allowedStyles) {
        return ApiResponse.error(415, "Unsupported PATCH Content Type")
                .setHeader(
                        "Accept-Patch",
                        EntityPatchUpdateStyle.acceptPatchHeaderValue(allowedStyles));
    }
}
