package uk.co.compendiumdev.thingifier.application.internalhttpconversion;

import spark.Request;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class SparkToHttpApiRequest {

    public static HttpApiRequest convert(final Request request) {
        HttpApiRequest apiRequest =
                new HttpApiRequest(request.pathInfo()).
                    setHeaders(headersAsMap(request)).
                    setBody(request.body()).
                    setQueryParams(queryParamsAsMap(request)).
                    setRawQueryParams(rawQueryParamsAsMap(request)).
                    setFilterableQueryParams(request.queryString()). // use our own parser to get params
                    setVerb(request.requestMethod()).
                    setUrl(request.url()).
                    setIP(request.ip()).
                        //this is a standard parser and splits on = it does not parse as we need for sorting, filtering etc. e.g. id>=17 becomes id> 17 not id >=17
                    setUrlParams(request.params()).
                    // the default Spark headers is a map so filters out duplicates, allow working with the raw list when necessary
                    setRawHeaders(getRawHeadersList(request.raw())
                );

        return apiRequest;
    }

    private static List<StringPair> getRawHeadersList(HttpServletRequest raw){

        ArrayList<StringPair> headersList = new ArrayList<StringPair>();

        for (Enumeration<String> headerNames = raw.getHeaderNames(); headerNames.hasMoreElements();){
            String headerName = headerNames.nextElement();
            for (Enumeration<String> headerValues = raw.getHeaders(headerName); headerValues.hasMoreElements();){
                String headerValue = headerValues.nextElement();
                headersList.add(new StringPair(headerName, headerValue));
            }
        }

        return headersList;
    }

    private static Map<String, String> rawQueryParamsAsMap(final Request request) {
        Map<String, String> params = new HashMap<>();

        // need to parse the request.queryString() or we lose param content
        for(String paramName : request.queryParams()){
            // todo: figure out what to do if more than one in each value, currently we lose the values
            String paramValue = request.queryParams(paramName);
            if(paramValue==null){
                paramValue="";
            }
            params.put(paramName, paramValue);
        }

        return params;
    }

    private static Map<String, String> headersAsMap(final Request request) {
        final Set<String> headerNames = request.headers();
        final Map<String, String> headers = new HashMap<>();

        for (String header : headerNames) {
            headers.put(header, request.headers(header));
        }
        return headers;
    }

    // query params request?param1=value&param2=value;
    private static Map<String, String> queryParamsAsMap(final Request request) {

        Map<String, String> params = new HashMap<>();

        for(String paramName : request.queryParams()){
            // todo: figure out what to do if more than one in each value, currently we lose the values
            String paramValue = request.queryParamsValues(paramName)[0];
            if(paramValue==null){
                paramValue="";
            }
            params.put(paramName, paramValue);
        }

        return params;
    }
}
