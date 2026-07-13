package uk.co.compendiumdev.thingifier.adapter.internalhttp.conversion;

import spark.Response;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;

public class SparkResponseToInternalHttpResponse {

    public static InternalHttpResponse convert(Response response) {
        InternalHttpResponse internalResponse = new InternalHttpResponse();

        internalResponse.setStatus(response.status());
        internalResponse.setType(response.type()); // content type
        internalResponse.setBody(response.body());

        for (String headerName : response.raw().getHeaderNames()) {
            internalResponse.setHeader(headerName, response.raw().getHeader(headerName));
        }

        return internalResponse;
    }

    public static void updateResponseFromInternal(
            final Response response, final InternalHttpResponse httpResponse) {
        InternalHttpResponseToSpark.updateResponseFromInternal(response, httpResponse);
    }
}
