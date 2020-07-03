package uk.co.compendiumdev.thingifier.api.http;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;

final public class ThingifierHttpApi {

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




    private BodyParser bodyAsMap(final HttpApiRequest request) {

        return new BodyParser(request, thingifier.getThingNames());


    }


}
