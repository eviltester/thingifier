package uk.co.compendiumdev.thingifier.api.http.hooks;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.apiconfig.JsonOutputConfig;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;

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
            return new HttpApiResponse(new HttpHeadersBlock(),
                    ApiResponse.error(500,"bypassed all processing"),
                    jsonThing, config);
        }
    }

    @Test
    void requestHookCanAmendTheRequest(){

        List<HttpApiRequestHook> requestHooks = new ArrayList<>();
        requestHooks.add(new AddAdditionalHeader());

        Thingifier thingifier = new Thingifier();
        thingifier.apiConfig().setApiToEnforceAcceptHeaderForResponses(false);

        final ThingifierHttpApi api =
                new ThingifierHttpApi(thingifier,
                        requestHooks, null);

        HttpApiRequest request = new HttpApiRequest("/bob");
        final HttpApiResponse response = api.get(request);
        Assertions.assertEquals("dobbs",request.getHeader("X-BOB"));

    }


    private class AddAdditionalHeader implements HttpApiRequestHook {
        @Override
        public HttpApiResponse run(final HttpApiRequest request, ThingifierApiConfig config) {
            request.addHeader("X-BOB","dobbs");
            return null;
        }
    }
}
