package uk.co.compendiumdev.thingifier.spark;

import spark.Request;
import spark.Response;
import spark.Route;
import uk.co.compendiumdev.thingifier.api.http.AcceptHeaderParser;

import java.util.Arrays;
import java.util.List;

import static spark.Spark.*;

/*
    Simple route config is a 'no code' handler for a verb
    it just returns the defined status code
 */
public class SimpleRouteConfig {

    private final String endpoint;

    public SimpleRouteConfig(String endpoint){
        this.endpoint = endpoint;
    }

    public SimpleRouteConfig handledRouteStatus(
                                final String verb,
                                Route routeHandler) {

        SimpleRouteConfig.addHandler(endpoint, verb, routeHandler);
        return this;
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

            // TODO: this should really reject if the the accept header is one that the main api does not accept
            Route route = (Request request, Response result) -> {
                final AcceptHeaderParser acceptParser = new AcceptHeaderParser(request.headers("Accept"));
                String preferred = new AcceptHeaderParser(request.headers("Accept")).getPreferredType();
                if(preferred == null || preferred.trim().length()==0 || acceptParser.willAcceptAnything()){
                    preferred="application/json"; // hard coded default
                }
                result.header("Content-Type", preferred);
                result.status(statuscode);
                return "";
            };

            addHandler(endpoint, matchVerb, route);
        }
    }

    public static void addHandler(final String endpoint, final String matchVerb, final Route route) {
        switch (matchVerb.toLowerCase()){
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
