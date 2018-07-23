package uk.co.compendiumdev.thingifier.api.http;

import com.google.gson.Gson;
import org.json.JSONObject;
import org.json.XML;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;

import java.util.ArrayList;
import java.util.Map;

public class ThingifierHttpApi {

    private final Thingifier thingifier;

    public ThingifierHttpApi(final Thingifier aThingifier) {
        this.thingifier = aThingifier;
    }

    public HttpApiResponse get(final HttpApiRequest request) {
        ApiResponse apiResponse = thingifier.api().get(request.getPath());
        HttpApiResponse httpResponse = new HttpApiResponse(request.getHeaders(), apiResponse);
        return httpResponse;
    }

    public HttpApiResponse delete(final HttpApiRequest request) {
        ApiResponse apiResponse = thingifier.api().delete(request.getPath());
        HttpApiResponse httpResponse = new HttpApiResponse(request.getHeaders(), apiResponse);
        return httpResponse;
    }

    public HttpApiResponse query(final HttpApiRequest request, final String query) {

        ApiResponse apiResponse = thingifier.api().get(query);
        HttpApiResponse httpResponse = new HttpApiResponse(request.getHeaders(), apiResponse);
        return httpResponse;

    }

    public HttpApiResponse post(final HttpApiRequest request) {
        ApiResponse apiResponse = thingifier.api().post(request.getPath(), bodyAsMap(request));
        HttpApiResponse httpResponse = new HttpApiResponse(request.getHeaders(), apiResponse);
        return httpResponse;
    }

    public HttpApiResponse put(final HttpApiRequest request) {
        ApiResponse apiResponse = thingifier.api().put(request.getPath(), bodyAsMap(request));
        HttpApiResponse httpResponse = new HttpApiResponse(request.getHeaders(), apiResponse);
        return httpResponse;
    }




    private Map bodyAsMap(final HttpApiRequest request) {

        // TODO refactor this out into a class that has unit tests
        // because we are using crude XML and JSON parsing
        // <project><title>My posted todo on the project</title></project>
        // would become {"project":{"title":"My posted todo on the project"}}
        // when we want {"title":"My posted todo on the project"}
        // this is just a quick hack to amend it to support XML
        // TODO: try to change this in the future to make it more robust, perhaps the API shouldn't take a String as the body, it should take a parsed class?
        // TODO: BUG - since we remove the wrapper we might send in a POST <project><title>My posted todo on the project</title></project> to /todo and it will work fine if the fields are the same
        if (request.getHeader("Content-Type") != null && request.getHeader("Content-Type").endsWith("/xml")) {

            // PROTOTYPE XML Conversion
            System.out.println(request.getBody());
            System.out.println(XML.toJSONObject(request.getBody()).toString());
            JSONObject conv = XML.toJSONObject(request.getBody());
            if (conv.keySet().size() == 1) {
                // if the key is an entity type then we just want the body
                ArrayList<String> keys = new ArrayList<String>(conv.keySet());

                if (thingifier.hasThingNamed(keys.get(0))) {
                    // just the body
                    String justTheBody = conv.get(keys.get(0)).toString();
                    System.out.println(justTheBody);
                    Map args = new Gson().fromJson(justTheBody, Map.class);
                    return args;
                }

            }
        }

        return new Gson().fromJson(request.getBody(), Map.class);
    }

}
