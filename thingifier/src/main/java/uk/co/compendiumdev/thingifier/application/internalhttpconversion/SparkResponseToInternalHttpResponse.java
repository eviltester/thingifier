package uk.co.compendiumdev.thingifier.application.internalhttpconversion;

import spark.Response;

import java.util.Set;

public class SparkResponseToInternalHttpResponse {

    public static InternalHttpResponse convert(Response response){
        InternalHttpResponse internalResponse = new InternalHttpResponse();

        internalResponse.setStatus(response.status());
        internalResponse.setType(response.type()); // content type
        internalResponse.setBody(response.body());

        for(String headerName : response.raw().getHeaderNames()){
            internalResponse.setHeader(headerName, response.raw().getHeader(headerName));
        }

        return internalResponse;
    }

    public static void updateResponseFromInternal(final Response response,
                                                  final InternalHttpResponse httpResponse) {

        response.status(httpResponse.getStatusCode());

        if (httpResponse.hasType()) {
            response.type(httpResponse.getType());
        }

        final Set<String> keys = httpResponse.getHeaders().keySet();
        for (String headerKey : keys) {
            response.raw().setHeader(headerKey, httpResponse.getHeaders().get(headerKey));
        }

        response.body(httpResponse.getBody());
    }
}
