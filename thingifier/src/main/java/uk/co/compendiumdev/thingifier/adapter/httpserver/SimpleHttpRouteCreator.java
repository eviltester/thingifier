package uk.co.compendiumdev.thingifier.adapter.httpserver;

import static uk.co.compendiumdev.thingifier.adapter.httpserver.ServerRoutes.*;

import java.util.List;
import uk.co.compendiumdev.thingifier.api.http.headers.headerparser.AcceptHeaderParser;

/*
   Simple route config is a 'no code' handler for a verb
   it just returns the defined status code
*/
public class SimpleHttpRouteCreator {

    private final String endpoint;

    public SimpleHttpRouteCreator(String endpoint) {
        this.endpoint = endpoint;
    }

    public SimpleHttpRouteCreator handledRouteStatus(
            final String verb, HttpRouteHandler routeHandler) {

        addHandler(endpoint, verb, routeHandler);
        return this;
    }

    public SimpleHttpRouteCreator status(
            final int statusCode, boolean allowResponseIndexing, final List<String> verbs) {

        routeStatus(statusCode, endpoint, allowResponseIndexing, verbs);
        return this;
    }

    public SimpleHttpRouteCreator statusWhenNot(
            final int statusCode, final List<String> excludedVerbs) {

        routeStatusWhenNot(statusCode, endpoint, excludedVerbs);
        return this;
    }

    /**
     * for each verb, create an end point routing that returns the given status code
     *
     * @param statuscode
     * @param endpoint
     * @param verbs
     */
    public static void routeStatus(
            final int statuscode,
            final String endpoint,
            boolean allowResponseIndexing,
            final List<String> verbs) {

        for (String verb : verbs) {
            String matchVerb = verb.trim().toLowerCase();

            // TODO: this should really reject if the the accept header is one that the main api
            // does not accept
            HttpRouteHandler route =
                    (request, result) -> {
                        final AcceptHeaderParser acceptParser =
                                new AcceptHeaderParser(request.header("Accept"));
                        String preferred =
                                new AcceptHeaderParser(request.header("Accept")).getPreferredType();
                        if (preferred == null
                                || preferred.trim().isEmpty()
                                || acceptParser.willAcceptAnything()) {
                            preferred = "application/json"; // hard coded default
                        }
                        result.header("Content-Type", preferred);
                        if (!allowResponseIndexing) {
                            result.header("x-robots-tag", "noindex");
                        }
                        result.status(statuscode);
                        return "";
                    };

            addHandler(endpoint, matchVerb, route);
        }
    }

    public static void addHandler(
            final String endpoint, final String matchVerb, final HttpRouteHandler route) {
        switch (matchVerb.toLowerCase()) {
            case "get":
                get(endpoint, route);
                break;
            case "head":
                head(endpoint, route);
                break;
            case "query":
                query(endpoint, route);
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
            default:
                break;
        }
    }

    /**
     * for each verb not listed, create an end point routing that returns the given status code
     *
     * @param statuscode
     * @param endpoint
     * @param excludedVerbs
     */
    public static void routeStatusWhenNot(
            final int statuscode, final String endpoint, final List<String> excludedVerbs) {

        String[] verbs = {
            "get", "options", "head", "query", "put", "post", "patch", "trace", "delete"
        };

        for (String verb : verbs) {
            if (!excludedVerbs.contains(verb)) {
                routeStatus(statuscode, endpoint, true, List.of(verb));
            }
        }
    }
}
