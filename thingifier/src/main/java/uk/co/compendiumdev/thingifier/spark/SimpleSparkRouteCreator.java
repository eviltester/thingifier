package uk.co.compendiumdev.thingifier.spark;

import spark.Request;
import spark.Response;
import spark.Route;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;

import java.util.List;

import static spark.Spark.*;

/*
    Simple route config is a 'no code' handler for a verb
    it just returns the defined status code
 */
public class SimpleSparkRouteCreator {

    private final String endpoint;

    public SimpleSparkRouteCreator(String endpoint){
        this.endpoint = endpoint;
    }

    public SimpleSparkRouteCreator handledRouteStatus(final String verb, Route routeHandler) {

        addHandler(endpoint, verb, routeHandler);
        return this;
    }

    @Deprecated // should pass in the allow response indexing
    public SimpleSparkRouteCreator status(final int statusCode, final List<String> verbs) {
        return status(statusCode, true, verbs);
    }

    public SimpleSparkRouteCreator status(final int statusCode, boolean allowResponseIndexing, final List<String> verbs) {

        routeStatus(statusCode, endpoint, allowResponseIndexing, verbs);
        return this;
    }

    public SimpleSparkRouteCreator statusWhenNot(final int statusCode, final List<String> excludedVerbs) {

        routeStatusWhenNot(statusCode, endpoint, excludedVerbs);
        return this;
    }

    /**
     * for each verb, create an end point routing
     * that returns the given status code
     *  @param statuscode
     * @param endpoint
     * @param verbs
     */
    public static void routeStatus(final int statuscode, final String endpoint, boolean allowResponseIndexing, final List<String> verbs) {

        for(String verb : verbs){
            String matchVerb = verb.trim().toLowerCase();

            // TODO: this should really reject if the the accept header is one that the main api does not accept
            Route route = (Request request, Response result) -> {
                final AcceptHeaderParser acceptParser = new AcceptHeaderParser(request.headers("Accept"));
                String preferred = new AcceptHeaderParser(request.headers("Accept")).getPreferredType();
                if(preferred == null || preferred.trim().isEmpty() || acceptParser.willAcceptAnything()){
                    preferred="application/json"; // hard coded default
                }
                result.header("Content-Type", preferred);
                if(!allowResponseIndexing) {
                    result.header("x-robots-tag", "noindex");
                }
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
                                          final List<String> excludedVerbs) {

        String[] verbs = {"get", "options", "head", "put",
                "post", "patch", "trace", "delete"};

        for(String verb : verbs){
            if(!excludedVerbs.contains(verb)){
                routeStatus(statuscode, endpoint, true, List.of(verb));
            }
        }
    }

}
