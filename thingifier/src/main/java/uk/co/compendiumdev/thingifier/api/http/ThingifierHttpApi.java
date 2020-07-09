package uk.co.compendiumdev.thingifier.api.http;

import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;

final public class ThingifierHttpApi {

    private final Thingifier thingifier;
    private final JsonThing jsonThing;

    public ThingifierHttpApi(final Thingifier aThingifier) {
        this.thingifier = aThingifier;
        jsonThing = new JsonThing(thingifier.apiConfig().jsonOutput());
    }

    public HttpApiResponse get(final HttpApiRequest request) {
        ApiResponse apiResponse = thingifier.api().get(request.getPath());
        HttpApiResponse httpResponse = new HttpApiResponse(request.getHeaders(), apiResponse, jsonThing);
        return httpResponse;
    }

    public HttpApiResponse delete(final HttpApiRequest request) {
        ApiResponse apiResponse = thingifier.api().delete(request.getPath());
        HttpApiResponse httpResponse = new HttpApiResponse(request.getHeaders(), apiResponse, jsonThing);
        return httpResponse;
    }

    public HttpApiResponse query(final HttpApiRequest request, final String query) {

        ApiResponse apiResponse = thingifier.api().get(query);
        HttpApiResponse httpResponse = new HttpApiResponse(request.getHeaders(), apiResponse, jsonThing);
        return httpResponse;

    }

    public HttpApiResponse post(final HttpApiRequest request) {
        ApiResponse apiResponse = thingifier.api().post(request.getPath(), bodyAsMap(request));
        HttpApiResponse httpResponse = new HttpApiResponse(request.getHeaders(), apiResponse, jsonThing);
        return httpResponse;
    }

    public HttpApiResponse put(final HttpApiRequest request) {
        ApiResponse apiResponse = thingifier.api().put(request.getPath(), bodyAsMap(request));
        HttpApiResponse httpResponse = new HttpApiResponse(request.getHeaders(), apiResponse, jsonThing);
        return httpResponse;
    }




    private BodyParser bodyAsMap(final HttpApiRequest request) {

        return new BodyParser(request, thingifier.getThingNames());

    }


}
