package uk.co.compendiumdev.thingifier.application;

import spark.Request;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ThingifierApiDefn;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseError;
import uk.co.compendiumdev.thingifier.api.routings.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.ApiRoutingDefinitionGenerator;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiResponseHook;
import uk.co.compendiumdev.thingifier.application.internalhttpconversion.*;
import uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks.InternalHttpRequestHook;
import uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks.InternalHttpResponseHook;
import uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks.SparkRequestResponseHook;
import uk.co.compendiumdev.thingifier.htmlgui.DefaultGUIHTML;
import uk.co.compendiumdev.thingifier.htmlgui.RestApiDocumentationGenerator;
import uk.co.compendiumdev.thingifier.swaggerizer.Swaggerizer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static spark.Spark.*;

public class ThingifierRestServer {


    private final ThingifierApiDefn apiDefn;
    private String urlPath;
    private List<SparkRequestResponseHook> preSparkHttpRequestHooks;
    private List<SparkRequestResponseHook> postSparkHttpResponseHooks;
    private List<InternalHttpRequestHook> preInternalHttpRequestHooks;
    private List<InternalHttpResponseHook> postInternalHttpResponseHooks;
    private List<HttpApiRequestHook> httpApiRequestHooks;
    private final List<HttpApiResponseHook> httpApiResponseHooks;


    // todo : we should be able to configure the API routing for authorisation and support logging
    // todo: we should split the REST and Documentation stuff out and have thingifier as a core non-http set of libraries


    public ThingifierRestServer(final String path,
                                final Thingifier thingifier,
                                ThingifierApiDefn apiDefn,
                                DefaultGUIHTML guiManagement) {

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

        this.urlPath = null;
        // can set path, but if not set, pick up from requests
        if(path!=null && path.length()>0) {
            this.urlPath = path;
        }

        before((request, response) -> {

            if(this.urlPath==null){
                // capture the protocol and authority to use as rendered urls
                try{
                    final URL requestUrl = new URL(request.url());
                    this.urlPath = requestUrl.getProtocol() + "://" + requestUrl.getAuthority();
                }catch(MalformedURLException e){
                    System.out.println(request.url() + " " + e.getMessage());
                }
            }

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
        ApiRoutingDefinition routingDefinitions = new ApiRoutingDefinitionGenerator(thingifier).generate();


        // / - default for documentation
        get("/docs", (request, response) -> {
            response.type("text/html");
            response.status(200);
            return new RestApiDocumentationGenerator(thingifier, guiManagement).
                    getApiDocumentation(routingDefinitions, apiDefn.getAdditionalRoutes(), this.urlPath);
        });

        // now that we have an api definition we should be able to generate swagger
        get("/docs/swagger", (request, response) -> {
            response.type("text/html");
            response.status(200);
            String nameprefix = "";
            try {
                nameprefix = apiDefn.getThingifier().getTitle().replace(" ", "-") + "-";
            }catch (Exception e){
                // invalid apidefn setup
                System.out.println("Possibly incomplete swagger generation, api not defined from model");
            }
            response.header("Content-Type", "application/octet-stream");
            response.header("Content-Disposition",
                    String.format("attachment; filename=\"%sswagger.json\"",nameprefix));
            return new Swaggerizer(apiDefn).asJson();
        });


        guiManagement.appendMenuItem("API documentation","/docs");


        for (RoutingDefinition defn : routingDefinitions.definitions()) {
            switch (defn.verb()) {
                case GET:
                    if (defn.status().isReturnedFromCall()) {
                        get(defn.url(), (request, response) -> {
                            //return apiBridge.get(request, response);
                            final HttpApiRequest theRequest = SparkToHttpApiRequest.convert(request);
                            final HttpApiResponse theResponse = apiBridge.get(theRequest);
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
            }
        }

        // Undocumented admin interface - this needs to be authentication controlled and toggelable from command line
        get("/admin/query/*", (request, response) -> {
            //return apiBridge.query(request, response, request.splat()[0]);
            final HttpApiRequest theRequest = SparkToHttpApiRequest.convert(request);
            final HttpApiResponse theResponse = apiBridge.query(theRequest, request.splat()[0]);
            return HttpApiResponseToSpark.convert(theResponse, response);
        });

        // Undocumented admin interface - this needs to be authentication controlled and toggelable from command line
        post("/admin/data/thingifier", (request, response) -> {
            thingifier.clearAllData();
            response.status(200);
            return "";
        });

        // TODO : allow this to be overwritten by config
        // nothing else is supported
        head("*", (request, response) -> {
            response.status(404);
            return "";
        });
        get("*", (request, response) -> {
            response.status(404);
            return "";
        });
        options("*", (request, response) -> {
            response.status(404);
            return "";
        });
        put("*", (request, response) -> {
            response.status(404);
            return "";
        });
        post("*", (request, response) -> {
            response.status(404);
            return "";
        });
        patch("*", (request, response) -> {
            response.status(404);
            return "";
        });
        delete("*", (request, response) -> {
            response.status(404);
            return "";
        });

        exception(RuntimeException.class, (e, request, response) -> {
            response.status(400);
            response.body(getExceptionErrorResponse(e, request));
        });

        exception(Exception.class, (e, request, response) -> {
            response.status(500);
            response.body(getExceptionErrorResponse(e, request));
        });

    }

    private String getExceptionErrorResponse(final Exception e, final Request request) {
        if(e.getMessage()==null) {
            return ApiResponseError.asAppropriate(request.headers("Accept"), e.toString());
        }else{
            return ApiResponseError.asAppropriate(request.headers("Accept"), e.getMessage());
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

    public void registerHttpApiRequestHook(final HttpApiRequestHook hook) {
        // pre-request hooks run pre-every-api-request
        httpApiRequestHooks.add(hook);
    }

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
