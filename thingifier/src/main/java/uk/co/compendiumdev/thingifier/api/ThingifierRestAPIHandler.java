package uk.co.compendiumdev.thingifier.api;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.DefaultThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.apihandlers.ThingifierApiRuntime;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleContext;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleHookRegistry;
import uk.co.compendiumdev.thingifier.api.http.ApiRequestEnvelope;
import uk.co.compendiumdev.thingifier.api.http.ThingifierRequestContext;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.ApiBodyFields;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.restapihandlers.*;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;

public class ThingifierRestAPIHandler {
    private final ThingifierApiRuntime runtime;
    private final RestApiDeleteHandler delete;
    private final RestApiPostHandler post;
    private final RestApiPutHandler put;
    private final RestApiPatchHandler patch;
    private final RestApiGetHandler get;
    private final RestApiQueryHandler query;

    public ThingifierRestAPIHandler(final Thingifier aThingifier) {
        this(new DefaultThingifierApiRuntime(aThingifier));
    }

    public ThingifierRestAPIHandler(final ThingifierApiRuntime runtime) {
        this(runtime, new ThingifierApiLifecycleHookRegistry());
    }

    public ThingifierRestAPIHandler(
            final ThingifierApiRuntime runtime,
            final ThingifierApiLifecycleHookRegistry lifecycleHooks) {
        this.runtime = runtime;
        ThingifierApiLifecycleHookRegistry hooks =
                lifecycleHooks == null ? new ThingifierApiLifecycleHookRegistry() : lifecycleHooks;
        this.get = new RestApiGetHandler(runtime, hooks);
        this.delete = new RestApiDeleteHandler(runtime, hooks);
        this.post = new RestApiPostHandler(runtime, hooks);
        this.put = new RestApiPutHandler(runtime, hooks);
        this.patch = new RestApiPatchHandler(runtime, hooks);
        this.query = new RestApiQueryHandler(runtime, hooks);
    }

    // TODO: we should be able to accept xml with correct content type
    // TODO: we should be able to accept html forms with correct content type
    // todo allow an accept text/html to create different output - (probably handled by routings
    // rather than api)
    // todo : generate examples when outputing the api documentation

    // TODO: - listed here
    // https://www.lisihocke.com/2018/07/testing-tour-stop-16-pair-exploring-an-api-with-thomas.html
    // TODO: ensure that relationshps enforce the type of thing e.g. if I pass in a GUID of the
    // wrong type then it should not cross ref
    // TODO: possibly consider an X- header which has the number of items in the collection

    public ApiResponse get(
            final String url, final QueryFilterParams queryParams, HttpHeadersBlock headers) {
        ThingifierRequestContext context = contextFrom(headers);
        return withRepository(get.handle(url, queryParams, context), context);
    }

    public ApiResponse get(final ApiRequestEnvelope request) {
        return get(request, null);
    }

    public ApiResponse get(
            final ApiRequestEnvelope request, final ThingifierApiLifecycleContext lifecycle) {
        ThingifierRequestContext context = contextFrom(request.headers());
        return withRepository(
                get.handle(request.path(), request.queryParams(), context, lifecycle), context);
    }

    public ApiResponse head(
            final String url, final QueryFilterParams queryParams, HttpHeadersBlock headers) {
        ThingifierRequestContext context = contextFrom(headers);
        final ApiResponse response = get.handle(url, queryParams, context);
        response.clearBody();
        return withRepository(response, context);
    }

    public ApiResponse head(final ApiRequestEnvelope request) {
        return head(request, null);
    }

    public ApiResponse head(
            final ApiRequestEnvelope request, final ThingifierApiLifecycleContext lifecycle) {
        ThingifierRequestContext context = contextFrom(request.headers());
        final ApiResponse response =
                get.handle(request.path(), request.queryParams(), context, lifecycle);
        response.clearBody();
        return withRepository(response, context);
    }

    public ApiResponse query(final ApiRequestEnvelope request) {
        return query(request, null);
    }

    public ApiResponse query(
            final ApiRequestEnvelope request, final ThingifierApiLifecycleContext lifecycle) {
        ThingifierRequestContext context = contextFrom(request.headers());
        return withRepository(
                query.handle(
                        request.path(),
                        request.queryParams(),
                        request.queryBodyFormat(),
                        request.body(),
                        context,
                        lifecycle),
                context);
    }

    public ApiResponse delete(final String url, HttpHeadersBlock headers) {
        ThingifierRequestContext context = contextFrom(headers);
        return withRepository(delete.handle(url, context), context);
    }

    public ApiResponse delete(final ApiRequestEnvelope request) {
        return delete(request, null);
    }

    public ApiResponse delete(
            final ApiRequestEnvelope request, final ThingifierApiLifecycleContext lifecycle) {
        ThingifierRequestContext context = contextFrom(request.headers());
        return withRepository(delete.handle(request.path(), context, lifecycle), context);
    }

    public ApiResponse post(final String url, final BodyParser args, HttpHeadersBlock headers) {
        ThingifierRequestContext context = contextFrom(headers);
        return post(url, args.bodyFields(), context);
    }

    public ApiResponse post(final ApiRequestEnvelope request) {
        return post(request, null);
    }

    public ApiResponse post(
            final ApiRequestEnvelope request, final ThingifierApiLifecycleContext lifecycle) {
        ThingifierRequestContext context = contextFrom(request.headers());
        return withRepository(
                post.handle(request.path(), request.bodyFields(), context, lifecycle), context);
    }

    public ApiResponse post(
            final String url,
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context) {
        return withRepository(post.handle(url, bodyFields, context), context);
    }

    public ApiResponse put(final String url, final BodyParser args, HttpHeadersBlock headers) {
        ThingifierRequestContext context = contextFrom(headers);
        return put(url, args.bodyFields(), context);
    }

    public ApiResponse put(final ApiRequestEnvelope request) {
        return put(request, null);
    }

    public ApiResponse put(
            final ApiRequestEnvelope request, final ThingifierApiLifecycleContext lifecycle) {
        ThingifierRequestContext context = contextFrom(request.headers());
        return withRepository(
                put.handle(request.path(), request.bodyFields(), context, lifecycle), context);
    }

    public ApiResponse put(
            final String url,
            final ApiBodyFields bodyFields,
            final ThingifierRequestContext context) {
        return withRepository(put.handle(url, bodyFields, context), context);
    }

    public ApiResponse patch(final String url, final BodyParser args, HttpHeadersBlock headers) {
        ThingifierRequestContext context = contextFrom(headers);
        return patch(url, args.rawBody(), headers, context);
    }

    public ApiResponse patch(final String url, final String body, HttpHeadersBlock headers) {
        ThingifierRequestContext context = contextFrom(headers);
        return patch(url, body, headers, context);
    }

    public ApiResponse patch(final ApiRequestEnvelope request) {
        return patch(request, null);
    }

    public ApiResponse patch(
            final ApiRequestEnvelope request, final ThingifierApiLifecycleContext lifecycle) {
        ThingifierRequestContext context = contextFrom(request.headers());
        return withRepository(
                patch.handle(request.path(), request.body(), request.headers(), context, lifecycle),
                context);
    }

    public ApiResponse patch(
            final String url,
            final String body,
            final HttpHeadersBlock headers,
            final ThingifierRequestContext context) {
        return withRepository(patch.handle(url, body, headers, context), context);
    }

    private ThingifierRequestContext contextFrom(final HttpHeadersBlock headers) {
        return runtime.contextFrom(headers);
    }

    private ApiResponse withRepository(
            final ApiResponse response, final ThingifierRequestContext context) {
        if (response == null) {
            return null;
        }
        return response.usingRelationships(context.store().relationships());
    }
}
