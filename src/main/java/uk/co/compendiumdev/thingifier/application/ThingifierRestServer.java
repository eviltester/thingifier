package uk.co.compendiumdev.thingifier.application;

import org.json.JSONObject;
import org.json.XML;
import spark.Request;
import spark.Response;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.*;
import uk.co.compendiumdev.thingifier.reporting.ThingReporter;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static spark.Spark.*;

public class ThingifierRestServer {

    private String justThePath(String path){
        if(path.startsWith("/")){
            return path.substring(1);
        }
        return path;
    }


    private String justTheBody(Request request, Thingifier thingifier) {

        // because we are using crude XML and JSON parsing
        // <project><title>My posted todo on the project</title></project>
        // would become {"project":{"title":"My posted todo on the project"}}
        // when we want {"title":"My posted todo on the project"}
        // this is just a quick hack to amend it to support XML
        // TODO: try to change this in the future to make it more robust, perhaps the API shouldn't take a String as the body, it should take a parsed class?
        // TODO: BUG - since we remove the wrapper we might send in a POST <project><title>My posted todo on the project</title></project> to /todo and it will work fine if the fields are the same
        if(request.headers("Content-Type")!=null && request.headers("Content-Type").endsWith("/xml")) {

            // PROTOTYPE XML Conversion
            System.out.println(request.body());
            System.out.println(XML.toJSONObject(request.body()).toString());
            JSONObject conv = XML.toJSONObject(request.body());
            if (conv.keySet().size() == 1) {
                // if the key is an entity type then we just want the body
                ArrayList<String> keys = new ArrayList<String>();
                keys.addAll(conv.keySet());
                if (thingifier.hasThingNamed(keys.get(0))) {
                    // just the body
                    String justTheBody = conv.get(keys.get(0)).toString();
                    System.out.println(justTheBody);
                    return justTheBody;
                }

            }
        }

        return request.body();
    }

    // todo : we should be able to configure the API routing for authorisation and support logging
    // todo : honour different Content-Type headers at the moment we assume and treat it as application/json


    public ThingifierRestServer(String[] args, String path, Thingifier thingifier) {


        before((request, response) -> {

            // TODO: wrap this in a --verbose option
            try {
                System.out.println("**REQUEST**");
                System.out.println(request.url());
                System.out.println(request.pathInfo());
                System.out.println(request.body());
            }catch (Exception e){
                System.out.println(e);
            }


            // TODO: wrap this in a --verbose option
            System.out.println("**PROCESSING**");
        });

        after((request, response) -> {
            // TODO: wrap this in a --verbose option
            try {
                System.out.println("**RESPONSE**");
                System.out.println(response.status());
                System.out.println(response.body());
            }catch (Exception e){
                System.out.println(e);
            }
        });

        // TODO : this now needs HTTP level automated coverage

        // configure it based on a thingifier
        ApiRoutingDefinition routingDefinitions = new ApiRoutingDefinitionGenerator(thingifier).generate();


        // / - default for documentation
        get("/", (request, response) -> {
            response.type("text/html");
            response.status(200);
            return new ThingReporter(thingifier).getApiDocumentation(routingDefinitions);
        });


        for(RoutingDefinition defn : routingDefinitions.definitions()){
            switch (defn.verb()){
                case GET:
                    if(defn.status().isReturnedFromCall()) {
                        get(defn.url(), (request, response) -> {
                            ApiResponse apiResponse = thingifier.api().get(justThePath(request.pathInfo()));
                            response.status(apiResponse.getStatusCode());
                            addHeaders(apiResponse.getHeaders(),response);
                            return new HttpApiResponse(request, response, apiResponse).getBody();
                            //return apiResponse.getBody();
                        });
                    }
                    break;
                case POST:
                    if(defn.status().isReturnedFromCall()) {
                        post(defn.url(), (request, response) -> {
                            ApiResponse apiResponse = thingifier.api().post(justThePath(request.pathInfo()), justTheBody(request, thingifier));
                            response.status(apiResponse.getStatusCode());
                            addHeaders(apiResponse.getHeaders(),response);
                            return new HttpApiResponse(request, response, apiResponse).getBody();
                            //return apiResponse.getBody();
                        });
                    }
                    break;
                case HEAD:
                    if(!defn.status().isReturnedFromCall()) {
                        head(defn.url(), (request, response) -> {
                            response.status(defn.status().value());return "";
                        });
                    }
                    break;
                case DELETE:
                    if(!defn.status().isReturnedFromCall()) {
                        delete(defn.url(), (request, response) -> {
                            response.status(defn.status().value());return "";
                        });
                    }else{
                        delete(defn.url(), (request, response) -> {
                            ApiResponse apiResponse = thingifier.api().delete(justThePath(request.pathInfo()));
                            response.status(apiResponse.getStatusCode());
                            addHeaders(apiResponse.getHeaders(),response);
                            return new HttpApiResponse(request, response, apiResponse).getBody();
                            //return apiResponse.getBody();
                            });
                    }
                    break;
                case PATCH:
                    if(!defn.status().isReturnedFromCall()) {
                        patch(defn.url(), (request, response) -> {
                            response.status(defn.status().value());return "";
                        });
                    }
                    break;
                case PUT:
                    if(!defn.status().isReturnedFromCall()) {
                        put(defn.url(), (request, response) -> {
                            response.status(defn.status().value());return "";
                        });
                    }else{
                        put(defn.url(), (request, response) -> {
                            ApiResponse apiResponse = thingifier.api().put(justThePath(request.pathInfo()), justTheBody(request, thingifier));
                            response.status(apiResponse.getStatusCode());
                            addHeaders(apiResponse.getHeaders(),response);
                            return new HttpApiResponse(request, response, apiResponse).getBody();
                            //return apiResponse.getBody();
                        });
                    }
                    break;
                case OPTIONS:
                    if(!defn.status().isReturnedFromCall()) {
                        options(defn.url(), (request, response) -> {
                            response.status(defn.status().value());
                            response.header(defn.header(), defn.headerValue());
                            return "";
                        });
                    }
                    break;
            }
        }


        // TODO : allow this to be overwritten by config
        // nothing else is supported
        head("*", (request, response) -> {response.status(404); return "";});
        get("*", (request, response) -> {response.status(404); return "";});
        options("*", (request, response) -> {response.status(404); return "";});
        put("*", (request, response) -> {response.status(404); return "";});
        post("*", (request, response) -> {response.status(404); return "";});
        patch("*", (request, response) -> {response.status(404); return "";});
        delete("*", (request, response) -> {response.status(404); return "";});

        exception(RuntimeException.class, (e, request, response) -> {
            response.status(400);
            response.body(ApiResponseError.asAppropriate(request.headers("Accept"),e.getMessage()));
        });

        exception(Exception.class, (e, request, response) -> {
            response.status(500);
            response.body(ApiResponseError.asAppropriate(request.headers("Accept"),e.getMessage()));
        });

    }



    private void addHeaders(Set<Map.Entry<String, String>> headers, Response response) {
        for(Map.Entry<String, String> header : headers){
            response.header(header.getKey(), header.getValue());
        }
    }
}
