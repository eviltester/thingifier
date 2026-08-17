package uk.co.compendiumdev.thingifier.adapter.httpserver;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.hooks.HookScope;
import uk.co.compendiumdev.thingifier.adapter.hooks.ScopedHook;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.AfterActionHook;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.AfterValidationHook;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.BeforeActionHook;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.BeforeValidationHook;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.BodyParsedHook;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.RouteMatchedHook;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleHookRegistry;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiHookRegistry;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiResponseHook;
import uk.co.compendiumdev.thingifier.adapter.httpserver.conversion.HttpServerRequestToInternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.conversion.HttpServerResponseToInternalHttpResponse;
import uk.co.compendiumdev.thingifier.adapter.httpserver.conversion.InternalHttpResponseToHttpServer;
import uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks.HttpRequestResponseHook;
import uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks.InternalHttpRequestHook;
import uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks.InternalHttpResponseHook;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpMethod;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.conversion.ThingifierHttpApiBridge;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinitionDocGenerator;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;

public class ThingifierHttpApiRoutings {

    private static final String INTERNAL_HTTP_REQUEST_ATTRIBUTE = "thingifier.internalHttpRequest";

    //    private String urlPath;
    private List<HttpRequestResponseHook> preHttpRequestHooks;
    private List<HttpRequestResponseHook> postHttpResponseHooks;
    private List<ScopedHook<InternalHttpRequestHook>> preInternalHttpRequestHooks;
    private List<ScopedHook<InternalHttpResponseHook>> postInternalHttpResponseHooks;
    private final HttpApiHookRegistry httpApiHooks;
    private final ThingifierApiLifecycleHookRegistry lifecycleHooks;

    // todo : we should be able to configure the API routing for authorisation and support logging

    public ThingifierHttpApiRoutings(
            final Thingifier thingifier, ThingifierApiDocumentationDefn apiDefn) {

        // hooks that take httpserver request and responses pre and post the http message receipt /
        // sending
        preHttpRequestHooks = new ArrayList<>();
        postHttpResponseHooks = new ArrayList<>();

        // hooks that take internal representations of HTTP pre and post
        preInternalHttpRequestHooks = new ArrayList<>();
        postInternalHttpResponseHooks = new ArrayList<>();

        // pre and post api request processing, using internal representations
        httpApiHooks = new HttpApiHookRegistry();
        lifecycleHooks = new ThingifierApiLifecycleHookRegistry();

        ThingifierHttpApiBridge apiBridge =
                ThingifierHttpApiBridge.withHookRegistries(
                        thingifier, httpApiHooks, lifecycleHooks);

        before(
                (request, response) -> {

                    // TODO: this would be more appropriate in a before in the HTTP GUI or docs
                    // routings
                    //            if(this.urlPath==null){
                    //                // capture the protocol and authority to use as rendered urls
                    //                try{
                    //                    final URL requestUrl = new URL(request.url());
                    //                    this.urlPath = requestUrl.getProtocol() + "://" +
                    // requestUrl.getAuthority();
                    //                }catch(MalformedURLException e){
                    //                    System.out.println(request.url() + " " + e.getMessage());
                    //                }
                    //            }

                    // Run any hooks at the HTTP server request and response level.
                    if (preHttpRequestHooks != null) {
                        for (HttpRequestResponseHook hook : preHttpRequestHooks) {
                            // todo: catch exceptions and `halt`
                            hook.run(request, response);
                        }
                    }

                    InternalHttpRequest iRequest = internalRequestFrom(request);
                    // now run the HttpApiRequestHook hooks on this iRequest
                    if (preInternalHttpRequestHooks != null) {
                        for (ScopedHook<InternalHttpRequestHook> scopedHook :
                                preInternalHttpRequestHooks) {
                            if (!scopedHook.matches(
                                    iRequest.getPath(), routingVerbFor(iRequest.getVerb()), "")) {
                                continue;
                            }
                            // todo: catch exceptions and `halt`
                            InternalHttpResponse hookResponse = scopedHook.hook().run(iRequest);
                            if (hookResponse != null) {
                                InternalHttpResponseToHttpServer.convert(hookResponse, response);
                                halt(hookResponse.getStatusCode(), hookResponse.getBody());
                            }
                        }
                    }
                });

        after(
                (request, response) -> {

                    // now run the HttpApiResponseHook hooks
                    // on this iRequest and iResponse
                    InternalHttpRequest iRequest = internalRequestFrom(request);
                    InternalHttpResponse iResponse =
                            HttpServerResponseToInternalHttpResponse.convert(response);

                    // now run the HttpApiRequestHook hooks on this iRequest
                    if (postInternalHttpResponseHooks != null) {
                        for (ScopedHook<InternalHttpResponseHook> scopedHook :
                                postInternalHttpResponseHooks) {
                            if (!scopedHook.matches(
                                    iRequest.getPath(), routingVerbFor(iRequest.getVerb()), "")) {
                                continue;
                            }
                            // todo: catch exceptions and `halt`
                            scopedHook.hook().run(iRequest, iResponse);
                        }
                    }

                    HttpServerResponseToInternalHttpResponse.updateResponseFromInternal(
                            response, iResponse);

                    if (postHttpResponseHooks != null) {
                        for (HttpRequestResponseHook hook : postHttpResponseHooks) {
                            // todo: catch exceptions and let the response return
                            hook.run(request, response);
                        }
                    }
                });

        // configure it based on a thingifier
        ApiRoutingDefinition routingDefinitions =
                new ApiRoutingDefinitionDocGenerator(thingifier).generate(apiDefn.getPathPrefix());

        for (RoutingDefinition defn : routingDefinitions.definitions()) {
            if (defn.isDisabled()) {
                continue;
            }
            switch (defn.verb()) {
                case GET:
                    if (!defn.status().isReturnedFromCall()) {
                        get(
                                defn.url(),
                                (request, response) -> {
                                    applyStaticResponse(defn, response);
                                    return "";
                                });
                    } else {
                        get(
                                defn.url(),
                                (request, response) -> {
                                    // return apiBridge.get(request, response);
                                    final InternalHttpRequest theRequest =
                                            internalRequestFrom(request);
                                    // TODO: allow amending the request and the response at a
                                    // request level from framework
                                    // .e.g
                                    // Add the Challenger GUID identifier as a Thingifier HTTP
                                    // Session header
                                    // in a hook we could write -
                                    // request.addHeader(HTTP_SESSION_HEADER_NAME,
                                    // challenger.getXChallenger());
                                    // runAnyCustomHttpApiRequestAmendmentHooks(theRequest)
                                    final InternalHttpResponse theResponse =
                                            apiBridge.get(theRequest);
                                    // TODO: similarly allow amending the response from the API
                                    return InternalHttpResponseToHttpServer.convert(
                                            theResponse, response);
                                });
                    }
                    break;
                case POST:
                    if (!defn.status().isReturnedFromCall()) {
                        post(
                                defn.url(),
                                (request, response) -> {
                                    applyStaticResponse(defn, response);
                                    return "";
                                });
                    } else {
                        post(
                                defn.url(),
                                (request, response) -> {
                                    // return apiBridge.post(request, response);
                                    final InternalHttpRequest theRequest =
                                            internalRequestFrom(request);
                                    final InternalHttpResponse theResponse =
                                            apiBridge.post(theRequest);
                                    return InternalHttpResponseToHttpServer.convert(
                                            theResponse, response);
                                });
                    }
                    break;
                case QUERY:
                    if (!defn.status().isReturnedFromCall()) {
                        query(
                                defn.url(),
                                (request, response) -> {
                                    applyStaticResponse(defn, response);
                                    return "";
                                });
                    } else {
                        query(
                                defn.url(),
                                (request, response) -> {
                                    final InternalHttpRequest theRequest =
                                            internalRequestFrom(request);
                                    final InternalHttpResponse theResponse =
                                            apiBridge.queryRequest(theRequest);
                                    return InternalHttpResponseToHttpServer.convert(
                                            theResponse, response);
                                });
                    }
                    break;
                default:
                    break;
                case HEAD:
                    if (!defn.status().isReturnedFromCall()) {
                        head(
                                defn.url(),
                                (request, response) -> {
                                    applyStaticResponse(defn, response);
                                    return "";
                                });
                    } else {
                        head(
                                defn.url(),
                                (request, response) -> {
                                    // return apiBridge.head(request, response);
                                    final InternalHttpRequest theRequest =
                                            internalRequestFrom(request);
                                    final InternalHttpResponse theResponse =
                                            apiBridge.head(theRequest);
                                    return InternalHttpResponseToHttpServer.convert(
                                            theResponse, response);
                                });
                    }
                    break;
                case DELETE:
                    if (!defn.status().isReturnedFromCall()) {
                        delete(
                                defn.url(),
                                (request, response) -> {
                                    applyStaticResponse(defn, response);
                                    return "";
                                });
                    } else {
                        delete(
                                defn.url(),
                                (request, response) -> {
                                    // return apiBridge.delete(request, response);
                                    final InternalHttpRequest theRequest =
                                            internalRequestFrom(request);
                                    final InternalHttpResponse theResponse =
                                            apiBridge.delete(theRequest);
                                    return InternalHttpResponseToHttpServer.convert(
                                            theResponse, response);
                                });
                    }
                    break;
                case PATCH:
                    if (!defn.status().isReturnedFromCall()) {
                        patch(
                                defn.url(),
                                (request, response) -> {
                                    applyStaticResponse(defn, response);
                                    return "";
                                });
                    } else {
                        patch(
                                defn.url(),
                                (request, response) -> {
                                    final InternalHttpRequest theRequest =
                                            internalRequestFrom(request);
                                    final InternalHttpResponse theResponse =
                                            apiBridge.patch(theRequest);
                                    return InternalHttpResponseToHttpServer.convert(
                                            theResponse, response);
                                });
                    }
                    break;
                case PUT:
                    if (!defn.status().isReturnedFromCall()) {
                        put(
                                defn.url(),
                                (request, response) -> {
                                    applyStaticResponse(defn, response);
                                    return "";
                                });
                    } else {
                        put(
                                defn.url(),
                                (request, response) -> {
                                    // return apiBridge.put(request, response);
                                    final InternalHttpRequest theRequest =
                                            internalRequestFrom(request);
                                    final InternalHttpResponse theResponse =
                                            apiBridge.put(theRequest);
                                    return InternalHttpResponseToHttpServer.convert(
                                            theResponse, response);
                                });
                    }
                    break;
                case OPTIONS:
                    if (!defn.status().isReturnedFromCall()) {
                        options(
                                defn.url(),
                                (request, response) -> {
                                    applyStaticResponse(defn, response);
                                    return "";
                                });
                    }
                    break;
                case TRACE:
                    if (!defn.status().isReturnedFromCall()) {
                        trace(
                                defn.url(),
                                (request, response) -> {
                                    applyStaticResponse(defn, response);
                                    return "";
                                });
                    }
                    break;
            }
        }

        // Undocumented admin interface
        if (thingifier.apiConfig().adminConfig().isAdminSearchAllowed()) {
            get(
                    thingifier.apiConfig().adminConfig().getAdminSearchUrl(),
                    (request, response) -> {
                        // return apiBridge.query(request, response, request.splat()[0]);
                        final InternalHttpRequest theRequest = internalRequestFrom(request);
                        final InternalHttpResponse theResponse =
                                apiBridge.query(theRequest, request.splat());
                        return InternalHttpResponseToHttpServer.convert(theResponse, response);
                    });
        }

        // Undocumented admin interface
        if (thingifier.apiConfig().adminConfig().isAdminDataClearAllowed()) {
            post(
                    thingifier.apiConfig().adminConfig().getAdminDataClearUrl(),
                    (request, response) -> {
                        thingifier.clearAllData();
                        response.status(200);
                        return "";
                    });
        }

        // create an API end point level 404 handler
        if (apiDefn.getPathPrefix() != null && !apiDefn.getPathPrefix().isEmpty()) {
            SimpleHttpRouteCreator.routeStatus(
                    404,
                    apiDefn.getPathPrefix() + "/*",
                    true,
                    List.of("head", "get", "query", "options", "put", "post", "patch", "delete"));
        }
    }

    public void registerPreRequestHook(final HttpRequestResponseHook hook) {
        // pre-request hooks run pre-every-request
        preHttpRequestHooks.add(hook);
    }

    public void registerPostResponseHook(final HttpRequestResponseHook hook) {
        // post-request hooks run after-every-response
        postHttpResponseHooks.add(hook);
    }

    /*
       HttpApiRequestHooks are run in the API Bridge routing, prior to being
       processed by the API handlers - these will be unique to each thingifier.
    */
    public void registerHttpApiRequestHook(final HttpApiRequestHook hook) {
        // pre-request hooks run pre-every-api-request
        httpApiHooks.registerRequestHook(hook);
    }

    public void registerHttpApiRequestHook(final HookScope scope, final HttpApiRequestHook hook) {
        httpApiHooks.registerRequestHook(scope, hook);
    }

    public void registerHttpApiRequestHook(
            final String pathPattern, final HttpApiRequestHook hook) {
        registerHttpApiRequestHook(HookScope.endpoint(pathPattern), hook);
    }

    public void registerHttpApiRequestHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final HttpApiRequestHook hook) {
        registerHttpApiRequestHook(HookScope.endpointAndVerbs(pathPattern, verbs), hook);
    }

    /*
    HttpApiResponseHooks are run in the API Bridge routing, after being
    processed by the API handlers - these will be unique to each thingifier.
    */
    public void registerHttpApiResponseHook(final HttpApiResponseHook hook) {
        // pre-request hooks run pre-every-api-request
        httpApiHooks.registerResponseHook(hook);
    }

    public void registerHttpApiResponseHook(final HookScope scope, final HttpApiResponseHook hook) {
        httpApiHooks.registerResponseHook(scope, hook);
    }

    public void registerHttpApiResponseHook(
            final String pathPattern, final HttpApiResponseHook hook) {
        registerHttpApiResponseHook(HookScope.endpoint(pathPattern), hook);
    }

    public void registerHttpApiResponseHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final HttpApiResponseHook hook) {
        registerHttpApiResponseHook(HookScope.endpointAndVerbs(pathPattern, verbs), hook);
    }

    public void registerRouteMatchedHook(final RouteMatchedHook hook) {
        lifecycleHooks.registerRouteMatchedHook(hook);
    }

    public void registerRouteMatchedHook(final HookScope scope, final RouteMatchedHook hook) {
        lifecycleHooks.registerRouteMatchedHook(scope, hook);
    }

    public void registerRouteMatchedHook(final String pathPattern, final RouteMatchedHook hook) {
        lifecycleHooks.registerRouteMatchedHook(pathPattern, hook);
    }

    public void registerRouteMatchedHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final RouteMatchedHook hook) {
        lifecycleHooks.registerRouteMatchedHook(pathPattern, verbs, hook);
    }

    public void registerBodyParsedHook(final BodyParsedHook hook) {
        lifecycleHooks.registerBodyParsedHook(hook);
    }

    public void registerBodyParsedHook(final HookScope scope, final BodyParsedHook hook) {
        lifecycleHooks.registerBodyParsedHook(scope, hook);
    }

    public void registerBodyParsedHook(final String pathPattern, final BodyParsedHook hook) {
        lifecycleHooks.registerBodyParsedHook(pathPattern, hook);
    }

    public void registerBodyParsedHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final BodyParsedHook hook) {
        lifecycleHooks.registerBodyParsedHook(pathPattern, verbs, hook);
    }

    public void registerBeforeValidationHook(final BeforeValidationHook hook) {
        lifecycleHooks.registerBeforeValidationHook(hook);
    }

    public void registerBeforeValidationHook(
            final HookScope scope, final BeforeValidationHook hook) {
        lifecycleHooks.registerBeforeValidationHook(scope, hook);
    }

    public void registerBeforeValidationHook(
            final String pathPattern, final BeforeValidationHook hook) {
        lifecycleHooks.registerBeforeValidationHook(pathPattern, hook);
    }

    public void registerBeforeValidationHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final BeforeValidationHook hook) {
        lifecycleHooks.registerBeforeValidationHook(pathPattern, verbs, hook);
    }

    public void registerAfterValidationHook(final AfterValidationHook hook) {
        lifecycleHooks.registerAfterValidationHook(hook);
    }

    public void registerAfterValidationHook(final HookScope scope, final AfterValidationHook hook) {
        lifecycleHooks.registerAfterValidationHook(scope, hook);
    }

    public void registerAfterValidationHook(
            final String pathPattern, final AfterValidationHook hook) {
        lifecycleHooks.registerAfterValidationHook(pathPattern, hook);
    }

    public void registerAfterValidationHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final AfterValidationHook hook) {
        lifecycleHooks.registerAfterValidationHook(pathPattern, verbs, hook);
    }

    public void registerBeforeActionHook(final BeforeActionHook hook) {
        lifecycleHooks.registerBeforeActionHook(hook);
    }

    public void registerBeforeActionHook(final HookScope scope, final BeforeActionHook hook) {
        lifecycleHooks.registerBeforeActionHook(scope, hook);
    }

    public void registerBeforeActionHook(final String pathPattern, final BeforeActionHook hook) {
        lifecycleHooks.registerBeforeActionHook(pathPattern, hook);
    }

    public void registerBeforeActionHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final BeforeActionHook hook) {
        lifecycleHooks.registerBeforeActionHook(pathPattern, verbs, hook);
    }

    public void registerAfterActionHook(final AfterActionHook hook) {
        lifecycleHooks.registerAfterActionHook(hook);
    }

    public void registerAfterActionHook(final HookScope scope, final AfterActionHook hook) {
        lifecycleHooks.registerAfterActionHook(scope, hook);
    }

    public void registerAfterActionHook(final String pathPattern, final AfterActionHook hook) {
        lifecycleHooks.registerAfterActionHook(pathPattern, hook);
    }

    public void registerAfterActionHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final AfterActionHook hook) {
        lifecycleHooks.registerAfterActionHook(pathPattern, verbs, hook);
    }

    public void registerInternalHttpResponseHook(final InternalHttpResponseHook hook) {
        // pre-request hooks run post api processing on an internal http representation
        postInternalHttpResponseHooks.add(ScopedHook.any(hook));
    }

    public void registerInternalHttpResponseHook(
            final HookScope scope, final InternalHttpResponseHook hook) {
        postInternalHttpResponseHooks.add(ScopedHook.forScope(scope, hook));
    }

    public void registerInternalHttpResponseHook(
            final String pathPattern, final InternalHttpResponseHook hook) {
        registerInternalHttpResponseHook(HookScope.endpoint(pathPattern), hook);
    }

    public void registerInternalHttpResponseHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final InternalHttpResponseHook hook) {
        registerInternalHttpResponseHook(HookScope.endpointAndVerbs(pathPattern, verbs), hook);
    }

    public void registerInternalHttpRequestHook(final InternalHttpRequestHook hook) {
        // pre-request hooks run pre api routing on an internal http representation
        preInternalHttpRequestHooks.add(ScopedHook.any(hook));
    }

    public void registerInternalHttpRequestHook(
            final HookScope scope, final InternalHttpRequestHook hook) {
        preInternalHttpRequestHooks.add(ScopedHook.forScope(scope, hook));
    }

    public void registerInternalHttpRequestHook(
            final String pathPattern, final InternalHttpRequestHook hook) {
        registerInternalHttpRequestHook(HookScope.endpoint(pathPattern), hook);
    }

    public void registerInternalHttpRequestHook(
            final String pathPattern,
            final Collection<RoutingVerb> verbs,
            final InternalHttpRequestHook hook) {
        registerInternalHttpRequestHook(HookScope.endpointAndVerbs(pathPattern, verbs), hook);
    }

    private InternalHttpRequest internalRequestFrom(final HttpServerRequest request) {
        Object existingRequest = request.attribute(INTERNAL_HTTP_REQUEST_ATTRIBUTE);

        if (existingRequest instanceof InternalHttpRequest) {
            return (InternalHttpRequest) existingRequest;
        }

        InternalHttpRequest internalRequest =
                HttpServerRequestToInternalHttpRequest.convert(request);
        request.attribute(INTERNAL_HTTP_REQUEST_ATTRIBUTE, internalRequest);
        return internalRequest;
    }

    private void applyStaticResponse(
            final RoutingDefinition defn, final HttpServerResponse response) {
        response.status(defn.status().value());
        if (!defn.header().isEmpty()) {
            response.header(defn.header(), defn.headerValue());
        }
        if (defn.hasResponseHeaders()) {
            for (String headerName : defn.getResponseHeaderNames()) {
                response.header(headerName, defn.getResponseHeaderValue(headerName));
            }
        }
        if (defn.headerValue().contains("QUERY")) {
            response.header(
                    ThingifierHttpApi.ACCEPT_QUERY_HEADER,
                    ThingifierHttpApi.SUPPORTED_QUERY_CONTENT_TYPES);
        }
    }

    private RoutingVerb routingVerbFor(final InternalHttpMethod method) {
        if (method == null) {
            return null;
        }
        try {
            return RoutingVerb.valueOf(method.name());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
