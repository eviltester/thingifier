package uk.co.compendiumdev.thingifier.application;

import spark.Request;
import spark.Response;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsJson;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsXml;

public class HttpApiResponse {
    private final Request request;
    private final Response response;
    private final ApiResponse apiResponse;

    public HttpApiResponse(Request request, Response response, ApiResponse apiResponse) {
        this.request = request;
        this.response = response;
        this.apiResponse = apiResponse;
    }

    public String getBody() {

        boolean asJson=true; //default to json

        if(request.headers("Accept")!=null) {
            if (request.headers("Accept").endsWith("/xml")) {
                asJson = false;
            }
            if (request.headers("Accept").endsWith("/json")) {
                // todo : should probably return a 406 Not Acceptable http status message if we don't support the asked for type
            }
        }

        String returnBody="";

        if(asJson){
            returnBody = new ApiResponseAsJson(apiResponse).getJson();
            response.type("application/json");
        }else{
            returnBody = new ApiResponseAsXml(apiResponse).getXml();
            response.type("application/xml");
        }

        return returnBody;
    }
}
