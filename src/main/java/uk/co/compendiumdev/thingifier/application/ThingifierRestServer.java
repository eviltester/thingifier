package uk.co.compendiumdev.thingifier.application;

import org.json.JSONObject;
import org.json.XML;
import spark.Response;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.*;
import uk.co.compendiumdev.thingifier.reporting.ThingReporter;

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

    // todo : we should be able to configure the API routing for authorisation and support logging


    /*
    TODO: need to tidy up the return objects
     - use single except for xml where <projects><project><guid>...
     - for single instance return {... details ...} but xml <todo> .... details ... </todo>

    { "todos" : [ { "doneStatus" : "FALSE",  "guid" : "b1a71c87-fe7b-4595-9131-86230dd55dd4",  "description" : "",  "title" : "file paperwork"},  { "doneStatus" : "FALSE",  "guid" : "9af3f230-db65-449e-accc-075b4244331f",  "description" : "",  "title" : "scan paperwork"}]}
<todos><doneStatus>FALSE</doneStatus><guid>b1a71c87-fe7b-4595-9131-86230dd55dd4</guid><description/><title>file paperwork</title></todos><todos><doneStatus>FALSE</doneStatus><guid>9af3f230-db65-449e-accc-075b4244331f</guid><description/><title>scan paperwork</title></todos>

org.json.JSONException: A JSONObject text must begin with '{' at 1 [character 2 line 1]
{ "projects" : [ { "guid" : "93327398-a148-4ca7-9d7c-bdf6d9d80cb4",  "description" : "",  "active" : "TRUE",  "completed" : "FALSE",  "title" : "Office Work"}]}
<projects><guid>93327398-a148-4ca7-9d7c-bdf6d9d80cb4</guid><description/><active>TRUE</active><completed>FALSE</completed><title>Office Work</title></projects>

org.json.JSONException: A JSONObject text must begin with '{' at 1 [character 2 line 1]
{ "todos" : [ { "doneStatus" : "FALSE",  "guid" : "b1a71c87-fe7b-4595-9131-86230dd55dd4",  "description" : "",  "title" : "file paperwork"},  { "doneStatus" : "FALSE",  "guid" : "9af3f230-db65-449e-accc-075b4244331f",  "description" : "",  "title" : "scan paperwork"}]}
<todos><doneStatus>FALSE</doneStatus><guid>b1a71c87-fe7b-4595-9131-86230dd55dd4</guid><description/><title>file paperwork</title></todos><todos><doneStatus>FALSE</doneStatus><guid>9af3f230-db65-449e-accc-075b4244331f</guid><description/><title>scan paperwork</title></todos>

org.json.JSONException: A JSONObject text must begin with '{' at 1 [character 2 line 1]
{ "projects" : [ { "guid" : "93327398-a148-4ca7-9d7c-bdf6d9d80cb4",  "description" : "",  "active" : "TRUE",  "completed" : "FALSE",  "title" : "Office Work"}]}
<projects><guid>93327398-a148-4ca7-9d7c-bdf6d9d80cb4</guid><description/><active>TRUE</active><completed>FALSE</completed><title>Office Work</title></projects>
     */


    public ThingifierRestServer(String[] args, String path, Thingifier thingifier) {


        before((request, response) -> {


            // Prototype xml json to see if it works in principle
            try {
                System.out.println(request.body());
                new JSONObject(request.body());
                System.out.println(XML.toString(new JSONObject(request.body())));
            }catch (Exception e){
                System.out.println(e);
            }

            // force json
            response.type("application/json");
        });

        after((request, response) -> {
            // Prototype xml json to see if it works in principle
            try {
                System.out.println(response.body());
                System.out.println(XML.toString(new JSONObject(response.body())));
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
                            return apiResponse.getBody();
                        });
                    }
                    break;
                case POST:
                    if(defn.status().isReturnedFromCall()) {
                        post(defn.url(), (request, response) -> {
                            ApiResponse apiResponse = thingifier.api().post(justThePath(request.pathInfo()), request.body());
                            response.status(apiResponse.getStatusCode());
                            addHeaders(apiResponse.getHeaders(),response);
                            return apiResponse.getBody();
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
                            return apiResponse.getBody();});
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
                            ApiResponse apiResponse = thingifier.api().put(justThePath(request.pathInfo()), request.body());
                            response.status(apiResponse.getStatusCode());
                            addHeaders(apiResponse.getHeaders(),response);
                            return apiResponse.getBody();
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
            response.body(ApiResponse.getErrorMessageJson(e.getMessage()));
        });

        exception(Exception.class, (e, request, response) -> {
            response.status(500);
            response.body(ApiResponse.getErrorMessageJson(e.getMessage()));
        });

    }

    private void addHeaders(Set<Map.Entry<String, String>> headers, Response response) {
        for(Map.Entry<String, String> header : headers){
            response.header(header.getKey(), header.getValue());
        }
    }
}
