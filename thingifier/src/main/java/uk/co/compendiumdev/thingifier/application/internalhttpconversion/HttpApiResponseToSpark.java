package uk.co.compendiumdev.thingifier.application.internalhttpconversion;

import spark.Response;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;

import java.util.Set;

public class HttpApiResponseToSpark {

    public static String convert(HttpApiResponse internalResponse, Response response){
        updateResponseFromHttpResponse(internalResponse, response);
        return internalResponse.getBody();
    }

    private static void updateResponseFromHttpResponse(final HttpApiResponse httpResponse,
                                                       final Response response) {

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
