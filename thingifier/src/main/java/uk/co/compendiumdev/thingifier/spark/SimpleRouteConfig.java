package uk.co.compendiumdev.thingifier.spark;

import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Arrays;
import java.util.List;

import static spark.Spark.*;

public class SimpleRouteConfig {

    private final String endpoint;

    public SimpleRouteConfig(String endpoint){
        this.endpoint = endpoint;
    }

    public SimpleRouteConfig status(final int statusCode, final String...verbs) {
        SimpleRouteConfig.routeStatus(statusCode, endpoint, verbs);
        return this;
    }

    public SimpleRouteConfig statusWhenNot(final int statusCode, final String...excludedVerbs) {
        SimpleRouteConfig.routeStatusWhenNot(statusCode, endpoint, excludedVerbs);
        return this;
    }

    /**
     * for each verb, create an end point routing
     * that returns the given status code
     *  @param statuscode
     * @param endpoint
     * @param verbs
     * @return
     */
    public static void routeStatus(final int statuscode, final String endpoint, final String... verbs) {


        for(String verb : verbs){
            String matchVerb = verb.trim().toLowerCase();

            Route route = (Request request, Response result) -> {
                result.status(statuscode);
                return "";
            };

            switch (matchVerb){
                case "get":
                    get(endpoint, route);
                    break;
                case "head":
                    head(endpoint, route);
                    break;
                case "options":
                    options(endpoint, route);
                    break;
                case "post":
                    post(endpoint, route);
                    break;
                case "put":
                    put(endpoint, route);
                    break;
                case "patch":
                    patch(endpoint, route);
                    break;
                case "delete":
                    delete(endpoint, route);
                    break;
                case "trace":
                    trace(endpoint, route);
                    break;
            }
        }
    }

    /**
     * for each verb not listed, create an end point routing
     * that returns the given status code
     *
     * @param statuscode
     * @param endpoint
     * @param excludedVerbs
     */
    public static void routeStatusWhenNot(final int statuscode, final String endpoint,
                                     final String... excludedVerbs) {

        String[] verbs = {"get", "options", "head", "put",
                        "post", "patch", "trace", "delete"};

        List<String> excluded = Arrays.asList(excludedVerbs);

        for(String verb : verbs){
            if(!excluded.contains(verb)){
                routeStatus(statuscode, endpoint, verb);
            }
        }
    }


}
