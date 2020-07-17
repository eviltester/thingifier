package uk.co.compendiumdev.thingifier.application;

import org.eclipse.jetty.http.HttpFields;
import spark.Request;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseError;
import uk.co.compendiumdev.thingifier.api.routings.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.ApiRoutingDefinitionGenerator;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiResponseHook;
import uk.co.compendiumdev.thingifier.application.sparkhttpmessageHooks.SparkRequestResponseHook;
import uk.co.compendiumdev.thingifier.htmlgui.DefaultGUIHTML;
import uk.co.compendiumdev.thingifier.htmlgui.RestApiDocumentationGenerator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static spark.Spark.*;

public class ThingifierRestServer {

    private final List<RoutingDefinition> additionalRoutes;
    private String urlPath;
    private List<SparkRequestResponseHook> preRequestHooks;
    private List<SparkRequestResponseHook> postRequestHooks;
    private List<HttpApiRequestHook> httpApiRequestHooks;
    private final List<HttpApiResponseHook> httpApiResponseHooks;


    // todo : we should be able to configure the API routing for authorisation and support logging


    public ThingifierRestServer(final String path,
                                final Thingifier thingifier,
                                final List<RoutingDefinition> additionalDocumentedRoutes,
                                DefaultGUIHTML guiManagement) {

        this.additionalRoutes = additionalDocumentedRoutes;

        preRequestHooks = new ArrayList<>();
        postRequestHooks = new ArrayList<>();
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

            if(preRequestHooks!=null){
                for(SparkRequestResponseHook hook : preRequestHooks){
                    hook.run(request, response);
                }
            }

        });

        after((request, response) -> {
            if(postRequestHooks!=null){
                for(SparkRequestResponseHook hook : postRequestHooks){
                    hook.run(request, response);
                }
            }
        });

        // TODO : this now needs HTTP level automated coverage

        // configure it based on a thingifier
        ApiRoutingDefinition routingDefinitions = new ApiRoutingDefinitionGenerator(thingifier).generate();


        // / - default for documentation
        get("/docs", (request, response) -> {
            response.type("text/html");
            response.status(200);
            return new RestApiDocumentationGenerator(thingifier, guiManagement).
                    getApiDocumentation(routingDefinitions, additionalRoutes, this.urlPath);
        });

        guiManagement.addMenuItem("API documentation","/docs");


        for (RoutingDefinition defn : routingDefinitions.definitions()) {
            switch (defn.verb()) {
                case GET:
                    if (defn.status().isReturnedFromCall()) {
                        get(defn.url(), (request, response) -> {
                            return apiBridge.get(request, response);
                        });
                    }
                    break;
                case POST:
                    if (defn.status().isReturnedFromCall()) {
                        post(defn.url(), (request, response) -> {
                            return apiBridge.post(request, response);
                        });
                    }
                    break;
                case HEAD:
                    if (defn.status().isReturnedFromCall()) {
                        head(defn.url(), (request, response) -> {
                            return apiBridge.head(request, response);
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
                            return apiBridge.delete(request, response);
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
                            return apiBridge.put(request, response);
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
            return apiBridge.query(request, response, request.splat()[0]);
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
        preRequestHooks.add(hook);
    }

    public void registerPostRequestHook(final SparkRequestResponseHook hook) {
        // post-request hooks run after-every-response
        postRequestHooks.add(hook);
    }

    public void registerHttpApiRequestHook(final HttpApiRequestHook hook) {
        // pre-request hooks run pre-every-api-request
        httpApiRequestHooks.add(hook);
    }

    public void registerHttpApiResponseHook(final HttpApiResponseHook hook) {
        // pre-request hooks run pre-every-api-request
        httpApiResponseHooks.add(hook);
    }
}
