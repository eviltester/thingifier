package uk.co.compendiumdev.thingifier.api.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.JsonOutputConfig;
import uk.co.compendiumdev.thingifier.Thing;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiRequestHook;
import uk.co.compendiumdev.thingifier.application.httpapimessagehooks.HttpApiResponseHook;
import uk.co.compendiumdev.thingifier.reporting.JsonThing;

import java.util.ArrayList;
import java.util.List;

class ThingifierHttpApiResponseHooksTest {

    JsonThing jsonThing = new JsonThing(new JsonOutputConfig());

    @Test
    void responseHookCanEndResponseProcessing(){

        List<HttpApiResponseHook> responseHooks = new ArrayList<>();
        responseHooks.add(new And404Becomes500Error());

        Thingifier aThingifier = new Thingifier();
        aThingifier.createThing("thing", "things");

        final ThingifierHttpApi api =
                new ThingifierHttpApi(new Thingifier(),
                    null, responseHooks);

        final HttpApiResponse response = api.get(new HttpApiRequest("/thing/1234"));
        Assertions.assertEquals(500, response.getStatusCode());

    }


    private class And404Becomes500Error implements HttpApiResponseHook {
        @Override
        public HttpApiResponse run(final HttpApiResponse response) {
            if(response.getStatusCode()==404){
                return new HttpApiResponse(null,
                        ApiResponse.error(500,"bypassed all processing"),
                        jsonThing);
            }
            return null;
        }
    }
}
