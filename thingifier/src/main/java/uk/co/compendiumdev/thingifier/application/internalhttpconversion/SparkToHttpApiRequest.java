package uk.co.compendiumdev.thingifier.application.internalhttpconversion;

import spark.Request;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SparkToHttpApiRequest {

    public static HttpApiRequest convert(final Request request) {
        HttpApiRequest apiRequest =
                new HttpApiRequest(request.pathInfo()).
                    setHeaders(headersAsMap(request)).
                    setBody(request.body()).
                    setQueryParams(queryParamsAsMap(request)).
                    setVerb(request.requestMethod());

        return apiRequest;
    }

    private static Map<String, String> headersAsMap(final Request request) {
        final Set<String> headerNames = request.headers();
        final Map<String, String> headers = new HashMap<>();

        for (String header : headerNames) {
            headers.put(header, request.headers(header));
        }
        return headers;
    }

    private static Map<String, String> queryParamsAsMap(final Request request) {

        Map<String, String> params = new HashMap<>();

        for(String paramName : request.queryParams()){
            // todo: figure out what to do if more than one
            String paramValue = request.queryParamsValues(paramName)[0];
            if(paramValue==null){
                paramValue="";
            }
            params.put(paramName, paramValue);
        }

        return params;
    }
}
