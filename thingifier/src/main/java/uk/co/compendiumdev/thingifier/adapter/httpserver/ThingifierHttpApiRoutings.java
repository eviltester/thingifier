package uk.co.compendiumdev.thingifier.adapter.httpserver;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.*;

import java.util.ArrayList;
import java.util.List;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.adapter.http.messagehooks.HttpApiResponseHook;
import uk.co.compendiumdev.thingifier.adapter.httpserver.conversion.HttpServerRequestToInternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.httpserver.conversion.HttpServerResponseToInternalHttpResponse;
import uk.co.compendiumdev.thingifier.adapter.httpserver.conversion.InternalHttpResponseToHttpServer;
import uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks.HttpRequestResponseHook;
import uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks.InternalHttpRequestHook;
import uk.co.compendiumdev.thingifier.adapter.httpserver.messagehooks.InternalHttpResponseHook;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpRequest;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.conversion.ThingifierHttpApiBridge;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinitionDocGenerator;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;

public class ThingifierHttpApiRoutings {

    private static final String INTERNAL_HTTP_REQUEST_ATTRIBUTE = "thingifier.internalHttpRequest";

    //    private String urlPath;
    private List<HttpRequestResponseHook> preHttpRequestHooks;
    private List<HttpRequestResponseHook> postHttpResponseHooks;
    private List<InternalHttpRequestHook> preInternalHttpRequestHooks;
    private List<InternalHttpResponseHook> postInternalHttpResponseHooks;
    private List<HttpApiRequestHook> httpApiRequestHooks;
    private final List<HttpApiResponseHook> httpApiResponseHooks;

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
        httpApiRequestHooks = new ArrayList<>();
        httpApiResponseHooks = new ArrayList<>();

        ThingifierHttpApiBridge apiBridge =
                new ThingifierHttpApiBridge(thingifier, httpApiRequestHooks, httpApiResponseHooks);

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
                        for (InternalHttpRequestHook hook : preInternalHttpRequestHooks) {
                            // todo: catch exceptions and `halt`
                            InternalHttpResponse hookResponse = hook.run(iRequest);
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
                        for (InternalHttpResponseHook hook : postInternalHttpResponseHooks) {
                            // todo: catch exceptions and `halt`
                            hook.run(iRequest, iResponse);
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
                    if (defn.status().isReturnedFromCall()) {
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
                    if (defn.status().isReturnedFromCall()) {
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
                    if (defn.status().isReturnedFromCall()) {
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
        httpApiRequestHooks.add(hook);
    }

    /*
    HttpApiResponseHooks are run in the API Bridge routing, after being
    processed by the API handlers - these will be unique to each thingifier.
    */
    public void registerHttpApiResponseHook(final HttpApiResponseHook hook) {
        // pre-request hooks run pre-every-api-request
        httpApiResponseHooks.add(hook);
    }

    public void registerInternalHttpResponseHook(final InternalHttpResponseHook hook) {
        // pre-request hooks run post api processing on an internal http representation
        postInternalHttpResponseHooks.add(hook);
    }

    public void registerInternalHttpRequestHook(final InternalHttpRequestHook hook) {
        // pre-request hooks run pre api routing on an internal http representation
        preInternalHttpRequestHooks.add(hook);
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
        if (defn.headerValue().contains("QUERY")) {
            response.header(
                    ThingifierHttpApi.ACCEPT_QUERY_HEADER, ThingifierHttpApi.QUERY_CONTENT_TYPE);
        }
    }
}
