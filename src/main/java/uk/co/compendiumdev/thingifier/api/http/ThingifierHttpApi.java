package uk.co.compendiumdev.thingifier.api.http;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;

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

//    public String post(final Request request, final Response response) {
//        ApiResponse apiResponse = thingifier.api().post(justThePath(request.pathInfo()), justTheBody(request));
//        return responseBodyFor(headersAsMap(request), apiResponse, response);
//    }
//
//    public String delete(final Request request, final Response response) {
//        ApiResponse apiResponse = thingifier.api().delete(justThePath(request.pathInfo()));
//        return responseBodyFor(headersAsMap(request), apiResponse, response);
//    }
//
//    public String put(final Request request, final Response response) {
//        ApiResponse apiResponse = thingifier.api().put(justThePath(request.pathInfo()), justTheBody(request));
//        return responseBodyFor(headersAsMap(request), apiResponse, response);
//    }
//
//
//    public String query(final Request request, final Response response, final String query) {
//        ApiResponse apiResponse = thingifier.api().get(query);
//        return responseBodyFor(headersAsMap(request), apiResponse, response);
//    }


//
//
//
//    private void addHeaders(final Set<Map.Entry<String, String>> headers, final Response response) {
//        for (Map.Entry<String, String> header : headers) {
//            response.header(header.getKey(), header.getValue());
//        }
//    }
//
//    private String justThePath(final String path) {
//        if (path.startsWith("/")) {
//            return path.substring(1);
//        }
//        return path;
//    }
//
//
//    private Map justTheBody(final Request request) {
//
//        // TODO refactor this out into a class that has unit tests
//        // because we are using crude XML and JSON parsing
//        // <project><title>My posted todo on the project</title></project>
//        // would become {"project":{"title":"My posted todo on the project"}}
//        // when we want {"title":"My posted todo on the project"}
//        // this is just a quick hack to amend it to support XML
//        // TODO: try to change this in the future to make it more robust, perhaps the API shouldn't take a String as the body, it should take a parsed class?
//        // TODO: BUG - since we remove the wrapper we might send in a POST <project><title>My posted todo on the project</title></project> to /todo and it will work fine if the fields are the same
//        if (request.headers("Content-Type") != null && request.headers("Content-Type").endsWith("/xml")) {
//
//            // PROTOTYPE XML Conversion
//            System.out.println(request.body());
//            System.out.println(XML.toJSONObject(request.body()).toString());
//            JSONObject conv = XML.toJSONObject(request.body());
//            if (conv.keySet().size() == 1) {
//                // if the key is an entity type then we just want the body
//                ArrayList<String> keys = new ArrayList<String>(conv.keySet());
//
//                if (thingifier.hasThingNamed(keys.get(0))) {
//                    // just the body
//                    String justTheBody = conv.get(keys.get(0)).toString();
//                    System.out.println(justTheBody);
//                    Map args = new Gson().fromJson(justTheBody, Map.class);
//                    return args;
//                }
//
//            }
//        }
//
//        return new Gson().fromJson(request.body(), Map.class);
//    }
//


}
