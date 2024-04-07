package uk.co.compendiumdev.thingifier.application.httprouting;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinitionDocGenerator;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiResponseHook;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.*;
import uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks.InternalHttpRequestHook;
import uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks.InternalHttpResponseHook;
import uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks.SparkRequestResponseHook;
import uk.co.compendiumdev.thingifier.spark.SimpleSparkRouteCreator;
import java.util.ArrayList;
import java.util.List;

import static spark.Spark.*;

public class ThingifierHttpApiRoutings {


    private final ThingifierApiDocumentationDefn apiDefn;

//    private String urlPath;
    private List<SparkRequestResponseHook> preSparkHttpRequestHooks;
    private List<SparkRequestResponseHook> postSparkHttpResponseHooks;
    private List<InternalHttpRequestHook> preInternalHttpRequestHooks;
    private List<InternalHttpResponseHook> postInternalHttpResponseHooks;
    private List<HttpApiRequestHook> httpApiRequestHooks;
    private final List<HttpApiResponseHook> httpApiResponseHooks;


    // todo : we should be able to configure the API routing for authorisation and support logging

    public ThingifierHttpApiRoutings(final Thingifier thingifier,
                                     ThingifierApiDocumentationDefn apiDefn) {

        this.apiDefn = apiDefn;

        // hooks that take Spark request and responses pre and post the http message receipt / sending
        preSparkHttpRequestHooks = new ArrayList<>();
        postSparkHttpResponseHooks = new ArrayList<>();

        // hooks that take internal representations of HTTP pre and post
        preInternalHttpRequestHooks = new ArrayList<>();
        postInternalHttpResponseHooks = new ArrayList<>();

        // pre and post api request processing, using internal representations
        httpApiRequestHooks = new ArrayList<>();
        httpApiResponseHooks = new ArrayList<>();

        ThingifierHttpApiBridge apiBridge = new ThingifierHttpApiBridge(
                                                thingifier,
                                                httpApiRequestHooks, httpApiResponseHooks);

        before((request, response) -> {

            // TODO: this would be more appropriate in a before in the HTTP GUI or docs routings
//            if(this.urlPath==null){
//                // capture the protocol and authority to use as rendered urls
//                try{
//                    final URL requestUrl = new URL(request.url());
//                    this.urlPath = requestUrl.getProtocol() + "://" + requestUrl.getAuthority();
//                }catch(MalformedURLException e){
//                    System.out.println(request.url() + " " + e.getMessage());
//                }
//            }

            // Run any hooks at the Spark Request and Response level
            if(preSparkHttpRequestHooks !=null){
                for(SparkRequestResponseHook hook : preSparkHttpRequestHooks){
                    // todo: catch exceptions and `halt`
                    hook.run(request, response);
                }
            }

            HttpApiRequest iRequest = SparkToHttpApiRequest.convert(request);
            // now run the HttpApiRequestHook hooks on this iRequest
            if(preInternalHttpRequestHooks !=null){
                for(InternalHttpRequestHook hook : preInternalHttpRequestHooks){
                    // todo: catch exceptions and `halt`
                    hook.run(iRequest);
                }
            }
        });

        after((request, response) -> {

            // now run the HttpApiResponseHook hooks
            // on this iRequest and iResponse
            HttpApiRequest iRequest = SparkToHttpApiRequest.convert(request);
            InternalHttpResponse iResponse = SparkResponseToInternalHttpResponse.
                                                   convert(response);

            // now run the HttpApiRequestHook hooks on this iRequest
            if(postInternalHttpResponseHooks !=null){
                for(InternalHttpResponseHook hook : postInternalHttpResponseHooks){
                    // todo: catch exceptions and `halt`
                    hook.run(iRequest, iResponse);
                }
            }

            SparkResponseToInternalHttpResponse.
                    updateResponseFromInternal(response, iResponse);


            if(postSparkHttpResponseHooks !=null){
                for(SparkRequestResponseHook hook : postSparkHttpResponseHooks){
                    // todo: catch exceptions and let the response return
                    hook.run(request, response);
                }
            }
        });


        // configure it based on a thingifier
        ApiRoutingDefinition routingDefinitions = new ApiRoutingDefinitionDocGenerator(thingifier).generate(apiDefn.getPathPrefix());


        for (RoutingDefinition defn : routingDefinitions.definitions()) {
            switch (defn.verb()) {
                case GET:
                    if (defn.status().isReturnedFromCall()) {
                        get(defn.url(), (request, response) -> {
                            //return apiBridge.get(request, response);
                            final HttpApiRequest theRequest = SparkToHttpApiRequest.convert(request);
                            // TODO: allow amending the request and the response at a request level from framework
                            // .e.g
                            // Add the Challenger GUID identifier as a Thingifier HTTP Session header
                            // in a hook we could write - request.addHeader(HTTP_SESSION_HEADER_NAME, challenger.getXChallenger());
                            //runAnyCustomHttpApiRequestAmendmentHooks(theRequest)
                            final HttpApiResponse theResponse = apiBridge.get(theRequest);
                            // TODO: similarly allow amending the response from the API
                            return HttpApiResponseToSpark.convert(theResponse, response);
                        });
                    }
                    break;
                case POST:
                    if (defn.status().isReturnedFromCall()) {
                        post(defn.url(), (request, response) -> {
                            //return apiBridge.post(request, response);
                            final HttpApiRequest theRequest = SparkToHttpApiRequest.convert(request);
                            final HttpApiResponse theResponse = apiBridge.post(theRequest);
                            return HttpApiResponseToSpark.convert(theResponse, response);
                        });
                    }
                    break;
                case HEAD:
                    if (defn.status().isReturnedFromCall()) {
                        head(defn.url(), (request, response) -> {
                            //return apiBridge.head(request, response);
                            final HttpApiRequest theRequest = SparkToHttpApiRequest.convert(request);
                            final HttpApiResponse theResponse = apiBridge.head(theRequest);
                            return HttpApiResponseToSpark.convert(theResponse, response);
                        });
                    }
                    break;
                case DELETE:
                    if (!defn.status().isReturnedFromCall()) {
                        delete(defn.url(), (request, response) -> {
                            response.status(defn.status().value());
                            return "";
                        });
                    } else {
                        delete(defn.url(), (request, response) -> {
                            //return apiBridge.delete(request, response);
                            final HttpApiRequest theRequest = SparkToHttpApiRequest.convert(request);
                            final HttpApiResponse theResponse = apiBridge.delete(theRequest);
                            return HttpApiResponseToSpark.convert(theResponse, response);
                        });
                    }
                    break;
                case PATCH:
                    if (!defn.status().isReturnedFromCall()) {
                        patch(defn.url(), (request, response) -> {
                            response.status(defn.status().value());
                            return "";
                        });
                    }
                    break;
                case PUT:
                    if (!defn.status().isReturnedFromCall()) {
                        put(defn.url(), (request, response) -> {
                            response.status(defn.status().value());
                            return "";
                        });
                    } else {
                        put(defn.url(), (request, response) -> {
                            //return apiBridge.put(request, response);
                            final HttpApiRequest theRequest = SparkToHttpApiRequest.convert(request);
                            final HttpApiResponse theResponse = apiBridge.put(theRequest);
                            return HttpApiResponseToSpark.convert(theResponse, response);
                        });
                    }
                    break;
                case OPTIONS:
                    if (!defn.status().isReturnedFromCall()) {
                        options(defn.url(), (request, response) -> {
                            response.status(defn.status().value());
                            response.header(defn.header(), defn.headerValue());
                            return "";
                        });
                    }
                    break;
                case TRACE:
                    if (!defn.status().isReturnedFromCall()) {
                        trace(defn.url(), (request, response) -> {
                            response.status(defn.status().value());
                            return "";
                        });
                    }
                    break;
            }
        }

        // Undocumented admin interface
        if(thingifier.apiConfig().adminConfig().isAdminSearchAllowed()) {
            get(thingifier.apiConfig().adminConfig().getAdminSearchUrl(), (request, response) -> {
                //return apiBridge.query(request, response, request.splat()[0]);
                final HttpApiRequest theRequest = SparkToHttpApiRequest.convert(request);
                final HttpApiResponse theResponse = apiBridge.query(theRequest, request.splat()[0]);
                return HttpApiResponseToSpark.convert(theResponse, response);
            });
        }

        // Undocumented admin interface
        if(thingifier.apiConfig().adminConfig().isAdminDataClearAllowed()) {
            post(thingifier.apiConfig().adminConfig().getAdminDataClearUrl(), (request, response) -> {
                thingifier.clearAllData();
                response.status(200);
                return "";
            });
        }

        // create an API end point level 404 handler
        if(apiDefn.getPathPrefix()!=null && !apiDefn.getPathPrefix().isEmpty()) {
            SimpleSparkRouteCreator.routeStatus(404, apiDefn.getPathPrefix() + "/*", true, List.of("head", "get", "options", "put", "post", "patch", "delete"));
        }

    }

    public void registerPreRequestHook(final SparkRequestResponseHook hook) {
        // pre-request hooks run pre-every-request
        preSparkHttpRequestHooks.add(hook);
    }

    public void registerPostResponseHook(final SparkRequestResponseHook hook) {
        // post-request hooks run after-every-response
        postSparkHttpResponseHooks.add(hook);
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

}
