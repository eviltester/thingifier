package uk.co.compendiumdev.thingifier.application;

import org.json.JSONObject;
import org.json.XML;
import spark.Request;
import spark.Response;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class ThingifierHttpApiBridge {

    private final Thingifier thingifier;

    public ThingifierHttpApiBridge(final Thingifier aThingifier) {
        this.thingifier = aThingifier;
    }

    public String get(final Request request, final Response response) {
        ApiResponse apiResponse = thingifier.api().get(justThePath(request.pathInfo()));
        return responseBodyFor(request.headers(), apiResponse, response);
    }

    public String post(final Request request, final Response response) {
        ApiResponse apiResponse = thingifier.api().post(justThePath(request.pathInfo()), justTheBody(request));
        return responseBodyFor(request.headers(), apiResponse, response);
    }

    public String delete(final Request request, final Response response) {
        ApiResponse apiResponse = thingifier.api().delete(justThePath(request.pathInfo()));
        return responseBodyFor(request.headers(), apiResponse, response);
    }

    public String put(final Request request, final Response response) {
        ApiResponse apiResponse = thingifier.api().put(justThePath(request.pathInfo()), justTheBody(request));
        return responseBodyFor(request.headers(), apiResponse, response);
    }


    public String query(final Request request, final Response response, final String query) {
        ApiResponse apiResponse = thingifier.api().get(query);
        return responseBodyFor(request.headers(), apiResponse, response);
    }

    private String responseBodyFor(final Set<String> headers, final ApiResponse apiResponse, final Response response) {
        HttpApiResponse httpResponse = new HttpApiResponse(headers, apiResponse);

        updateResponseFromHttpResponse(httpResponse, response);

        return httpResponse.getBody();
    }


    private void updateResponseFromHttpResponse(final HttpApiResponse httpResponse, final Response response) {

        response.status(httpResponse.getStatusCode());
        addHeaders(httpResponse.getHeaders(), response);
        addType(httpResponse, response);

    }

    private void addType(final HttpApiResponse apiResponse, final Response response) {
        if (apiResponse.hasType()){
            response.type(apiResponse.getType());
        }
    }



    private void addHeaders(final Set<Map.Entry<String, String>> headers, final Response response) {
        for (Map.Entry<String, String> header : headers) {
            response.header(header.getKey(), header.getValue());
        }
    }

    private String justThePath(final String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }


    private String justTheBody(final Request request) {

        // TODO refactor this out into a class that has unit tests
        // because we are using crude XML and JSON parsing
        // <project><title>My posted todo on the project</title></project>
        // would become {"project":{"title":"My posted todo on the project"}}
        // when we want {"title":"My posted todo on the project"}
        // this is just a quick hack to amend it to support XML
        // TODO: try to change this in the future to make it more robust, perhaps the API shouldn't take a String as the body, it should take a parsed class?
        // TODO: BUG - since we remove the wrapper we might send in a POST <project><title>My posted todo on the project</title></project> to /todo and it will work fine if the fields are the same
        if (request.headers("Content-Type") != null && request.headers("Content-Type").endsWith("/xml")) {

            // PROTOTYPE XML Conversion
            System.out.println(request.body());
            System.out.println(XML.toJSONObject(request.body()).toString());
            JSONObject conv = XML.toJSONObject(request.body());
            if (conv.keySet().size() == 1) {
                // if the key is an entity type then we just want the body
                ArrayList<String> keys = new ArrayList<String>(conv.keySet());

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



}
