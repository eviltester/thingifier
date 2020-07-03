package uk.co.compendiumdev.thingifier.application;


import spark.Request;
import spark.Response;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final public class ThingifierHttpApiBridge {

    private final Thingifier thingifier;

    public ThingifierHttpApiBridge(final Thingifier aThingifier) {
        this.thingifier = aThingifier;
    }

    public String get(final Request request, final Response response) {

        final HttpApiRequest theRequest = new HttpApiRequest(request.pathInfo()).setHeaders(headersAsMap(request));
        final HttpApiResponse theResponse = new ThingifierHttpApi(thingifier).get(theRequest);
        updateResponseFromHttpResponse(theResponse, response);
        return theResponse.getBody();

    }

    public String post(final Request request, final Response response) {

        final HttpApiRequest theRequest = new HttpApiRequest(request.pathInfo()).setHeaders(headersAsMap(request)).setBody(request.body());
        final HttpApiResponse theResponse = new ThingifierHttpApi(thingifier).post(theRequest);
        updateResponseFromHttpResponse(theResponse, response);
        return theResponse.getBody();

    }

    public String delete(final Request request, final Response response) {

        final HttpApiRequest theRequest = new HttpApiRequest(request.pathInfo()).setHeaders(headersAsMap(request));
        final HttpApiResponse theResponse = new ThingifierHttpApi(thingifier).delete(theRequest);
        updateResponseFromHttpResponse(theResponse, response);
        return theResponse.getBody();

    }

    public String put(final Request request, final Response response) {

        final HttpApiRequest theRequest = new HttpApiRequest(request.pathInfo()).setHeaders(headersAsMap(request)).setBody(request.body());
        final HttpApiResponse theResponse = new ThingifierHttpApi(thingifier).put(theRequest);
        updateResponseFromHttpResponse(theResponse, response);
        return theResponse.getBody();

    }


    public String query(final Request request, final Response response, final String query) {

        final HttpApiRequest theRequest = new HttpApiRequest(request.pathInfo()).setHeaders(headersAsMap(request));
        final HttpApiResponse theResponse = new ThingifierHttpApi(thingifier).query(theRequest, query);
        updateResponseFromHttpResponse(theResponse, response);
        return theResponse.getBody();
    }

    private Map<String, String> headersAsMap(final Request request) {
        final Set<String> headerNames = request.headers();
        final Map<String, String> headers = new HashMap<>();

        for (String header : headerNames) {
            headers.put(header, request.headers(header));
        }
        return headers;
    }


    private void updateResponseFromHttpResponse(final HttpApiResponse httpResponse, final Response response) {

        response.status(httpResponse.getStatusCode());

        if (httpResponse.hasType()) {
            response.type(httpResponse.getType());
        }

        final Set<String> keys = httpResponse.getHeaders().keySet();
        for (String headerKey : keys) {
            response.header(headerKey, httpResponse.getHeaders().get(headerKey));
        }
    }


}
