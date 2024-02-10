package uk.co.compendiumdev.thingifier.api.http.requests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;

import java.util.HashMap;
import java.util.Map;

public class ThingifierHttpApiRequestStatusCodeTest {

    private Thingifier getTestThingifier(){

        Thingifier thingifier = new Thingifier();
        thingifier.apiConfig().setApiToEnforceAcceptHeaderForResponses(false);
        EntityDefinition defn = thingifier.getERmodel().createEntityDefinition("thing", "things");
        defn.addField(Field.is("title", FieldType.STRING));

        return thingifier;
    }

    @Test
    public void a413GeneratedWhenRequestTooLong(){

        Thingifier thingifier = getTestThingifier();
        thingifier.apiConfig().statusCodes().setMaxRequestBodyLengthBytes(1000);

        final ThingifierHttpApi api =
                new ThingifierHttpApi(thingifier, null, null);

        final Map<String,String> headers = new HashMap<>();
        headers.put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, "other_things");

        final HttpApiResponse response = api.get(new HttpApiRequest("/things").setHeaders(headers));


        final HttpApiResponse response413 = api.post(new HttpApiRequest("/things").setHeaders(headers).setBody(stringOfLength(1001)));

        Assertions.assertEquals(413, response413.getStatusCode());
        Assertions.assertTrue(response413.getBody().contains("Error: Request body too large, max allowed is 1000 bytes"));
    }

    private String stringOfLength(int length) {
        StringBuilder str = new StringBuilder();
        for(int currLen = 0; currLen < length; currLen++){
            str.append('a');
        }
        return str.toString();
    }


}
