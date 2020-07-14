package uk.co.compendiumdev.thingifier.api.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.apiconfig.JsonOutputConfig;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class ThingifierHttpApiRequestHooksTest {

    JsonThing jsonThing = new JsonThing(new JsonOutputConfig());

    @Test
    void requestHookCanBypassRequestProcessing(){

        List<HttpApiRequestHook> requestHooks = new ArrayList<>();
        requestHooks.add(new Instant500Error());

        Thingifier thingifier = new Thingifier();
        thingifier.apiConfig().setApiToEnforceAcceptHeaderForResponses(false);

        final ThingifierHttpApi api =
                new ThingifierHttpApi(thingifier,
                    requestHooks, null);

        final HttpApiResponse response = api.get(new HttpApiRequest("/bob"));
        Assertions.assertEquals(500, response.getStatusCode());

    }


    private class Instant500Error implements HttpApiRequestHook {
        @Override
        public HttpApiResponse run(final HttpApiRequest request, ThingifierApiConfig config) {
            return new HttpApiResponse(new HashMap<>(),
                    ApiResponse.error(500,"bypassed all processing"),
                    jsonThing, config);
        }
    }
}
