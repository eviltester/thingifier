package uk.co.compendiumdev.thingifier.adapter.internalhttp.conversion;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.adapter.internalhttp.InternalHttpResponse;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.JsonOutputConfig;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;

class HttpApiResponseToInternalHttpResponseTest {

    @Test
    void mapsLegacyApiResponseToInternalHttpResponse() {
        ThingifierApiConfig apiConfig = new ThingifierApiConfig("");
        ApiResponse apiResponse = ApiResponse.error(418, "short and stout");
        apiResponse.setHeader("X-Test", "value");

        HttpApiResponse httpResponse =
                new HttpApiResponse(
                        new HttpHeadersBlock(),
                        apiResponse,
                        new JsonThing(new JsonOutputConfig()),
                        apiConfig);

        InternalHttpResponse internalResponse =
                HttpApiResponseToInternalHttpResponse.convert(httpResponse);

        Assertions.assertEquals(418, internalResponse.getStatusCode());
        Assertions.assertEquals("application/json", internalResponse.getType());
        Assertions.assertEquals("value", internalResponse.getHeader("X-Test"));
        Assertions.assertTrue(internalResponse.getBody().contains("short and stout"));
    }
}
